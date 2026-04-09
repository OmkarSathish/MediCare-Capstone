# API Endpoints

## Auth — `/api/auth`

| Method | Path                       | Access        | Description                                     |
| ------ | -------------------------- | ------------- | ----------------------------------------------- |
| `POST` | `/api/auth/signup`         | Public        | Register a new customer account                 |
| `POST` | `/api/auth/login`          | Public        | Authenticate and receive JWT tokens             |
| `POST` | `/api/auth/token/refresh`  | Public        | Exchange a refresh token for a new access token |
| `POST` | `/api/auth/logout`         | Public        | Revoke a refresh token                          |
| `GET`  | `/api/auth/me`             | Authenticated | Get the current user's profile                  |
| `POST` | `/api/auth/admin/register` | Admin only    | Register a new admin account                    |

---

## Patients — `/api/patients`

| Method   | Path                               | Access         | Description                         |
| -------- | ---------------------------------- | -------------- | ----------------------------------- |
| `POST`   | `/api/patients`                    | Authenticated  | Register a patient profile          |
| `GET`    | `/api/patients/{username}`         | Owner or Admin | Get a patient profile by username   |
| `PUT`    | `/api/patients/{username}`         | Owner or Admin | Update a patient profile            |
| `GET`    | `/api/patients/{username}/results` | Owner or Admin | Get all test results for a patient  |
| `GET`    | `/api/patients/results/{id}`       | Authenticated  | Get a specific test result by ID    |
| `POST`   | `/api/patients/results`            | Admin only     | Add a test result to an appointment |
| `PUT`    | `/api/patients/results/{id}`       | Admin only     | Update a test result                |
| `DELETE` | `/api/patients/results/{id}`       | Admin only     | Delete a test result                |

---

## Appointments — `/api/appointments`

| Method   | Path                            | Access                | Description                                                   |
| -------- | ------------------------------- | --------------------- | ------------------------------------------------------------- |
| `POST`   | `/api/appointments`             | Authenticated         | Book a new appointment                                        |
| `GET`    | `/api/appointments`             | Authenticated / Admin | List appointments (own, or by patient name, or all for admin) |
| `GET`    | `/api/appointments/{id}`        | Owner or Admin        | Get appointment details by ID                                 |
| `GET`    | `/api/appointments/{id}/status` | Owner or Admin        | Get approval status and history                               |
| `DELETE` | `/api/appointments/{id}`        | Owner or Admin        | Cancel an appointment                                         |

---

## Admin — Appointments — `/api/admin/appointments`

| Method | Path                                   | Access     | Description                                            |
| ------ | -------------------------------------- | ---------- | ------------------------------------------------------ |
| `GET`  | `/api/admin/appointments`              | Admin only | List appointments filtered by center, test, and status |
| `PUT`  | `/api/admin/appointments/{id}/approve` | Admin only | Approve a pending appointment                          |
| `PUT`  | `/api/admin/appointments/{id}/reject`  | Admin only | Reject a pending appointment                           |

---

## Admin — Dashboard — `/api/admin`

| Method | Path                   | Access     | Description                                    |
| ------ | ---------------------- | ---------- | ---------------------------------------------- |
| `GET`  | `/api/admin/dashboard` | Admin only | Get dashboard summary (totals + pending count) |

---

## Diagnostic Centers — `/api/centers`

| Method   | Path                                 | Access     | Description                                |
| -------- | ------------------------------------ | ---------- | ------------------------------------------ |
| `GET`    | `/api/centers`                       | Public     | List all centers or search by name keyword |
| `GET`    | `/api/centers/{id}`                  | Public     | Get a center by ID                         |
| `GET`    | `/api/centers/{id}/tests`            | Public     | Get all tests offered at a center          |
| `GET`    | `/api/centers/{id}/tests/{testName}` | Public     | Get details of a specific test at a center |
| `GET`    | `/api/centers/{id}/appointments`     | Admin only | Get all appointments at a center           |
| `GET`    | `/api/centers/offering/{testId}`     | Public     | Get all centers offering a specific test   |
| `POST`   | `/api/centers`                       | Admin only | Create a new diagnostic center             |
| `PUT`    | `/api/centers/{id}`                  | Admin only | Update a diagnostic center                 |
| `POST`   | `/api/centers/{id}/tests/{testId}`   | Admin only | Add a test to a center's offerings         |
| `DELETE` | `/api/centers/{id}`                  | Admin only | Soft-delete a diagnostic center            |

---

## Diagnostic Tests — `/api/tests`

| Method   | Path                               | Access     | Description                      |
| -------- | ---------------------------------- | ---------- | -------------------------------- |
| `GET`    | `/api/tests`                       | Public     | List all tests or search by name |
| `GET`    | `/api/tests/{id}`                  | Public     | Get a test by ID                 |
| `GET`    | `/api/tests/category/{categoryId}` | Public     | Get all tests in a category      |
| `POST`   | `/api/tests`                       | Admin only | Create a new diagnostic test     |
| `PUT`    | `/api/tests/{id}`                  | Admin only | Update an existing test          |
| `DELETE` | `/api/tests/{id}`                  | Admin only | Soft-delete a diagnostic test    |
