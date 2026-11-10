# Technical audit and improvement roadmap

**Audit date:** 10 November 2026  
**Scope:** Static review of the uploaded source archive, build configuration, form navigation, SQL statements, and a clean Ant build. No live MySQL schema or seed data was supplied.

## 1. Executive assessment

The project demonstrates the core shape of a desktop Library Management System: catalogue and student CRUD, book issue/return operations, dashboard summaries, and overdue/history screens. Most UI forms contain direct JDBC logic and NetBeans-generated Swing layouts.

It is a useful prototype, but it is not ready for real student accounts or operational library data. The highest-risk gaps are plaintext application passwords, broken student/admin separation, non-atomic circulation updates, and missing database migrations/tests. The user interface also relies on fixed absolute coordinates, with evidence of components extending beyond their windows.

## 2. Changes applied in this update

| File | Change | Why it matters |
|---|---|---|
| `src/jFrame/DBConnection.java` | Removed the hard-coded MySQL password; read `DB_URL`, `DB_USER`, `DB_PASSWORD` or matching JVM properties; throw a clear `SQLException` when the password is missing; create a new connection per call | Removes a committed secret and makes the project configurable across machines |
| `nbproject/project.properties` | Replaced developer-specific absolute JAR paths with the bundled `Dependencies/` paths; removed obsolete JCalendar and duplicate local paths from the compile classpath | Makes the Ant compile reproducible from the repository |
| `nbproject/private/` | Removed the machine-specific NetBeans private configuration from the working copy | Prevents the absolute path override and personal local IDE path data from being distributed |
| `.gitignore` | Added build output, NetBeans private state, local secrets, and logs | Keeps generated files and credentials out of source control |
| `src/jFrame/ViewAllRecord.java` | Fixed the first-row-skipping bug; validate both dates and date order; use try-with-resources; show useful search/database messages; order results newest first | Makes the date-range report return every matching loan and close its JDBC resources |
| `src/jFrame/HomePage.java` | Replaced four scroll-to-last row-count scans with one prepared statement using `COUNT(*)` subqueries and `CURRENT_DATE()` | Avoids loading all rows just to count them and closes resources reliably |

### Verification

`ant -q clean jar` completed successfully with OpenJDK 21 and Apache Ant 1.10.15 after removing the archived `nbproject/private` override. One source/target warning remains because the project targets Java 18 while the audit environment uses JDK 21; `SignupPage.java` also produces a note about a deprecated API. No SQL integration tests were run because the uploaded archive contains no database schema or test data.

## 3. Findings by severity

### Critical — resolve before handling real accounts or circulation data

| Finding | Evidence | Impact | Recommended action |
|---|---|---|---|
| Plaintext application passwords | `SignupPage.insertSignupDetails()` writes the password to `users.password`; `LoginPage.login()` compares it directly | A database leak exposes users' passwords | Migrate to a password hash using PBKDF2, bcrypt, or Argon2; never retrieve/compare stored plaintext |
| Student/admin authorization is broken | `StudentLogin.login()` queries `Students` but opens `HomePage`; `SignupPage` inserts into `users` and does not create `Students` rows | Student sign-up/login is inconsistent; a student session reaches admin functions | Unify authentication around a role-aware account model, create a distinct student home screen, and guard every admin action |
| Public administrator account creation | `WelcomePage` routes the Admin section to `SignupPage`; there is no role or invitation check | Anyone who can open the application can create an account that reaches the admin dashboard | Remove open admin self-registration; bootstrap the first admin securely and require an existing admin to create later admin accounts |
| Issue/return are not atomic | `IssueBook.issueBook()` and `updateBookCount()` are separate operations; `ReturnBook.returnBook()` and `updateBookCount()` are also separate | Failed stock updates can leave the loan ledger and available quantity inconsistent; concurrent issues can oversubscribe a book | Move circulation logic to a service and execute all ledger + quantity changes in one transaction with row locking and guarded updates |

