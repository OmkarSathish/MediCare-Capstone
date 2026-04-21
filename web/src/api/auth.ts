import api from "./axios";
import type {
  ApiResponse,
  AuthResponse,
  LoginRequest,
  SignupRequest,
  UserProfileResponse,
} from "../types";

export const authApi = {
  signup: (data: SignupRequest) =>
    api.post<ApiResponse<UserProfileResponse>>("/auth/signup", data),

  login: (data: LoginRequest) =>
    api.post<ApiResponse<AuthResponse>>("/auth/login", data),

  me: () => api.get<ApiResponse<UserProfileResponse>>("/auth/me"),

  logout: (refreshToken: string) =>
    api.post<ApiResponse<void>>("/auth/logout", { refreshToken }),

  listCenterAdmins: () =>
    api.get<ApiResponse<UserProfileResponse[]>>("/auth/admin/center-admins"),

  createCenterAdmin: (data: {
    fullName: string;
    email: string;
    password: string;
    centerId: number;
  }) => api.post<ApiResponse<void>>("/auth/admin/center-admins", data),

  removeCenterAdmin: (userId: number) =>
    api.delete<ApiResponse<void>>(`/auth/admin/center-admins/${userId}`),

  listCenterStaff: () =>
    api.get<ApiResponse<UserProfileResponse[]>>("/auth/center-admin/staff"),

  createCenterStaff: (data: {
    fullName: string;
    email: string;
    password: string;
  }) => api.post<ApiResponse<void>>("/auth/center-admin/staff", data),

  removeCenterStaff: (userId: number) =>
    api.delete<ApiResponse<void>>(`/auth/center-admin/staff/${userId}`),

  forgotPassword: (email: string) =>
    api.post<ApiResponse<void>>("/auth/forgot-password", { email }),

  resetPassword: (email: string, otp: string, newPassword: string) =>
    api.post<ApiResponse<void>>("/auth/reset-password", {
      email,
      otp,
      newPassword,
    }),
};
