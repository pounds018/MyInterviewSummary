package cn.pounds.netty.inout;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * @Date 2021/6/16 23:25
 * @Author by pounds
 * @Description TODO
 */
public class MyClientHandler extends SimpleChannelInboundHandler<Long> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Long msg) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(" client 发送数据");
//        ctx.writeAndFlush(123456L);
        ctx.writeAndFlush(Unpooled.copiedBuffer(" 测试发送字符串给 处理long类型的编码器会怎样", CharsetUtil.UTF_8));
    }
}
