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
        String zipkinUrl = RpcConfig.getZipkinUrl();
        log.info("初始化 ZipkinReporter，Zipkin URL: {}", zipkinUrl);
        OkHttpSender sender = OkHttpSender.create(zipkinUrl);
        reporter = AsyncReporter.create(sender);
    }

    public static void reportSpan(String traceId, String spanId, String parentSpanId,
            String name, long startTimeStamp, long duration,
            String serviceName, String type) {

        // 参数校验
        if (traceId == null || spanId == null || serviceName == null) {
            log.warn("reportSpan 参数无效: traceId={}, spanId={}, serviceName={}",
                    traceId, spanId, serviceName);
            return;
        }

        // 构建 localEndpoint，Zipkin 要求必须设置 serviceName
        Endpoint localEndpoint = Endpoint.newBuilder()
                .serviceName(serviceName)
                .build();

        // 根据 type 确定 Span.Kind
        Span.Kind kind = "client".equalsIgnoreCase(type) ? Span.Kind.CLIENT : Span.Kind.SERVER;

        // 构建 Span
        Span.Builder spanBuilder = Span.newBuilder()
                .traceId(traceId)
                .id(spanId)
                .name(name)
                .kind(kind)
                .timestamp(startTimeStamp * 1000)
                .duration(duration * 1000)
                .localEndpoint(localEndpoint);

        // 只有当 parentSpanId 不为 null 且不为空时才设置
        if (parentSpanId != null && !parentSpanId.isEmpty()) {
            spanBuilder.parentId(parentSpanId);
        }

        Span span = spanBuilder.build();
        reporter.report(span);
//        log.info("trace id: {}, span id: {}, kind: {}, report success", traceId, spanId, kind);
    }

    public static void close() {
        reporter.close();
    }
}
