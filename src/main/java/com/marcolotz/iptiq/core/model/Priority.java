package com.marcolotz.iptiq.core.model;

import lombok.Getter;

@Getter
public enum Priority {
  // I made an interval to be future friendly here. Imagine other priority levels may be required in the future.
  LOW(0),
  MEDIUM(500),
  HIGH(1000);

  final int priorityNumber;

  Priority(int priorityNumber) {
    this.priorityNumber = priorityNumber;
  }
}
