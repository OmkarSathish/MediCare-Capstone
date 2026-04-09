import api from "./axios";
import type { ApiResponse, AdminDashboardResponse } from "../types";

export const adminApi = {
  getDashboard: () =>
    api.get<ApiResponse<AdminDashboardResponse>>("/admin/dashboard"),
};
