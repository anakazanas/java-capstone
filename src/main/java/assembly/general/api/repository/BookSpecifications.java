package assembly.general.api.repository;

import assembly.general.api.entity.Book;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecifications {

    private BookSpecifications() {}

    public static Specification<Book> titleOrAuthorContains(String query) {
        return (root, cq, cb) -> {
            String pattern = "%" + query.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("author")), pattern)
            );
        };
    }

    public static Specification<Book> hasGenre(String genre) {
        return (root, cq, cb) -> cb.equal(root.get("genre"), genre);
    }

    public static Specification<Book> hasIsbn(String isbn) {
        return (root, cq, cb) -> cb.equal(root.get("isbn"), isbn);
    }

    public static Specification<Book> isAvailable() {
        return (root, cq, cb) -> cb.greaterThan(root.get("availableCopies"), 0);
    }
}