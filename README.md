# Library Management System

A Java Swing desktop application for managing a small library's catalogue, student records, book circulation, issue history, and overdue loans. The application uses JDBC to connect to MySQL and is maintained as a NetBeans/Ant project.

> **Project status:** prototype / academic project. The source audit found useful working CRUD and circulation flows, but authentication, transaction safety, database provisioning, and parts of the desktop layout still need work before the application should be used with real library data.

## Features currently present

| Area | Existing screens / behaviour |
|---|---|
| Entry and authentication | Welcome screen, administrator sign-up, administrator login, student login |
| Dashboard | Book-title count, student count, currently issued count, overdue count, most-issued-books chart, recent catalogue/student tables |
| Catalogue | Add, update, delete, and list books |
| Students | Add, update, delete, and list student records |
| Circulation | Issue a book, reject a duplicate active issue for the same student/book pair, return a book, adjust available quantity |
| Reporting | View active issues, view all issue records, filter records by issue date, view overdue loans |

The student-facing authentication path is not currently complete. See [the technical audit](docs/TECHNICAL_AUDIT.md).

## Technology

- Java (source/target level 18; the project was compiled with JDK 21 during the latest audit)
- Java Swing and NetBeans-generated form classes (`.java` and `.form`)
- MySQL through MySQL Connector/J 8.0.31
- Apache Ant / NetBeans project build scripts
- JFreeChart for the dashboard pie chart
- Bundled UI libraries: `RSCalendar.jar`, `RSFoto_v1.0.jar`, and `RojeruSan.parte1.jar`

## Repository layout

```text
Library_management_system-main/
├── README.md
├── docs/
│   ├── ARCHITECTURE.md
│   ├── DATABASE.md
│   ├── TECHNICAL_AUDIT.md
│   └── USER_GUIDE.md
├── Library_Management_System/
│   ├── Dependencies/       # third-party JAR dependencies
│   ├── src/
│   │   ├── jFrame/         # Swing screens and JDBC interaction
│   │   └── library_management_system/
│   ├── nbproject/          # NetBeans/Ant project configuration
│   ├── build.xml
│   └── manifest.mf
└── .gitignore
```

Generated `build/`, `dist/`, and machine-specific `nbproject/private/` content are intentionally excluded from source control.

## Requirements

1. JDK 18 or newer.
2. Apache Ant 1.10 or newer, or a recent NetBeans IDE with Java support.
3. MySQL 8.x running locally or at a reachable database host.
4. The MySQL database and tables described in [Database setup and schema contract](docs/DATABASE.md).

The uploaded project did **not** contain a database dump, SQL migration, or schema-creation script. The database table/column contract in the database guide has therefore been inferred from the SQL statements in the source. Verify it against your existing database before migrating data.

## Configure the database

The database connection no longer contains a password in source code. Configure the following environment variables in the shell that will launch the application:

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/library_management_system` | JDBC URL, including any required connection options |
| `DB_USER` | `library_app` | MySQL application account |
| `DB_PASSWORD` | required | Password for the application account |

### Windows PowerShell

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/library_management_system"
$env:DB_USER = "library_app"
$env:DB_PASSWORD = "replace-with-your-local-secret"
```

### Linux / macOS

```bash
export DB_URL='jdbc:mysql://localhost:3306/library_management_system'
export DB_USER='library_app'
export DB_PASSWORD='replace-with-your-local-secret'
```

The matching JVM system properties are also supported: `db.url`, `db.user`, and `db.password`. Environment variables take precedence over system properties. Do not commit passwords, local environment files, or database exports.

Use a dedicated, least-privilege MySQL account rather than `root`. A sample account/grant statement is documented in [docs/DATABASE.md](docs/DATABASE.md).

## Build and run

From the project directory:

```bash
cd Library_Management_System
ant clean jar
ant run
```

`ant run` launches the configured entry point, `jFrame.WelcomePage`, using the project's compile/runtime classpath. The command-line `jar` target creates the application JAR; in a plain Ant environment it does not automatically bundle all third-party JARs. To launch the packaged JAR directly, include the dependency directory on the runtime classpath:

**Windows PowerShell**

```powershell
java -cp "dist/Library_Management_System.jar;Dependencies/*" jFrame.WelcomePage
```

**Linux / macOS**

```bash
java -cp 'dist/Library_Management_System.jar:Dependencies/*' jFrame.WelcomePage
```

The database must be configured and populated before the screens can load their tables. Compilation does not verify database connectivity or business workflows.

## Documentation

- [Architecture and workflows](docs/ARCHITECTURE.md)
- [Database setup and schema contract](docs/DATABASE.md)
- [Technical audit, defects, and improvement roadmap](docs/TECHNICAL_AUDIT.md)
- [User guide](docs/USER_GUIDE.md)

## Security and data warning

The previous source version contained a literal MySQL password. Treat that credential as exposed: rotate it in MySQL and use the environment-based configuration above. This change protects the database connection secret, but it does **not** fix the application's current plaintext application-password storage or its incomplete student/admin role separation. Do not use real student credentials or production library data until the P0 items in the audit are resolved.

## Build verification

The updated source was built with `ant clean jar` using OpenJDK 21 and Ant 1.10.15. Compilation succeeded. One compiler warning remains for the configured Java 18 source level on JDK 21, and `SignupPage.java` still uses a deprecated Swing password-field accessor. End-to-end database and GUI testing was not possible because no database schema/seed data was included with the upload.
