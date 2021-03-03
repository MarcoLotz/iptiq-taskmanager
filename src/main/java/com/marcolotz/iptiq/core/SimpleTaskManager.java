package com.marcolotz.iptiq.core;

import com.marcolotz.iptiq.core.exceptions.MaximumCapacityReachedException;
import com.marcolotz.iptiq.core.exceptions.ProcessNotFoundException;
import com.marcolotz.iptiq.core.model.AbstractProcess;
import com.marcolotz.iptiq.core.model.AcceptedProcessDecorator;
import com.marcolotz.iptiq.core.model.Priority;
import com.marcolotz.iptiq.core.model.Process;
import com.marcolotz.iptiq.ports.TaskManager;
import com.marcolotz.iptiq.ports.TimeProvider;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Synchronized;

public class SimpleTaskManager implements TaskManager {

  protected final int capacity;
  protected final TimeProvider timeProvider;
  protected Collection<AcceptedProcessDecorator> tasks = new LinkedList<>();

  public SimpleTaskManager(final int capacity, final TimeProvider timeProvider) {
    if (capacity < 1) {
      throw new IllegalArgumentException("Capacity needs to be higher than 0");
    }
    this.capacity = capacity;
    this.timeProvider = timeProvider;
  }

  @Override
  @Synchronized
  public void addProcess(Process process) throws MaximumCapacityReachedException {
    verifyCapacity();
    tasks.add(new AcceptedProcessDecorator(process, timeProvider.getTime()));
  }

  private void verifyCapacity() throws MaximumCapacityReachedException {
    final int currentSize = tasks.size();
    if (currentSize + 1 > capacity) {
      throw new MaximumCapacityReachedException("Task Manager at max design capacity of " + capacity);
    }
  }

  @Override
  public List<Process> listRunningProcess(SortingMethod method) {
    Comparator<AcceptedProcessDecorator> processComparator = method.comparator();
    return tasks.stream().sorted(processComparator).map(AcceptedProcessDecorator::getProcess).collect(Collectors.toList());
  }

  @Override
  @Synchronized
  public void killProcess(String pid) throws ProcessNotFoundException {
    UUID uuid = UUID.fromString(pid);
    Optional<AcceptedProcessDecorator> process = tasks
        .stream()
        .filter(p -> p.getPid().equals(uuid))
        .findFirst();

    if (process.isPresent()) {
      process.get().kill();
    } else {
      throw new ProcessNotFoundException("The process with pid" + pid + " could not be found");
    }
  }

  @Override
  @Synchronized
  public void killGroup(Priority priority) {
    Set<AbstractProcess> killedProcesses = tasks.stream()
        .filter(p -> p.getPriority().equals(priority))
        .peek(AbstractProcess::kill)
        .collect(Collectors.toSet());
    List<AcceptedProcessDecorator> aliveProcesses = tasks.stream().filter(p -> !killedProcesses.contains(p)).collect(Collectors.toList());
    tasks.clear();
    tasks.addAll(aliveProcesses);
  }

  @Override
  @Synchronized
  public void killAll() {
    tasks.forEach(AbstractProcess::kill);
    tasks.clear();
  }
}
