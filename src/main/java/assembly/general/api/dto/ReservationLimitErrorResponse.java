package assembly.general.api.dto;

public class ReservationLimitErrorResponse {
    private final String error = "RESERVATION_LIMIT_EXCEEDED";
    private String message;
    private long currentReservations;

    public ReservationLimitErrorResponse(String message, long currentReservations) {
        this.message = message;
        this.currentReservations = currentReservations;
    }

    public String getError() { return error; }
    public String getMessage() { return message; }
    public long getCurrentReservations() { return currentReservations; }
}