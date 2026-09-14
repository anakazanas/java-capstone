package assembly.general.api.dto;

public class InvalidStatusErrorResponse {
    private final String error = "INVALID_STATUS";
    private String message;
    private String currentStatus;

    public InvalidStatusErrorResponse(String message, String currentStatus) {
        this.message = message;
        this.currentStatus = currentStatus;
    }

    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getCurrentStatus() { return currentStatus; }
}