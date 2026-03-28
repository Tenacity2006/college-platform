package com.kevin.service.impl;

import com.kevin.Utils.FileUtil;
import com.kevin.mapper.UserMapper;
import com.kevin.pojo.User;
import com.kevin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserMapper userMapper;


    //注册
    @Override
    public User register(User user) {
        //检验
        if (user.getNumber() == null || user.getPassword() == null) {
            throw new RuntimeException("账号和密码不能为空");
        }
        //查重
        if (userMapper.findByNumber(user.getNumber()) != null) {
            throw new RuntimeException("该学号已存在");
        }
       //设置默认值
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName("用户" + user.getNumber());
        }
        user.setRole(0);
        user.setStatus(0);
        //注册用户
        if (userMapper.insert(user) == 0) {
            throw new RuntimeException("注册失败");
        }
        user.setPassword(null);
        return user;
    }

    //登录
    @Override
    public User login(String userNumber, String password) {
        //查询信息并检验
        User user = userMapper.findByNumber(userNumber);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
       //查重
        if (user.getStatus() == 1) {
            throw new RuntimeException("账号已被封禁");
        }
       //防止泄露
        user.setPassword(null);
        return user;
    }

    //修改个人信息
    @Override
    @Transactional
    public boolean updateProfile(Integer userId, String name, String password, MultipartFile imageFile) {
        if (userId == null) {
            throw new RuntimeException("用户ID缺失");
        }
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageUrl = FileUtil.uploadFile(imageFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setName(name);
        updateUser.setPassword(password);
        updateUser.setImage(imageUrl);

        int rows = userMapper.updateProfile(updateUser);
        if (rows == 0) {
            throw new RuntimeException("修改失败");
        }
        return true;
    }

        //封禁
    @Override
    @Transactional
    public boolean manageStatus(String number, Integer status, Integer id) {
        //检验
        if (number == null || status == null) {
            throw new RuntimeException("参数不能为空");
        }
       //封禁用户
        int rows = userMapper.updateStatus(number, status);
        if (rows == 0) {
            throw new RuntimeException("封禁失败");
        }
        return true;
    }

    //注销
    @Override
    @Transactional
    public boolean delete(String password, User currentUser) {
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        Integer userId = currentUser.getId();
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        String dbPassword = user.getPassword();

         if (dbPassword == null || !dbPassword.equals(password)) {
             throw new RuntimeException("密码错误");
         }

        int rows = userMapper.deleteById(userId);
        if (rows == 0) {
            throw new RuntimeException("注销失败");
        }
        return true;
    }
}

