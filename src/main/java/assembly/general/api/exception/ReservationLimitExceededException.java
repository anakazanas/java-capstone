package assembly.general.api.exception;

public class ReservationLimitExceededException extends RuntimeException {
    private final long currentReservations;

    public ReservationLimitExceededException(String message, long currentReservations) {
        super(message);
        this.currentReservations = currentReservations;
    }

    public long getCurrentReservations() { return currentReservations; }
}