package com.marcolotz.iptiq.adapters.inbound.rest;

import com.marcolotz.iptiq.core.model.Process;
import com.marcolotz.iptiq.ports.TaskManager;
import com.marcolotz.taskmanager.openapi.handler.V1Api;
import com.marcolotz.taskmanager.openapi.model.AddedProcessDTO;
import com.marcolotz.taskmanager.openapi.model.PriorityTypes;
import com.marcolotz.taskmanager.openapi.model.RunningProcessDTO;
import com.marcolotz.taskmanager.openapi.model.SortingMethodDTO;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
public class TaskManagerController implements V1Api {

    @Autowired
    protected TaskManager taskManager;

    @Autowired
    protected DtoToDomainMapper mapper;

    @Override
    @SneakyThrows // TODO: Controller advice
    public ResponseEntity<Void> v1ProcessesDelete(final @DecimalMin("0") @DecimalMax("50") @Valid List<String> pids,
                                                  final @Valid PriorityTypes priorityGroup,
                                                  final @Valid Boolean killAll) {
        if (killAll) {
            taskManager.killAll();
        } else {
            final Runnable killAllPids = () -> pids.forEach(pid -> {
                try {
                    taskManager.killProcess(pid);
                } catch (final Exception e) {
                    // TODO
                }
            });
            final CompletableFuture<Void> pidFuture = CompletableFuture.runAsync(killAllPids);

            final Runnable killAllFromGroup = () -> taskManager.killGroup(mapper.fromPriorityType(priorityGroup));
            final CompletableFuture<Void> groupFuture = CompletableFuture.runAsync(killAllFromGroup);

            CompletableFuture.allOf(pidFuture, groupFuture).join();
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
