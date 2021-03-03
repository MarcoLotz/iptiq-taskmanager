package com.marcolotz.iptiq.core;

import com.marcolotz.iptiq.core.exceptions.ProcessNotFoundException;
import com.marcolotz.iptiq.core.model.AcceptedProcessDecorator;
import com.marcolotz.iptiq.core.model.Priority;
import com.marcolotz.iptiq.core.model.Process;
import com.marcolotz.iptiq.ports.TaskManager;
import com.marcolotz.iptiq.ports.TimeProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PriorityBasedTaskManager implements TaskManager {

  private final int capacity;
  private final Map<Priority, Map<UUID, AcceptedProcessDecorator>> taskQueues;
  private final List<Priority> prioritiesInAscendingOrder;
  private final TimeProvider timeProvider;

  public PriorityBasedTaskManager(final int capacity, final TimeProvider timeProvider) {
    this.capacity = capacity;
    this.taskQueues = new HashMap<>();
    this.prioritiesInAscendingOrder = Arrays.stream(Priority.values().clone())
        .sorted(Comparator.comparingInt(Priority::getPriorityNumber)).collect(Collectors.toList());
    this.timeProvider = timeProvider;
  }

  @Override
  @Synchronized
  public void addProcess(Process process) {
    final int numberOfElements = taskQueues.values().stream().map(Map::size).mapToInt(e -> e).sum();
    if (numberOfElements < capacity) {
      Map<UUID, AcceptedProcessDecorator> prioQueue = taskQueues.getOrDefault(process.getPriority(), new LinkedHashMap<>());
      prioQueue.put(process.getPid(), new AcceptedProcessDecorator(process, timeProvider.getTime()));
      taskQueues.put(process.getPriority(), prioQueue);
    } else {
      Optional<Priority> priorityEntry = prioritiesInAscendingOrder.stream()
          .filter(
              prio -> !taskQueues.getOrDefault(prio, new LinkedHashMap<>()).isEmpty() &&
                  prio.getPriorityNumber() < process.getPriority().getPriorityNumber())
          .findFirst();

      if (priorityEntry.isPresent()) {
        final Priority priority = priorityEntry.get();
        Map.Entry<UUID, AcceptedProcessDecorator> oldest = taskQueues.get(priority).entrySet().iterator().next();
        taskQueues.get(priority).remove(oldest.getKey());
        oldest.getValue().kill();
        Map<UUID, AcceptedProcessDecorator> prioQueue = taskQueues.getOrDefault(process.getPriority(), new LinkedHashMap<>());
        prioQueue.put(process.getPid(), new AcceptedProcessDecorator(process, timeProvider.getTime()));
        taskQueues.put(process.getPriority(), prioQueue);
      } else {
        log.info("Could not remove lower priority process to add new one");
      }
    }
  }

  @Override
  public List<Process> listRunningProcess(final SortingMethod method) {
    if (method.equals(SortingMethod.PRIORITY)) {
      LinkedList<Process> list = new LinkedList<>();
      prioritiesInAscendingOrder
          .forEach(prio ->
              taskQueues.getOrDefault(prio, new LinkedHashMap<>())
                  .forEach((key, value) -> list.addFirst(value.getProcess()))
          );
      return list;
    }
    return taskQueues.values().stream().flatMap(e -> e.values().stream()).sorted(method.comparator())
        .map(AcceptedProcessDecorator::getProcess).collect(Collectors.toList());
  }

  @Override
  @Synchronized
  public void killProcess(final String pid) throws ProcessNotFoundException {
    UUID inputPid = UUID.fromString(pid);
    Optional<AcceptedProcessDecorator> processToKill = taskQueues.values()
        .stream()
        .map(e -> e.get(inputPid))
        .filter(Objects::nonNull)
        .findFirst();

    if (processToKill.isPresent()) {
      processToKill.get().kill();
    } else {
      throw new ProcessNotFoundException("The process with pid " + pid + " could not be found");
    }
  }

  @Override
  @Synchronized
  public void killGroup(final Priority priority) {
    taskQueues.getOrDefault(priority, Collections.emptyMap())
        .values()
        .forEach(AcceptedProcessDecorator::kill);
    taskQueues.remove(priority);
  }

  @Override
  @Synchronized
  public void killAll() {
    taskQueues.values().stream().flatMap(e -> e.values().stream()).forEach(AcceptedProcessDecorator::kill);
  }
}
