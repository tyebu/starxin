package com.star.starxin.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author Star
 * @createdDate
 * @description
 */
public class WSServer {
    private static class SingleWsServer {
        static final WSServer instance = new WSServer();
    }
    public static WSServer getInstance() {
        return SingleWsServer.instance;
    }
    private EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private ServerBootstrap serverBootstrap;
    private ChannelFuture future;
    public WSServer() {
        // 定义一对主从线程组
        mainGroup = new NioEventLoopGroup();
        subGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(mainGroup, subGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WSServerInitializer());        //初始化器 子处理器
    }
    public void start() {
        this.future = serverBootstrap.bind(8088);
        System.err.println("netty server started......");
    }
    /*public static void main(String[] args) throws InterruptedException {
        // 定义一对主从线程组
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup subGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(mainGroup, subGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new WSServerInitializer());        //初始化器 子处理器

            ChannelFuture future = serverBootstrap.bind(8089).sync();
            future.channel().closeFuture().sync();
        } finally {
            mainGroup.shutdownGracefully();
            subGroup.shutdownGracefully();
        }
    }*/
}
