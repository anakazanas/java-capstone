package assembly.general.api.service;

import assembly.general.api.dto.BookDetail;
import assembly.general.api.dto.BookSummary;
import assembly.general.api.dto.PageResponse;
import assembly.general.api.entity.Book;
import assembly.general.api.exception.ResourceNotFoundException;
import assembly.general.api.repository.BookRepository;
import assembly.general.api.repository.BookSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BookService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("title", "author", "publicationYear");

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public PageResponse<BookSummary> browse(int page, int size, String sortBy, String sortOrder,
                                            String query, String genre, String isbn, boolean availableOnly) {

        String sortField = SORTABLE_FIELDS.contains(sortBy) ? sortBy : "title";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Specification<Book> spec = Specification.where(null);

        if (query != null && !query.isBlank()) {
            spec = spec.and(BookSpecifications.titleOrAuthorContains(query));
        }
        if (genre != null && !genre.isBlank()) {
            spec = spec.and(BookSpecifications.hasGenre(genre));
        }
        if (isbn != null && !isbn.isBlank()) {
            spec = spec.and(BookSpecifications.hasIsbn(isbn));
        }
        if (availableOnly) {
            spec = spec.and(BookSpecifications.isAvailable());
        }

        Page<Book> result = bookRepository.findAll(spec, pageable);
        List<BookSummary> content = result.getContent().stream().map(this::toSummary).toList();

        return PageResponse.from(result, content);
    }

    public BookDetail getBookDetail(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));
        return toDetail(book);
    }

    private BookSummary toSummary(Book book) {
        return new BookSummary(
                book.getId(), book.getIsbn(), book.getTitle(), book.getAuthor(), book.getGenre(),
                book.getPublicationYear(), book.getDescription(), book.getTotalCopies(),
                book.getAvailableCopies(), book.getStatus()
        );
    }

    private BookDetail toDetail(Book book) {
        return new BookDetail(
                book.getId(), book.getIsbn(), book.getTitle(), book.getAuthor(), book.getGenre(),
                book.getPublicationYear(), book.getDescription(), book.getPublisher(), book.getPageCount(),
                book.getLanguage(), book.getTotalCopies(), book.getAvailableCopies(), book.getStatus(),
                book.getCreatedAt(), book.getUpdatedAt()
        );
    }
}