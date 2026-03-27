package com.tgu.trace;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerTraceInterceptor {
    public static void beforeHandle() {
        String traceId = TraceContext.getTraceId();
        if (traceId == null || traceId.isEmpty()) {
            traceId = TraceIdGenerator.generateTraceId();
        }
        String parentSpanId = TraceContext.getParentSpanId();
        String spanId = TraceIdGenerator.generateSpanId();
        TraceContext.setTraceId(traceId);
        TraceContext.setSpanId(spanId);
        TraceContext.setParentSpanId(parentSpanId);

        long startTimestamp = System.currentTimeMillis();
        TraceContext.setStartTimestamp(String.valueOf(startTimestamp));
    }

    public static void afterHandle(String serviceName) {
        String traceId = TraceContext.getTraceId();
        String spanId = TraceContext.getSpanId();
        String startTimestampValue = TraceContext.getStartTimestamp();
        if (traceId == null || spanId == null || startTimestampValue == null) {
            TraceContext.clear();
            log.info("ServerTraceInterceptor afterHandle: traceId, spanId 或 startTimestamp 为空，无法上报 Zipkin");
            return;
        }
        long endTimestamp = System.currentTimeMillis();
        long startTimestamp = Long.parseLong(startTimestampValue);
        long duration = endTimestamp - startTimestamp;

        ZipkinReporter.reportSpan(
                traceId,
                spanId,
                TraceContext.getParentSpanId(),
                "server-" + serviceName,
                startTimestamp,
                duration,
                serviceName,
                "server"
        );
        TraceContext.clear();
    }
}
