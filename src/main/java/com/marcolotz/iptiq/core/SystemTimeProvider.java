package com.marcolotz.iptiq.core;

import com.marcolotz.iptiq.ports.TimeProvider;

import java.time.Instant;

public class SystemTimeProvider implements TimeProvider {

    @Override
    public Instant getTime() {
        return Instant.now();
    }
}
