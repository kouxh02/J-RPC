package com.tgu.transport.codec;

import com.tgu.enums.MessageType;
import com.tgu.serializers.Serializer;
import com.tgu.trace.TraceContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.tgu.constants.TransportProtocolConstants.*;

@Slf4j
@AllArgsConstructor
public class CustomDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        while (true) {
            // 1) 连最小帧头都不够，等待更多数据
            if (in.readableBytes() < PRE_FRAME_HEADER_LENGTH) {
                return;
            }

            in.markReaderIndex();
            int frameStartIndex = in.readerIndex();

            try {
                // 2) 协议头
                int magic = in.readInt();
                if (magic != MAGIC) {
                    log.warn("Invalid magic, channel={}, remote={}, magic=0x{}", ctx.channel().id(), ctx.channel().remoteAddress(), Integer.toHexString(magic));
                    ctx.close();
                    return;
                }

                byte version = in.readByte();
                if (version != VERSION) {
                    log.warn("Unsupported version, channel={}, remote={}, version={}", ctx.channel().id(), ctx.channel().remoteAddress(), version);
                    ctx.close();
                    return;
                }

                int fullLength = in.readInt();
                if (fullLength < BASE_HEADER_LENGTH || fullLength > MAX_FRAME_LENGTH) {
                    log.warn("Invalid fullLength, channel={}, remote={}, fullLength={}, readable={}", ctx.channel().id(), ctx.channel().remoteAddress(), fullLength, in.readableBytes());
                    ctx.close();
                    return;
                }

                int remainingBytesOfFrame = fullLength - PRE_FRAME_HEADER_LENGTH;
                // 3) 半包：当前缓冲区还没到齐完整帧，回滚等待下一次
                if (in.readableBytes() < remainingBytesOfFrame) {
                    in.resetReaderIndex();
                    return;
                }

                // 4) traceLength
                int traceLength = in.readInt();
                if (traceLength < 0 || traceLength > MAX_TRACE_LENGTH) {
                    log.warn("Invalid traceLength, channel={}, remote={}, traceLength={}", ctx.channel().id(), ctx.channel().remoteAddress(), traceLength);
                    ctx.close();
                    return;
                }

                // 5) traceBytes
                byte[] traceBytes = new byte[traceLength];
                in.readBytes(traceBytes);
                applyTraceContext(traceBytes);

                // 6) messageType
                short messageType = in.readShort();
                if (messageType != MessageType.REQUEST.getCode() && messageType != MessageType.RESPONSE.getCode()) {
                    log.warn("Unsupported messageType, channel={}, remote={}, messageType={}", ctx.channel().id(), ctx.channel().remoteAddress(), messageType);
                    ctx.close();
                    return;
                }

                // 7) serializerType
                short serializerType = in.readShort();
                Serializer serializer = Serializer.getSerializerByCode(serializerType);
                if (serializer == null) {
                    log.warn("Unknown serializerType, channel={}, remote={}, serializerType={}", ctx.channel().id(), ctx.channel().remoteAddress(), serializerType);
                    ctx.close();
                    return;
                }

                // 8) bodyLength
                int bodyLength = in.readInt();
                if (bodyLength < 0) {
                    log.warn("Invalid bodyLength, channel={}, remote={}, bodyLength={}", ctx.channel().id(), ctx.channel().remoteAddress(), bodyLength);
                    ctx.close();
                    return;
                }

                // 9) bodyBytes + 反序列化
                byte[] bodyBytes = new byte[bodyLength];
                in.readBytes(bodyBytes);

                Object decoded = serializer.deserializer(bodyBytes, messageType);
                if (decoded == null) {
                    log.warn("Deserializer returned null, channel={}, serializerType={}, messageType={}", ctx.channel().id(), serializerType, messageType);
                    ctx.close();
                    return;
                }

                // 10) 帧消费一致性检查
                int frameEndIndex = in.readerIndex();
                int consumed = frameEndIndex - frameStartIndex;
                if (consumed != fullLength) {
                    log.warn("Frame length mismatch, channel={}, expected={}, actual={}", ctx.channel().id(), fullLength, consumed);
                    ctx.close();
                    return;
                }

                out.add(decoded);

                // 11) 粘包场景：如果后面还有数据，继续循环解下一帧
                if (!in.isReadable()) {
                    return;
                }
            } catch (Exception e) {
                log.error("Decode failed, channel={}, remote={}", ctx.channel().id(), ctx.channel().remoteAddress(), e);
                ctx.close();
                return;
            }
        }
    }

    private void applyTraceContext(byte[] traceBytes) {
        TraceContext.clear();
        if (traceBytes.length == 0) {
            return;
        }
        String traceMsg = new String(traceBytes, java.nio.charset.StandardCharsets.UTF_8);
        if (traceMsg.isEmpty()) {
            log.info("Empty trace field, skipping trace context setup");
            return;
        }
        String[] parts = traceMsg.split(";", -1);
        if (parts.length > 0 && !parts[0].isEmpty()) {
            TraceContext.setTraceId(parts[0]);
        }
        if (parts.length > 1 && !parts[1].isEmpty()) {
            TraceContext.setParentSpanId(parts[1]);
        }
    }

}
