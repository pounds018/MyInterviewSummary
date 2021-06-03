package cn.pounds.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

/**
 * @Date 2021/5/30 17:38
 * @Author by pounds
 * @Description
 * TestHttpServerHandler: 实际处理netty任务的handler,处理http请求的时候就继承这个SimpleChannelInboundHandler
 * HttpObject: 指定客户端和服务端通信的数据在netty中被封装成HttpObject类型
 */
public class TestHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    /**
     * channelRead0 读取客户端数据
     * @param ctx
     * @param msg --- server和client通信数据,为httpObject类型
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest){
            System.out.println(" msg 类型="+ msg.getClass());
            System.out.println(" 客户端地址"+ ctx.channel().remoteAddress());

            // 设置回复信息
            ByteBuf byteBuf = Unpooled.copiedBuffer("hello,我是服务器", StandardCharsets.UTF_8);
            // 对请求路径进行过滤
            HttpRequest request = (HttpRequest) msg;
            if ("/favicon.ico".equals(request.uri())){
                byteBuf.clear();
                byteBuf.writeBytes("你请求了不该请求的位置".getBytes(StandardCharsets.UTF_8));
            }
            // 构造一个http响应,即httpResponse
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                    // 设置http协议版本
                    HttpVersion.HTTP_1_1,
                    // 设置响应状态码
                    HttpResponseStatus.OK,
                    // 设置响应内容
                    byteBuf
            );

            // 设置响应头
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=utf-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());

            ctx.writeAndFlush(response);
        }
    }
}
