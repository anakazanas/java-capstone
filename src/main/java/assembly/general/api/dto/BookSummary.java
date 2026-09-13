package assembly.general.api.dto;

import java.util.UUID;

public class BookSummary {
    private UUID bookId;
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private Integer publicationYear;
    private String description;
    private Integer totalCopies;
    private Integer availableCopies;
    private String status;

    public BookSummary(UUID bookId, String isbn, String title, String author, String genre,
                       Integer publicationYear, String description, Integer totalCopies,
                       Integer availableCopies, String status) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publicationYear = publicationYear;
        this.description = description;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.status = status;
    }

    public UUID getBookId() { return bookId; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public Integer getPublicationYear() { return publicationYear; }
    public String getDescription() { return description; }
    public Integer getTotalCopies() { return totalCopies; }
    public Integer getAvailableCopies() { return availableCopies; }
    public String getStatus() { return status; }
}