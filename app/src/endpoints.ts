import type { Endpoint } from "./types";

const setToken = (data: unknown) => {
  const token = (data as { data?: { accessToken?: string } })?.data
    ?.accessToken;
  const refresh = (data as { data?: { refreshToken?: string } })?.data
    ?.refreshToken;
  if (token) {
    window.dispatchEvent(new CustomEvent("token-received", { detail: token }));
  }
  if (refresh) {
    localStorage.setItem("refreshToken", refresh);
  }
};

const fillRefreshToken = (fieldId: string) => {
  setTimeout(() => {
    const el = document.getElementById(fieldId) as HTMLInputElement | null;
    if (el) el.value = localStorage.getItem("refreshToken") ?? "";
  }, 50);
};

export const ENDPOINTS: Record<string, Endpoint> = {
  // ── AUTH ────────────────────────────────────────────────────────────────
  "auth-signup": {
    title: "Register New Customer",
    desc: "Creates a new customer account. No auth required.",
    method: "POST",
    path: "/api/auth/signup",
    bodyFields: [
      { key: "fullName", label: "Full Name", placeholder: "Jane Doe" },
      { key: "email", label: "Email", placeholder: "jane@example.com" },
      { key: "phone", label: "Phone", placeholder: "555-1234" },
      {
        key: "password",
        label: "Password",
        placeholder: "••••••••",
        type: "password",
      },
      {
        key: "role",
        label: "Role",
        type: "select",
        options: ["CUSTOMER", "ADMIN"],
      },
    ],
  },
  "auth-login": {
    title: "Login",
    desc: "Authenticate and receive JWT tokens. Token is auto-saved on success.",
    method: "POST",
    path: "/api/auth/login",
    bodyFields: [
      {
        key: "username",
        label: "Email / Username",
        placeholder: "jane@example.com",
      },
      {
        key: "password",
        label: "Password",
        placeholder: "••••••••",
        type: "password",
      },
    ],
    onSuccess: setToken,
  },
  "auth-refresh": {
    title: "Refresh Access Token",
    method: "POST",
    path: "/api/auth/token/refresh",
    bodyFields: [
      {
        key: "refreshToken",
        label: "Refresh Token",
        placeholder: "auto-filled",
      },
    ],
    onOpen: () => fillRefreshToken("body-refreshToken"),
    onSuccess: setToken,
  },
  "auth-logout": {
    title: "Logout",
    method: "POST",
    path: "/api/auth/logout",
    bodyFields: [
      {
        key: "refreshToken",
        label: "Refresh Token",
        placeholder: "auto-filled",
      },
    ],
    onOpen: () => fillRefreshToken("body-refreshToken"),
  },
  "auth-me": {
    title: "Get Current User",
    method: "GET",
    path: "/api/auth/me",
    auth: true,
  },
  "auth-admin-register": {
    title: "Register Admin Account",
    method: "POST",
    path: "/api/auth/admin/register",
    auth: true,
    bodyFields: [
      { key: "fullName", label: "Full Name", placeholder: "Admin User" },
      { key: "email", label: "Email", placeholder: "admin@example.com" },
      {
        key: "password",
        label: "Password",
        placeholder: "••••••••",
        type: "password",
      },
      {
        key: "role",
        label: "Role",
        type: "select",
        options: ["ADMIN", "CUSTOMER"],
      },
    ],
  },
  "patients-create": {
    title: "Register Patient Profile",
    method: "POST",
    path: "/api/patients",
    auth: true,
    bodyFields: [
      { key: "name", label: "Name", placeholder: "Jane Doe" },
      { key: "phoneNo", label: "Phone", placeholder: "555-1234" },
      { key: "age", label: "Age", placeholder: "30", type: "number" },
      {
        key: "gender",
        label: "Gender",
        type: "select",
        options: ["MALE", "FEMALE", "OTHER"],
      },
    ],
  },
  "patients-get": {
    title: "Get Patient Profile",
    method: "GET",
    path: "/api/patients/{username}",
    auth: true,
    pathParams: [
      { key: "username", label: "Username", placeholder: "jane@example.com" },
    ],
  },
  "patients-update": {
    title: "Update Patient Profile",
    method: "PUT",
    path: "/api/patients/{username}",
    auth: true,
    pathParams: [
      { key: "username", label: "Username", placeholder: "jane@example.com" },
    ],
    bodyFields: [
      { key: "name", label: "Name", placeholder: "Jane Doe" },
      { key: "phoneNo", label: "Phone", placeholder: "555-1234" },
      { key: "age", label: "Age", placeholder: "30", type: "number" },
      {
        key: "gender",
        label: "Gender",
        type: "select",
        options: ["MALE", "FEMALE", "OTHER"],
      },
    ],
  },
  "patients-results": {
    title: "Get Patient Test Results",
    method: "GET",
    path: "/api/patients/{username}/results",
    auth: true,
    pathParams: [
      { key: "username", label: "Username", placeholder: "jane@example.com" },
    ],
  },
  "patients-result-by-id": {
    title: "Get Test Result by ID",
    method: "GET",
    path: "/api/patients/results/{id}",
    auth: true,
    pathParams: [
      { key: "id", label: "Result ID", placeholder: "1", type: "number" },
    ],
  },
  "patients-result-add": {
    title: "Add Test Result (Admin)",
    method: "POST",
    path: "/api/patients/results",
    auth: true,
    bodyFields: [
      {
        key: "appointmentId",
        label: "Appointment ID",
        placeholder: "1",
        type: "number",
      },
      { key: "testReading", label: "Test Reading", placeholder: "5.4 mmol/L" },
      {
        key: "medicalCondition",
        label: "Medical Condition",
        placeholder: "Normal",
      },
    ],
  },
  "patients-result-update": {
    title: "Update Test Result (Admin)",
    method: "PUT",
    path: "/api/patients/results/{id}",
    auth: true,
    pathParams: [
      { key: "id", label: "Result ID", placeholder: "1", type: "number" },
    ],
    bodyFields: [
      {
        key: "appointmentId",
        label: "Appointment ID",
        placeholder: "1",
        type: "number",
      },
      { key: "testReading", label: "Test Reading", placeholder: "5.4 mmol/L" },
      {
        key: "medicalCondition",
        label: "Medical Condition",
        placeholder: "Normal",
      },
    ],
  },
  "patients-result-delete": {
    title: "Delete Test Result (Admin)",
    method: "DELETE",
    path: "/api/patients/results/{id}",
    auth: true,
    pathParams: [
      { key: "id", label: "Result ID", placeholder: "1", type: "number" },
    ],
  },

  // ── APPOINTMENTS ──────────────────────────────────────────────────────────
  "appt-book": {
    title: "Book Appointment",
    method: "POST",
    path: "/api/appointments",
    auth: true,
    bodyFields: [
      { key: "centerId", label: "Center ID", placeholder: "1", type: "number" },
      {
        key: "testIds",
        label: "Test IDs (e.g. [1,2])",
        placeholder: "[1]",
        type: "textarea",
      },
      {
        key: "appointmentDate",
        label: "Appointment Date",
        placeholder: "2026-05-01",
      },
    ],
  },
  "appt-list": {
    title: "List Appointments",
    method: "GET",
    path: "/api/appointments",
    auth: true,
    queryParams: [
      {
        key: "patient",
        label: "Patient Name (optional)",
        placeholder: "Jane Doe",
      },
    ],
  },
  "appt-get": {
    title: "Get Appointment by ID",
    method: "GET",
    path: "/api/appointments/{id}",
    auth: true,
    pathParams: [
      { key: "id", label: "Appointment ID", placeholder: "1", type: "number" },
    ],
  },
  "appt-status": {
    title: "Get Appointment Status",
    method: "GET",
    path: "/api/appointments/{id}/status",
    auth: true,
    pathParams: [
      { key: "id", label: "Appointment ID", placeholder: "1", type: "number" },
    ],
  },
  "appt-cancel": {
    title: "Cancel Appointment",
    method: "DELETE",
    path: "/api/appointments/{id}",
    auth: true,
    pathParams: [
      { key: "id", label: "Appointment ID", placeholder: "1", type: "number" },
    ],
  },

  // ── ADMIN APPOINTMENTS ────────────────────────────────────────────────────
  "admin-appt-list": {
    title: "List Appointments (Admin)",
    method: "GET",
    path: "/api/admin/appointments",
    auth: true,
    queryParams: [
      { key: "centerId", label: "Center ID (0 = all)", placeholder: "0" },
      { key: "test", label: "Test Name (empty = all)", placeholder: "" },
      { key: "status", label: "Status (0 = all)", placeholder: "0" },
    ],
  },
  "admin-appt-approve": {
    title: "Approve Appointment (Admin)",
    method: "PUT",
    path: "/api/admin/appointments/{id}/approve",
    auth: true,
    pathParams: [
      { key: "id", label: "Appointment ID", placeholder: "1", type: "number" },
    ],
    bodyFields: [
      { key: "remarks", label: "Remarks", placeholder: "Approved." },
    ],
  },
  "admin-appt-reject": {
    title: "Reject Appointment (Admin)",
    method: "PUT",
    path: "/api/admin/appointments/{id}/reject",
    auth: true,
    pathParams: [
      { key: "id", label: "Appointment ID", placeholder: "1", type: "number" },
    ],
    bodyFields: [
      { key: "remarks", label: "Remarks", placeholder: "Slot unavailable." },
    ],
  },

  // ── ADMIN DASHBOARD ───────────────────────────────────────────────────────
  "admin-dashboard": {
    title: "Admin Dashboard",
    method: "GET",
    path: "/api/admin/dashboard",
    auth: true,
  },

  // ── CENTERS ───────────────────────────────────────────────────────────────
  "centers-list": {
    title: "List / Search Centers",
    method: "GET",
    path: "/api/centers",
    queryParams: [
      {
        key: "search",
        label: "Search keyword (optional)",
        placeholder: "downtown",
      },
    ],
  },
  "centers-get": {
    title: "Get Center by ID",
    method: "GET",
    path: "/api/centers/{id}",
    pathParams: [
      { key: "id", label: "Center ID", placeholder: "1", type: "number" },
    ],
  },
  "centers-tests": {
    title: "Get Tests at Center",
    method: "GET",
    path: "/api/centers/{id}/tests",
    pathParams: [
      { key: "id", label: "Center ID", placeholder: "1", type: "number" },
    ],
  },
  "centers-test-detail": {
    title: "Get Test Detail at Center",
    method: "GET",
    path: "/api/centers/{id}/tests/{testName}",
    pathParams: [
      { key: "id", label: "Center ID", placeholder: "1" },
      { key: "testName", label: "Test Name", placeholder: "Blood Glucose" },
    ],
  },
  "centers-appointments": {
    title: "Appointments at Center (Admin)",
    method: "GET",
    path: "/api/centers/{id}/appointments",
    auth: true,
    pathParams: [
      { key: "id", label: "Center ID", placeholder: "1", type: "number" },
    ],
  },
  "centers-by-test": {
    title: "Centers Offering Test",
    method: "GET",
    path: "/api/centers/offering/{testId}",
    pathParams: [
      { key: "testId", label: "Test ID", placeholder: "1", type: "number" },
    ],
  },
  "centers-create": {
    title: "Create Center (Admin)",
    method: "POST",
    path: "/api/centers",
    auth: true,
    bodyFields: [
      { key: "name", label: "Name", placeholder: "Downtown Diagnostics" },
      { key: "contactNo", label: "Contact Number", placeholder: "555-0001" },
      { key: "address", label: "Address", placeholder: "123 Main St" },
      {
        key: "contactEmail",
        label: "Contact Email",
        placeholder: "info@dd.com",
      },
      {
        key: "servicesOffered",
        label: "Services Offered (comma-separated)",
        placeholder: "Blood tests, X-Ray",
        type: "array",
      },
    ],
  },
  "centers-update": {
    title: "Update Center (Admin)",
    method: "PUT",
    path: "/api/centers/{id}",
    auth: true,
    pathParams: [
      { key: "id", label: "Center ID", placeholder: "1", type: "number" },
    ],
    bodyFields: [
      { key: "name", label: "Name", placeholder: "Downtown Diagnostics" },
      { key: "contactNo", label: "Contact Number", placeholder: "555-0001" },
      { key: "address", label: "Address", placeholder: "123 Main St" },
      {
        key: "contactEmail",
        label: "Contact Email",
        placeholder: "info@dd.com",
      },
      {
        key: "servicesOffered",
        label: "Services Offered (comma-separated)",
        placeholder: "Blood tests, X-Ray",
        type: "array",
      },
    ],
  },
  "centers-add-test": {
    title: "Add Test to Center (Admin)",
    method: "POST",
    path: "/api/centers/{id}/tests/{testId}",
    auth: true,
    pathParams: [
      { key: "id", label: "Center ID", placeholder: "1", type: "number" },
      { key: "testId", label: "Test ID", placeholder: "1", type: "number" },
    ],
  },
  "centers-delete": {
    title: "Delete Center (Admin)",
    method: "DELETE",
    path: "/api/centers/{id}",
    auth: true,
    pathParams: [
      { key: "id", label: "Center ID", placeholder: "1", type: "number" },
    ],
  },

  // ── TESTS ─────────────────────────────────────────────────────────────────
  "tests-list": {
    title: "List / Search Tests",
    method: "GET",
    path: "/api/tests",
    queryParams: [
      {
        key: "search",
        label: "Search keyword (optional)",
        placeholder: "glucose",
      },
    ],
  },
  "tests-get": {
    title: "Get Test by ID",
    method: "GET",
    path: "/api/tests/{id}",
    pathParams: [
      { key: "id", label: "Test ID", placeholder: "1", type: "number" },
    ],
  },
  "tests-by-category": {
    title: "Tests by Category",
    method: "GET",
    path: "/api/tests/category/{categoryId}",
    pathParams: [
      {
        key: "categoryId",
        label: "Category ID",
        placeholder: "1",
        type: "number",
      },
    ],
  },
  "tests-create": {
    title: "Create Test (Admin)",
    method: "POST",
    path: "/api/tests",
    auth: true,
    bodyFields: [
      { key: "testName", label: "Test Name", placeholder: "Blood Glucose" },
      {
        key: "testPrice",
        label: "Price",
        placeholder: "49.99",
        type: "number",
      },
      { key: "normalValue", label: "Normal Value", placeholder: "70–99 mg/dL" },
      { key: "units", label: "Units", placeholder: "mg/dL" },
      {
        key: "categoryId",
        label: "Category ID",
        placeholder: "1",
        type: "number",
      },
    ],
  },
  "tests-update": {
    title: "Update Test (Admin)",
    method: "PUT",
    path: "/api/tests/{id}",
    auth: true,
    pathParams: [
      { key: "id", label: "Test ID", placeholder: "1", type: "number" },
    ],
    bodyFields: [
      { key: "testName", label: "Test Name", placeholder: "Blood Glucose" },
      {
        key: "testPrice",
        label: "Price",
        placeholder: "49.99",
        type: "number",
      },
      { key: "normalValue", label: "Normal Value", placeholder: "70–99 mg/dL" },
      { key: "units", label: "Units", placeholder: "mg/dL" },
    ],
  },
  "tests-delete": {
    title: "Delete Test (Admin)",
    method: "DELETE",
    path: "/api/tests/{id}",
    auth: true,
    pathParams: [
      { key: "id", label: "Test ID", placeholder: "1", type: "number" },
    ],
  },
};

