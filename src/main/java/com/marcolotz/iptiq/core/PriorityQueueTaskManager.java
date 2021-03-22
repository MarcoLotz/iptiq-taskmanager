package com.marcolotz.iptiq.core;

import com.marcolotz.iptiq.core.model.AbstractProcess;
import com.marcolotz.iptiq.core.model.AcceptedProcessDecorator;
import com.marcolotz.iptiq.core.model.Process;
import com.marcolotz.iptiq.ports.TimeProvider;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

@Log4j2
public class PriorityQueueTaskManager extends SimpleTaskManager {

    private final static Comparator<AcceptedProcessDecorator> PRIORITY_COMPARATOR = SortingMethod.PRIORITY.comparator();

    public PriorityQueueTaskManager(final int capacity, final TimeProvider timeProvider) {
        super(capacity, timeProvider);
        tasks = new PriorityQueue<>(PRIORITY_COMPARATOR);
    }

    @Override
    @Synchronized
    public void addProcess(final Process process) {
        if (tasks.size() == capacity) {
            final List<AbstractProcess> lowerPriority = tasks.stream()
                .filter(e -> e.getPriority().getPriorityNumber() < process.getPriority().getPriorityNumber())
                .collect(Collectors.toList());

            if (!lowerPriority.isEmpty()) {
                final AbstractProcess lowestAndOldest = lowerPriority.get(lowerPriority.size() - 1);
                lowestAndOldest.kill();
                tasks.remove(lowestAndOldest);
                tasks.add(new AcceptedProcessDecorator(process, timeProvider.getTime()));
            } else {
                log.debug("No process with lower priority was found, skipping it");
            }
        } else {
            tasks.add(new AcceptedProcessDecorator(process, timeProvider.getTime()));
        }
    }

}
