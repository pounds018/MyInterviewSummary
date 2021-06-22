package cn.pounds.action.chapter2_echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * @Date 2021/6/21 23:33
 * @Author by pounds
 * @Description netty实战第二章EchoServer
 */
public class EchoSever {
    private final int port;

    public EchoSever(int port){
        this.port = port;
    }

    public static void main(String[] args) {
        if (args.length != 1){
            System.err.println("Usage"+ EchoSever.class.getSimpleName() + "<port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new EchoSever(port).start();
    }

    private void start() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new EchoServerHandler());
                        }
                    });

            ChannelFuture future = serverBootstrap.bind().sync();
            future.channel().closeFuture().sync();
        }catch (Exception e) {
            e.printStackTrace();
        } finally{
            bossGroup.shutdownGracefully();
        }
    }
}
