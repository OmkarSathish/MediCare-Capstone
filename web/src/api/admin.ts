import api from "./axios";
import type {
  ApiResponse,
  AdminDashboardResponse,
  CenterAdminDashboardResponse,
} from "../types";

export const adminApi = {
  getDashboard: () =>
    api.get<ApiResponse<AdminDashboardResponse>>("/admin/dashboard"),
  getCenterDashboard: () =>
    api.get<ApiResponse<CenterAdminDashboardResponse>>(
      "/admin/dashboard/center",
    ),
};
