package assembly.general.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ReturnResponse {
    private UUID reservationId;
    private Instant returnedAt;
    private Instant dueDate;
    private int lateDays;
    private BigDecimal lateFee;
    private String message;

    public ReturnResponse(UUID reservationId, Instant returnedAt, Instant dueDate,
                          int lateDays, BigDecimal lateFee, String message) {
        this.reservationId = reservationId;
        this.returnedAt = returnedAt;
        this.dueDate = dueDate;
        this.lateDays = lateDays;
        this.lateFee = lateFee;
        this.message = message;
    }

    public UUID getReservationId() { return reservationId; }
    public Instant getReturnedAt() { return returnedAt; }
    public Instant getDueDate() { return dueDate; }
    public int getLateDays() { return lateDays; }
    public BigDecimal getLateFee() { return lateFee; }
    public String getMessage() { return message; }
}