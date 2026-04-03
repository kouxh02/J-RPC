package com.tgu.trace;

import com.tgu.config.RpcConfig;
import lombok.extern.slf4j.Slf4j;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Slf4j
public class ZipkinReporter {

    private static final AsyncReporter<Span> reporter;

    static {
        AsyncReporter<Span> tempReporter = null;
        if (RpcConfig.isTracingEnabled()) {
            try {
                String zipkinUrl = RpcConfig.getZipkinUrl();
                OkHttpSender sender = OkHttpSender.create(zipkinUrl);
                tempReporter = AsyncReporter.create(sender);
                Runtime.getRuntime().addShutdownHook(new Thread(ZipkinReporter::close));
                log.info("初始化 ZipkinReporter，Zipkin URL: {}", zipkinUrl);
            } catch (Exception e) {
                log.error("初始化 ZipkinReporter 失败: {}", e.getMessage());
            }
        }
        reporter = tempReporter;
    }

    public static void reportSpan(String traceId, String spanId, String parentSpanId,
            String spanName, long startTimeStamp, long duration,
            String type) {
        if (!RpcConfig.isTracingEnabled() || reporter == null) {
            return;
        }

        if (traceId == null || spanId == null || spanName == null) {
            log.warn("reportSpan 参数无效: traceId={}, spanId={}, spanName={}",
                    traceId, spanId, spanName);
            return;
        }

        try {
            Endpoint localEndpoint = Endpoint.newBuilder()
                    .serviceName(RpcConfig.getTracingServiceName())
                    .build();

            Span.Kind kind = "client".equalsIgnoreCase(type) ? Span.Kind.CLIENT : Span.Kind.SERVER;

            Span.Builder spanBuilder = Span.newBuilder()
                    .traceId(traceId)
                    .id(spanId)
                    .name(spanName)
                    .kind(kind)
                    .timestamp(startTimeStamp * 1000)
                    .duration(duration * 1000)
                    .localEndpoint(localEndpoint);

            if (parentSpanId != null && !parentSpanId.isEmpty()) {
                spanBuilder.parentId(parentSpanId);
            }

            reporter.report(spanBuilder.build());
        } catch (Exception e) {
            log.error("上报 Zipkin 失败: {}", e.getMessage());
        }
    }

    public static void close() {
        if (reporter != null) {
            reporter.close();
        }
    }
}
