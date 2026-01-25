package com.tgu.transport.codec;

import com.tgu.enums.MessageType;
import com.tgu.pojo.RpcRequest;
import com.tgu.pojo.RpcResponse;
import com.tgu.serializers.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class CustomEncoder extends MessageToByteEncoder {

    private Serializer serializer;

    // netty 在写数据的时候会调用这个方法，将Java对象进行编码
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
//        log.info("ready to encode >>> {}", msg.getClass().getName());

        if (msg instanceof RpcRequest) {
            out.writeShort(MessageType.REQUEST.getCode());
        } else if (msg instanceof RpcResponse) {
            out.writeShort(MessageType.RESPONSE.getCode());
        }

        // 写入当前序列化起的类型标识
        out.writeShort(serializer.getType());

        // 将消息转化为字符数组
        byte[] serializeBytes = serializer.serialize(msg);

        // 写入消息的字节长度
        out.writeInt(serializeBytes.length);

        // 将字节数据内容写入输出缓冲区中
        out.writeBytes(serializeBytes);

    }
}
