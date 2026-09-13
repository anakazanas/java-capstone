package assembly.general.api.controllers;

import assembly.general.api.dto.BookDetail;
import assembly.general.api.dto.BookSummary;
import assembly.general.api.dto.PageResponse;
import assembly.general.api.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final BookService bookService;

    public CatalogController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books")
    public ResponseEntity<PageResponse<BookSummary>> browseBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String isbn,
            @RequestParam(defaultValue = "false") boolean availableOnly) {

        return ResponseEntity.ok(bookService.browse(page, size, sortBy, sortOrder, query, genre, isbn, availableOnly));
    }

    @GetMapping("/books/{bookId}")
    public ResponseEntity<BookDetail> getBook(@PathVariable UUID bookId) {
        return ResponseEntity.ok(bookService.getBookDetail(bookId));
    }
}