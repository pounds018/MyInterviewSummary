package cn.pounds.netty.heartbeat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @Date 2021/6/7 22:31
 * @Author by pounds
 * @Description TODO
 */
public class HearBeatServer {

    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 服务端的日志handler
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            /**
                             * IdleStateHandler --- netty提供的处理空闲状态的处理器,心跳机制就是靠这个实现的
                             *             int readerIdleTimeSeconds: 表示多长时间没有读,就会发送一个心跳包给客户端,去检测连接还在不
                             *             int writerIdleTimeSeconds: 表示多长时间没有写, 就会发送一个心跳包给客户端,去检测连接还在不
                             *             int allIdleTimeSeconds: 表示多长时间没有读或者写,就会发送一个心跳检测包给客户端,去检测连接还在不
                             * idleStateHandler实际上只是起到触发 idleStateEvent事件的作用,在触发之后传递给管道的下一个handler去处理.
                             * 处理方式: 通过下一个handler的UserEventTriggered方法来处理 todo 这里好像有问题,如果有数据触发的也是这个方法吗?
                             * 空闲事件发生先后顺序: 是按照配置的时间来的.
                             */
                            pipeline.addLast("heartBeat",new IdleStateHandler(3,5,7, TimeUnit.SECONDS));
                            /**
                             * 加入一个处理idleStateEvent的handler
                             */
                            pipeline.addLast(new HearBeatEventHandler());
                        }
                    });
            // 启动
            ChannelFuture bindFuture = bootstrap.bind("127.0.0.1", 6668).sync();
            bindFuture.channel().closeFuture().sync();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
