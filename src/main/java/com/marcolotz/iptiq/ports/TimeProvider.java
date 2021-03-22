package com.marcolotz.iptiq.ports;

import java.time.Instant;

public interface TimeProvider {

    Instant getTime();

}
