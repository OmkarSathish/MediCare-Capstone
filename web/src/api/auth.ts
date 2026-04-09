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

  refreshToken: (refreshToken: string) =>
    api.post<ApiResponse<AuthResponse>>("/auth/token/refresh", {
      refreshToken,
    }),
};
