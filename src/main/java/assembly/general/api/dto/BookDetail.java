package assembly.general.api.dto;

import java.time.Instant;
import java.util.UUID;

public class BookDetail {
    private UUID bookId;
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private Integer publicationYear;
    private String description;
    private String publisher;
    private Integer pageCount;
    private String language;
    private Integer totalCopies;
    private Integer availableCopies;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public BookDetail(UUID bookId, String isbn, String title, String author, String genre,
                      Integer publicationYear, String description, String publisher, Integer pageCount,
                      String language, Integer totalCopies, Integer availableCopies, String status,
                      Instant createdAt, Instant updatedAt) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publicationYear = publicationYear;
        this.description = description;
        this.publisher = publisher;
        this.pageCount = pageCount;
        this.language = language;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getBookId() { return bookId; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public Integer getPublicationYear() { return publicationYear; }
    public String getDescription() { return description; }
    public String getPublisher() { return publisher; }
    public Integer getPageCount() { return pageCount; }
    public String getLanguage() { return language; }
    public Integer getTotalCopies() { return totalCopies; }
    public Integer getAvailableCopies() { return availableCopies; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}