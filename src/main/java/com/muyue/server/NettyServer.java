package com.muyue.server;

import com.muyue.handler.ChatHandler;
import com.muyue.handler.HttpHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @Auther: MY
 * @Description: 服务端
 * @Date: 15:33 2021/2/23
**/
public class NettyServer {
    public static void main(String[] args) throws Exception{
        //接收处理建立连接的请求
        EventLoopGroup boss=new NioEventLoopGroup();
        //接收处理消息的请求
        EventLoopGroup worker=new NioEventLoopGroup();
        try {
            //服务器启动的引导器，一个工具类
            ServerBootstrap bootstrap=new ServerBootstrap();
            //初始化管道加入一些handler处理器
            bootstrap.group(boss,worker).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline=ch.pipeline();
                    //http编码解码器
                    pipeline.addLast(new HttpServerCodec());
                    //分块传输
                    pipeline.addLast(new ChunkedWriteHandler());
                    //块聚合
                    pipeline.addLast(new HttpObjectAggregator(1024*1024));
                    //进入聊天室的http处理器
                    pipeline.addLast(new HttpHandler());
                    //自定义的处理器
                    pipeline.addLast(new ChatHandler());
                }
            });
            //绑定端口启动
            ChannelFuture channelFuture=bootstrap.bind(8081).sync();
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }


}
