package cn.pounds.netty.tcpstickunpack;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * @Date 2021/6/17 22:32
 * @Author by pounds
 * @Description TODO
 */
public class MyServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private int count;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        String info = new String(bytes, Charset.forName("utf-8"));
        System.out.println( " 服务器端接口到消息 " + info);
        System.out.println(" 服务器端接收到消息量 " + (++this.count));

        ctx.writeAndFlush(Unpooled.copiedBuffer(UUID.randomUUID().toString(),Charset.forName("utf-8")));
    }
}
