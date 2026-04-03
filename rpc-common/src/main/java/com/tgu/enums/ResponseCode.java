package com.tgu.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ResponseCode {

    SUCCESS(200),
    TIMEOUT(408),
    FAIL(500);

    private final int code;

    public int getCode() {
        return code;
    }
}
