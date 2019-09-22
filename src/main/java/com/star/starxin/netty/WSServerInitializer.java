package com.star.starxin.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
/**
 * @author Star
 * @createdDate
 * @description
 */
public class WSServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // websocket给予http，所以需要http编码解码器
        pipeline.addLast(new HttpServerCodec());
        // 对写大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());
        // 对HttpMessage进行聚合 FullHttpRequest或FullHttpResponse
        // 几乎所有netty编程都会使用次handler
        pipeline.addLast(new HttpObjectAggregator(1024*64));
        // =============以上用于支持http协议================
        // websockt服务器处理的协议， 用于h制定给客户端链接访问的路由
        // 此handler会处理握手的动作 handsshaking（close ping pong）
        // 对于websocket来讲，都是以frames传输的，不同的数据类型对应的frames也不同
        pipeline.addLast(new WebSocketServerProtocolHandler("/wws"));
        pipeline.addLast(new ChatHandler());
    }
}
