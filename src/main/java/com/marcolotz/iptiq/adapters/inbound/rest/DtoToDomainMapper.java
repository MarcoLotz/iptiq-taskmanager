package com.marcolotz.iptiq.adapters.inbound.rest;

import com.marcolotz.iptiq.core.SortingMethod;
import com.marcolotz.iptiq.core.model.Priority;
import com.marcolotz.iptiq.core.model.Process;
import com.marcolotz.taskmanager.openapi.model.AddedProcessDTO;
import com.marcolotz.taskmanager.openapi.model.PriorityTypes;
import com.marcolotz.taskmanager.openapi.model.RunningProcessDTO;
import com.marcolotz.taskmanager.openapi.model.SortingMethodDTO;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper
public interface DtoToDomainMapper {

    Process fromAddedProcessDTO(final AddedProcessDTO addedProcessDTO);

    SortingMethod fromSortingMethodDto(final SortingMethodDTO sortingMethodDTO);

    RunningProcessDTO toRunningProcessDto(final Process process);

    Priority fromPriorityType(final PriorityTypes priorityGroup);

    default String map(UUID value) {
        return value.toString();
    }
}
