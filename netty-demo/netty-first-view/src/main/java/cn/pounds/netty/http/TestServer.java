package cn.pounds.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;


/**
 * @Date 2021/5/30 17:37
 * @Author by pounds
 * @Description netty入门教程2: netty实现一个简单的http服务
 * 1. netty监听 6668端口,浏览器发出请求 "http://localhost:6668"
 * 2. netty向浏览器回复"hello moto",并对特殊资源进行过滤
 * 目的: 体验netty实例做http服务开发,理解handler实例和客户端及其请求的关系
 */
public class TestServer {
    public static void main(String[] args) {
        // 1. 创建boss和worker事件循环组:
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 2. 创建服务端netty的启动对象serverBootstrap
            ServerBootstrap bootstrap = new ServerBootstrap();

            // 3.设置启动参数
            bootstrap
                    // 父子group
                    .group(bossGroup,workerGroup)
                    // 服务端通道
                    .channel(NioServerSocketChannel.class)
                    // 服务端channel等待连接队列的容量
                    .option(ChannelOption.SO_BACKLOG,128)
                    // 设置客户端连接时候一直保持连接
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    // 设置真正实行业务逻辑的handler
                    .childHandler(
                         // 创建客户端通道初试话对象,提成了一个类,不再写成匿名内部类的形式
                        new TestServerInitializer()
                    );
            System.out.println(" ........... server is ready ...........");

            // 4. 绑定服务器端口,并同步监听端口绑定事件,服务channel关闭事件:这里才是server启动的时候
            ChannelFuture channelFuture = bootstrap.bind(10086).sync();

            channelFuture.channel().closeFuture().sync();

        }catch (Exception e) {

        } finally{

        }
    }
}
/**
 * 一些问题:
 * 浏览器发过来的请求会有两次:
 *  msg 类型=class io.netty.handler.codec.http.DefaultHttpRequest
 *  客户端地址/127.0.0.1:63366
 *  msg 类型=class io.netty.handler.codec.http.DefaultHttpRequest
 *  客户端地址/127.0.0.1:63366
 *  原因:
 * 1次请求服务器,2次请求favicon.ico
 *
 * 每一个浏览器在服务端对应的pipeline和handler都是唯一的一个.
 */
