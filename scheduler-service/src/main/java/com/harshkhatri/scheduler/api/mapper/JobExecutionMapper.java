package com.harshkhatri.scheduler.api.mapper;

import com.harshkhatri.scheduler.api.dto.response.JobExecutionResponse;
import com.harshkhatri.scheduler.entity.JobExecution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JobExecutionMapper {

    @Mapping(target = "jobId", source = "job.id")
    JobExecutionResponse toResponse(JobExecution execution);
}
