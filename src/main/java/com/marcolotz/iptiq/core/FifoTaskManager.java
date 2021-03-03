package com.marcolotz.iptiq.core;

import com.marcolotz.iptiq.core.model.AbstractProcess;
import com.marcolotz.iptiq.core.model.AcceptedProcessDecorator;
import com.marcolotz.iptiq.core.model.Process;
import com.marcolotz.iptiq.ports.TimeProvider;
import java.util.LinkedList;
import java.util.Optional;

public class FifoTaskManager extends SimpleTaskManager {

  public FifoTaskManager(final int capacity, final TimeProvider timeProvider) {
    super(capacity, timeProvider);
    this.tasks = new LinkedList<>();
  }

  @Override
  public void addProcess(Process process) {
    if (tasks.size() == capacity) {
      LinkedList<AcceptedProcessDecorator> mgmtTasks = (LinkedList<AcceptedProcessDecorator>) tasks;
      Optional.ofNullable(mgmtTasks.remove())
          .ifPresent(AbstractProcess::kill);
    }
    tasks.add(new AcceptedProcessDecorator(process, timeProvider.getTime()));
  }
}
