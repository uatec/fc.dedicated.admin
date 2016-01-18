package com.hidef.fc.dedicated.admin.task;

import com.hidef.fc.dedicated.admin.task.Task;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TaskRepository extends PagingAndSortingRepository<Task, String> {

}
