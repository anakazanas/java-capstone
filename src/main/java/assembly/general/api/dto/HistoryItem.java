package assembly.general.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryItem {
    private UUID reservationId;
    private String bookTitle;
    private String bookAuthor;
    private Instant reservedAt;
    private Instant checkedOutAt;
    private Instant returnedAt;
    private Instant dueDate;
    private String status;
    private boolean wasLate;

    public HistoryItem(UUID reservationId, String bookTitle, String bookAuthor, Instant reservedAt,
                       Instant checkedOutAt, Instant returnedAt, Instant dueDate, String status, boolean wasLate) {
        this.reservationId = reservationId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.reservedAt = reservedAt;
        this.checkedOutAt = checkedOutAt;
        this.returnedAt = returnedAt;
        this.dueDate = dueDate;
        this.status = status;
        this.wasLate = wasLate;
    }

    public UUID getReservationId() { return reservationId; }
    public String getBookTitle() { return bookTitle; }
    public String getBookAuthor() { return bookAuthor; }
    public Instant getReservedAt() { return reservedAt; }
    public Instant getCheckedOutAt() { return checkedOutAt; }
    public Instant getReturnedAt() { return returnedAt; }
    public Instant getDueDate() { return dueDate; }
    public String getStatus() { return status; }
    public boolean isWasLate() { return wasLate; }
}