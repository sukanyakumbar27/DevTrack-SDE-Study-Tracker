# DevTrack Learning Guide

Read this together with the code. Each part tells you which files to open.

## 1. What each folder does
- `backend/` - the Java server. It owns the data and the rules. It never draws any screen.
- `frontend/` - the React app that runs in the browser. It draws screens and talks to the backend over HTTP.
- `database/` - reference SQL for the tables.
- `docs/` - API docs, Postman collection, this guide.

Inside `backend/src/main/java/com/devtrack/`:
| Folder | Job | Analogy |
|---|---|---|
| `controller/` | Receives HTTP requests, returns JSON | Waiter taking orders |
| `service/` | Business logic (rules, calculations) | Kitchen |
| `repository/` | Talks to the database | Pantry access |
| `entity/` | Java classes that map to tables | Shape of each shelf |
| `dto/` | Shapes of data going in and out of the API | Order slips |
| `security/`, `config/` | JWT, password hashing, CORS | Door lock |
| `exception/` | Turns errors into clean JSON | Complaint desk |

Inside `frontend/src/`: `pages/` (one file per screen), `components/` (reusable pieces: Layout, Modal, TaskForm),
`context/` (login state and theme shared everywhere), `api/client.js` (the one place that calls the backend),
`hooks/useFetch.js` (load data with loading/error handling).

## 2. What each key file does
- `DevTrackApplication.java` - `main` method; `@SpringBootApplication` starts the server.
- `pom.xml` - Maven file listing dependencies (Spring Web, JPA, Security, MySQL driver, JWT).
- `application.properties` - port, database URL, JWT secret, CORS. Values like `${DB_PASSWORD:}` read environment variables.
- `entity/OwnedEntity.java` - shared fields (id, user, createdAt) so each row belongs to one user.
- `entity/StudyTask.java` - one table row = one study task. `@Entity`, `@Table`, `@Column` map fields to columns.
- `repository/StudyTaskRepository.java` - an interface only. Spring writes the SQL from method names
  like `findByIdAndUserId`.
- `service/StudyTaskService.java` - create/update/toggle/log time. `@Transactional` = all-or-nothing database work.
- `controller/StudyTaskController.java` - maps URLs (`/api/tasks`) to service methods.
- `service/DashboardService.java`, `AnalyticsService.java`, `StatsCalculator.java` - read saved data and compute statistics.
- `security/JwtService.java` - creates and checks tokens. `JwtAuthFilter.java` - reads the token on each request.
- `config/SecurityConfig.java` - which URLs are public, password encoder, CORS.
- `frontend/src/main.jsx` - starts React. `App.jsx` - the list of routes (URL -> page).
- `pages/Planner.jsx` - the study planner screen. `components/TaskForm.jsx` - the add/edit popup.

## 3. How the React frontend works
React builds the screen from **components** (functions that return JSX, HTML-like code).
- `useState` stores values that change (the form fields, the selected filter). When state changes, React redraws.
- `useEffect` runs code after drawing (e.g. load tasks when the page opens). Our `useFetch` hook wraps this.
- React Router shows a different page per URL without reloading the browser.
- Context (`AuthContext`) shares the logged-in user with every component.
- Tailwind CSS gives small classes like `rounded-xl p-5` instead of writing separate CSS files.

## 4. How the Spring Boot backend works
Request path for `POST /api/tasks`:
1. Tomcat (embedded server) receives it.
2. Security filters run (`JwtAuthFilter` checks the token).
3. Spring finds `StudyTaskController.create` from `@PostMapping`.
4. `@RequestBody` turns the JSON into a `TaskRequest` record; `@Valid` runs validation (`@NotBlank` etc.).
   Invalid -> `GlobalExceptionHandler` returns 400 with field messages.
5. Controller calls `StudyTaskService.create`, which builds a `StudyTask` and calls `repository.save`.
6. Hibernate creates the SQL `INSERT`. MySQL stores the row.
7. The saved entity is converted to JSON and returned with status 201.
Spring creates the objects and passes them into constructors (**dependency injection**), so we never write `new StudyTaskService()`.

