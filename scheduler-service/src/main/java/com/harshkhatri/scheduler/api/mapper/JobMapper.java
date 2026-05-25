package com.harshkhatri.scheduler.api.mapper;

import com.harshkhatri.scheduler.api.dto.request.CreateJobRequest;
import com.harshkhatri.scheduler.api.dto.response.JobResponse;
import com.harshkhatri.scheduler.entity.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JobMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "nextFireTime", ignore = true)
    @Mapping(target = "lastFireTime", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Job toEntity(CreateJobRequest request);

    JobResponse toResponse(Job job);
}