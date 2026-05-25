package com.harshkhatri.scheduler.api.mapper;

import com.harshkhatri.scheduler.api.dto.response.DeadLetterEventResponse;
import com.harshkhatri.scheduler.entity.DeadLetterEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeadLetterEventMapper {

    DeadLetterEventResponse toResponse(DeadLetterEvent event);
}