package com.kevin.service;

import com.kevin.pojo.User;

public interface UserService {
    User register(User user);

    User login(String number, String password);

    boolean updateProfile(User user);

    boolean manageStatus(String number, Integer status, Integer id);

    boolean delete(String password, User currentUser);
}
