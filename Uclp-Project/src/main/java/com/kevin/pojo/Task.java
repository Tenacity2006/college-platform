package com.kevin.pojo;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private Integer id;
    private Integer publisherId;
    private Integer categoryId;
    private String detail;
    private String detailFile;
    private Double reward;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deadline;
    private Integer status;
    private Integer acceptorId;
    private String proof;
    private LocalDateTime createtime;
    private LocalDateTime updatetime;
}
