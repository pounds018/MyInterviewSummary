package cn.pounds.netty.tcpstickunpack;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * @Date 2021/6/16 22:59
 * @Author by pounds
 * @Description TODO
 */
public class MyServer {

    public static void main(String[] args) {
        try {
            NioEventLoopGroup bos = new NioEventLoopGroup(1);
            NioEventLoopGroup wor = new NioEventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(bos,wor)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(6668))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new MyServerHandler());
                        }
                    });

            ChannelFuture bindFuture = serverBootstrap.bind().sync();
            bindFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
