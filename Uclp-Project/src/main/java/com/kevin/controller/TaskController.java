package com.kevin.controller;


import com.kevin.Utils.JWTUtil;
import com.kevin.pojo.Result;
import com.kevin.pojo.Task;
import com.kevin.pojo.User;
import com.kevin.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Slf4j
@RestController
public class TaskController {

    @Autowired
    private TaskService taskService;



    //发布
    @PostMapping("/publishtasks")
    public Result publish(@ModelAttribute Task task,
                          @RequestParam(value = "detailFile", required = false) MultipartFile detailFile,
                          HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            taskService.publishTask(task, detailFile, currentUser.getId());
            return Result.success("发布成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


    //修改任务
    @PutMapping("/updatetasks/{id}")
    public Result update(@PathVariable Integer id,
                         @ModelAttribute Task task,
                         @RequestParam(value = "detailFile", required = false) MultipartFile detailFile,
                         HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            taskService.updateTask(task, detailFile,currentUser.getId());
            return Result.success("任务修改成功");
        } catch (Exception e) {
            log.error("修改任务失败", e);
            return Result.error(e.getMessage());
        }
    }


    //取消
    @DeleteMapping("/deletetasks/{id}")
    public Result cancel(@PathVariable Integer id, HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录或登录信息已过期");
            }
            Integer userId = currentUser.getId();
            Integer role = currentUser.getRole();
            taskService.cancelTask(id, userId, role);
            String operator = (role == 1) ? "管理员" : "用户";
            return Result.success("任务已成功取消 (" + operator + "操作)");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("系统繁忙，请稍后再试");
        }
    }

    //查询我发布的订单
    @GetMapping("/mypublishedtasks")
    public Result getMyTasks(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            Integer userId = currentUser.getId();
            List<Task> list = taskService.getMyPublishedTasks(userId);
            return Result.success(list);
        } catch (Exception e) {
            log.error("查询我发布的订单失败: {}", e.getMessage(), e);
            return Result.error("系统繁忙，请稍后再试");
        }
    }

    //查询可接的全部订单
    @GetMapping("/publiclist")
    public Result getPublicTasks(@RequestParam(required = false) Integer categoryId) {
        try {
            List<Task> list = taskService.getPublicTasks(categoryId);
            return Result.success(list);
        } catch (Exception e) {
            log.error("查询可接的全部订单失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    //接单
    @PutMapping("/accepttasks/{id}")
    public Result acceptTask(@PathVariable Integer id, HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户信息丢失，请重新登录");
            }
            Integer userId = currentUser.getId();
            taskService.acceptTask(id, userId);
            return Result.success("接单成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("系统繁忙，请稍后再试");
        }
    }

    //取消接单
    @PutMapping("/cancelaccept/{id}")
    public Result cancelAccept(@PathVariable Integer id, HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录，请重新登录");
            }
            Integer userId = currentUser.getId();
            taskService.cancelAccept(id, userId);
            return Result.success("取消接单成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("系统繁忙，请稍后再试");
        }
    }


    //完成任务
    @PutMapping("/completetasks/{id}")
    public Result completeTask(@PathVariable Integer id,
                               @RequestParam(value = "file", required = false) MultipartFile proof,
                               HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            Integer currentUserId = currentUser.getId();
            taskService.completeTask(id, currentUserId, proof);
            return Result.success("任务已完成，凭证已提交");
        } catch (Exception e) {
            log.error("完成任务失败", e);
            return Result.error(e.getMessage());
        }
    }

    //查询我的接单
    @GetMapping("/myacceptedtasks")
    public Result getMyAcceptedTasks(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录，请重新登录");
            }
            List<Task> taskList = taskService.getMyAcceptedTasks(currentUser.getId());
            return Result.success(taskList);
        } catch (Exception e) {
            log.error("查询接单任务失败：{}", e.getMessage(), e);
            return Result.error("系统繁忙，请稍后再试");
        }
    }
}



