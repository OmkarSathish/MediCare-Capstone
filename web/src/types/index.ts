// ─── Auth ────────────────────────────────────────────────────────────────────

export interface SignupRequest {
  fullName: string;
  email: string;
  phone?: string;
  password: string;
  role: "CUSTOMER" | "ADMIN";
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface UserProfileResponse {
  userId: number;
  fullName: string;
  email: string;
  phone?: string;
  roles: string[];
  centerId?: number | null;
}

// ─── Patient ─────────────────────────────────────────────────────────────────

export interface PatientProfileRequest {
  name: string;
  phoneNo?: string;
  age: number;
  gender?: string;
}

export interface PatientProfileResponse {
  patientId: number;
  name: string;
  phoneNo?: string;
  age: number;
  gender?: string;
  username: string;
}

export interface TestResultResponse {
  id: number;
  testReading: string;
  medicalCondition: string;
  appointmentId: number;
}

// ─── Appointment ──────────────────────────────────────────────────────────────

export type ApprovalStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";

export interface CreateAppointmentRequest {
  centerId: number;
  testIds: number[];
  appointmentDate: string; // ISO date string
  specialRequests?: string;
}

export interface AppointmentResponse {
  id: number;
  appointmentDate: string;
  approvalStatus: ApprovalStatus;
  patientName: string;
  centerName: string;
  remarks?: string;
  specialRequests?: string;
}

export interface AppointmentDetailResponse {
  id: number;
  appointmentDate: string;
  approvalStatus: ApprovalStatus;
  remarks?: string;
  specialRequests?: string;
  patient: PatientProfileResponse;
  center: { id: number; name: string; address: string };
  diagnosticTests: { id: number; testName: string; testPrice: number }[];
  statusHistory: StatusHistoryEntry[];
}

export interface StatusHistoryEntry {
  previousStatus?: ApprovalStatus;
  newStatus: ApprovalStatus;
  changedBy: string;
  changedAt: string;
  comments?: string;
}

export interface AppointmentStatusResponse {
  appointmentId: number;
  currentStatus: ApprovalStatus;
  history: StatusHistoryEntry[];
}

// ─── Diagnostic Center ────────────────────────────────────────────────────────

export interface CenterSearchResponse {
  id: number;
  name: string;
  address: string;
}

export interface CenterResponse {
  id: number;
  name: string;
  contactNo?: string;
  address: string;
  contactEmail?: string;
  status: string;
  servicesOffered: string[];
  tests: { id: number; testName: string; testPrice: number }[];
}

export interface CenterTestOfferingResponse {
  centerId: number;
  testId: number;
  testName: string;
  testPrice: number;
}

// ─── Diagnostic Tests ─────────────────────────────────────────────────────────

export interface TestResponse {
  id: number;
  testName: string;
  testPrice: number;
  status: string;
  categoryName?: string;
}

export interface TestSearchResponse {
  id: number;
  testName: string;
  testPrice: number;
}

// ─── Admin ────────────────────────────────────────────────────────────────────

export interface CenterAdminDashboardResponse {
  centerName: string;
  totalAppointments: number;
  pendingAppointments: number;
  approvedAppointments: number;
  rejectedAppointments: number;
  cancelledAppointments: number;
  assignedTests: number;
  appointmentsByMonth: Record<string, number>;
  topTests: Record<string, number>;
}

export interface AdminDashboardResponse {
  totalCenters: number;
  totalTests: number;
  totalPatients: number;
  totalAppointments: number;
  totalCenterAdmins: number;
  pendingAppointments: number;
  approvedAppointments: number;
  rejectedAppointments: number;
  cancelledAppointments: number;
  appointmentsByCenter: Record<string, number>;
  appointmentsByMonth: Record<string, number>;
  topTests: Record<string, number>;
}

// ─── Pricing ──────────────────────────────────────────────────────────────────

export interface TestPriceEntry {
  centerId: number;
  centerName: string;
  price: number;
}

export interface SuggestedPriceResponse {
  suggestedPrice: number;
  basis: "average" | "platform_floor";
}

// ─── API Wrapper ──────────────────────────────────────────────────────────────

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
}
