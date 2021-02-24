package com.muyue.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.ImmediateEventExecutor;

import java.nio.charset.Charset;


public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static ChannelGroup channels = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
    public int getChannelsNum(){
        return channels.size();
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //提取发送的消息
        String text=msg.text();
        /*
            处理接收到的text消息，对特殊字符进行处理
         */
        text = text.replaceAll("f","*");
        String username=(String) ctx.channel().attr(AttributeKey.valueOf("USERNAME")).get();
        String id=ctx.channel().id().asLongText();
            if(channels != null){
                TextWebSocketFrame me=new TextWebSocketFrame("我: "+text+"#"+channels.size());
                //遍历所有channel，然后发信息过去
                for(Channel channel:channels){
                    String channelId=channel.id().asLongText();
                    if(id.equals(channelId)){
                        channel.writeAndFlush(me);
                    }else {
                        TextWebSocketFrame other=new TextWebSocketFrame(username+": "+text+"#"+channels.size());
                        channel.writeAndFlush(other);
                    }
                }
            }

    }



    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channels.add(channel);
        System.out.println("channel:"+channels.size());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("处于活跃状态:" + ctx.channel().remoteAddress());
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("不活跃状态:" + ctx.channel().remoteAddress());
    }
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        channels.writeAndFlush("[客户端]" + ctx.channel().remoteAddress() + "===离线");
    }
}
