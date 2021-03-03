package com.marcolotz.iptiq.core.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class AcceptedProcessDecorator extends AbstractProcess {

  final Process process;
  final Instant creationTime;

  public AcceptedProcessDecorator(Process process, Instant creationTime) {
    this.process = process;
    this.creationTime = creationTime;
  }

  @Override
  public void kill() {
    process.kill();
  }

  @Override
  public UUID getPid() {
    return process.getPid();
  }

  @Override
  public Priority getPriority() {
    return process.getPriority();
  }
}
