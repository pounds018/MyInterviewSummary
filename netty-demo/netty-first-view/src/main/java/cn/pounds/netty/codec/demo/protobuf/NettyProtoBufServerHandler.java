package cn.pounds.netty.codec.demo.protobuf;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;


/**
 * @Date 2021/5/9 18:47
 * @Author by pounds
 * @Description netty服务器端真正业务处理类
 */
public class NettyProtoBufServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * handler的read 方法,处理read事件的方法
     * @param ctx ---  上下文对象,包含 pipeline,通道,地址
     * @param msg --- 客户端的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        StudentPOJO.Student student = (StudentPOJO.Student) msg;

        System.out.println(
                "客户端发送的数据为 student , id = " + student.getId() + " , name = " + student.getName()
        );
    }

    /**
     * handler的write方法,会在数据读完之后触发,也就是read处理完之后触发
     * @param ctx --- 上下文对象
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 写入缓存并发送出去,返回的数据需要进行编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello 客户端~ ",CharsetUtil.UTF_8));
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
