package assembly.general.api.service;

import assembly.general.api.dto.BookDetail;
import assembly.general.api.dto.BookSummary;
import assembly.general.api.dto.PageResponse;
import assembly.general.api.entity.Book;
import assembly.general.api.exception.ResourceNotFoundException;
import assembly.general.api.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookService bookService;
    private Book cleanCode;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository);

        cleanCode = new Book();
        cleanCode.setId(UUID.randomUUID());
        cleanCode.setIsbn("978-0-13-468599-1");
        cleanCode.setTitle("Clean Code");
        cleanCode.setAuthor("Robert C. Martin");
        cleanCode.setGenre("Technology");
        cleanCode.setPublicationYear(2008);
        cleanCode.setDescription("A handbook of agile software craftsmanship");
        cleanCode.setPublisher("Prentice Hall");
        cleanCode.setPageCount(464);
        cleanCode.setLanguage("English");
        cleanCode.setTotalCopies(5);
        cleanCode.setAvailableCopies(2);
        cleanCode.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        cleanCode.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void browse_mapsBooksToSummariesWithComputedStatus() {
        Page<Book> page = new PageImpl<>(List.of(cleanCode));
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<BookSummary> result = bookService.browse(0, 20, "title", "asc", null, null, null, false);

        assertThat(result.getContent()).hasSize(1);
        BookSummary summary = result.getContent().get(0);
        assertThat(summary.getTitle()).isEqualTo("Clean Code");
        assertThat(summary.getStatus()).isEqualTo("AVAILABLE");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void browse_marksZeroAvailableCopiesAsCheckedOut() {
        cleanCode.setAvailableCopies(0);
        Page<Book> page = new PageImpl<>(List.of(cleanCode));
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<BookSummary> result = bookService.browse(0, 20, "title", "asc", null, null, null, false);

        assertThat(result.getContent().get(0).getStatus()).isEqualTo("CHECKED_OUT");
    }

    @Test
    void browse_ignoresUnsupportedSortFieldAndDefaultsToTitle() {
        Page<Book> page = new PageImpl<>(List.of(cleanCode));
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<BookSummary> result = bookService.browse(0, 20, "password", "asc", null, null, null, false);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getBookDetail_returnsFullDetailWhenFound() {
        when(bookRepository.findById(cleanCode.getId())).thenReturn(Optional.of(cleanCode));

        BookDetail detail = bookService.getBookDetail(cleanCode.getId());

        assertThat(detail.getIsbn()).isEqualTo("978-0-13-468599-1");
        assertThat(detail.getPublisher()).isEqualTo("Prentice Hall");
        assertThat(detail.getPageCount()).isEqualTo(464);
        assertThat(detail.getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void getBookDetail_throwsWhenNotFound() {
        UUID missingId = UUID.randomUUID();
        when(bookRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookDetail(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }
}