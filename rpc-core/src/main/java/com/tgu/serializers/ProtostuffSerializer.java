package com.tgu.serializers;

import com.tgu.exception.SerializeException;
import com.tgu.pojo.RpcRequest;
import com.tgu.pojo.RpcResponse;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;


public class ProtostuffSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Cannot Serialize null object");
        }
        Schema schema = RuntimeSchema.getSchema(obj.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        byte[] bytes;
        try {
            bytes = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
        return bytes;
    }

    @Override
    public Object deserializer(byte[] bytes, int messageType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Cannot deserialize null or empty byte array");
        }
        Class<?> aClass = getClass4MessageType(messageType);
        Schema schema = RuntimeSchema.getSchema(aClass);
        Object obj;
        try {
            obj = aClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new SerializeException("Deserialize failed due to reflection issues");
        }
        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }

    @Override
    public int getType() {
        return 2;
    }

    @Override
    public String getName() {
        return "protostuff";
    }

    private Class<?> getClass4MessageType(int messageType) {
        switch (messageType) {
            case 0 -> {
                return RpcRequest.class;
            }
            case 1 -> {
                return RpcResponse.class;
            }
            default -> {
                throw new SerializeException("Unknown message type: " + messageType);
            }
        }
    }
}