## 5. How REST APIs connect frontend and backend
REST = use URLs for things and HTTP methods for actions: GET read, POST create, PUT update, PATCH small change, DELETE remove.
Example in `Planner.jsx`: `api.patch('/tasks/12/toggle')` -> the backend flips task 12 -> the page calls `reload()` -> fresh list.
The browser and server only exchange JSON text. Status codes tell the result: 200 OK, 201 created, 400 bad input,
401 not logged in, 404 not found, 409 conflict, 500 server bug.

## 6. How MySQL stores the information
Each entity is a table; each object is a row; each field is a column.
`study_tasks.user_id` is a **foreign key** to `users.id`, so MySQL guarantees a task belongs to a real user.
`study_sessions.task_id` links time blocks to tasks. `project_tasks.project_id` links tasks to projects.
Dashboard hours come from `study_sessions`; completion percentages come from `study_tasks`.
`ddl-auto=update` lets Hibernate create missing tables on startup and never drops your data, so refresh/restart keeps everything.

## 7. How authentication works
1. Register: the password is hashed with **BCrypt** (one-way, salted). Only the hash is stored.
2. Login: BCrypt checks the typed password against the hash. If correct the server creates a **JWT**:
   a signed text token containing the email and an expiry time.
3. The frontend stores the token in `localStorage` and `api/client.js` adds `Authorization: Bearer <token>` to every request.
4. `JwtAuthFilter` verifies the signature (needs `JWT_SECRET`), loads the user, and marks the request as authenticated.
5. Services always query with the logged-in user's id (`findByIdAndUserId`), so user A can never read user B's rows.
Without a valid token the server answers 401 and the frontend sends you to the login page.
Trade-off to mention in interviews: localStorage is simple but exposed to XSS; httpOnly cookies plus CSRF protection are an alternative.

## 8. Data flow: button click to database and back
`Planner.jsx` checkbox -> `toggle(t)` -> `api.patch('/tasks/{id}/toggle')` -> `StudyTaskController.toggle`
-> `StudyTaskService.toggle` (find task for this user, flip status, set completedAt) -> Hibernate `UPDATE study_tasks`
-> JSON response -> `reload()` -> `GET /api/tasks` -> React redraws. The dashboard later calls `GET /api/dashboard`,
which counts completed tasks from the same table, so statistics update automatically.

## 9. Run locally
See the root `README.md`: start MySQL, run `mvn spring-boot:run` in `backend/`, run `npm install` then `npm run dev` in `frontend/`,
open http://localhost:5173.

## 10. Debugging common errors
- Read the **first** `Caused by:` line in the Spring Boot log, not the whole stack trace.
- Browser: F12 -> Network tab -> click the failing request -> see status, request body, response JSON.
- 400: check `fieldErrors` in the response. 401: log in again. 404: wrong URL or id. 500: look at the backend console.
- Test one endpoint in Postman before blaming the React code.
- DB problems: confirm MySQL is running and `DB_PASSWORD` is set; check tables with MySQL Workbench or `SHOW TABLES;`.
- Blank page: browser Console tab shows JavaScript errors.

## 11. Upload to GitHub
See README "Upload to GitHub". Commit small and often with clear messages. Add screenshots to the README and a
short "what I learned" section. Never commit passwords.

## 12. Explaining this project in an SDE interview
**30-second pitch:** "DevTrack is a full-stack study tracker I built with React, Spring Boot and MySQL. It has JWT authentication,
a layered REST API with validation and per-user data isolation, and analytics computed from the database.
I built it to organize my own placement preparation."

Be ready for:
- Why layers (controller/service/repository)? Separation of concerns, testability.
- How is auth done? BCrypt + stateless JWT + filter; why stateless scales.
- How do you stop one user reading another's data? Every query includes the user id.
- Why DTOs? Control what the client can send and validate it; do not expose internals.
- N+1 problem? See `findAllWithProject` (join fetch) in `ProjectTaskRepository`.
- What would you improve? Flyway migrations, pagination, refresh tokens, Docker, CI, more tests, SQL aggregation for large data.
- Hardest bug you fixed? Prepare one real story from your own debugging.
