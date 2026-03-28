package com.kevin.mapper;


import com.kevin.pojo.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper{

    //1.注册
    int insert(User user);

    //2. 根据学号查询
    User findByNumber(@Param("number") String number);

    // 3. 根据ID查询
    User findById(Integer id);

    //4.修改信息
    int updateProfile(User user);

    //5.用户注销
    int deleteById(Integer id);

    //6.用户封禁
    int updateStatus(@Param("number") String number, @Param("status") Integer status);
}