export const GROUPS = [
  {
    label: "Auth",
    keys: [
      "auth-signup",
      "auth-login",
      "auth-refresh",
      "auth-logout",
      "auth-me",
      "auth-admin-register",
    ],
  },
  {
    label: "Patients",
    keys: [
      "patients-create",
      "patients-get",
      "patients-update",
      "patients-results",
      "patients-result-by-id",
      "patients-result-add",
      "patients-result-update",
      "patients-result-delete",
    ],
  },
  {
    label: "Appointments",
    keys: ["appt-book", "appt-list", "appt-get", "appt-status", "appt-cancel"],
  },
  {
    label: "Admin — Appointments",
    keys: ["admin-appt-list", "admin-appt-approve", "admin-appt-reject"],
  },
  {
    label: "Admin — Dashboard",
    keys: ["admin-dashboard"],
  },
  {
    label: "Diagnostic Centers",
    keys: [
      "centers-list",
      "centers-get",
      "centers-tests",
      "centers-test-detail",
      "centers-appointments",
      "centers-by-test",
      "centers-create",
      "centers-update",
      "centers-add-test",
      "centers-delete",
    ],
  },
  {
    label: "Diagnostic Tests",
    keys: [
      "tests-list",
      "tests-get",
      "tests-by-category",
      "tests-create",
      "tests-update",
      "tests-delete",
    ],
  },
];
