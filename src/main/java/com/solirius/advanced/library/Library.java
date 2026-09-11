package com.solirius.advanced.library;

import com.solirius.advanced.library.exceptions.AlreadyBorrowedException;
import com.solirius.advanced.library.exceptions.BookNotFoundException;
import com.solirius.advanced.library.exceptions.DuplicateBookException;
import com.solirius.advanced.library.exceptions.NotBorrowedException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.solirius.advanced.library.Constants.*;


/**
 * Represents a library that holds a collection of books.
 */
public class Library {
    /**
     * Library Books.
     */
    private final List<Book> books;
    /**
     * SqlLite Connection.
     */
    private final Connection connection;

    /**
     * Creates a library from SqlLite if not existing else updates book list with entries.
     */
    public Library(Connection connection) {
        this.books = new ArrayList<>();
        this.connection = connection;
        try {
            // Creates a table for books if not existing
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS books (title TEXT, author TEXT, isBorrowed BOOLEAN)");
            // Query the books table
            Statement libraryStatement = connection.createStatement();
            ResultSet libraryResultSet = libraryStatement.executeQuery("SELECT title, author, isBorrowed FROM books");
            // Add books from the table to the library
            while (libraryResultSet.next()) {
                String title = libraryResultSet.getString("title");
                String author = libraryResultSet.getString("author");
                boolean isBorrowed = libraryResultSet.getBoolean("isBorrowed");
                Book book = new Book(title, author);
                if (isBorrowed) {
                    book.borrowBook();
                }
                this.books.add(book);
            }
            libraryResultSet.close();
            libraryStatement.close();
        } catch (SQLException e) {
            System.out.println("Error in connecting to the library database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a book to the library.
     *
     * @param book the book to add
     * @return true if successful, otherwise false
     */
    public boolean addBook(final Book book) throws DuplicateBookException {
        if (book == null || book.getTitle().isBlank() || book.getAuthor().isBlank()) {
            throw new IllegalArgumentException(BOOK_EMPTY_FIELDS);
        }

        if (bookAlreadyExists(book)) {
            throw new DuplicateBookException(BOOK_ALREADY_EXISTS);
        }

        try {
            String query = "INSERT INTO books (title, author, isBorrowed) VALUES (?, ?, ?)";
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, book.getTitle());
            preparedStatement.setString(2, book.getAuthor());
            preparedStatement.setBoolean(3, book.isBorrowed());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            books.add(book);
        } catch (SQLException e) {
            System.out.println("Error adding book to the library: " + e.getMessage());
            return false;
        }
        return true;
    }


    /**
     * Deletes a book from the library.
     *
     * @param book the book to delete
     * @return true if successful, otherwise false
     */
    public boolean deleteBook(final Book book) {
        try {
            String query = "DELETE FROM books WHERE title = ? AND author = ?";
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, book.getTitle());
            preparedStatement.setString(2, book.getAuthor());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            books.remove(book);
        } catch (SQLException e) {
            System.out.println("Error deleting book from the library: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Gets a list of available books.
     *
     * @return a list of books that are not borrowed
     */
    public List<Book> viewAvailableBooks() {
        return books.stream().filter(book -> !book.isBorrowed()).collect(Collectors.toList());
    }

    /**
     * Gets a list of all books.
     *
     * @return a list of all books in library, borrowed or not.
     */
    public List<Book> viewAllBooks() {
        return books;
    }

    /**
     * Searches for a book by its title or author.
     * The search is case-insensitive and supports partial matches.
     *
     * @param titleAuthor the title or author, or part of the title or author,
     *                    to search for
     * @return the first matching book
     * @throws BookNotFoundException if no matching book is found
     */
    public Book searchBook(final String titleAuthor) throws BookNotFoundException {
        final String searchTerm = titleAuthor.toLowerCase();

        return books.stream()
                .filter(book -> book.getTitle().equalsIgnoreCase(titleAuthor)
                        || book.getAuthor().equalsIgnoreCase(titleAuthor)
                        || book.getTitle().toLowerCase().contains(searchTerm)
                        || book.getAuthor().toLowerCase().contains(searchTerm))
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException(BOOK_NOT_FOUND));
    }

    /**
     * Searches for books by author.
     * The search is case-insensitive and supports partial matches.
     *
     * @param author the author name or part of the author name to search for
     * @return a list of books written by the matching author
     * @throws BookNotFoundException if no books are found for the author
     */
    public List<Book> searchByAuthor(final String author) throws BookNotFoundException {
        final String searchTerm = author.toLowerCase();

        final List<Book> results = books.stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            throw new BookNotFoundException(BOOK_NOT_FOUND);
        }

        return results;
    }

    /**
     * Borrows a book by its title.
     *
     * @param title the title of the book to borrow
     * @return true if the book is successfully borrowed
     */
    public boolean borrowBook(final String title) throws BookNotFoundException, AlreadyBorrowedException {
        Book book = searchBook(title);
        if (book.isBorrowed()) {
            throw new AlreadyBorrowedException(BOOK_ALREADY_BORROWED);
        }
        boolean borrowed = book.borrowBook();
        persistBorrowedState(book);
        return borrowed;
    }

    /**
     * Returns a book by its title.
     *
     * @param title the title of the book to return
     * @return true if the book is successfully returned, otherwise false
     */
    public boolean returnBook(final String title) throws BookNotFoundException, NotBorrowedException {
        Book book = searchBook(title);
        if (!book.isBorrowed()) {
            throw new NotBorrowedException(BOOK_NOT_BORROWED);
        }
        boolean returned = book.returnBook();
        persistBorrowedState(book);
        return returned;
    }

    /**
     * Writes the book's borrowed flag to the database so it survives restart.
     *
     * @param book the book whose state should be saved
     */
    private void persistBorrowedState(final Book book) {
        try {
            String query = "UPDATE books SET isBorrowed = ? WHERE title = ? AND author = ?";
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setBoolean(1, book.isBorrowed());
            preparedStatement.setString(2, book.getTitle());
            preparedStatement.setString(3, book.getAuthor());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("Error saving library to the database: " + e.getMessage());
        }
    }

    /**
     * Checks whether a book with the same title and author
     * already exists in the library.
     *
     * @param book the book to check for duplicates
     * @return true if a book with the same title and author exists,
     * otherwise false
     */
    public boolean bookAlreadyExists(final Book book) {
        for (Book existingBook : viewAllBooks()) {
            if (existingBook.getTitle().equalsIgnoreCase(book.getTitle())
                    && existingBook.getAuthor().equalsIgnoreCase(book.getAuthor())) {
                return true;
            }
        }
        return false;
    }
}
