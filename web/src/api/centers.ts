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
};
