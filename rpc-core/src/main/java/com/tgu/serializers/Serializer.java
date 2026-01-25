package com.tgu.serializers;

/*
    用于为对象提供序列化和反序列化的功能
    通过一个静态工厂方法根据类型代码返回具体的序列化器实例
 */
public interface Serializer {

    // 把对象序列化为字节数组
    byte[] serialize(Object obj);

    Object deserializer(byte[] bytes, int messageType);

    int getType();

    static Serializer getSerializerByCode(int code) {
        switch (code) {
            case 0:
                return new ObjectSerializer();
            case 1:
                return new JsonSerializer();
            default:
                return null;
        }
    }



}
