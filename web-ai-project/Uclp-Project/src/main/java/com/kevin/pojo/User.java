package com.kevin.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String number;
    private String password;
    private String name;
    private String image;
    private Integer role;
    private Integer status;
    private LocalDateTime createTime;
}
