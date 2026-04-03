package com.tgu.serializers;

import com.tgu.spi.ExtensionLoader;
import com.tgu.spi.NamedExtension;

/*
    用于为对象提供序列化和反序列化的功能
    通过一个静态工厂方法根据类型代码返回具体的序列化器实例
 */
public interface Serializer extends NamedExtension {

    // 把对象序列化为字节数组
    byte[] serialize(Object obj);

    Object deserializer(byte[] bytes, int messageType);

    int getType();

    static Serializer getSerializerByCode(int code) {
        return ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(code);
    }

    static Serializer getSerializerByName(String name) {
        return ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(name);
    }

}
