package assembly.general.api.dto;

public class BookUnavailableErrorResponse {
    private final String error = "BOOK_UNAVAILABLE";
    private String message;
    private int availableCopies;

    public BookUnavailableErrorResponse(String message, int availableCopies) {
        this.message = message;
        this.availableCopies = availableCopies;
    }

    public String getError() { return error; }
    public String getMessage() { return message; }
    public int getAvailableCopies() { return availableCopies; }
}