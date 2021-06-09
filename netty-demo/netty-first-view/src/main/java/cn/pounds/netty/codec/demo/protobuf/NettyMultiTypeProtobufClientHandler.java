package cn.pounds.netty.codec.demo.protobuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.Random;

/**
 * @Date 2021/6/9 22:44
 * @Author by pounds
 * @Description TODO
 */
public class NettyMultiTypeProtobufClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * channel准备就绪就会触发这个方法,向服务端发送消息
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        int count = 3;
        while (--count > 0){
            System.out.println("开始发送数据"+(count));

            // 随机发送一个 Student 对象或者 Worker对象出去
            int nextInt = new Random().nextInt(3);
            System.out.println("nextInt : " + nextInt);
            MultiTypeDto.ControlMessage controlMessage = null;
            switch (nextInt){
                case 1:
                    controlMessage = MultiTypeDto.ControlMessage
                            .newBuilder()
                            .setDataType(MultiTypeDto.ControlMessage.DataType.StudentType)
                            .setStudent(MultiTypeDto.Student.newBuilder().setId(5).setName("玉麒麟 卢俊义").build())
                            .build();
                    break;
                default:
                    controlMessage = MultiTypeDto.ControlMessage
                            .newBuilder()
                            .setDataType(MultiTypeDto.ControlMessage.DataType.WorkerType)
                            .setWorker(MultiTypeDto.Worker.newBuilder().setAge(20).setName("黑旋风 李蛋儿").build())
                            .build();
            }
            // 发送数据
            ctx.write(controlMessage);
        }
        ctx.flush();
    }

    /**
     * 读事件
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("执行了");
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("服务器回复的消息: " + byteBuf.toString(CharsetUtil.UTF_8));
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
