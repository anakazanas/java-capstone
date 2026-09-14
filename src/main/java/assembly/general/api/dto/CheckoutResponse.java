package assembly.general.api.dto;

import java.time.Instant;
import java.util.UUID;

public class CheckoutResponse {
    private UUID reservationId;
    private String status;
    private Instant checkedOutAt;
    private Instant dueDate;
    private String message;

    public CheckoutResponse(UUID reservationId, String status, Instant checkedOutAt, Instant dueDate, String message) {
        this.reservationId = reservationId;
        this.status = status;
        this.checkedOutAt = checkedOutAt;
        this.dueDate = dueDate;
        this.message = message;
    }

    public UUID getReservationId() { return reservationId; }
    public String getStatus() { return status; }
    public Instant getCheckedOutAt() { return checkedOutAt; }
    public Instant getDueDate() { return dueDate; }
    public String getMessage() { return message; }
}