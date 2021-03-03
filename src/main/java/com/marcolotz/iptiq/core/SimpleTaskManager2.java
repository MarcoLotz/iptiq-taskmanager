//package com.marcolotz.iptiq.core;
//
//import com.marcolotz.iptiq.core.exceptions.MaximumCapacityReachedException;
//import com.marcolotz.iptiq.core.exceptions.ProcessNotFoundException;
//import com.marcolotz.iptiq.core.model.Priority;
//import com.marcolotz.iptiq.core.model.Process;
//import com.marcolotz.iptiq.ports.TaskManager;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//import lombok.Synchronized;
//
//public class SimpleTaskManager2 implements TaskManager {
//
//  private final int capacity;
//  private final Map<String, Process> tasks;
//
//  public SimpleTaskManager2(int capacity) {
//    this.capacity = capacity;
//    this.tasks = new HashMap<>();
//  }
//
//  @Override
//  @Synchronized
//  public void addProcess(Process process) {
//    verifyCapacity();
//    tasks.put(process.getPid(), process);
//  }
//
//  private void verifyCapacity() {
//    final int currentSize = tasks.size();
//    if (currentSize + 1 > capacity) {
//      throw new MaximumCapacityReachedException("Task Manager at max design capacity of " + capacity);
//    }
//  }
//
//  @Override
//  @Synchronized
//  public void killProcess(String pid) {
//    Optional.ofNullable(tasks.get(pid))
//        .ifPresentOrElse(Process::kill, () -> {
//          throw new ProcessNotFoundException("The process with pid" + pid + " could not be found");
//        });
//  }
//
//  @Override
//  @Synchronized
//  public void killGroup(Priority priority) {
//    tasks.values().stream().filter(p -> p.getPriority().equals(priority))
//        .forEach(process -> killAndRemove(tasks, process));
//  }
//
//  private void killAndRemove(Map<String, Process> tasks, Process process) {
//    process.kill();
//    tasks.remove(process.getPid());
//  }
//
//  @Override
//  @Synchronized
//  public void killAll() {
//    tasks.values().forEach(process -> killAndRemove(tasks, process));
//  }
//}
