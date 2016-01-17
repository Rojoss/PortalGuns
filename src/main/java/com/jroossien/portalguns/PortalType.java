package com.jroossien.portalguns;

public enum PortalType {
    PRIMARY,
    SECONDARY;

    public PortalType opposite() {
        if (this == PRIMARY) {
            return SECONDARY;
        } else {
            return PRIMARY;
        }
    }
}
