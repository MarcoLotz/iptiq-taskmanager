package com.marcolotz.iptiq.core.model;

import java.util.UUID;

public interface AbstractProcess {

    void kill();

    UUID getPid();

    Priority getPriority();
}
