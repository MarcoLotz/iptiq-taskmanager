package com.marcolotz.iptiq.core.model;

import java.util.UUID;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class AbstractProcess {

  public void kill() {
    log.info("Killing process:", this::toString);
  }

  public abstract UUID getPid();

  public abstract Priority getPriority();
}
