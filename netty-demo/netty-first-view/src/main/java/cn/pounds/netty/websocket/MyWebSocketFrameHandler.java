package cn.pounds.netty.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.time.LocalDateTime;

/**
 * @Date 2021/6/8 22:21
 * @Author by pounds
 * @Description TODO
 */
public class MyWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // msg拿不到文本消息内容,要使用text方法
        System.out.println("服务器收到消息" + msg.text());

        // 回复消息
        ctx.channel().writeAndFlush(new TextWebSocketFrame("于服务器时间 " + LocalDateTime.now() + " 收到客户端消息: "+ msg.text()));
    }

    /**
     * 当web客户端连接后,创建channel的时候就会有handler添加,然后会触发这个方法
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        // id 表示唯一的值,LongText是唯一的 shortText不是唯一的
        System.out.println(" handlerAdded 被调用" + ctx.channel().id().asLongText());
        System.out.println(" handlerAdded 被调用" + ctx.channel().id().asShortText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println(" handlerRemove 被调用" + ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(" 通信发生异常 : " + cause.getMessage());
        System.out.println(" 通信发生异常 : " + cause.getCause());

        ctx.close();
    }
}
