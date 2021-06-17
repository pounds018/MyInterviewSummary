package cn.pounds.netty.tcpstickunpack;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;

/**
 * @Date 2021/6/17 22:29
 * @Author by pounds
 * @Description TODO
 */
public class MyClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private int count;
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int i = 0; i < 10; i++) {
            ctx.writeAndFlush(Unpooled.copiedBuffer("hello,server [" + i + "]", Charset.forName("utf-8")));
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        String info = new String(bytes, Charset.forName("utf-8"));
        System.out.println( " 客户端接口到消息 " + info);
        System.out.println(" 客户端接收到消息量 " + (++this.count));
    }
}
