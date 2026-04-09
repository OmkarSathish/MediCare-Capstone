import api from "./axios";
import type {
  ApiResponse,
  AppointmentDetailResponse,
  AppointmentResponse,
  AppointmentStatusResponse,
  CreateAppointmentRequest,
} from "../types";

export const appointmentApi = {
  book: (data: CreateAppointmentRequest) =>
    api.post<ApiResponse<AppointmentResponse>>("/appointments", data),

  list: (patient?: string) =>
    api.get<ApiResponse<AppointmentResponse[]>>("/appointments", {
      params: patient ? { patient } : undefined,
    }),

  getById: (id: number) =>
    api.get<ApiResponse<AppointmentDetailResponse>>(`/appointments/${id}`),

  getStatus: (id: number) =>
    api.get<ApiResponse<AppointmentStatusResponse>>(
      `/appointments/${id}/status`,
    ),

  cancel: (id: number) =>
    api.delete<ApiResponse<AppointmentResponse>>(`/appointments/${id}`),
};

export const adminAppointmentApi = {
  list: (params?: { centerId?: number; test?: string; status?: number }) =>
    api.get<ApiResponse<AppointmentResponse[]>>("/admin/appointments", {
      params,
    }),

  approve: (id: number, remarks?: string) =>
    api.put<ApiResponse<AppointmentResponse>>(
      `/admin/appointments/${id}/approve`,
      { remarks },
    ),

  reject: (id: number, remarks: string) =>
    api.put<ApiResponse<AppointmentResponse>>(
      `/admin/appointments/${id}/reject`,
      { remarks },
    ),
};
