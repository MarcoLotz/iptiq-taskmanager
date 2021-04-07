package com.marcolotz.iptiq.core;

import com.marcolotz.iptiq.core.model.AcceptedProcessDecorator;

import java.util.Comparator;

public enum SortingMethod {
    CREATION_TIME,
    PRIORITY,
    ID;

    public Comparator<AcceptedProcessDecorator> comparator() {
        if (this == CREATION_TIME) {
            return Comparator.comparing(AcceptedProcessDecorator::getCreationTime);
        }
        if (this == PRIORITY) {
            return Comparator.comparingInt((AcceptedProcessDecorator p) -> p.getPriority().getPriorityNumber()).reversed();
        } // can only be ID
        return (p1, p2) -> -p1.getPid().compareTo(p2.getPid());
    }
}
