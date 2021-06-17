package cn.pounds.netty.inout;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Date 2021/6/16 23:11
 * @Author by pounds
 * @Description 如果接受的是编码器的数据,泛型只直接填写编码器将数据编码的类型
 */
public class MyServerHandler extends SimpleChannelInboundHandler<Long> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx,Long msg) throws Exception {
        System.out.println("从 客户端 " + ctx.channel().remoteAddress() + " 读取到 long : " + msg);

        ctx.writeAndFlush(789456L);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
