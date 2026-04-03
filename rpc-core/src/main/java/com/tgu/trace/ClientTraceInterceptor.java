package com.tgu.trace;

public class ClientTraceInterceptor {
    public static void beforeInvoke() {
        String traceId = TraceContext.getTraceId();
        if (traceId == null) {
            String generateTraceId = TraceIdGenerator.generateTraceId();
            TraceContext.setTraceId(generateTraceId);
        }
        String spanId = TraceIdGenerator.generateSpanId();
        TraceContext.setSpanId(spanId);

        long currentTimeMillis = System.currentTimeMillis();
        TraceContext.setStartTimestamp(String.valueOf(currentTimeMillis));
    }

    public static void afterInvoke(String spanName) {
        String traceId = TraceContext.getTraceId();
        String spanId = TraceContext.getSpanId();
        String startTimestampValue = TraceContext.getStartTimestamp();
        if (traceId == null || spanId == null || startTimestampValue == null) {
            TraceContext.clear();
            return;
        }

        long endTimeStamp = System.currentTimeMillis();
        long startTimestamp = Long.parseLong(startTimestampValue);
        long duration = endTimeStamp - startTimestamp;

        ZipkinReporter.reportSpan(
                traceId,
                spanId,
                TraceContext.getParentSpanId(),
                spanName,
                startTimestamp,
                duration,
                "client"
        );
        TraceContext.clear();
    }
}
