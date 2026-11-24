# DevTrack - Personal SDE Preparation & Productivity Dashboard

A full-stack web app to plan study tasks, track DSA problems and Java topics, manage personal projects,
see analytics, and keep notes. Built as a layered Spring Boot REST API with a React dashboard.

| Layer     | Technology |
|-----------|-----------|
| Frontend  | React 18, Vite, Tailwind CSS, Recharts, React Router, Axios |
| Backend   | Java 17+, Spring Boot 3.3, Spring Data JPA, Spring Security (JWT), Bean Validation |
| Database  | MySQL 8 |
| Tooling   | Maven, Git, Postman |

## Features
Dashboard (date, hours, tasks, productivity %, weekly chart, streak, DSA count) - Study planner (CRUD, filters, checkbox
completion, time logging) - DSA tracker (11 topics, difficulty, status, notes, summaries) - Java learning checklist -
Analytics (7 charts + improvement trend) - Projects with milestones and GitHub links - Calendar (month/week) - Notes.
Dark/light theme and responsive layout. Every user only sees their own data.

## Folder structure
```
DevTrack/
  backend/    Spring Boot API (pom.xml, src/main/java/com/devtrack/..., src/test)
  frontend/   React app (package.json, src/pages, src/components, ...)
  database/   schema.sql (reference DDL)
  docs/       API.md, DevTrack.postman_collection.json, LEARNING_GUIDE.md
```

## Prerequisites
- JDK 17 or newer (`java -version`)
- Maven 3.9+ (`mvn -v`), or use your IDE's Maven support
- Node.js 18+ and npm
- MySQL 8 running locally

## Setup
### 1. Database
Nothing to create by hand: the backend connects with `createDatabaseIfNotExist=true` and Hibernate creates the tables.
Just make sure MySQL is running and you know your username and password.
(`database/schema.sql` shows the same tables if you want to read or run them manually.)

### 2. Backend
```bash
cd backend
# Set your MySQL password (Windows PowerShell: $env:DB_PASSWORD="yourpassword")
export DB_PASSWORD=yourpassword
export JWT_SECRET=replace-with-a-long-random-string-of-32-plus-characters
mvn spring-boot:run
```
API runs at http://localhost:8080. See `backend/.env.example` for all variables
(`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ORIGINS`).

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
```
Open http://localhost:5173. The Vite dev server proxies `/api` to `localhost:8080`.

### 4. First run
Click **Register**, keep "Load sample data" ticked, and explore. Data is stored in MySQL, so it survives restarts.

## Tests
```bash
cd backend
mvn test
```
Integration tests use an in-memory H2 database (no MySQL needed) and cover registration/login, wrong password,
task completion updating the dashboard, DSA and Java topics, projects, notes and per-user data isolation.

## Common problems
| Symptom | Fix |
|---|---|
| `Access denied for user 'root'` | Wrong `DB_PASSWORD`/`DB_USERNAME` |
| `Communications link failure` | MySQL is not running or wrong port in `DB_URL` |
| Port 8080 already in use | Stop the other app or set `server.port` |
| Frontend shows "Cannot reach the server" | Backend not running, or run frontend with `npm run dev` (proxy) |
| 401 on every call | Token expired or JWT_SECRET changed: log in again |
| CORS error in browser | Add your frontend URL to `CORS_ORIGINS` |

## Upload to GitHub
```bash
cd DevTrack
git init
git add .
git commit -m "Initial commit: DevTrack full-stack app"
git branch -M main
# create an EMPTY repo on github.com first, then:
git remote add origin https://github.com/<your-username>/devtrack.git
git push -u origin main
```
Never commit real passwords or secrets (`.env` files are already git-ignored).

## Security notes
Passwords are hashed with BCrypt; auth uses stateless JWT; every query is filtered by the logged-in user's id;
inputs are validated. Before deploying publicly: set a strong `JWT_SECRET`, use HTTPS, restrict `CORS_ORIGINS`,
and consider `ddl-auto=validate` with migrations (Flyway).
