package cn.pounds.netty.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @Date 2021/5/9 18:08
 * @Author by pounds
 * @Description netty入门教程:
 * 1. 服务端端口号 6668, 接受客户端的消息,并返回消息"hello,客户端"
 * 2. 客户端连接服务端,发送消息"hello,服务端"
 */
public class NettyServer {
    public static void main(String[] args) {
        // 1. 创建两个NioEventLoopGroup,分别作为Boss Group 和  Worker Group
        // 两个Group的实际子线程NioEventLoop的个数为: 实际核心数 * 2
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 2.创建netty服务器的启动对象,主要是用来配置参数
            ServerBootstrap bootstrap = new ServerBootstrap();

            // 3. 配置参数
            bootstrap
                    // 设置parent group,child group
                    .group(bossGroup,workerGroup)
                    // 设置服务端通道,如同Nio中的serverSocketChannel
                    .channel(NioServerSocketChannel.class)
                    // 设置服务端channel等待连接队列的容量
                    .option(ChannelOption.SO_BACKLOG,128)
                    // 设置保持活动连接状态,因该是设置workerGroup的属性
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    // 设置真正执行业务逻辑的handler
                    .childHandler(
                            // 创建通道初始化对象,初始化的是socketChannel
                            new ChannelInitializer<SocketChannel>() {
                        /**
                         * 给pipeline设置一个handler
                         * @param ch --- SocketChannel
                         * @throws Exception
                         */
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 入门案例的演示
//                            ch.pipeline().addLast(new NettyServerHandler());
                            // task使用方式的演示
                            ch.pipeline().addLast(new NettyServerTaskQueueHandler());

                        }
                    });

            System.out.println(" ..........  server is ready ............");

            // 4. 将服务器绑定一个端口,并同步监听:  真正的启动服务器操作
            ChannelFuture channelFuture = bootstrap.bind(6668).sync();

            // future-listener机制举个栗子,给端口绑定事件添加一个监听器:
            // 操作(端口绑定事件)完成触发
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (channelFuture.isSuccess()){
                    System.out.println("绑定成功");
                }else{
                    System.out.println("绑定端口失败.");
                }
            });

            // 5.  监听服务器channel关闭事件
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
