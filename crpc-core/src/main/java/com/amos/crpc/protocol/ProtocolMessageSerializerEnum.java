package com.amos.crpc.protocol;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 协议消息序列化器枚举
 */
@Getter
public enum ProtocolMessageSerializerEnum {

    JDK(0, "jdk"),
    JSON(1, "json"),
    KRYO(2, "kryo"),
    HESSIAN(3, "hessian");

    private final int key;

    private final String value;

    ProtocolMessageSerializerEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 根据key获取枚举
     *
     * @param key
     * @return
     */
    public static ProtocolMessageSerializerEnum getEnumByKey(int key) {
        for (ProtocolMessageSerializerEnum serializerEnum : ProtocolMessageSerializerEnum.values()) {
            if (serializerEnum.key == key) {
                return serializerEnum;
            }
        }
        return null;
    }

    /**
     * 根据value获取枚举
     *
     * @param value
     * @return
     */
    public static ProtocolMessageSerializerEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (ProtocolMessageSerializerEnum serializerEnum : ProtocolMessageSerializerEnum.values()) {
            if (serializerEnum.value.equals(value)) {
                return serializerEnum;
            }
        }
        return null;
    }
}
