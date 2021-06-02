package cn.pounds.netty.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * @Date 2021/5/9 19:24
 * @Author by pounds
 * @Description 跟NettyServerHandler 一回事
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * channel准备就绪就会触发这个方法,向服务端发送消息
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client: " + ctx);
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello server: 我是netty客户端", CharsetUtil.UTF_8));
    }

    /**
     * 读事件
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("执行了");
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("服务器回复的消息: " + byteBuf.toString(CharsetUtil.UTF_8));
        System.out.println("服务器地址: " + ctx.channel().remoteAddress());
    }

    /**
     * 异常处理
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(String.format("出现了异常 [%s] : %s",cause.getMessage(),cause));
        ctx.close();
    }

}
