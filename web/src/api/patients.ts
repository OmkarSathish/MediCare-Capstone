import api from "./axios";
import type {
  ApiResponse,
  PatientProfileRequest,
  PatientProfileResponse,
  TestResultResponse,
} from "../types";

export const patientApi = {
  createProfile: (data: PatientProfileRequest) =>
    api.post<ApiResponse<PatientProfileResponse>>("/patients", data),

  getProfile: (username: string) =>
    api.get<ApiResponse<PatientProfileResponse>>(`/patients/${username}`),

  updateProfile: (username: string, data: PatientProfileRequest) =>
    api.put<ApiResponse<PatientProfileResponse>>(`/patients/${username}`, data),

  getResults: (username: string) =>
    api.get<ApiResponse<TestResultResponse[]>>(`/patients/${username}/results`),

  getResult: (id: number) =>
    api.get<ApiResponse<TestResultResponse>>(`/patients/results/${id}`),
};
