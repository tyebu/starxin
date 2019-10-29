package com.star.starxin.mapper;

import com.star.starxin.pojo.ChatMsg;
import com.star.starxin.utils.MyMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ChatMsgMapper extends MyMapper<ChatMsg> {
    void batchMsgIdListSign(List<String> msgList);
}