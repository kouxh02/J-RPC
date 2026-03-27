package com.tgu.trace;

import com.tgu.config.RpcConfig;
import lombok.extern.slf4j.Slf4j;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;


/**
 * 客户端层面，我利用动态代理拦截器（Interceptor）在 RPC 调用前生成 TraceID 并存入 MDC。
 * 为了解决 Netty 异步线程切换导致 ThreadLocal 上下文丢失的问题，我在发送请求前将 MDC 数据取出并绑定到 Netty Channel 的 Attribute 属性中，
 * 随后在自定义编码器（Encoder）里取出该属性，将 TraceID/SpanID 封装成二进制字节流（长度+内容）写入 TCP 协议头，随业务数据一起发送。
 *
 * 服务端层面，自定义解码器（Decoder）在解析数据包时，优先读取协议头中的 Trace 信息并还原到上下文。
 * 服务端拦截器（Handler）在执行业务逻辑前获取该 ID，确保调用链不中断。
 * 最终，客户端和服务端拦截器分别在 finally 块中统计执行耗时，生成 Span 对象并异步上报至 Zipkin，完成全链路监控。
 */


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
