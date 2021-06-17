package cn.pounds.netty.tcpstickunpack.solution;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Date 2021/6/17 23:09
 * @Author by pounds
 * @Description TODO
 */


public class MyMessageEncoder extends MessageToByteEncoder<MessageProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageProtocol msg, ByteBuf out) throws Exception {
        System.out.println("MyMessageEncoder encode 方法被调用");
        out.writeInt(msg.getMessageLen());
        out.writeBytes(msg.getContent());
    }
}
