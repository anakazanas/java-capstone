package assembly.general.api.dto;

import java.time.Instant;
import java.util.UUID;

public class ReservationResponse {
    private UUID reservationId;
    private UUID bookId;
    private UUID userId;
    private String bookTitle;
    private String status;
    private Instant reservedAt;
    private Instant expiresAt;
    private String message;

    public ReservationResponse(UUID reservationId, UUID bookId, UUID userId, String bookTitle,
                               String status, Instant reservedAt, Instant expiresAt, String message) {
        this.reservationId = reservationId;
        this.bookId = bookId;
        this.userId = userId;
        this.bookTitle = bookTitle;
        this.status = status;
        this.reservedAt = reservedAt;
        this.expiresAt = expiresAt;
        this.message = message;
    }

    public UUID getReservationId() { return reservationId; }
    public UUID getBookId() { return bookId; }
    public UUID getUserId() { return userId; }
    public String getBookTitle() { return bookTitle; }
    public String getStatus() { return status; }
    public Instant getReservedAt() { return reservedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public String getMessage() { return message; }
}