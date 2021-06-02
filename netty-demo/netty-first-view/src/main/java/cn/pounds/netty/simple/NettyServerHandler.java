package cn.pounds.netty.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;


/**
 * @Date 2021/5/9 18:47
 * @Author by pounds
 * @Description netty服务器端真正业务处理类
 * 几点说明:
 * 1. 我们自定义的handler 需要继承netty规定好的某个HandlerAdapter,比如下面继承这个
 * 2. 这是自定义handler的方式,也可以使用netty自带的handler
 * 3. 需要重写一些方法
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * handler的read 方法,处理read事件的方法
     * @param ctx ---  上下文对象,包含 pipeline,通道,地址
     * @param msg --- 客户端的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("server ctx= " + ctx);
        // 将msg转换成netty提供的 ByteBuf ,功能与nio ByteBuffer差不多,但是性能更好
        ByteBuf byteBuf = (ByteBuf) msg;
        // 读取数据展示,附带解码流程
        System.out.println("客户端发送消息是: "+byteBuf.toString(CharsetUtil.UTF_8));
        System.out.println("客户端地址: "+ ctx.channel().remoteAddress());
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
