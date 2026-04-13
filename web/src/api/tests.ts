import api from "./axios";
import type { ApiResponse, TestPriceEntry, TestResponse } from "../types";

export const testsApi = {
  list: (search?: string) =>
    api.get<ApiResponse<TestResponse[]>>("/tests", {
      params: search ? { search } : undefined,
    }),

  getById: (id: number) => api.get<ApiResponse<TestResponse>>(`/tests/${id}`),

  getByCategory: (categoryId: number) =>
    api.get<ApiResponse<TestResponse[]>>(`/tests/category/${categoryId}`),

  create: (data: {
    testName: string;
    testPrice: number;
    normalValue?: string;
    units?: string;
    categoryId?: number;
  }) => api.post<ApiResponse<TestResponse>>("/tests", data),

  update: (
    id: number,
    data: {
      testName: string;
      testPrice: number;
      normalValue?: string;
      units?: string;
    },
  ) => api.put<ApiResponse<TestResponse>>(`/tests/${id}`, data),

  delete: (id: number) => api.delete<ApiResponse<TestResponse>>(`/tests/${id}`),

  getPrices: (testId: number) =>
    api.get<ApiResponse<TestPriceEntry[]>>(`/tests/${testId}/prices`),
};
