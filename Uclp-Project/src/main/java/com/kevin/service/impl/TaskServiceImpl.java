package com.kevin.service.impl;

import com.kevin.Utils.FileUtil;
import com.kevin.mapper.TaskMapper;
import com.kevin.pojo.Task;
import com.kevin.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;



@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    @Override
    @Transactional
    public void publishTask(Task task,MultipartFile detailFile, Integer userId) {
        if (task.getCategoryId() == null || task.getDetail() == null || task.getReward() == null) {
            throw new IllegalArgumentException("类型、内容、奖赏不能为空");
        }
        if (task.getDeadline() == null || task.getDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("截止日期无效");
        }
        String filePath = null;
        if (detailFile != null && !detailFile.isEmpty()) {
            try {
                filePath = FileUtil.uploadFile(detailFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        task.setPublisherId(userId);
        task.setStatus(0);
        task.setAcceptorId(null);
        task.setProof(null);
        task.setDetailFile(filePath);
        task.setCreatetime(LocalDateTime.now());
        task.setUpdatetime(LocalDateTime.now());
        if (taskMapper.publish(task) == 0) {
            throw new RuntimeException("发布失败");
        }
        log.info("用户 {} 发布任务 {}", userId, task.getId());
    }

    @Override
    @Transactional
    public void updateTask(Task task, MultipartFile detailFile, Integer userId) {
        Integer taskId = task.getId();
        if (taskId == null) {
            throw new IllegalArgumentException("任务id不能为空");
        }

        Task oldTask = taskMapper.selectById(taskId);
        if (oldTask == null) {
            throw new RuntimeException("任务不存在");
        }

        if (!oldTask.getPublisherId().equals(userId)) {
            throw new RuntimeException("无权修改，非任务发布者");
        }
        if (oldTask.getStatus() != 0) {
            throw new RuntimeException("任务正在进行中或已完成，不可修改");
        }

        // 处理文件路径
        if (detailFile != null && !detailFile.isEmpty()){
            String filePath = "D:/uploads/" + detailFile.getOriginalFilename();
            try {
                // 将文件保存到本地
                detailFile.transferTo(new File(filePath));
                // 更新数据库中的文件路径
                oldTask.setDetailFile(filePath);
            } catch (IOException e) {
                throw new RuntimeException("文件上传失败", e);
            }
        }

            if (task.getCategoryId() != null) {
                oldTask.setCategoryId(task.getCategoryId());
            }
            if (task.getDetail() != null) {
                oldTask.setDetail(task.getDetail());
            }
            if (task.getReward() != null) {
                oldTask.setReward(task.getReward());
            }
            if (task.getDeadline() != null) {
                oldTask.setDeadline(task.getDeadline());
            }
            oldTask.setUpdatetime(LocalDateTime.now());

            if (taskMapper.updateById(oldTask) == 0) {
                throw new RuntimeException("修改失败");
            }
            log.info("用户 {} 修改任务 {}", userId, task.getId());
        }


    @Override
    @Transactional
    public void cancelTask(Integer id, Integer userId, Integer role) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        //权限判断
        if (role == 1) {
            log.warn("管理员id:{} 强制删除了任务 id:{} ", userId,id);
        } else {
            if (!task.getPublisherId().equals(userId)) {
                throw new RuntimeException("无权取消");
            }
            if (task.getStatus() != 0) {
                throw new RuntimeException("该任务不可线上取消，若想取消可联系接单者，已完成则无法取消");
           }
       }
        int rows = taskMapper.deleteById(id);
        if (rows != 1) {
            throw new RuntimeException("删除操作失败：未找到该任务或已被删除");
        }
        log.info("任务 ID:{} 被 {} 取消", id, (role == 1 ? "管理员" : "用户"));
    }


    @Override
    public List<Task> getMyPublishedTasks(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return taskMapper.selectMyPublishedListById(userId);
    }

    @Override
    public List<Task> getPublicTasks(Integer categoryId) {
        final Integer STATUS_PENDING = 0;
        return taskMapper.selectPublicTasks(categoryId, STATUS_PENDING);
    }


    @Override
    @Transactional
    public void acceptTask(Integer id, Integer userId) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (task.getStatus() != 0) {
            throw new RuntimeException("无法接单");
        }
        //防止并发下多人同时接单
        if (task.getAcceptorId() != null) {
            throw new RuntimeException("该任务已被其他人抢先接单");
        }
        Task updateParam = new Task();
        updateParam.setId(id);
        updateParam.setAcceptorId(userId);
        updateParam.setStatus(1);
        int rows = taskMapper.accept(updateParam);
        if (rows == 0) {
            // 这里的 0 意味着 WHERE 条件没匹配上
            // 可能是：1. 任务已被别人抢了 (status!=0)  2. 任务已经被人接了 (acceptor_id IS NOT NULL)
            throw new RuntimeException("接单失败");
        }
        log.info("用户 {} 成功接单任务 {}", userId, id);
    }


    @Override
    @Transactional
    public void cancelAccept(Integer id, Integer userId) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (task.getStatus() != 1) {
            throw new RuntimeException("任务当前状态无法取消，请刷新后重试");
        }
        if (task.getAcceptorId() == null || !task.getAcceptorId().equals(userId)) {
            throw new RuntimeException("非法操作，您不是该任务的接单人");
        }
        Task updateParam = new Task();
        updateParam.setId(id);
        updateParam.setAcceptorId(null); // 清空接单人
        updateParam.setStatus(0);         // 状态变更为待接单
        int rows = taskMapper.cancelAccept(updateParam);
        if (rows == 0) {
            throw new RuntimeException("数据已被修改，请刷新页面重试");
        }
        log.info("用户 {} 取消了任务 {} 的接单", userId, id);
    }

    @Override
    @Transactional
    public void completeTask(Integer id, Integer userId, MultipartFile proof) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (task.getStatus() != 1) {
            throw new RuntimeException("无法提交完成");
        }
        if (task.getAcceptorId() == null || !task.getAcceptorId().equals(userId)) {
            throw new RuntimeException("无权提交完成");
        }
        //处理凭证文件上传
        String proofPath = null;
        if (proof != null && !proof.isEmpty()) {
            try {
                proofPath = FileUtil.uploadFile(proof);
            } catch (IOException e) {
                throw new RuntimeException("文件上传失败");
            }
        }
        Task updateParam = new Task();
        updateParam.setId(id);
        updateParam.setStatus(2);
        updateParam.setProof(proofPath);
        int rows = taskMapper.completeTask(updateParam,userId);
        if (rows == 0) {
            throw new RuntimeException("任务状态异常或已被修改");
        }
        log.info("用户 {} 完成了任务 {} 的接单", userId, id);
    }


    @Override
    public List<Task> getMyAcceptedTasks(Integer userId) {
        return taskMapper.selectByAcceptorId(userId);
    }

}

