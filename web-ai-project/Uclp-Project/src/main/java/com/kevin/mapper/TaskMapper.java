package com.kevin.mapper;


import com.kevin.pojo.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;

@Mapper
public interface TaskMapper {
    int publish(Task task);

    Task selectById(@Param("id") Integer id);

    int updateById(Task task);

    int deleteById(@Param("id") Integer id);

    List<Task> selectMyPublishedListById(@Param("publisherId") Integer publisherId);

    List<Task> selectPublicTasks(@Param("categoryId") Integer categoryId, @Param("status") Integer status);

    int accept(Task task);

    int completeTask(@Param("task") Task task, @Param("acceptorId") Integer acceptorId);

    List<Task> selectByAcceptorId(@Param("acceptorId") Integer acceptorId);

    int cancelAccept(Task task);
}
