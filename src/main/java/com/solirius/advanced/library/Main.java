package com.solirius.advanced.library;

import com.solirius.advanced.library.exceptions.AlreadyBorrowedException;
import com.solirius.advanced.library.exceptions.BookNotFoundException;
import com.solirius.advanced.library.exceptions.DuplicateBookException;
import com.solirius.advanced.library.exceptions.NotBorrowedException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import static com.solirius.advanced.library.Constants.*;

public final class Main {

    private static Library library;

    public static void setLibrary(Library library) {
        Main.library = library;
    }

    /**
     * Initialises the LMS program.
     *
     * @param args from the command line.
     */
    public static void main(final String[] args) {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:library.db");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println(OPENED);
        if (library == null) {
            library = new Library(connection);
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println(WELCOME);

        while (running) {
            System.out.println(MENU);
            int choice = validateChoices(scanner, 1, 9);
            scanner.nextLine(); // Consume newline
            String title;
            switch (choice) {
                case ADD_A_BOOK_OPTION:
                    System.out.print(ENTER_TITLE);
                    title = scanner.nextLine();
                    System.out.print(ENTER_AUTHOR);
                    String author = scanner.nextLine();
                    try {
                        boolean success = library.addBook(new Book(title, author));
                        System.out.println(success ? BOOK_ADDED : BOOK_NOT_ADDED);
                    } catch (DuplicateBookException | IllegalArgumentException exception) {
                        System.out.println(exception.getMessage());
                    }
                    break;

                case LIST_ALL_BOOKS:
                    List<Book> allBooks = library.viewAllBooks();
                    if (allBooks.isEmpty()) {
                        System.out.println(NO_BOOKS_AVAILABLE);
                        break;
                    }
                    System.out.println(SORT_MENU);
                    int sortAllChoice = validateChoices(scanner, 1, 2);
                    System.out.println(ALL_BOOKS);
                    switch (sortAllChoice) {
                        case AUTHOR:
                            allBooks.stream()
                                    .sorted(Comparator.comparing(Book::getAuthor))
                                    .forEach(System.out::println);
                            break;
                        case TITLE:
                            allBooks.stream()
                                    .sorted(Comparator.comparing(Book::getTitle))
                                    .forEach(System.out::println);
                            break;
                        default:
                            System.out.println(INVALID);
                    }
                    break;

                case LIST_AVAILABLE_BOOKS:
                    List<Book> availableBooks = library.viewAvailableBooks();
                    if (availableBooks.isEmpty()) {
                        System.out.println(NO_BOOKS_AVAILABLE);
                        break;
                    }

                    System.out.println(SORT_MENU);
                    int sortAvailableChoice = validateChoices(scanner, 1, 2);
                    System.out.println(AVAILABLE_BOOKS);
                    switch (sortAvailableChoice) {
                        case AUTHOR:
                            availableBooks.stream()
                                    .sorted(Comparator.comparing(Book::getAuthor))
                                    .forEach(System.out::println);
                            break;
                        case TITLE:
                            availableBooks.stream()
                                    .sorted(Comparator.comparing(Book::getTitle))
                                    .forEach(System.out::println);
                            break;
                        default:
                            System.out.println(INVALID);
                    }
                    break;

                case SEARCH_BOOK_OPTION:
                    System.out.print(TITLE_AUTHOR_SEARCH);
                    title = scanner.nextLine();
                    try {
                        Book book = library.searchBook(title);
                        System.out.println(book);
                    } catch (BookNotFoundException bookNotFoundException) {
                        System.out.println(bookNotFoundException.getMessage());
                    }
                    break;

                case BORROW_BOOK_OPTION:
                    System.out.print(TITLE_BORROW);
                    title = scanner.nextLine();
                    try {
                        library.borrowBook(title);
                        System.out.println(BORROWED);
                    } catch (BookNotFoundException | AlreadyBorrowedException bookNotFoundException) {
                        System.out.println(bookNotFoundException.getMessage());
                    }
                    break;

                case RETURN_BOOK_OPTION:
                    System.out.print(TITLE_RETURN);
                    title = scanner.nextLine();
                    try {
                        library.returnBook(title);
                        System.out.println(RETURNED);
                    } catch (BookNotFoundException | NotBorrowedException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case VIEW_BOOKS_BY_AUTHOR:
                    System.out.print(TITLE_VIEW_BOOKS_BY_AUTHOR);
                    title = scanner.nextLine();
                    try {
                        List<Book> booksByAuthor = library.searchByAuthor(title);
                        if (booksByAuthor.isEmpty()) {
                            System.out.println(NO_BOOKS_AVAILABLE);
                            break;
                        }

                        booksByAuthor.forEach(System.out::println);
                    } catch (BookNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case DELETE_A_BOOK:
                    List<Book> viewAvailableBooksToDelete = library.viewAvailableBooks();
                    if (viewAvailableBooksToDelete.isEmpty()) {
                        System.out.println(NO_BOOKS_AVAILABLE);
                        break;
                    }
                    for (int i = 0; i < viewAvailableBooksToDelete.size(); i++) {
                        System.out.println((i + 1) + " : " + viewAvailableBooksToDelete.get(i));
                    }
                    int deleteChoice = validateChoices(scanner, 1, viewAvailableBooksToDelete.size());
                    Book bookToDelete = viewAvailableBooksToDelete.get(deleteChoice - 1);
                    library.deleteBook(bookToDelete);
                    break;

                case EXIT_OPTION:
                    running = false;
                    System.out.println(EXIT);
                    break;

                default:
                    System.out.println(INVALID);
            }
        }

        scanner.close();
    }

    private static int validateChoices(Scanner scanner, int min, int max) {
        int choice = 0;
        boolean validChoice = false;
        while (!validChoice) {
            try {
                choice = scanner.nextInt();
                if (choice < min || choice > max) {
                    throw new InputMismatchException();
                }
                validChoice = true;
            } catch (InputMismatchException inputMismatchException) {
                System.out.println(INVALID);
                if (!scanner.hasNextInt() && scanner.hasNext()) {
                    scanner.next();
                }
            }
        }
        return choice;
    }
}
