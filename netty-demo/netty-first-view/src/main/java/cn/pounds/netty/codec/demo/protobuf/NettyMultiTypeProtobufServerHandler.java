package cn.pounds.netty.codec.demo.protobuf;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * @Date 2021/6/9 23:45
 * @Author by pounds
 * @Description TODO
 */
public class NettyMultiTypeProtobufServerHandler extends SimpleChannelInboundHandler<MultiTypeDto.ControlMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MultiTypeDto.ControlMessage msg) throws Exception {
        MultiTypeDto.ControlMessage.DataType dataType = msg.getDataType();
        if (dataType == MultiTypeDto.ControlMessage.DataType.StudentType){
            System.out.println(
                    "客户端发送的数据为 student , id = " + msg.getStudent().getId() + " , name = " + msg.getStudent().getName()
            );
        }else{
            System.out.println(
                    "客户端发送的数据为 student , id = " + msg.getWorker().getAge() + " , name = " + msg.getWorker().getName()
            );
        }
    }


    /**
     * handler的write方法,会在数据读完之后触发,也就是read处理完之后触发
     * @param ctx --- 上下文对象
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 写入缓存并发送出去,返回的数据需要进行编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello 客户端~ ", CharsetUtil.UTF_8));
    }

    /**
     * 异常处理
     * @param ctx --- 上下文对象
     * @param cause --- 异常
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
