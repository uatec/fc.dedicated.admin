package com.hidef.fc.dedicated.admin.task;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

enum Action {
    Create,
    Start,
    Stop,
    Delete
}

enum TaskStatus {
    Queued,
    Running,
    Succeeded,
    Faulted
}

@Data
@Entity
public class Task {

    private @Id
    @GeneratedValue
    String id;
    private String serverId;
    private Action action;
    private TaskStatus taskStatus = TaskStatus.Queued;
    private String userEmail;
    private String message;

    public Task(){}
    public Task(String id, String serverId, Action action, TaskStatus taskStatus, String userEmail, String message) {
        this.id = id;
        this.serverId = serverId;
        this.action = action;
        this.taskStatus = taskStatus;
        this.userEmail = userEmail;
        this.message = message;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
