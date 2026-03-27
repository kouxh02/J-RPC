package com.tgu.transport.client;

import com.tgu.trace.TraceContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Attr;

import java.util.Map;


/**
 * MDCChannelHandler 负责在发送请求时将 TraceContext 从 Channel 属性中取出并设置到当前线程的 MDC 中，以便在日志中正确记录 TraceId 和其他上下文信息。
 * 发送完成后会清除 MDC 中的 TraceContext，避免对后续请求造成影响。
 * 通过 Channel 属性传递 TraceContext 可以确保在异步发送请求时，TraceContext 能够正确地传递到处理请求的线程中，保持日志上下文的一致性。
 */
@Slf4j
public class MDCChannelHandler extends ChannelOutboundHandlerAdapter {

    public static final AttributeKey<Map<String, String>> TRACE_CONTEXT_KEY = AttributeKey.valueOf("TraceContext");

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Map<String, String> traceContext = ctx.channel().attr(TRACE_CONTEXT_KEY).get();
        ctx.channel().attr(TRACE_CONTEXT_KEY).set(null);

        if (traceContext != null && !traceContext.isEmpty()) {
            // 设置当前线程的 TraceContext
            TraceContext.clone(traceContext);
            log.info("MDCChannelHandler 设置 TraceContext: {}", traceContext);
        } else {
            TraceContext.clear();
            log.error("MDCChannelHandler 未找到 TraceContext");
        }
        promise.addListener(future -> TraceContext.clear());
        super.write(ctx, msg, promise);

    }

}
