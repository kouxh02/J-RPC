package com.tgu.trace;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;


@Slf4j
public class TraceContext {

    public static void setTraceId(String traceId) {
        putOrRemove("traceId", traceId);
    }

    public static String getTraceId() {
        return MDC.get("traceId");
    }

    public static void setSpanId(String spanId) {
        putOrRemove("spanId", spanId);
    }

    public static String getSpanId() {
        return MDC.get("spanId");
    }

    public static void setParentSpanId(String parentSpanId) {
        putOrRemove("parentSpanId", parentSpanId);
    }

    public static String getParentSpanId() {
        return MDC.get("parentSpanId");
    }

    public static void setStartTimestamp(String startTimestamp) {
        putOrRemove("startTimestamp", startTimestamp);
    }

    public static String getStartTimestamp() {
        return MDC.get("startTimestamp");
    }

    public static void clear() {
        MDC.clear();
    }

    private static void putOrRemove(String key, String value) {
        if (value == null || value.isEmpty()) {
            MDC.remove(key);
            return;
        }
        MDC.put(key, value);
    }
}
