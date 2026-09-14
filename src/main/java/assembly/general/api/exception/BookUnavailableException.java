package assembly.general.api.exception;

public class BookUnavailableException extends RuntimeException {
    private final int availableCopies;

    public BookUnavailableException(String message, int availableCopies) {
        super(message);
        this.availableCopies = availableCopies;
    }

    public int getAvailableCopies() { return availableCopies; }
}