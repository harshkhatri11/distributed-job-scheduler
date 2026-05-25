package com.harshkhatri.worker.executor;

import com.harshkhatri.worker.model.JobTriggerPayload;

public interface JobExecutor {

    String execute(JobTriggerPayload payload) throws Exception;

    com.harshkhatri.worker.enums.JobType supports();
}
