package com.star.starxin.enums;

public enum OperatorFriendRequestTypeEnum {
    IGNORE("忽略", 0),
    PASS("通过", 1);
    public final String msg;
    public final Integer type;
    OperatorFriendRequestTypeEnum(String msg, Integer type) {
        this.msg = msg;
        this.type = type;
    }
    public Integer getType() {
        return this.type;
    }
    public static String getMsgByType(Integer type) {
        for (OperatorFriendRequestTypeEnum operType : OperatorFriendRequestTypeEnum.values()) {
            if (operType.getType() == type) {
                return operType.msg;
            }
        }
        return null;
    }
}
