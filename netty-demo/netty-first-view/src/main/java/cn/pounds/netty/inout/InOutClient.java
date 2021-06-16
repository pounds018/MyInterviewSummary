package cn.pounds.netty.inout;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * @Date 2021/6/16 23:17
 * @Author by pounds
 * @Description TODO
 */
public class InOutClient {
    public static void main(String[] args) {
        // 1. 创建客户端事件循环组
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();

        try {
            // 2. 创建启动对象,注意: 要使用netty的包,并且不是的ServerBootStrap
            Bootstrap bootstrap = new Bootstrap();
            // 3. 设置属性
            bootstrap
                    // 设置事件组
                    .group(eventExecutors)
                    // 设置通道
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(6668))
                    // 设置handler
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new MyByteToLongEncoder());
                            p.addLast(new MyClientHandler());
                        }
                    });
            System.out.println(" 客户端准备完毕 ......");

            // 4. 启动客户端,并同步监听
            ChannelFuture channelFuture = bootstrap.connect().sync();
            // 5. 同步监听socketChannel 关闭
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e) {
            e.printStackTrace();
        } finally{
            eventExecutors.shutdownGracefully();
        }
    }
}
