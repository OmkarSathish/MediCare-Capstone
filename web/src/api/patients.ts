import api from "./axios";
import type {
  ApiResponse,
  PatientProfileRequest,
  PatientProfileResponse,
} from "../types";

export const patientApi = {
  createProfile: (data: PatientProfileRequest) =>
    api.post<ApiResponse<PatientProfileResponse>>("/patients", data),

  getProfile: (username: string) =>
    api.get<ApiResponse<PatientProfileResponse>>(`/patients/${username}`),

  updateProfile: (username: string, data: PatientProfileRequest) =>
    api.put<ApiResponse<PatientProfileResponse>>(`/patients/${username}`, data),
};
