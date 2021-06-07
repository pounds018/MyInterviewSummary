package cn.pounds.netty.groupchat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @Date 2021/6/6 19:05
 * @Author by pounds
 * @Description Netty应用实例:
 * 1. 编写一个Netty群聊系统,实现服务器端和客户端之间的数据简单通讯(非阻塞)
 * 2. 实现多人聊天
 * 3. 服务端: 可以监听用户上线,离线,并实现消息转发功能
 * 4. 客户端: 通过channel可以无阻塞发送消息给其他用户,同时可以接收其他用户发来的消息
 */
public class NettyGroupChatServer {
    /** 服务端监听端口 */
    private int port ;

    public NettyGroupChatServer(int port) {
        this.port = port;
    }

    /**
     * 处理客户端请求的方法
     */
    public void run(){
        // 创建两个线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 默认8个NioEventLoop即8个线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {


            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    // 设置客户端channel中pipeline中的handler
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    // 解码器
                                    .addLast("decoder",new StringDecoder())
                                    // 编码器
                                    .addLast("encoder",new StringEncoder())
                                    // 添加真正的业务handler
                                    .addLast("bizHandler",new NettyGroupChatServerHandler());
                        }
                    });
            System.out.println("netty 服务端 get ready");

            // 绑定并启动服务端
            ChannelFuture bootStrapBindFuture = serverBootstrap.bind(port).sync();

            // 监听bootstrap关闭时间
            bootStrapBindFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
           bossGroup.shutdownGracefully();
           workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyGroupChatServer(6668).run();
    }
}
