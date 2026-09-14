package assembly.general.api.service;

import assembly.general.api.dto.*;
import assembly.general.api.entity.*;
import assembly.general.api.exception.BookUnavailableException;
import assembly.general.api.exception.InvalidStatusException;
import assembly.general.api.exception.ReservationLimitExceededException;
import assembly.general.api.exception.ResourceNotFoundException;
import assembly.general.api.repository.BookRepository;
import assembly.general.api.repository.ReservationRepository;
import assembly.general.api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private static final int MAX_ACTIVE_RESERVATIONS = 5;
    private static final int RESERVATION_EXPIRY_DAYS = 7;
    private static final int CHECKOUT_PERIOD_DAYS = 14;
    private static final BigDecimal LATE_FEE_PER_DAY = new BigDecimal("1.00");
    private static final DateTimeFormatter DUE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy").withZone(ZoneOffset.UTC);

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository, BookRepository bookRepository,
                              UserRepository userRepository, Clock clock) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationResponse reserve(UUID userId, UUID bookId) {
        long activeCount = reservationRepository.countByUser_IdAndStatusIn(
                userId, List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT));

        if (activeCount >= MAX_ACTIVE_RESERVATIONS) {
            throw new ReservationLimitExceededException(
                    "You have reached the maximum of 5 active reservations", activeCount);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        if (book.getAvailableCopies() <= 0) {
            throw new BookUnavailableException("No copies available for reservation", book.getAvailableCopies());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + userId));

        Instant now = Instant.now(clock);

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setReservedAt(now);
        reservation.setExpiresAt(now.plus(RESERVATION_EXPIRY_DAYS, ChronoUnit.DAYS));

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        Reservation saved = reservationRepository.save(reservation);

        return new ReservationResponse(
                saved.getId(), book.getId(), user.getId(), book.getTitle(),
                saved.getStatus().name(), saved.getReservedAt(), saved.getExpiresAt(),
                "Book reserved successfully. Please pick up within 7 days."
        );
    }

    @Transactional(readOnly = true)
    public ActiveReservationsResponse getActiveReservations(UUID userId) {
        List<Reservation> active = reservationRepository.findActiveByUserId(
                userId, List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT));

        Instant now = Instant.now(clock);

        List<ActiveReservationItem> items = active.stream().map(r -> {
            ActiveReservationItem item = new ActiveReservationItem();
            item.setReservationId(r.getId());
            item.setBookId(r.getBook().getId());
            item.setBookTitle(r.getBook().getTitle());
            item.setBookAuthor(r.getBook().getAuthor());
            item.setStatus(r.getStatus().name());

            if (r.getStatus() == ReservationStatus.RESERVED) {
                item.setReservedAt(r.getReservedAt());
                item.setExpiresAt(r.getExpiresAt());
                item.setDaysUntilExpiry(ChronoUnit.DAYS.between(now, r.getExpiresAt()));
            } else {
                item.setCheckedOutAt(r.getCheckedOutAt());
                item.setDueDate(r.getDueDate());
                item.setDaysUntilDue(ChronoUnit.DAYS.between(now, r.getDueDate()));
            }
            return item;
        }).toList();

        return new ActiveReservationsResponse(items, items.size());
    }

    @Transactional
    public CheckoutResponse checkout(UUID reservationId, String notes) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new InvalidStatusException(
                    "Can only checkout reservations with RESERVED status", reservation.getStatus().name());
        }

        Instant now = Instant.now(clock);
        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        reservation.setCheckedOutAt(now);
        reservation.setDueDate(now.plus(CHECKOUT_PERIOD_DAYS, ChronoUnit.DAYS));
        reservation.setCheckoutNotes(notes);

        Reservation saved = reservationRepository.save(reservation);

        String message = "Book checked out successfully. Due date: " + DUE_DATE_FORMAT.format(saved.getDueDate());

        return new CheckoutResponse(saved.getId(), saved.getStatus().name(), saved.getCheckedOutAt(),
                saved.getDueDate(), message);
    }

    @Transactional
    public ReturnResponse returnBook(UUID reservationId, BookCondition condition, String notes) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.CHECKED_OUT) {
            throw new InvalidStatusException(
                    "Can only return books with CHECKED_OUT status", reservation.getStatus().name());
        }

        Instant now = Instant.now(clock);

        LocalDate dueDateOnly = reservation.getDueDate().atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate returnedDateOnly = now.atZone(ZoneOffset.UTC).toLocalDate();
        long lateDays = Math.max(0, ChronoUnit.DAYS.between(dueDateOnly, returnedDateOnly));

        BigDecimal lateFee = LATE_FEE_PER_DAY.multiply(BigDecimal.valueOf(lateDays));

        reservation.setStatus(ReservationStatus.RETURNED);
        reservation.setReturnedAt(now);
        reservation.setReturnCondition(condition);
        reservation.setReturnNotes(notes);
        reservation.setLateDays((int) lateDays);
        reservation.setLateFee(lateFee);

        Book book = reservation.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        Reservation saved = reservationRepository.save(reservation);

        String message = lateDays > 0
                ? String.format("Book returned. Late fee of $%.2f applied to account.", lateFee)
                : "Book returned successfully";

        return new ReturnResponse(saved.getId(), saved.getReturnedAt(), saved.getDueDate(),
                (int) lateDays, lateFee, message);
    }

    @Transactional(readOnly = true)
    public PageResponse<HistoryItem> getHistory(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Reservation> result = reservationRepository.findHistoryByUserId(userId, pageable);

        List<HistoryItem> content = result.getContent().stream().map(this::toHistoryItem).toList();

        return PageResponse.from(result, content);
    }

    private HistoryItem toHistoryItem(Reservation r) {
        boolean wasLate = r.getReturnedAt() != null && r.getDueDate() != null
                && r.getReturnedAt().isAfter(r.getDueDate());
        return new HistoryItem(
                r.getId(), r.getBook().getTitle(), r.getBook().getAuthor(),
                r.getReservedAt(), r.getCheckedOutAt(), r.getReturnedAt(), r.getDueDate(),
                r.getStatus().name(), wasLate
        );
    }
}