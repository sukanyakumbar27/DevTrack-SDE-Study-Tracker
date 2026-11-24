# DevTrack REST API

Base URL: `http://localhost:8080/api`. All endpoints except `/auth/*` need the header
`Authorization: Bearer <token>`. Bodies are JSON. Errors look like
`{"status":400,"message":"Validation failed","fieldErrors":{"title":"must not be blank"},"timestamp":"..."}`.

## Auth
| Method | Path | Body | Result |
|---|---|---|---|
| POST | /auth/register | `{name, email, password(8-72), loadSampleData?}` | 201 `{token, name, email}` (409 if email used) |
| POST | /auth/login | `{email, password}` | 200 `{token, name, email}` (401 if wrong) |

## Study tasks
| Method | Path | Notes |
|---|---|---|
| GET | /tasks?status=&from=&to= | status: PENDING, IN_PROGRESS, COMPLETED; dates `YYYY-MM-DD` |
| POST | /tasks | `{title, description?, subject?, priority(LOW/MEDIUM/HIGH)?, status?, estimatedMinutes?, scheduledDate?}` |
| PUT | /tasks/{id} | same body |
| PATCH | /tasks/{id}/toggle | flips COMPLETED and PENDING |
| POST | /tasks/{id}/log-time | `{minutes}`: adds time and records a study session |
| DELETE | /tasks/{id} | 204 |

## Study sessions
GET /sessions?from=&to= - POST /sessions `{subject, minutes, sessionDate?}` - DELETE /sessions/{id}

## DSA
GET /dsa - GET /dsa/summary - POST /dsa - PUT /dsa/{id} - DELETE /dsa/{id}
Body: `{name, platform?, topic, difficulty(EASY/MEDIUM/HARD), status(SOLVED/ATTEMPTED/REVISIT), solvedDate?, notes?}`
Topics: ARRAYS, STRINGS, LINKED_LISTS, STACKS_AND_QUEUES, HASHING, BINARY_SEARCH, SORTING, RECURSION, TREES, GRAPHS, DYNAMIC_PROGRAMMING

## Java topics
GET /java-topics - PUT /java-topics/{id} `{completed?, notes?}` (12 topics are created for every new user)

## Projects
GET /projects (includes totalTasks, doneTasks, completionPercent) - POST /projects - PUT /projects/{id} - DELETE /projects/{id}
Body: `{name, description?, status(PLANNING/IN_PROGRESS/COMPLETED/ON_HOLD)?, githubUrl?}`
Tasks: GET /projects/{id}/tasks - POST /projects/{id}/tasks - PUT /projects/{id}/tasks/{taskId} - DELETE /projects/{id}/tasks/{taskId}
Task body: `{title, milestone?, done?, dueDate?}`

## Notes
GET /notes - POST /notes - PUT /notes/{id} - DELETE /notes/{id}. Body `{title, content?, category(CODING/CONCEPT/INTERVIEW)?}`

## Dashboard and analytics
GET /dashboard - GET /analytics (computed from your saved tasks, sessions and problems)
