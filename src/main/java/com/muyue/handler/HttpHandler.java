package com.muyue.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private WebSocketServerHandshaker handshaker;
    private static String IP = "IP";
    private static String USERNAME = "USERNAME";


    static {
        //创建常量的实例，后面要对上下文设置属性需要
        AttributeKey.newInstance(IP);
        AttributeKey.newInstance(USERNAME);
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        //解析url
        QueryStringDecoder decoder=new QueryStringDecoder(req.uri());
        //获取个人昵称
        String nickName=decoder.parameters().get("nickname").get(0);
        System.out.println(nickName);
        //检查请求是否不合法
        if(nickName==null || "".equals(nickName) || !req.decoderResult().isSuccess()||!"websocket".equals(req.headers().get("Upgrade"))){
            //不合法则返回400 bad request
            FullHttpResponse response=new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            ChannelFuture future=ctx.channel().writeAndFlush(response);
            future.addListener(ChannelFutureListener.CLOSE);
        }else{
            //获取IP
            String ip=((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
            //本地ip转换下
            if("0:0:0:0:0:0:0:1".equals(ip)){
                ip="127.0.0.1";
            }
            //把ip 用户名  聊天室名字设置到管道的属性去
            ctx.channel().attr(AttributeKey.valueOf(IP)).set(ip);
            ctx.channel().attr(AttributeKey.valueOf(USERNAME)).set(nickName);
            //建立握手工厂对象
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    req.uri(), null, false);
            //创建一个握手器
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                //发回支持的websocket版本
                WebSocketServerHandshakerFactory
                        .sendUnsupportedVersionResponse(ctx.channel());
            } else {
                //进行同步握手
                handshaker.handshake(ctx.channel(), req).sync();
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("通道建立");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("通道关闭");
        super.channelInactive(ctx);
    }
}
