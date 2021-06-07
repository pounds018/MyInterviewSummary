package cn.pounds.netty.groupchat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Date 2021/6/7 21:43
 * @Author by pounds
 * @Description netty 启动重连实例
 */
public class NettyReConnectExampleClient {

    private volatile NioEventLoopGroup clientGroup;
    private volatile Bootstrap bootstrap;
    private volatile boolean closed = false;
    private volatile AtomicInteger retryTime = new AtomicInteger(0);
    private String remoteHost = "";
    private int port = 0;

    public NettyReConnectExampleClient(String remoteHost, int port) {
        this.remoteHost = remoteHost;
        this.port = port;
    }

    /**
     * 客户端启动入口,在连接服务端的以后一定不要调用 ChannelFuture的sync方法,让异步变为同步
     * 如果调用了无法实现重连,而是捕捉到sync()抛出的连接事件异常,然后中断程序
     */
    public void run(){
        closed = false;

        clientGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap()
                .group(clientGroup)
                .channel(NioSocketChannel.class)
                // 这里实现的是中途断线重连,断线换成netty术语就是channel不活跃了
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        // 匿名内部类实现handler
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            // channel在运行过程中中断,就会触发这个(channelInactive)方法,然后通过这个方法去重新连接
                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                ctx.channel().eventLoop().schedule(()-> doConnect(),2, TimeUnit.SECONDS);
                            }
                        });
                    }
                });
        // 真正的连接操作
        doConnect();
    }

    public void doConnect(){
        if (closed){
            return;
        }

        // 限制重连次数
        if (retryTime.intValue() > 15){
            System.out.println(" 多次尝试连接失败, 不再尝试连接............");
            return;
        }

        retryTime.incrementAndGet();

        // 注意这里一定不要调用sync()
        ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(remoteHost, port));
        // 添加链接事件的监听器,当连接事件触发的时候就会调用监听器里面的operationComplete方法
        connectFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()){
                System.out.println(" 连接成功........");
            }else{
                System.out.println(String.format(" 连接失败,正在尝试重连 ..... 当前为 第 %s 次重连",retryTime.intValue()));
                future.channel().eventLoop().schedule(()->doConnect(),1,TimeUnit.SECONDS);
            }
        });

    }

    public void close() {
        closed = true;
        clientGroup.shutdownGracefully();
        System.out.println("Stopped Tcp Client: " + getServerInfo());
    }

    private String getServerInfo() {
        return String.format("RemoteHost=%s RemotePort=%d",
                remoteHost,
                port);
    }

    /**
     * 启动测试
     */
    public static void main(String[] args) {
        new NettyReConnectExampleClient("127.0.0.1",6668).run();
    }
}
