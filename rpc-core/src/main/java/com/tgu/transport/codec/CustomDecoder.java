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

@Slf4j
@AllArgsConstructor
public class CustomDecoder extends ByteToMessageDecoder {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int traceMsgLength = in.readInt();
        byte[] traceMsgBytes = new byte[traceMsgLength];
        in.readBytes(traceMsgBytes);
        serializeTraceMsg(traceMsgBytes);

        short messageType = in.readShort();
        if (messageType != MessageType.REQUEST.getCode() && messageType != MessageType.RESPONSE.getCode()) {
            log.error("不支持此类型");
        }
        short serializerType = in.readShort();
        Serializer serializer = Serializer.getSerializerByCode(serializerType);

        if (serializer == null) {
            log.error("不存在对应的序列化器");
            return;
        }

        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);

        Object deserializer = serializer.deserializer(bytes, messageType);

        out.add(deserializer);
    }

    private void serializeTraceMsg(byte[] traceMsgBytes) {
        String traceMsg = new String(traceMsgBytes);
//        log.info("traceMsg: {}", traceMsg);
        String[] parts = traceMsg.split(";");
        if (!parts[0].isEmpty()) {
             TraceContext.setTraceId(parts[0]);
        }
        if (!parts[1].isEmpty()) {
            TraceContext.setParentSpanId(parts[1]);
        }
    }
}
