package com.harshkhatri.worker.service;

import com.harshkhatri.worker.model.JobTriggerPayload;

public interface JobExecutionHandler {
    void handle(JobTriggerPayload payload);
}
