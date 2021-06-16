package cn.pounds.netty.inout;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @Date 2021/6/16 23:06
 * @Author by pounds
 * @Description TODO
 */
public class MyByteToLongDecoder extends ByteToMessageDecoder {
    /**
     * decode方法会被调用多次,当没有元素往list里面添加,或者ByteBuf里面没有数据的时候就不再调用decode方法
     * out不为空的时候,ChannelInboundHandler中的入站也会被调用多次
     * @param ctx  --- 上下文对线
     * @param in --- 入站ByteBuf
     * @param out --- 向下一个handler传递的集合
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 当byteBuf中可读字节 >= 8 的时候表示,一个long类型数据准备好了
        if (in.readableBytes() >= 8){
            out.add(in.readLong());
        }
    }
}
