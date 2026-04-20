package com.capstone.healthcare.admin.service;

import com.capstone.healthcare.admin.dto.CenterAdminDashboardResponse;

public interface ICenterAdminDashboardService {

    CenterAdminDashboardResponse getDashboardForCenter(int centerId);
}
