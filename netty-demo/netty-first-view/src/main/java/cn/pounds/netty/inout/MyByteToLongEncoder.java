package cn.pounds.netty.inout;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Date 2021/6/16 23:22
 * @Author by pounds
 * @Description TODO
 */
public class MyByteToLongEncoder extends MessageToByteEncoder<Long> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Long msg, ByteBuf out) throws Exception {
        System.out.println("MyByteToLongEncoder encode 方法 被调用");
        System.out.println("msg = " + msg);

        out.writeLong(msg);
    }
}
