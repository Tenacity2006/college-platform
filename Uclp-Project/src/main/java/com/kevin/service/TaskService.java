package com.kevin.service;

import com.kevin.pojo.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaskService {


    void publishTask(Task task, MultipartFile detailFile, Integer userId);

    void updateTask(Task task, MultipartFile detailFile, Integer userId);

    void cancelTask(Integer id, Integer userId, Integer role);

    List<Task> getMyPublishedTasks(Integer userId);

    List<Task> getPublicTasks(Integer categoryId);

    void acceptTask(Integer id, Integer userId);

    void cancelAccept(Integer id, Integer userId);

    void completeTask(Integer id, Integer userId, MultipartFile proof);

    List<Task> getMyAcceptedTasks(Integer userId);
}
