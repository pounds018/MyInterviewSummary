package cn.pounds.action.chapter2_echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * @Date 2021/6/22 0:14
 * @Author by pounds
 * @Description TODO
 */
public class EchoClient {

    private final String addr ;
    private final int port ;

    public EchoClient(String addr ,int port) {
        this.addr = addr;
        this.port = port;
    }

    private void start() {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(eventExecutors)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(addr,port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new EchoClientHandler());
                        }
                    });

            ChannelFuture channelFuture = b.connect().sync();
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e) {
            e.printStackTrace();
        } finally{
            eventExecutors.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        if (args.length != 2){
            System.err.println("Usage"+ EchoClient.class.getSimpleName()+ "<host><port>");
        }
        String addr = args[0];
        int port = Integer.parseInt(args[1]);
        new EchoClient(addr,port).start();

    }
}
