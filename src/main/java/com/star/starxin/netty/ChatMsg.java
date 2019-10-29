package com.star.starxin.netty;

import lombok.Data;

import java.io.Serializable;
@Data
public class ChatMsg implements Serializable {
    private static final Long serivalVersionUID = 57572789431L;
    private String senderId;
    private String receiverId;
    private String msg;
    private String msgId;
}
