package com.kevin.controller;


import com.kevin.Utils.FileUtil;
import com.kevin.Utils.JWTUtil;
import com.kevin.pojo.Result;
import com.kevin.pojo.User;
import com.kevin.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    //1.注册
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        try {
            User newUser = userService.register(user);
            return Result.success(newUser); // 返回新用户信息
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    //2.登录
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        try {
            //1.获取用户
            User loginUser = userService.login(user.getNumber(), user.getPassword());
            //2.生成 Token
            String token = JWTUtil.generateToken(loginUser.getId(), loginUser.getRole(),loginUser.getStatus());
            //3.组装返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", loginUser);
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    //3.修改个人资料
    @PutMapping("/update")
    public Result updateProfile(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            HttpServletRequest request
    ) {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("未登录");
            }

            try {
                boolean success = userService.updateProfile(currentUser.getId(), name, password, imageFile);
                if (success) {
                    return Result.success("修改成功");
                } else {
                    return Result.error("修改失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(e.getMessage());
            }

    }

    //4.封禁用户 (管理员专用)
    @PutMapping("/ban")
    public Result manageStatus(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            User admin = (User) request.getAttribute("currentUser");
            if (admin == null || admin.getRole() != 1) {
                return Result.error("权限不足");
            }
            String number = (String) params.get("number");
            Integer status = (Integer) params.get("status");
            if (number == null || status == null) {
                return Result.error("身份信息错误");
            }
            userService.manageStatus(number, status, admin.getId());
            return Result.success("操作成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    //5.用户注销
    @DeleteMapping("/delete")
    public Result deleteUser(@RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            String password = params.get("password");
            if (password == null || password.isEmpty()) {
                return Result.error("密码不能为空");
            }
            userService.delete(password, currentUser);
            return Result.success("账号注销成功，感谢你的使用！");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}




