package assembly.general.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class ReserveRequest {
    @NotNull(message = "bookId is required")
    private UUID bookId;

    public UUID getBookId() { return bookId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }
}