package assembly.general.api.dto;

import assembly.general.api.entity.BookCondition;
import jakarta.validation.constraints.NotNull;

public class ReturnRequest {
    @NotNull(message = "condition is required")
    private BookCondition condition;

    private String notes;

    public BookCondition getCondition() { return condition; }
    public void setCondition(BookCondition condition) { this.condition = condition; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}