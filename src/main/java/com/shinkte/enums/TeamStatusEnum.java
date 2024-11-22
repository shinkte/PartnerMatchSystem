package com.shinkte.enums;

/**
 * @Author: shinkte
 * @Description: 队伍表状态枚举类
 * @CreateTime: 2024-11-21
 */
public enum TeamStatusEnum {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "保密"),
    ;
    private int value;
    private String text;

    public static TeamStatusEnum getEnum(Integer value) {
        if (value == null) {
            return null;
        }
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum item : values) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }
    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
