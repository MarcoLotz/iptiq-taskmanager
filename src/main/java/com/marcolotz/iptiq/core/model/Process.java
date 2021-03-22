package com.marcolotz.iptiq.core.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class Process extends AbstractProcess {

    final UUID pid;
    final Priority priority;

    public Process(final Priority priority) {
        this.priority = priority;
        // I have decided to assign the PID on construction for this scenario, instead of leaving to the
        // constructor to define a PID. Also I decided to use UUID instead of unix like integers.
        // The model doesn't specify. This avoids collision on a simple model of the problem.
        // If the requirements were different, then a collision avoidance component would have to create pid
        // and use as a constructor parameter.
        this.pid = UUID.randomUUID();
    }

}
