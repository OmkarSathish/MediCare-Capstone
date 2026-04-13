package com.capstone.healthcare.auth.service;

import com.capstone.healthcare.auth.model.UserAccount;

import java.util.List;

public interface IAdminService {

    void registerAdmin(String username, String password);

    void registerCenterAdmin(String email, String password, String fullName, int centerId);

    List<UserAccount> listCenterAdmins();

    void removeCenterAdmin(int userId);

    void registerCenterStaff(String email, String password, String fullName, int centerId);

    List<UserAccount> listStaffForCenter(int centerId);

    void removeCenterStaff(int userId, int requestingCenterId);
}
