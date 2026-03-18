# Patika.dev Desktop App

This project now starts with a **login screen** and routes users by `usertype`:

- `admin` -> Admin panel (`AdminGUI`)
- `educator` -> Educator placeholder screen
- `student` -> Student placeholder screen

## Entry Point

Use `com.patikadev.App` (recommended) or `com.patikadev.View.AdminGUI`.
Both now open the login window first.

## Build

```bash
cd "/Users/ihsancakmak/IdeaProjects/Patika.dev_DesktopApp"
javac -cp "src/postgresql-42.7.5.jar:src" $(find src/com -name "*.java")
```

## Run

```bash
cd "/Users/ihsancakmak/IdeaProjects/Patika.dev_DesktopApp"
java -cp "src:src/postgresql-42.7.5.jar" com.patikadev.App
```

