//package com.marcolotz.iptiq.core;
//
//import com.marcolotz.iptiq.core.exceptions.MaximumCapacityReachedException;
//import com.marcolotz.iptiq.core.exceptions.ProcessNotFoundException;
//import com.marcolotz.iptiq.core.model.Priority;
//import com.marcolotz.iptiq.core.model.Process;
//import com.marcolotz.iptiq.core.SortingMethod;
//import com.marcolotz.iptiq.ports.TaskManager;
//import lombok.Synchronized;
//
//import java.util.*;
//
//public class PriorityBasedTaskManager implements TaskManager {
//
//    private final int capacity;
//    private final Map<Priority, Map<String, Process>> taskQueues;
//
//    public PriorityBasedTaskManager(int capacity)
//    {
//        this.capacity = capacity;
//        this.taskQueues = new HashMap<>();
//    }
//
//    @Override
//    @Synchronized
//    public void addProcess(Process process) {
//        verifyCapacity();
//        Map<String, Process> prioritizedQueue = taskQueues.getOrDefault(process.getPriority(), new LinkedHashMap<>());
//        prioritizedQueue.put(process.getPid(), process);
//        taskQueues.put(process.getPriority(), prioritizedQueue);
//    }
//
//    @Override
//    public List<Process> listRunningProcess(SortingMethod method) {
//        Comparator<Process> processComparator = method.comparator();
//        return taskQueues
//    }
//
//    private void verifyCapacity() {
//        final int currentSize = taskQueues.values().stream().map(Map::size).mapToInt(e -> e).sum();
//        if (currentSize + 1 > capacity) throw new MaximumCapacityReachedException("Task Manager at max design capacity of " + capacity);
//    }
//
//    @Override
//    @Synchronized
//    public void killProcess(String pid) {
//        taskQueues.values()
//                .stream()
//                .map(e -> e.get(pid))
//                .filter(Objects::nonNull)
//                .findFirst()
//                .ifPresentOrElse(Process::kill, () ->  {
//                    throw new ProcessNotFoundException("The process with pid" + pid + " could not be found");
//                });
//    }
//
//    @Override
//    @Synchronized
//    public void killGroup(Priority priority) {
//        taskQueues.getOrDefault(priority, Collections.emptyMap())
//                .values()
//                .forEach(Process::kill);
//    }
//
//    @Override
//    @Synchronized
//    public void killAll() {
//        taskQueues.values().stream().flatMap(e -> e.values().stream()).forEach(Process::kill);
//    }
//}
