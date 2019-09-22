package com.star.starxin.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.time.LocalDateTime;

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
        // 获取客户端传输过来的消息
        String text = textWebSocketFrame.text();
        System.out.println(text);
//        for(Channel channel:clients) {
//            channel.writeAndFlush(new TextWebSocketFrame("服务器接收消息"+LocalDateTime.now()+"接受到消息，消息为"+text));
//        }
        // 与上面for循环一样的效果
        clients.writeAndFlush(new TextWebSocketFrame("服务器接收消息"+LocalDateTime.now()+"接受到消息，消息为"+text));
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
        System.out.println("客户端断开"+ctx.channel().id().asLongText());
        System.out.println("客户端断开"+ctx.channel().id().asShortText());
    }
}
