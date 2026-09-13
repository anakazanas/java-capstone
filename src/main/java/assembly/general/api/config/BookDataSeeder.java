package assembly.general.api.config;

import assembly.general.api.entity.Book;
import assembly.general.api.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class BookDataSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;

    public BookDataSeeder(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) {
        if (bookRepository.count() > 0) {
            return;
        }

        bookRepository.save(book("978-0-13-468599-1", "Clean Code", "Robert C. Martin", "Technology", 2008,
                "A handbook of agile software craftsmanship", "Prentice Hall", 464, "English", 5, 2));
        bookRepository.save(book("978-0-13-475759-9", "Refactoring", "Martin Fowler", "Technology", 2018,
                "Improving the design of existing code", "Addison-Wesley", 448, "English", 3, 0));
        bookRepository.save(book("978-0-596-00712-6", "Head First Design Patterns", "Eric Freeman", "Technology", 2004,
                "A brain-friendly guide to design patterns", "O'Reilly", 694, "English", 4, 4));
        bookRepository.save(book("978-0-452-28423-4", "1984", "George Orwell", "Fiction", 1949,
                "A dystopian social science fiction novel", "Secker & Warburg", 328, "English", 6, 3));
        bookRepository.save(book("978-0-06-112008-4", "To Kill a Mockingbird", "Harper Lee", "Fiction", 1960,
                "A novel about racial injustice in the American South", "J. B. Lippincott & Co.", 281, "English", 4, 1));
        bookRepository.save(book("978-0-14-303943-3", "Brave New World", "Aldous Huxley", "Fiction", 1932,
                "A dystopian novel set in a futuristic World State", "Chatto & Windus", 311, "English", 3, 3));
        bookRepository.save(book("978-0-307-45338-9", "The Immortal Life of Henrietta Lacks", "Rebecca Skloot", "Science", 2010,
                "The story of the woman behind the HeLa cell line", "Crown Publishing", 381, "English", 2, 0));
        bookRepository.save(book("978-0-553-38016-3", "A Brief History of Time", "Stephen Hawking", "Science", 1988,
                "An overview of cosmology for general readers", "Bantam Books", 256, "English", 3, 2));
        bookRepository.save(book("978-0-670-02215-2", "The Wright Brothers", "David McCullough", "History", 2015,
                "The story of Wilbur and Orville Wright", "Simon & Schuster", 320, "English", 2, 2));
        bookRepository.save(book("978-0-375-50167-0", "Team of Rivals", "Doris Kearns Goodwin", "History", 2005,
                "The political genius of Abraham Lincoln", "Simon & Schuster", 916, "English", 2, 1));
    }

    private Book book(String isbn, String title, String author, String genre, int year, String description,
                      String publisher, int pageCount, String language, int totalCopies, int availableCopies) {
        Book b = new Book();
        b.setIsbn(isbn);
        b.setTitle(title);
        b.setAuthor(author);
        b.setGenre(genre);
        b.setPublicationYear(year);
        b.setDescription(description);
        b.setPublisher(publisher);
        b.setPageCount(pageCount);
        b.setLanguage(language);
        b.setTotalCopies(totalCopies);
        b.setAvailableCopies(availableCopies);
        return b;
    }
}