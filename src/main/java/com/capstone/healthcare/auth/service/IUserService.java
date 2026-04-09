package com.capstone.healthcare.auth.service;

import com.capstone.healthcare.auth.model.UserAccount;

public interface IUserService {

    UserAccount validateUser(String username, String password);

    UserAccount findByEmail(String email);

    UserAccount addUser(UserAccount user);

    UserAccount removeUser(UserAccount user);
}