### High — fix before a wider release

| Finding | Evidence | Impact | Recommended action |
|---|---|---|---|
| Weak input validation | IDs are parsed with `Integer.parseInt()` before the database try/catch in issue/return and CRUD forms; date chooser values may be null | A blank or malformed value can throw on the Swing event thread; invalid due dates can be recorded | Validate trimmed text, numeric ranges, required selections, and `dueDate >= issueDate` before calling a service |
| SQL/resource management is inconsistent | Several screens create `Connection`, `Statement`, `PreparedStatement`, and `ResultSet` without try-with-resources; exceptions are often swallowed with `e.getMessage()` | Connections may remain open; failures look like empty screens or failed buttons without a useful diagnosis | Use try-with-resources everywhere; show user-safe errors and log technical details with `java.util.logging` or SLF4J |
| Hard deletes can damage history | `ManageBooks.deleteBook()` and `ManageStudents.deleteStudent()` delete master records without checking active loans/history | Deletion may fail silently under foreign keys or leave orphaned loan history without them | Prevent deletion when referenced; prefer `active`/`archived` flags for catalogue and student records |
| Database schema is not versioned | No SQL dump, migration, or test schema is included in the archive | A new developer cannot reliably provision or migrate the database | Add checked-in versioned migrations, seed data for development, and migration instructions |
| Swing event thread does database work | Screen constructors load tables/charts synchronously; button handlers query/update MySQL directly | Slow database operations freeze the entire window | Run I/O in `SwingWorker`; update Swing components only in `done()` on the Event Dispatch Thread |

### Medium — improve usability and maintainability

| Finding | Evidence | Recommended action |
|---|---|---|
| Fixed absolute layouts | Forms use `AbsoluteLayout` and hard-coded coordinates | Replace with `BorderLayout`, `GridBagLayout`, nested panels, and consistent padding; test at 100%, 125%, and 150% display scaling |
| Controls exceed their frame bounds | `WelcomePage` adds a 1500×800 panel in a 1284×742 frame and places its close control at x=1460; `StudentLogin` places a minimize label at x=1460 in a 1298×750 frame; `HomePage` places a 1250px-wide content panel at x=220 inside a 1289px-wide frame | Controls/content can be clipped or inaccessible on common displays | Normalize window dimensions and make the content scrollable/responsive; keep close/minimize controls within the visible title bar |
| Inconsistent visual language | Teal, red, and lime accents; varying typefaces/sizes; custom controls and labels used as buttons | The application feels visually inconsistent and interaction affordances are unclear | Define a small theme (primary, accent, danger, surface, text, spacing, typography) and reuse standard button/input components |
| Mouse-only navigation | Many actions are attached to `JLabel` mouse listeners | Keyboard and assistive-technology users may not discover or activate controls | Use `JButton` for actions, meaningful accessible names, predictable tab order, visible focus, and Enter/Escape behaviour |
| Limited table interaction | Lists have no evident pagination or robust search/filter controls for books/students | Large catalogues will become slow and hard to navigate | Add text search, column sorting, filter controls, pagination, and empty/loading/error states |
| Ambiguous book metric | Dashboard counts rows in `book_details`, not total physical copies | “No. of Books” can be mistaken for total inventory | Label the card “Book titles” or count the sum of available copies separately |
| Status stored as display strings | Logic depends on `Pending...` and `Returned!` | UI wording changes can break business rules | Use stable enum/code values in the database/domain and format labels in the UI |

## 4. Screen-by-screen UX recommendations

### Welcome and authentication

- Show clearly separated “Administrator” and “Student” actions with a short explanation of what each role can do.
- Keep the administrator route login-only after initial provisioning; do not expose unrestricted administrator registration.
- Add inline validation and field-level feedback rather than modal dialogs for every missing field.
- Use a consistent password visibility toggle and disable submit while an authentication request is running.
- Return students to a student-only dashboard with their current loans, due dates, and borrowing history.

