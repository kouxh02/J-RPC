package com.tgu.serializers;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class ObjectSerializer implements Serializer{
    @Override
    public byte[] serialize(Object obj) {
        log.info(" serialize >>> {}", obj);

        byte[] bytes = null;

        // ByteArrayOutputStream 是一个可变大小的字节数据缓冲区，数据都会写入这个缓冲区中
        // ByteArrayOutputStream 是桶
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // 讲对象转为二进制数据，把对象数据写入字节缓冲区
            // ObjectOutputStream是粉碎机，放在桶上面
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            // 把对象写入输出流中，触发序列化
            // 把对象放到粉碎机上粉碎
            oos.writeObject(obj);

            // 强制将缓冲区的数据刷新到底层流bos中
            // 把粉碎机里卡住的最后一点纸屑抖落到桶里
            oos.flush();

            // 把粉碎后的数据作为字节数组拿出来
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return bytes;
    }

    @Override
    public Object deserializer(byte[] bytes, int messageType) {
        log.info(" deserializer >>> {}", bytes.length);

        Object obj = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        }  catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String getName() {
        return "object";
    }
}
