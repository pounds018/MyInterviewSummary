package cn.pounds.netty.groupchat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * @Date 2021/6/6 21:44
 * @Author by pounds
 * @Description TODO
 */
public class NettyGroupChatClient {
    private String host;
    private int port;

    public NettyGroupChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run(){
        NioEventLoopGroup clientGroup = new NioEventLoopGroup();
        try {

            Bootstrap bootstrap = new Bootstrap()
                    .group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("decoder", new StringDecoder()).addLast("encoder", new StringEncoder()).addLast(
                                    "clientHandler", new NettyGroupChatClientHandler());
                        }
                    });

            // 启动客户端,sync()
            ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, port));

            // todo 把连接监听器,添加到ChannelFuture里面去,实现启动重连
            connectFuture.addListener(new ConnectionListener());
//            connectFuture.sync();
            // 实现输入消息并发布
            Channel clientChannel = connectFuture.channel();
            System.out.println(" -------------- "+clientChannel.localAddress()+" -------------- ");
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()){
                String msg = scanner.nextLine();
                clientChannel.writeAndFlush(msg + "\r\n");
            }

            clientChannel.closeFuture().sync();
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            clientGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) {
        new NettyGroupChatClient("127.0.0.1",6668).run();
    }
}
