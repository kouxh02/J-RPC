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

@Slf4j
public class MDCChannelHandler extends ChannelOutboundHandlerAdapter {

    public static final AttributeKey<Map<String, String>> TRACE_CONTEXT_KEY = AttributeKey.valueOf("TraceContext");

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Map<String, String> traceContext = ctx.channel().attr(TRACE_CONTEXT_KEY).get();

        if (traceContext != null) {
            // 设置当前线程的 TraceContext
            TraceContext.clone(traceContext);
//            log.info("MDCChannelHandler 设置 TraceContext: {}", traceContext);
        } else {
            log.error("MDCChannelHandler 未找到 TraceContext");
        }
        super.write(ctx, msg, promise);

    }

}
