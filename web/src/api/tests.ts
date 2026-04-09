import api from "./axios";
import type { ApiResponse, TestResponse } from "../types";

export const testsApi = {
  list: (search?: string) =>
    api.get<ApiResponse<TestResponse[]>>("/tests", {
      params: search ? { search } : undefined,
    }),

  getById: (id: number) => api.get<ApiResponse<TestResponse>>(`/tests/${id}`),

  getByCategory: (categoryId: number) =>
    api.get<ApiResponse<TestResponse[]>>(`/tests/category/${categoryId}`),
};
