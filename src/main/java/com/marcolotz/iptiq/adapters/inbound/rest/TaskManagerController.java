package com.marcolotz.iptiq.adapters.inbound.rest;

import com.marcolotz.iptiq.core.SortingMethod;
import com.marcolotz.iptiq.core.exceptions.ProcessNotFoundException;
import com.marcolotz.iptiq.core.model.Process;
import com.marcolotz.iptiq.ports.TaskManager;
import com.marcolotz.taskmanager.openapi.handler.V1Api;
import com.marcolotz.taskmanager.openapi.model.AddedProcessDTO;
import com.marcolotz.taskmanager.openapi.model.PriorityTypes;
import com.marcolotz.taskmanager.openapi.model.RunningProcessDTO;
import com.marcolotz.taskmanager.openapi.model.SortingMethodDTO;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Log4j2
public class TaskManagerController implements V1Api {

    @Autowired
    protected TaskManager taskManager;

    @Autowired
    protected DtoToDomainMapper mapper;

    @Override
    public ResponseEntity<Void> v1ProcessesDelete(final @DecimalMin("0") @DecimalMax("50") @Valid List<String> pids,
                                                  final @Valid PriorityTypes priorityGroup,
                                                  final @Valid Boolean killAll) {
        if (killAll) {
            taskManager.killAll();
        } else {
            final Set<String> pidsFromPriorityGroup = Optional.ofNullable(priorityGroup)
                .map(mapper::fromPriorityType)
                .map(type -> taskManager.listRunningProcess(SortingMethod.PRIORITY).stream().filter(p -> p.getPriority().equals(type))
                    .map(Process::getPid)
                    .map(UUID::toString)
                    .collect(Collectors.toSet()))
                .orElse(Set.of());

            final Set<String> pidsToKill = Stream.concat(pids.stream(), pidsFromPriorityGroup.stream()).collect(Collectors.toSet());

            try { // As an implementation decision, I think it makes sense to fail on the first error
                for (final String pid : pidsToKill) {
                    taskManager.killProcess(pid);
                }
            } catch (final ProcessNotFoundException e) {
                log.error("Process couldn't not be found", e);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<RunningProcessDTO>> v1ProcessesGet(final @NotNull @Valid SortingMethodDTO sortingMethod) {
        final List<Process> runningProcesses = taskManager.listRunningProcess(mapper.fromSortingMethodDto(sortingMethod));
        final List<RunningProcessDTO> runningProcessDTOS =
            runningProcesses.stream().map(mapper::toRunningProcessDto).collect(Collectors.toList());
        return ResponseEntity.ok(runningProcessDTOS);
    }

    @Override
    @SneakyThrows // TODO: Put controller advice
    public ResponseEntity<Void> v1ProcessesPut(final @Valid AddedProcessDTO addedProcessDTO) {
        taskManager.addProcess(mapper.fromAddedProcessDTO(addedProcessDTO));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
