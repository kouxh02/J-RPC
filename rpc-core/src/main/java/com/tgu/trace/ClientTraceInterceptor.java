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

    public static void afterInvoke(String serviceName) {
        long endTimeStamp = System.currentTimeMillis();
        long startTimestamp = Long.parseLong(TraceContext.getStartTimestamp());
        long duration = endTimeStamp - startTimestamp;

        ZipkinReporter.reportSpan(
                TraceContext.getTraceId(),
                TraceContext.getSpanId(),
                TraceContext.getParentSpanId(),
                "client-" + serviceName,
                startTimestamp,
                duration,
                serviceName,
                "client"
        );
        TraceContext.clear();
    }
}
