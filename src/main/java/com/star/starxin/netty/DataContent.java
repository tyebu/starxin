package com.star.starxin.netty;

import lombok.Data;

import java.io.Serializable;
@Data
public class DataContent implements Serializable {
    private static final Long serivalVersionUID = 57572789131L;
    // 动作类型
    private Integer action;
    // 消息内容
    private ChatMsg chatMsg;
    // 扩展字段
    private String expand;

}
