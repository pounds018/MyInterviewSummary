package cn.pounds.netty.simple;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

/**
 * @Date 2021/5/9 21:58
 * @Author by pounds
 * @Description taskQueue中task的几种使用方式的演示
 * 1. 用户程序自定义的普通任务
 * 2. 用户自定义定时任务
 * 3. 非当前Reactor线程调用 channel 的各种方法
 *      i. 例如在推送系统的业务线程里面,根据用户标识,找到对应的channel引用,然后调用write类方法
 *      先该用户推送消息,就会进入这种场景.最终的write会提交到任务队列中后被异步消费
 *      实现: 在initChannel的时候,将channel和用户绑定起来,通过ThreadLocal或者使用集合缓存
 */
public class NettyServerTaskQueueHandler extends ChannelInboundHandlerAdapter {
    /**
     * handler的read 方法,处理read事件的方法,演示task用法1
     * @param ctx ---  上下文对象,包含 pipeline,通道,地址
     * @param msg --- 客户端的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        long start = System.currentTimeMillis();
        // 1. 用户自定义的长耗时任务 -> 需要异步执行 -> 实现方式: 提交任务到该channel 对应的NioEventLoop的taskQueue中
        ctx.channel().eventLoop().execute(() -> {
            try {

                Thread.sleep(10 * 1000);
                ctx.writeAndFlush(Unpooled.copiedBuffer(start + " -> 用户自定义普通任务",CharsetUtil.UTF_8));
            }catch (Exception e) {
                e.printStackTrace();
            }
        });
        // 验证队列中任务处理的方式,结果显示  只有一个线程在处理队列中的任务
        ctx.channel().eventLoop().execute(() -> {
            try {
                Thread.sleep(20 * 1000);
                long end = System.currentTimeMillis();
                // 客户端显示   服务器回复的消息: 30011 -> 用户自定义普通任务2
                ctx.writeAndFlush(Unpooled.copiedBuffer(end - start + " -> 用户自定义普通任务2",CharsetUtil.UTF_8));
            }catch (Exception e) {
                e.printStackTrace();
            }
        });


        // 2. 用户自定义定时任务,
        // schedule方法只是延迟多少时间执行,并且如果前面有任务执行时间超过了延迟时间,前面任务执行完会立即执行定时任务队列中的任务
        // 定时任务要使用atFix之类的方法
        ctx.channel().eventLoop().schedule(()->{
            try {
//                Thread.sleep(5 * 1000);
                long end = System.currentTimeMillis();
                // 客户端显示   服务器回复的消息: 30011 -> 用户自定义普通任务2
                ctx.writeAndFlush(Unpooled.copiedBuffer(end - start + " -> 用户自定义定时任务",CharsetUtil.UTF_8));
            }catch (Exception e) {
                e.printStackTrace();
            }
        },5, TimeUnit.SECONDS);

        System.out.println("  time goes on ..... ");

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
