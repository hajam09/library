package com.solirius.advanced.library;

/**
 * Shared string and menu constants for the Library Management System.
 */
public final class Constants {

    private Constants() {
    }

    /**
     * The main menu.
     */
    public static final String MENU = "\nMenu:"
        + "\n1. Add a new book"
        + "\n2. View available books"
        + "\n3. View all books"
        + "\n4. Search for a book"
        + "\n5. Borrow a book"
        + "\n6. Return a book"
        + "\n7. View all books by author"
        + "\n8. Delete a book"
        + "\n9. Exit"
        + "\nEnter your choice: ";

    /**
     * The main menu.
     */
    public static final String SORT_MENU = "\nSort by:"
        + "\n1. Author"
        + "\n2. Title"
        + "\nEnter your choice: ";
    /**
     * Option to sort by author.
     */
    public static final int AUTHOR = 1;
    /**
     * Option to sort by title.
     */
    public static final int TITLE = 2;
    /**
     * Option to add a Book.
     */
    public static final int ADD_A_BOOK_OPTION = 1;
    /**
     * Option to list available Books.
     */
    public static final int LIST_AVAILABLE_BOOKS = 2;
    /**
     * Option to list all Books.
     */
    public static final int LIST_ALL_BOOKS = 3;
    /**
     * Option to search a Book.
     */
    public static final int SEARCH_BOOK_OPTION = 4;
    /**
     * Option to borrow a Book.
     */
    public static final int BORROW_BOOK_OPTION = 5;
    /**
     * Option to return a Book.
     */
    public static final int RETURN_BOOK_OPTION = 6;
    /**
     * Option to view books by Author.
     */
    public static final int VIEW_BOOKS_BY_AUTHOR = 7;
    /**
     * Option to delete a Book.
     */
    public static final int DELETE_A_BOOK = 8;
    /**
     * Option to terminate the program.
     */
    public static final int EXIT_OPTION = 9;

    /**
     * String constants.
     */
    public static final String OPENED = "Opened library database successfully";
    public static final String WELCOME = "Welcome to the Library Management System!";
    public static final String ENTER_TITLE = "Enter book title: ";
    public static final String ENTER_AUTHOR = "Enter book author: ";
    public static final String BOOK_ADDED = "Book added successfully!";
    public static final String BOOK_NOT_ADDED = "Book not added.";
    public static final String ALL_BOOKS = "All books:";
    public static final String AVAILABLE_BOOKS = "Available books:";
    public static final String INVALID = "Invalid choice. Please try again.";
    public static final String EXIT = "Thank you for using the Library Management System!";
    public static final String RETURNED = "Book returned successfully!";
    public static final String BORROWED = "Book borrowed successfully!";
    public static final String TITLE_AUTHOR_SEARCH = "Enter the title or author of the book to search: ";
    public static final String TITLE_BORROW = "Enter the title of the book to borrow: ";
    public static final String TITLE_RETURN = "Enter the title of the book to return: ";
    public static final String TITLE_VIEW_BOOKS_BY_AUTHOR = "Enter the author of the books to return: ";
    public static final String NO_BOOKS_AVAILABLE = "No books available at the moment.";

    public static final String BOOK_NOT_FOUND = "Book not found: Book is not in the library.";
    public static final String BOOK_ALREADY_BORROWED = "Book not borrowed: Book has already been borrowed.";
    public static final String BOOK_NOT_BORROWED = "Book not returned: Book has not been borrowed.";
    public static final String BOOK_ALREADY_EXISTS = "This book already exists in the library.";
    public static final String BOOK_EMPTY_FIELDS = "Book title and author cannot be blank.";
}
