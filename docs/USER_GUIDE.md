# User guide

This guide describes the visible screens and workflows in the current version. It does not imply that every path is ready for operational use; the student authentication path and security issues are called out explicitly.

## 1. Start the application

1. Start MySQL and confirm that the `library_management_system` database and expected tables exist.
2. Set `DB_URL`, `DB_USER`, and `DB_PASSWORD` in the shell used to launch the program.
3. From `Library_Management_System/`, run `ant run`.
4. The **Welcome** screen should open.

If a screen shows no records or a database error, confirm the database name, account grants, table names/capitalization, and required columns. The repository does not include a database seed or schema file.

## 2. Administrator workflow

The welcome page's Admin route currently leads to account registration. The account is written to `users`, and the administrator login checks that table. Because registration is not restricted to an existing administrator, this route must not be exposed in a shared or production environment.

After signing in, the dashboard provides navigation to the following tasks:

### Manage books

- Enter a unique book ID, book name, author, and quantity to add a book.
- Select a row in the table to load its values into the form.
- Use Update to save changes to the selected book ID.
- Use Delete to remove the selected book record.

Before deleting a record, ensure that the database's foreign-key rules and issue history are understood. The current application does not provide a safe archive workflow.

### Manage students

- Enter a unique student ID and student name.
- Select the course and branch from the available combo boxes.
- Add, update, or delete the student record.
- Select an existing row to edit its values.

The current student details form does not create a student login account. That is part of the unfinished role-aware authentication flow.

### Issue a book

1. Enter a book ID and move focus away from the field to load book details.
2. Enter a student ID and move focus away from the field to load student details.
3. Select issue and due dates.
4. Select Issue Book.

The current form checks available quantity and an existing pending loan for the same book/student pair. The database insert and stock decrement are separate operations, so verify the records after any database error. Do not rely on this workflow for live inventory until transactional circulation has been implemented.

### Return a book

1. Enter the book ID and student ID.
2. Select Find to load a matching pending loan.
3. Verify the displayed issue/due details.
4. Select Return Book.

The current implementation marks a matching pending row as returned and separately increments the book quantity. It should be replaced with a transaction before live use.

### Reports

- **Issued Book Details** lists loans with status `Pending...`.
- **View All Records** lists all issue records. Select both dates and search by issue date; Reset reloads the full list. The date-range search has been corrected to include the first matching result.
- **Defaulter List** lists loans still pending where the due date is earlier than the current date.

## 3. Student path: current limitation

The student login form queries a separate table named `Students`, but the available sign-up form inserts into `users`. The student login then opens the administrator dashboard. The project therefore does not currently provide a safe, complete student workflow. Do not use student login with real users until role separation and a student-specific dashboard are completed.

## 4. Common operational guidance

- Back up the database before importing, updating, or deleting records.
- Use unique book and student IDs.
- Do not edit status text in the database unless the corresponding Java logic is updated too.
- Do not commit passwords or database exports.
- Report any issue with the screen name, action performed, visible message, and the matching application/database log entry.
