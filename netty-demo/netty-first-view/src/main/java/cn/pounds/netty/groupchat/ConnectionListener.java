package cn.pounds.netty.groupchat;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

import java.util.concurrent.TimeUnit;

/**
 * @Date 2021/6/6 22:16
 * @Author by pounds
 * @Description 启动时断线重连
 * 实现思路:
 * 1. 创建一个监听器,实现ChannelFutureListener接口,重写operationComplete方法
 * 2. 判断监听的事件是否成功,不成功就开始重连
 * 3. 重连是通过channel所属的eventLoop,调用schedule去定时执行
 * 4. 在定时任务中通过channel去connect.
 * 5. 将写好的监听器在客户端启动代码中添加到ChannelFuture里面去
 */
public class ConnectionListener implements ChannelFutureListener {
    private static final NettyGroupChatClient clientGroup = new NettyGroupChatClient("127.0.0.1",6668);
    @Override
    public void operationComplete(ChannelFuture future) {
        // 监听到的事件没有成功
        if (!future.isSuccess()){
            // 拿到channel所属的eventLoop
            final EventLoop eventLoop = future.channel().eventLoop();
            // eventLoop本身就是一个定时任务线程池,通过它的定时任务方法去反复执行
            eventLoop.schedule(()->{
                System.out.println(" 启动时连接服务器失败 .... 正在尝试连接");
                try {
                    clientGroup.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },1, TimeUnit.SECONDS);
        } else {
            System.out.println(" 11111 连接服务端成功");
        }
    }
}