### Dashboard

- Keep the four high-level cards, but make labels precise: **Book titles**, **Registered students**, **Books currently on loan**, and **Overdue loans**.
- Add quick actions for “Issue a book” and “Register a student”.
- Give the chart an explicit empty state and consider a horizontal bar chart for easier book-name comparison.
- Add a clear refresh action and a last-updated timestamp.

### Manage books / students

- Put list search above the table and show record counts.
- Clearly distinguish Add, Update, and Archive/Delete actions; require confirmation for destructive actions.
- Validate fields inline; quantity must be a non-negative integer, IDs must be valid and unique, and names must not be blank.
- Keep the selected table row visually highlighted, allow keyboard selection, and provide a Clear Form action.
- Explain why a record cannot be archived/deleted when active loans or historical dependencies exist.

### Issue / return

- Default issue date to today and calculate a suggested due date based on a configurable loan policy.
- Validate that the book exists, available copies are greater than zero, the student is active, and due date is not before issue date.
- Display a clear confirmation summary (book, student, issue date, due date) before committing an issue.
- Return by loan ID or selected active-loan row instead of requiring two IDs to be re-entered.
- Show the active loan's due date and overdue status before confirming a return.
- Only show success after the transaction commits; on failure, explain that no partial update was saved.

### Reports

- Keep the date filter visible above the results and show the selected range in the report header.
- Include total result count, clear/reset filter action, and table sorting.
- Add filters for status, student, and book after the base flow is stable.
- Use one consistent date format throughout all screens and reports.

## 5. Prioritized implementation roadmap

### Phase 0 — Security and data correctness

1. Rotate the previously embedded MySQL credential; do not reuse it.
2. Inspect and version the actual database schema.
3. Replace plaintext password storage with a password hash and one role-aware user model.
4. Remove open administrator registration and implement role checks.
5. Implement transactional issue/return operations with database constraints and concurrency tests.
6. Add safe validation for every numeric/date field.

### Phase 1 — Reliability and testability

1. Introduce repositories/DAOs and services; remove inline SQL from JFrame classes.
2. Apply try-with-resources to every JDBC operation and add structured logging.
3. Move database work to `SwingWorker` so the UI stays responsive.
4. Add unit tests for validation/business rules and MySQL integration tests for repository and transaction behaviour.
5. Add reproducible database migrations and test fixtures.

### Phase 2 — UI/UX modernization

1. Replace absolute positioning with layout managers and responsive containers.
2. Standardize typography, palette, button styles, spacing, and form validation.
3. Replace clickable labels with accessible buttons and add keyboard support.
4. Add search, sort, filters, empty states, and explicit loading/error states.
5. Clarify dashboard metrics and circulation status labels.

### Phase 3 — Product capabilities

1. Student dashboard and student self-service loan history.
2. Loan policy configuration (loan duration, renewals, maximum active loans).
3. Fine calculation and payment tracking, if required by the library.
4. Book categories, ISBN, publisher, edition, shelf/location, and barcode/QR support.
5. Export reports to CSV/PDF and add scheduled overdue notifications.

## 6. Minimum regression test checklist

- Correct and incorrect administrator credentials.
- Student login cannot open administrator screens.
- Empty, malformed, negative, and duplicate book/student IDs.
- Book quantity of zero; issuing the final available copy; two simultaneous issue attempts.
- Same student cannot hold duplicate active loans for the same book.
- Due date equal to issue date, earlier than issue date, and later than issue date.
- Return a valid active loan; attempt a second return; ensure stock increments once.
- Force a failure between loan insert and stock update; verify rollback leaves both unchanged.
- Search for one record and multiple records; verify the first row appears.
- Search with missing dates and a reversed date range.
- Overdue boundary: due yesterday, due today, and due tomorrow.
- Run screens with a slow/unavailable database and verify the UI remains responsive and reports the error.
- Test at common screen sizes and Windows display scaling settings.
