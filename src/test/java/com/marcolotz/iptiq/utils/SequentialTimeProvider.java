package com.marcolotz.iptiq.utils;

import com.marcolotz.iptiq.ports.TimeProvider;
import java.time.Instant;

public class SequentialTimeProvider implements TimeProvider {

  Instant baseInstant = Instant.now();

  @Override
  public Instant getTime() {
    baseInstant = baseInstant.plusSeconds(1);
    return baseInstant;
  }
}
