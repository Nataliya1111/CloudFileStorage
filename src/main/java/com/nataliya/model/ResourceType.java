package com.nataliya.model;

import lombok.Getter;

@Getter
public enum ResourceType {
    FILE(0),
    DIRECTORY(1);

    private final short code;

    ResourceType(int code) {
        this.code = (short) code;
    }

    public static ResourceType fromCode(int code) {
        for (ResourceType t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown code of resource type: %s, expected 0 or 1", code));
    }

}
