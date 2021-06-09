package cn.pounds.netty.codec.demo.protobuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Date 2021/5/9 19:24
 * @Author by pounds
 * @Description protobuf使用入门案例
 * 这个handler也可以实现 SimpleChannelInboundHandler实现ChannelRead0方法
 */
public class NettyProtobufClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * channel准备就绪就会触发这个方法,向服务端发送消息
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 将 protobuf生成的对象发送到服务端
        System.out.println("开始发送数据1");
        StudentPOJO.Student student = StudentPOJO.Student.newBuilder().setId(4).setName("豹子头林聪").build();
        // 发送数据
        ctx.writeAndFlush(student);
        ctx.fireChannelActive();
    }

    /**
     * 读事件
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("执行了");
//        ByteBuf byteBuf = (ByteBuf) msg;
//        System.out.println("服务器回复的消息: " + byteBuf.toString(CharsetUtil.UTF_8));
        System.out.println("服务器地址: " + ctx.channel().remoteAddress());
    }

    /**
     * 异常处理
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(String.format("出现了异常 [%s] : %s",cause.getMessage(),cause));
        ctx.close();
    }

}
