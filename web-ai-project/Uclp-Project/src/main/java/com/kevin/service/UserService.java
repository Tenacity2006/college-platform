package com.kevin.service;

import com.kevin.pojo.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    User register(User user);

    User login(String number, String password);

    boolean updateProfile(Integer userId, String name, String password, MultipartFile imageFile);

    boolean manageStatus(String number, Integer status, Integer id);

    boolean delete(String password, User currentUser);
}
