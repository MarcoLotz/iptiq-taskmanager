package com.marcolotz.iptiq.adapters.inbound.rest;

import com.marcolotz.iptiq.core.SortingMethod;
import com.marcolotz.iptiq.core.exceptions.MaximumCapacityReachedException;
import com.marcolotz.iptiq.core.exceptions.ProcessNotFoundException;
import com.marcolotz.iptiq.core.model.Priority;
import com.marcolotz.iptiq.core.model.Process;
import com.marcolotz.iptiq.ports.TaskManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

@RequiredArgsConstructor
// Simple decorator for TaskManager interface in order to grant Async behaviour
public class AsyncTaskManager implements TaskManager {

    private final TaskManager taskManager;

    @Override
    @Async
    public void addProcess(final Process process) throws MaximumCapacityReachedException {
        taskManager.addProcess(process);
    }

    @Override
    @Async
    public List<Process> listRunningProcess(final SortingMethod method) {
        return taskManager.listRunningProcess(method);
    }

    @Override
    @Async
    public void killProcess(final String pid) throws ProcessNotFoundException {
        taskManager.killProcess(pid);
    }

    @Override
    @Async
    public void killGroup(final Priority priority) {
        taskManager.killGroup(priority);
    }

    @Override
    @Async
    public void killAll() {
        taskManager.killAll();
    }
}
