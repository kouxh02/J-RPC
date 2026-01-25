package com.tgu.transport.codec;

import com.tgu.enums.MessageType;
import com.tgu.serializers.Serializer;
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
}
