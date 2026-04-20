# MediCare — Diagnostic Appointment Booking System

A full-stack web application that lets patients discover diagnostic centers, book test appointments, and track their status, while giving center administrators and staff tools to manage centers, tests, and approvals.

---

## Role Hierarchy

| Role             | Description                                                                                                                           |
| ---------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| **System Admin** | Top-level administrator; manages all diagnostic centers, tests, and center admin accounts across the platform.                        |
| **Center Admin** | Manages a single assigned diagnostic center — configures test offerings, prices, and oversees staff and appointments for that center. |
| **Center Staff** | Operates under a Center Admin; can view and act on (approve/reject) appointments for their assigned center.                           |
| **Patient**      | End user; can browse centers and tests, book appointments, and track their own appointment history.                                   |

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8+

---

## 1 — Database Setup

1. Start your MySQL server.

2. Create the database (the app can also do this automatically on first run via `createDatabaseIfNotExist=true`):

   ```sql
   CREATE DATABASE healthcare_appointment_db;
   ```

3. The default connection expects:
   - **Host:** `localhost:3306`
   - **Database:** `healthcare_appointment_db`
   - **Username:** `root`
   - **Password:** `admin`

   To use different credentials, set environment variables before running:

   ```bash
   export DB_USERNAME=your_username
   export DB_PASSWORD=your_password
   ```

> **Schema & seed data** are created automatically on first startup via Hibernate (`ddl-auto: update`) and the built-in `DataSeeder`. No manual SQL scripts need to be run.

---

## 2 — Running the API (Spring Boot)

From the project root:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API starts on **http://localhost:8080**.

The database is seeded automatically on the **first** startup (skipped on subsequent runs). Seed output is logged to the console.

---

## 3 — Running the Frontend (React + Vite)

```bash
cd web
npm install
npm run dev
```

The app is available at **http://localhost:5173**.

The frontend proxies all `/api` calls to `http://localhost:8080` automatically — no extra configuration needed.

---

## 4 — Demo Credentials

The seeder creates these ready-to-use accounts. They are also shown as quick-fill buttons on the login page.

| Role         | Email                              | Password       |
| ------------ | ---------------------------------- | -------------- |
| System Admin | `admin@healthcare.com`             | `Admin@1234`   |
| Center Admin | `admin.healthfirst@healthcare.ph`  | `admin@1234`   |
| Center Staff | `staff1.healthfirst@healthcare.ph` | `staff@1234`   |
| Patient      | `juan.santos0@example.com`         | `Patient@1234` |

---

## 5 — Try-It-Out Workflows

### Book an appointment (Patient)

1. Log in as **Patient**.
2. Go to **Centers** → click any center → browse its available tests.
3. Click **Book Appointment**, select a date and one or more tests, and submit.
4. Go to **Appointments** to see the new booking in `PENDING` status.

### Approve an appointment (Center Staff / Center Admin)

1. Log in as **Center Staff** or **Center Admin**.
2. Go to **Appointments** — the pending booking from the patient appears here.
3. Click **Approve** (or open the appointment and approve from the detail view).
4. Log back in as the **Patient** — the appointment status is now `APPROVED`.

### Manage tests at a center (Center Admin)

1. Log in as **Center Admin**.
2. Go to **Centers** → select your assigned center.
3. Add or remove tests and update their prices.

### Add a staff member (Center Admin)

1. Log in as **Center Admin**.
2. Go to **Staff** in the navigation.
3. Click **Add Staff Admin**, fill in the details, and submit.
4. The new staff member can now log in and manage appointments for the same center.

### Manage the platform (System Admin)

1. Log in as **System Admin**.
2. From the **Dashboard**, review platform-wide appointment statistics.
3. Go to **Centers** to create, edit, or deactivate diagnostic centers.
4. Go to **Tests** to create or manage the global test catalog.
5. Go to **Center Admins** to assign or remove center admin accounts.
