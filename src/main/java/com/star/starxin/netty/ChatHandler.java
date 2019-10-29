package com.star.starxin.netty;

import com.alibaba.fastjson.JSON;
import com.star.starxin.SpringUtil;
import com.star.starxin.enums.MsgActionEnum;
import com.star.starxin.service.UserService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Star
 * @createdDate
 * @description 专门处理文本消息的的对象，frame是消息的载体
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    // 用于记录和管理所有客户端的channel
    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        /*// 获取客户端传输过来的消息
        String text = textWebSocketFrame.text();
        System.out.println(text);
//        for(Channel channel:clients) {
//            channel.writeAndFlush(new TextWebSocketFrame("服务器接收消息"+LocalDateTime.now()+"接受到消息，消息为"+text));
//        }
        // 与上面for循环一样的效果
        clients.writeAndFlush(new TextWebSocketFrame("服务器接收消息"+LocalDateTime.now()+"接受到消息，消息为"+text));*/
        // 获取客户端发来的消息
        String content = textWebSocketFrame.text();
        Channel currentChannel = channelHandlerContext.channel();
        DataContent dataContent = JSON.parseObject(content, DataContent.class);
        // 判断消息类型，处理不同的业务
        Integer action = dataContent.getAction();
        // 当websocket第一次open的时候，把用到的channel与userId关联起来
        if(action == MsgActionEnum.CONNECT.type) {
            String senderId = dataContent.getChatMsg().getSenderId();
            UserChannelRel.put(senderId, currentChannel);

            for(Channel channel: clients) {
                System.out.println(channel.id().asLongText());
            }
            UserChannelRel.output();
        } else if(action == MsgActionEnum.CHAT.type) {
            // 把聊天记录保存到数据库， 同时标记消息的签收状态，未签收
            ChatMsg chatMsg = dataContent.getChatMsg();
            String msg = chatMsg.getMsg();
            String receiverId = chatMsg.getReceiverId();
            String senderId = chatMsg.getSenderId();
            // 保存消息到数据库，并标记为未签收 Handler中是无法进行Service注入的
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            String msgId = userService.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);
            // 从全局channel获取接收方的channel
            Channel receiverChannel = UserChannelRel.get(receiverId);
            if(receiverChannel == null) {
                // todo 用户离线，给用户推送消息（JPush, 个推， 小米推送）

            } else {
                // 当receiveChannel不为空的时候，从ChannelGroup中查找该channel是否存在
                Channel findChannel = clients.find(receiverChannel.id());
                if(findChannel != null) {
                    // 用户在线
                    receiverChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(chatMsg)));
                } else {
                    // todo 用户离线状态
                }
            }
        } else if (action == MsgActionEnum.SIGNED.type) {
            // 针对具体的消息进行签收，修改数据库中对应消息的签收状态
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            // 获取消息的id
            String msgIdStr = dataContent.getExpand();
            List<String> msgIdList = new ArrayList<>();
            String[] msgIds = msgIdStr.split(",");
            if(StringUtils.isBlank(msgIdStr) || msgIds.length == 0) {
                return;
            }
            for(int i = 0;i < msgIds.length;i++) {
                msgIdList.add(msgIds[i]);
            }
            if(msgIdList.isEmpty()) {
               return;
            }
            // 批量签收
            userService.updateMsgListSigned(msgIdList);
        } else if (action == MsgActionEnum.KEEPALIVE.type) {
            // 心跳类型的消息
        }
    }

    /**
     * 当客户端连接服务端 获取客户端的channel放入channelGroup中进行管理
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx.channel());
    }

    /**
     * d当触发handlerRemoved之后，ChannelGroup会自动移除chandler
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        /*System.out.println("客户端断开"+ctx.channel().id().asLongText());
        System.out.println("客户端断开"+ctx.channel().id().asShortText());*/
        // 连接断开需要移除channel
        clients.remove(ctx.channel());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 发生异常后关闭连接，随后从channelGroup中移除
        ctx.channel().close();
        clients.remove(ctx.channel());
    }
}
