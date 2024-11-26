package com.amos.crpc.protocol;

import lombok.Getter;

/**
 * 协议消息状态枚举
 */
@Getter
public enum ProtocolMessageStatusEnum {
    OK("ok", 20),
    BAD_REQUEST("bad request", 40),
    BAD_RESPONSE("bad response", 50);

    private final String text;
    private final int value;

    ProtocolMessageStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据值获取枚举
     *
     * @param value
     * @return
     */
    public static ProtocolMessageStatusEnum getEnumByValue(int value) {
        for (ProtocolMessageStatusEnum statusEnum : ProtocolMessageStatusEnum.values()) {
            if (statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }
}
