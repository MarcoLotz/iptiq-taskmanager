package com.marcolotz.iptiq.core;

import com.marcolotz.iptiq.core.model.AcceptedProcessDecorator;

import java.util.Comparator;

public enum SortingMethod {
    CREATION_TIME,
    PRIORITY,
    ID;

    SortingMethod() {
    }

    public Comparator<AcceptedProcessDecorator> comparator() {
        if (this == CREATION_TIME) {
            return Comparator.comparing(AcceptedProcessDecorator::getCreationTime);
        }
        if (this == PRIORITY) {
            return (p1, p2) -> p1.getPriority().getPriorityNumber() < p2.getPriority().getPriorityNumber() ? 1 : -1;
        } // can only be ID
        return (p1, p2) -> -p1.getPid().compareTo(p2.getPid());
    }
}
