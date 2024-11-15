package com.amos.example.common.serializer;

import com.amos.crpc.serializer.Serializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Kryo序列化器
 */
public class KryoSerializer implements Serializer {
    /**
     * Kryo线程局部变量, 保证线程安全,使用ThreadLocal保证每一个线程都有一个Kryo对象
     */
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 设置动态注册序列化器
        // 禁止对象引用， Kryo 在序列化对象时不会记录对象之间的引用关系，而是每个对象都被视为独立的
        // 设置 false 时，Kryo 会重新序列化每个对象，即使是多个地方引用同一个对象。
        kryo.setReferences(false);
        // 设置是否注册类，默认值为 true，即默认情况下，Kryo 会在序列化时注册类
        kryo.setRegistrationRequired(false);
        return kryo;
    });


    @Override
    public <T> byte[] serialize(T object) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Output output = new Output(outputStream);
        KRYO_THREAD_LOCAL.get().writeObject(output, object);
        output.close();
        return outputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        Input input = new Input(inputStream);
        T result = KRYO_THREAD_LOCAL.get().readObject(input, type);
        input.close();
        return result;
    }
}
