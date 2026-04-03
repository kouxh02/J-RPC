package com.tgu.transport.codec;

import com.tgu.enums.MessageType;
import com.tgu.enums.RequestType;
import com.tgu.pojo.RpcRequest;
import com.tgu.pojo.RpcResponse;
import com.tgu.serializers.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import static com.tgu.constants.TransportProtocolConstants.*;

@AllArgsConstructor
@Slf4j
public class CustomEncoder extends MessageToByteEncoder<Object> {

    private Serializer serializer;

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        short messageType = resolveMessageType(msg);
        byte[] traceBytes = buildTraceBytes(msg);

        if (traceBytes.length > MAX_TRACE_LENGTH) {
            throw new IllegalArgumentException("Trace field too large: " + traceBytes.length);
        }

        byte[] bodyBytes = serializer.serialize(msg);
        if (bodyBytes == null || bodyBytes.length == 0) {
            throw new IllegalArgumentException("Serialized body is empty");
        }

        int frameLength = BASE_HEADER_LENGTH + traceBytes.length + bodyBytes.length;
        if (frameLength > MAX_FRAME_LENGTH) {
            throw new IllegalArgumentException("Frame too large: " + frameLength);
        }

        int frameStartIndex = out.writerIndex();

        // 1) 固定头
        out.writeInt(MAGIC);
        out.writeByte(VERSION);

        // 2) fullLength 占位，稍后回填
        int fullLengthIndex = out.writerIndex();
        out.writeInt(0);

        // 3) trace
        out.writeInt(traceBytes.length);
        out.writeBytes(traceBytes);

        // 4) 类型信息
        out.writeShort(messageType);
        out.writeShort(serializer.getType());

        // 5) 消息体
        out.writeInt(bodyBytes.length);
        out.writeBytes(bodyBytes);

        // 6) 回填 fullLength
        int frameEndIndex = out.writerIndex();
        int actualFrameLength = frameEndIndex - frameStartIndex;
        out.setInt(fullLengthIndex, actualFrameLength);
    }

    private short resolveMessageType(Object msg) {
        if (msg instanceof RpcRequest) {
            return (short) MessageType.REQUEST.getCode();
        } else if (msg instanceof RpcResponse) {
            return (short) MessageType.RESPONSE.getCode();
        } else {
            throw new IllegalArgumentException("Unsupported message type: " + msg.getClass().getName());
        }
    }

    private byte[] buildTraceBytes(Object msg) {
        if (!(msg instanceof RpcRequest request) || request.getType() != RequestType.NORMAL) {
            return new byte[0];
        }

        String traceId = request.getTraceId();
        String spanId = request.getSpanId();
        if (traceId == null || traceId.isEmpty() || spanId == null || spanId.isEmpty()) {
            return new byte[0];
        }
        return (traceId + ";" + spanId).getBytes(StandardCharsets.UTF_8);
    }
}
