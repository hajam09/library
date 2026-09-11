package com.solirius.advanced.library;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.solirius.advanced.library.exceptions.AlreadyBorrowedException;
import com.solirius.advanced.library.exceptions.BookNotFoundException;
import com.solirius.advanced.library.exceptions.DuplicateBookException;
import com.solirius.advanced.library.exceptions.NotBorrowedException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

class LibraryTest {
    private Library library;

    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private PreparedStatement mockPreparedStatement;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    void testLibraryConstructor_WhenDatabaseIsNotEmpty() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("title")).thenReturn("1984");
        when(mockResultSet.getString("author")).thenReturn("George Orwell");
        when(mockResultSet.getBoolean("isBorrowed")).thenReturn(false);
        library = new Library(mockConnection);
        verify(mockConnection, times(2)).createStatement();
        verify(mockStatement).executeQuery(anyString());
        verify(mockResultSet, times(2)).next();
        verify(mockResultSet).getString("title");
        verify(mockResultSet).getString("author");
        verify(mockResultSet).getBoolean("isBorrowed");
        verify(mockResultSet).close();
        verify(mockStatement).close();
        List<Book> books = library.viewAllBooks();
        assertEquals(1, books.size());
        assertEquals("1984", books.get(0).getTitle());
    }

    @Test
    void testLibraryConstructor_WhenDatabaseIsEmpty() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);
        when(mockResultSet.getString("title")).thenReturn("1984");
        when(mockResultSet.getString("author")).thenReturn("George Orwell");
        when(mockResultSet.getBoolean("isBorrowed")).thenReturn(false);
        library = new Library(mockConnection);
        verify(mockConnection, times(2)).createStatement();
        verify(mockStatement).executeQuery(anyString());
        verify(mockResultSet).next();
        verify(mockResultSet, never()).getString(anyString());
        verify(mockResultSet, never()).getBoolean(anyString());
        verify(mockResultSet).close();
        verify(mockStatement).close();
        List<Book> books = library.viewAllBooks();
        assertEquals(0, books.size());
    }

    @Test
    void testLibraryThrowsRuntimeException_WhenDatabaseConnectionThrows() throws SQLException {
        when(mockConnection.createStatement()).thenThrow(new SQLException("Test message."));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> new Library(mockConnection));
        assertEquals("java.sql.SQLException: Test message.", exception.getMessage());
        assertEquals("Error in connecting to the library database: Test message.", outputStreamCaptor.toString().trim());

    }

    @Test
    void testAddBook() throws SQLException, DuplicateBookException {
        Book book = new Book("Brave New World", "Aldous Huxley");
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        library = new Library(mockConnection);
        boolean result = library.addBook(book);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, "Brave New World");
        verify(mockPreparedStatement).setString(2, "Aldous Huxley");
        verify(mockPreparedStatement).setBoolean(3, false);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testAddBookReturnsFalse_WhenSqlExceptionThrown() throws SQLException, DuplicateBookException {
        Book book = new Book("Brave New World", "Aldous Huxley");
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test message."));
        library = new Library(mockConnection);
        boolean result = library.addBook(book);

        assertFalse(result);
        verify(mockPreparedStatement, never()).setString(1, "Brave New World");
        verify(mockPreparedStatement, never()).setString(2, "Aldous Huxley");
        verify(mockPreparedStatement, never()).setBoolean(3, false);
        verify(mockPreparedStatement, never()).executeUpdate();
        assertEquals("Error adding book to the library: Test message.", outputStreamCaptor.toString().trim());
    }

    @Test
    void testViewAvailableBooks() throws DuplicateBookException{
        Book book1 = new Book("1984", "George Orwell");
        Book book2 = new Book("The Catcher in the Rye", "J.D. Salinger");
        library = new Library(mockConnection);
        library.addBook(book1);
        library.addBook(book2);
        List<Book> availableBooks = library.viewAvailableBooks();

        assertEquals(2, availableBooks.size());
    }

    @Test
    void testViewAllBooks() throws AlreadyBorrowedException, BookNotFoundException, DuplicateBookException {
        Book book1 = new Book("1984", "George Orwell");
        Book book2 = new Book("The Catcher in the Rye", "J.D. Salinger");
        library = new Library(mockConnection);
        library.addBook(book1);
        library.addBook(book2);
        library.borrowBook("1984");
        List<Book> availableBooks = library.viewAvailableBooks();
        List<Book> allBooks = library.viewAllBooks();

        assertEquals(1, availableBooks.size());
        assertEquals(2, allBooks.size());
    }

    @Test
    void testSearchBook_WhenBookExists() throws BookNotFoundException, DuplicateBookException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);
        Book foundBook = library.searchBook("1984");

        assertNotNull(foundBook);
        assertEquals("1984", foundBook.getTitle());
    }

    @Test
    void testSearchBook_WhenBookDoesNotExist() {
        library = new Library(mockConnection);
        assertThrows(BookNotFoundException.class, () -> library.searchBook("Nonexistent Book"));
    }

    @Test
    void testBorrowBook_WhenBookIsAvailable() throws AlreadyBorrowedException, BookNotFoundException, DuplicateBookException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);
        boolean result = library.borrowBook("1984");

        assertTrue(result);
        assertTrue(book.isBorrowed());
    }

    @Test
    void testBorrowBook_WhenBookIsNotAvailable() throws DuplicateBookException {
        Book book = new Book("1984", "George Orwell");
        book.borrowBook();
        library = new Library(mockConnection);
        library.addBook(book);

        assertThrows(AlreadyBorrowedException.class, () -> library.borrowBook("1984"));
    }

    @Test
    void testBorrowBook_WhenBookDoesNotExist() {
        library = new Library(mockConnection);
        assertThrows(BookNotFoundException.class, () -> library.borrowBook("Nonexistent Book"));
    }

    @Test
    void testReturnBook_WhenBookIsBorrowed() throws AlreadyBorrowedException, BookNotFoundException, NotBorrowedException, DuplicateBookException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);
        library.borrowBook("1984");

        boolean result = library.returnBook("1984");

        assertTrue(result);
        assertFalse(book.isBorrowed());
    }

    @Test
    void testReturnBook_WhenBookIsNotBorrowed() throws DuplicateBookException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);

        assertThrows(NotBorrowedException.class, () -> library.returnBook("1984"));
    }

    @Test
    void testReturnBook_WhenBookDoesNotExist() {
        library = new Library(mockConnection);
        assertThrows(BookNotFoundException.class, () -> library.returnBook("Nonexistent Book"));
    }

    @Test
    void testLibraryConstructor_WhenDatabaseContainsBorrowedBook() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("title")).thenReturn("1984");
        when(mockResultSet.getString("author")).thenReturn("George Orwell");
        when(mockResultSet.getBoolean("isBorrowed")).thenReturn(true);
        library = new Library(mockConnection);

        List<Book> allBooks = library.viewAllBooks();
        assertEquals(1, allBooks.size());
        assertTrue(allBooks.get(0).isBorrowed());
        assertTrue(library.viewAvailableBooks().isEmpty());
    }

    @Test
    void testSearchBook_WhenMatchingAuthorIgnoringCase() throws BookNotFoundException, DuplicateBookException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);
        Book foundBook = library.searchBook("george orwell");

        assertEquals(book, foundBook);
        assertEquals("George Orwell", foundBook.getAuthor());
    }

    @Test
    void testSearchBook_WhenMatchingTitleIgnoringCase() throws BookNotFoundException, DuplicateBookException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);

        assertEquals(book, library.searchBook("1984"));
    }

    @Test
    void testSearchBook_WhenBookDoesNotExist_HasExpectedMessage() {
        library = new Library(mockConnection);
        BookNotFoundException exception = assertThrows(
                BookNotFoundException.class,
                () -> library.searchBook("Nonexistent Book"));
        assertEquals(Constants.BOOK_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testBorrowBook_WhenBookIsNotAvailable_HasExpectedMessage() throws DuplicateBookException {
        Book book = new Book("1984", "George Orwell");
        book.borrowBook();
        library = new Library(mockConnection);
        library.addBook(book);

        AlreadyBorrowedException exception = assertThrows(
                AlreadyBorrowedException.class,
                () -> library.borrowBook("1984"));
        assertEquals(Constants.BOOK_ALREADY_BORROWED, exception.getMessage());
    }

    @Test
    void testReturnBook_WhenBookIsNotBorrowed_HasExpectedMessage() throws DuplicateBookException{
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);

        NotBorrowedException exception = assertThrows(
                NotBorrowedException.class,
                () -> library.returnBook("1984"));
        assertEquals(Constants.BOOK_NOT_BORROWED, exception.getMessage());
    }

    @Test
    void testAddBook_WhenBookAlreadyBorrowed() throws DuplicateBookException{
        Book book = new Book("1984", "George Orwell");
        book.borrowBook();
        library = new Library(mockConnection);

        assertTrue(library.addBook(book));
        assertTrue(library.viewAvailableBooks().isEmpty());
        assertEquals(1, library.viewAllBooks().size());
    }

    @Test
    void testAddBook_WhenBookIsNull() throws IllegalArgumentException {
        library = new Library(mockConnection);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> library.addBook(null));

        assertEquals(0, library.viewAllBooks().size());
        assertEquals(Constants.BOOK_EMPTY_FIELDS, exception.getMessage());
    }

    @Test
    void testBorrowBook_PersistsBorrowedState() throws Exception {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);

        assertTrue(library.borrowBook("1984"));

        verify(mockConnection).prepareStatement("UPDATE books SET isBorrowed = ? WHERE title = ? AND author = ?");
        verify(mockPreparedStatement).setBoolean(1, true);
        verify(mockPreparedStatement, atLeastOnce()).setString(2, "1984");
        verify(mockPreparedStatement, atLeastOnce()).setString(3, "George Orwell");
    }

    @Test
    void testReturnBook_PersistsReturnedState() throws Exception {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);
        library.borrowBook("1984");

        assertTrue(library.returnBook("1984"));

        verify(mockConnection, times(2)).prepareStatement("UPDATE books SET isBorrowed = ? WHERE title = ? AND author = ?");
        verify(mockPreparedStatement).setBoolean(1, false);
        verify(mockPreparedStatement, atLeastOnce()).setString(2, "1984");
        verify(mockPreparedStatement, atLeastOnce()).setString(3, "George Orwell");
    }

    @Test
    void testAddSameBookAgain() throws DuplicateBookException{
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        assertTrue(library.addBook(book));

        DuplicateBookException exception = assertThrows(
                DuplicateBookException.class,
                () -> library.addBook(book));
        assertEquals(Constants.BOOK_ALREADY_EXISTS, exception.getMessage());
    }

    @Test
    void testSearchForBookByPartial() throws BookNotFoundException, DuplicateBookException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);
        Book foundBook = library.searchBook("george");

        assertNotNull(foundBook);
        assertEquals("1984", foundBook.getTitle());
    }


    @Test
    void testAddBookWithEmptyParameters() throws IllegalArgumentException {
        Book book = new Book("", "  ");
        library = new Library(mockConnection);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> library.addBook(book));
        assertEquals(Constants.BOOK_EMPTY_FIELDS, exception.getMessage());
    }

    @Test
    void testSearchBooksByAuthorAndFound() throws DuplicateBookException, BookNotFoundException {
        library = new Library(mockConnection);
        List<Book> books = List.of(
                new Book("Cup of Gold", "John Steinbeck"),
                new Book("The Pastures of Heaven", "John Steinbeck"),
                new Book("Keep the Aspidistra Flying", "George Orwell"),
                new Book("1984", "George Orwell"),
                new Book("Animal Farm", "George Orwell"),
                new Book("In Dubious Battle", "John Steinbeck")
        );

        for (Book book : books) {
            library.addBook(book);
        }

        assertEquals(6, library.viewAllBooks().size());

        List<Book> foundBooks = library.searchByAuthor("steinbeck");
        assertEquals(3, foundBooks.size());

        assertEquals("Cup of Gold", foundBooks.get(0).getTitle());
        assertEquals("The Pastures of Heaven", foundBooks.get(1).getTitle());
        assertEquals("In Dubious Battle", foundBooks.get(2).getTitle());
    }

    @Test
    void testSearchBooksByAuthorAndNoneFound() throws DuplicateBookException, BookNotFoundException {
        library = new Library(mockConnection);
        List<Book> books = List.of(
                new Book("Cup of Gold", "John Steinbeck"),
                new Book("The Pastures of Heaven", "John Steinbeck"),
                new Book("Keep the Aspidistra Flying", "George Orwell"),
                new Book("1984", "George Orwell"),
                new Book("Animal Farm", "George Orwell"),
                new Book("In Dubious Battle", "John Steinbeck")
        );

        for (Book book : books) {
            library.addBook(book);
        }

        assertEquals(6, library.viewAllBooks().size());
        BookNotFoundException exception = assertThrows(
                BookNotFoundException.class,
                () -> library.searchByAuthor("no"));
        assertEquals(Constants.BOOK_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testDeleteBook() throws DuplicateBookException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);

        assertEquals(1, library.viewAllBooks().size());
        assertTrue(library.deleteBook(book));
        assertEquals(0, library.viewAllBooks().size());
    }
}