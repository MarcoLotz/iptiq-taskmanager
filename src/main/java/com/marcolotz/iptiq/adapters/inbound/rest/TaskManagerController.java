package com.marcolotz.iptiq.adapters.inbound.rest;

import com.marcolotz.iptiq.core.SortingMethod;
import com.marcolotz.iptiq.core.exceptions.MaximumCapacityReachedException;
import com.marcolotz.iptiq.core.exceptions.ProcessNotFoundException;
import com.marcolotz.iptiq.core.model.Process;
import com.marcolotz.iptiq.ports.TaskManager;
import com.marcolotz.taskmanager.openapi.handler.V1Api;
import com.marcolotz.taskmanager.openapi.model.AddedProcessDTO;
import com.marcolotz.taskmanager.openapi.model.PriorityTypes;
import com.marcolotz.taskmanager.openapi.model.RunningProcessDTO;
import com.marcolotz.taskmanager.openapi.model.SortingMethodDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
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
    public DeferredResult<ResponseEntity<Void>> v1ProcessesDelete(final @Size(max = 50) @Valid List<String> pids,
                                                                  final @Valid PriorityTypes priorityGroup,
                                                                  final @Valid Boolean killAll) {
        log.debug("Received request to kill process with the following arguments: pids: {}, prioritygroup: {}, isKillAll: {}",
            () -> String.join(",", pids), priorityGroup::toString, killAll::toString);

        final DeferredResult<ResponseEntity<Void>> output = new DeferredResult<>();
        ForkJoinPool.commonPool().submit(() -> performDeletion(pids, priorityGroup, killAll, output));
        return output;
    }

    private void performDeletion(final List<String> pids, final PriorityTypes priorityGroup, final Boolean killAll,
                                 final DeferredResult<ResponseEntity<Void>> output) {
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
                output.setResult(ResponseEntity.ok().build());
            } catch (final ProcessNotFoundException e) {
                log.error("Process couldn't not be found", e);
                output.setErrorResult(e);
            }
        }
    }

    @Override
    // TODO: Use pagination
    public DeferredResult<ResponseEntity<List<RunningProcessDTO>>> v1ProcessesGet(final @NotNull @Valid SortingMethodDTO sortingMethod) {
        log.debug("getting all processes using the following sorting method: {}", sortingMethod::toString);

        final DeferredResult<ResponseEntity<List<RunningProcessDTO>>> output = new DeferredResult<>();
        ForkJoinPool.commonPool().submit(() -> {
            final List<Process> runningProcesses = taskManager.listRunningProcess(mapper.fromSortingMethodDto(sortingMethod));
            final List<RunningProcessDTO> runningProcessDTOS =
                runningProcesses.stream().map(mapper::toRunningProcessDto).collect(Collectors.toList());
            output.setResult(ResponseEntity.ok(runningProcessDTOS));
        });
        return output;
    }

    @Override
    public DeferredResult<ResponseEntity<Void>> v1ProcessesPut(final @Valid AddedProcessDTO addedProcessDTO) {
        log.debug("creating the following process: {}", addedProcessDTO::toString);
        final DeferredResult<ResponseEntity<Void>> output = new DeferredResult<>();

        ForkJoinPool.commonPool().submit(() -> {
            try {
                taskManager.addProcess(mapper.fromAddedProcessDTO(addedProcessDTO));
                output.setResult(ResponseEntity.ok().build());
            } catch (final MaximumCapacityReachedException e) {
                log.error("Could not add new process to task manager, maximum capacity reached");
                output.setErrorResult(e);
            }
        });
        return output;
    }
}
