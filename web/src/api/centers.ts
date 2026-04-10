import api from "./axios";
import type {
  ApiResponse,
  CenterResponse,
  CenterSearchResponse,
  CenterTestOfferingResponse,
} from "../types";

export const centersApi = {
  list: (search?: string) =>
    api.get<ApiResponse<CenterSearchResponse[]>>("/centers", {
      params: search ? { search } : undefined,
    }),

  getById: (id: number) =>
    api.get<ApiResponse<CenterResponse>>(`/centers/${id}`),

  getTests: (id: number) =>
    api.get<ApiResponse<CenterTestOfferingResponse[]>>(`/centers/${id}/tests`),

  getByTestId: (testId: number) =>
    api.get<ApiResponse<CenterSearchResponse[]>>(`/centers/offering/${testId}`),

  create: (data: {
    name: string;
    address: string;
    contactNo?: string;
    contactEmail?: string;
    servicesOffered?: string[];
  }) => api.post<ApiResponse<CenterResponse>>("/centers", data),

  update: (
    id: number,
    data: {
      name: string;
      address: string;
      contactNo?: string;
      contactEmail?: string;
      servicesOffered?: string[];
    },
  ) => api.put<ApiResponse<CenterResponse>>(`/centers/${id}`, data),

  addTest: (centerId: number, testId: number) =>
    api.post<ApiResponse<CenterTestOfferingResponse>>(
      `/centers/${centerId}/tests/${testId}`,
    ),

  removeTest: (centerId: number, testId: number) =>
    api.delete<ApiResponse<void>>(`/centers/${centerId}/tests/${testId}`),

  delete: (id: number) =>
    api.delete<ApiResponse<CenterResponse>>(`/centers/${id}`),
};
