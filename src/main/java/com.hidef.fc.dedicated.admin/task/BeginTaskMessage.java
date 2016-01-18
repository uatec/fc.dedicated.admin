package com.hidef.fc.dedicated.admin.task;

import com.hidef.fc.dedicated.admin.task.Task;

public class BeginTaskMessage {
    private Task task;

    BeginTaskMessage() {}
    BeginTaskMessage(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }
}
