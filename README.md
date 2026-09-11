This repository contains a Java challenge designed to assess the skills of a Java Guild candidate. The challenge includes problem-solving, coding, and demonstrating knowledge of Java fundamentals and best practices.

# Build a Library Management System
## Objective:
Create a simplified Library Management System (LMS) using Java that allows users to perform basic operations like adding books, borrowing books, and viewing available books.

# Requirements
## Book Class (visibility at candidate discretion)
Represents a book in the library.

### Attributes (visibility at candidate discretion)
 * String title
 * String author
 * boolean isBorrowed

### Methods (visibility at candidate discretion)
 * Book(String title, String author) - Constructor to initialise a book.
 * boolean borrowBook() - Marks the book as borrowed if it's not already borrowed.
 * boolean returnBook() - Marks the book as returned.
 * String toString() - Returns a string representation of the book (e.g., "Title by Author (Available/Borrowed)").

## Library Class (visibility at candidate discretion)
Manages a collection of books.

### Attributes (visibility at candidate discretion)
 * ArrayList<Book> books

### Methods (visibility at candidate discretion)
 * void addBook(Book book) - Adds a new book to the collection.
 * List<Book> viewAvailableBooks() - Returns a list of books that are not borrowed.
 * Book searchBook(String title) - Searches for a book by title.
 * boolean borrowBook(String title) - Allows a user to borrow a book by title.
 * boolean returnBook(String title) - Allows a user to return a book by title.

## Main Class (visibility at candidate discretion)
Provides a menu-driven interface for the user to interact with the Library Management System.

Options include:
 * Add a new book.
 * View available books.
 * Search for a book.
 * Borrow a book.
 * Return a book.
 * Exit the application.

## Incremental role-based extra features (eg. an intermediate is expected to build Graduate+Intermediate)
 * Graduate
   * Unit Tests: Write test cases for Book and Library classes. 
 * Intermediate
   * Exception Handling: Handle scenarios where a book is already borrowed, does not exist, or cannot be returned.
   * Persistence: Save the library state to a file and reload it upon application restart.
 * Advanced roles
   * Persistence: Save the library state to a portable file-based database.
   * Search: Find a book by title or author
   * Sorting: Provide an option to view books sorted by title or author.

## Instructions for Submission
Create a zip file (.7z or .zip) containing the code and any other files for your project.

Include:
 * The complete Java source code.
 * A README.md file with instructions on how to run the program.
 * A brief description of the approach taken and any additional features implemented.
 * Email the zip file to your talent acquisition contact as an attachment when completed.

## Evaluation Criteria
 * Code readability and organisation.
 * Proper use of object-oriented programming principles.
 * Handling edge cases and exceptions.
 * Implementation of bonus features (if any).
 * Clarity and thoroughness of the README file.

## How to run

Requires Java 11+ and Maven.

```bash
mvn test
mvn compile
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.mainClass=com.solirius.advanced.library.Main
```

Use the numbered menu to add, list, search, borrow, and return books. Catalogue state is stored in SQLite file `library.db` in the working directory and is reloaded the next time the application starts.

## Approach and features implemented

`Book` owns a single book's title, author, and borrow flag. `Library` owns the catalogue and the SQLite connection. `Main` only reads menu input and prints results.

**Graduate:** JUnit tests for `Book` and `Library` (`BookTest`, `LibraryTest`).

**Intermediate - exception handling:** Catalogue operations use checked exceptions instead of silent `false`/`null` results:
 * `BookNotFoundException` - search, borrow, or return of a title/author that is not in the library
 * `AlreadyBorrowedException` - borrow of a book that is already out
 * `NotBorrowedException` - return of a book that is not currently borrowed

`Main` catches these, prints the exception message, and keeps the menu running. Adding a `null` book is rejected (`addBook` returns `false`). Invalid menu input is rejected without exiting.

**Intermediate - persistence:** `Main` opens `jdbc:sqlite:library.db`. `Library` creates a `books` table if needed, loads existing rows on startup, inserts on add, and updates `isBorrowed` on borrow and return so a restart restores the catalogue.

Author search and sorting remain as they were; they are outside this persistence change.


I added this. Need to make it into good readme just for this section.
Handle exception when duplication books are being added. same author name and same book name.
Better search for books, by implementing contains and case insenstive and partialsearch
Cannot add books with empty parameters.
Better message when no books are in the system, or when none are available.
More options such as Search Books by author, delete a book and update a book and better tests and error handling.