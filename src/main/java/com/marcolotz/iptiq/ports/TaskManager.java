package com.marcolotz.iptiq.ports;

import com.marcolotz.iptiq.core.SortingMethod;
import com.marcolotz.iptiq.core.exceptions.MaximumCapacityReachedException;
import com.marcolotz.iptiq.core.exceptions.ProcessNotFoundException;
import com.marcolotz.iptiq.core.model.Priority;
import com.marcolotz.iptiq.core.model.Process;
import java.util.List;

public interface TaskManager {

  void addProcess(final Process process) throws MaximumCapacityReachedException;

  List<Process> listRunningProcess(final SortingMethod method);

  void killProcess(final String pid) throws ProcessNotFoundException;

  void killGroup(final Priority priority);

  void killAll();

}
