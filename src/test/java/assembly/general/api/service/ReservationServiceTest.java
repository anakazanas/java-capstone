package assembly.general.api.service;

import assembly.general.api.dto.CheckoutResponse;
import assembly.general.api.dto.ReservationResponse;
import assembly.general.api.dto.ReturnResponse;
import assembly.general.api.entity.*;
import assembly.general.api.exception.BookUnavailableException;
import assembly.general.api.exception.InvalidStatusException;
import assembly.general.api.exception.ReservationLimitExceededException;
import assembly.general.api.repository.BookRepository;
import assembly.general.api.repository.ReservationRepository;
import assembly.general.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;

    private ReservationService reservationService;
    private User user;
    private Book book;

    private void init(Instant now) {
        Clock fixedClock = Clock.fixed(now, ZoneOffset.UTC);
        reservationService = new ReservationService(reservationRepository, bookRepository, userRepository, fixedClock);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("jane.doe@example.com");

        book = new Book();
        book.setId(UUID.randomUUID());
        book.setTitle("Clean Code");
        book.setAvailableCopies(2);
        book.setTotalCopies(5);
    }

    @Test
    void reserve_decrementsAvailableCopiesAndSetsSevenDayExpiry() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        init(now);

        when(reservationRepository.countByUser_IdAndStatusIn(eq(user.getId()), any())).thenReturn(0L);
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationResponse response = reservationService.reserve(user.getId(), book.getId());

        assertThat(book.getAvailableCopies()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo("RESERVED");
        assertThat(response.getExpiresAt()).isEqualTo(now.plus(7, ChronoUnit.DAYS));
        verify(bookRepository).save(book);
    }

    @Test
    void reserve_throwsWhenActiveReservationsAtLimit() {
        init(Instant.parse("2026-01-01T00:00:00Z"));

        when(reservationRepository.countByUser_IdAndStatusIn(eq(user.getId()), any())).thenReturn(5L);

        assertThatThrownBy(() -> reservationService.reserve(user.getId(), book.getId()))
                .isInstanceOf(ReservationLimitExceededException.class);

        verifyNoInteractions(bookRepository);
    }

    @Test
    void reserve_throwsWhenNoBookCopiesAvailable() {
        init(Instant.parse("2026-01-01T00:00:00Z"));
        book.setAvailableCopies(0);

        when(reservationRepository.countByUser_IdAndStatusIn(eq(user.getId()), any())).thenReturn(0L);
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> reservationService.reserve(user.getId(), book.getId()))
                .isInstanceOf(BookUnavailableException.class);
    }

    @Test
    void checkout_throwsWhenReservationNotInReservedStatus() {
        init(Instant.parse("2026-01-01T00:00:00Z"));

        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setStatus(ReservationStatus.CHECKED_OUT);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.checkout(reservation.getId(), null))
                .isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void checkout_setsFourteenDayDueDate() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        init(now);

        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setStatus(ReservationStatus.RESERVED);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        CheckoutResponse response = reservationService.checkout(reservation.getId(), "Good condition");

        assertThat(response.getStatus()).isEqualTo("CHECKED_OUT");
        assertThat(response.getDueDate()).isEqualTo(now.plus(14, ChronoUnit.DAYS));
    }

    @Test
    void returnBook_onTimeHasNoLateFee() {
        Instant checkoutTime = Instant.parse("2026-01-01T00:00:00Z");
        Instant now = checkoutTime.plus(14, ChronoUnit.DAYS);
        init(now);

        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        reservation.setDueDate(checkoutTime.plus(14, ChronoUnit.DAYS));
        reservation.setBook(book);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReturnResponse response = reservationService.returnBook(reservation.getId(), BookCondition.GOOD, null);

        assertThat(response.getLateDays()).isZero();
        assertThat(response.getLateFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(book.getAvailableCopies()).isEqualTo(3);
    }

    @Test
    void returnBook_threeDaysLateChargesThreeDollars() {
        Instant dueDate = Instant.parse("2026-01-14T15:00:00Z");
        Instant now = Instant.parse("2026-01-17T10:00:00Z");
        init(now);

        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        reservation.setDueDate(dueDate);
        reservation.setBook(book);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReturnResponse response = reservationService.returnBook(reservation.getId(), BookCondition.FAIR, "worn");

        assertThat(response.getLateDays()).isEqualTo(3);
        assertThat(response.getLateFee()).isEqualByComparingTo(new BigDecimal("3.00"));
    }

    @Test
    void returnBook_throwsWhenNotCheckedOut() {
        init(Instant.parse("2026-01-01T00:00:00Z"));

        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setStatus(ReservationStatus.RESERVED);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.returnBook(reservation.getId(), BookCondition.GOOD, null))
                .isInstanceOf(InvalidStatusException.class);
    }
}