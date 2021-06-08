package cn.pounds.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @Date 2021/6/8 21:55
 * @Author by pounds
 * @Description TODO
 */
public class NettyWebSocketServer {

    private String remoteHost;
    private int remotePort;

    public NettyWebSocketServer(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public void start(){
        try {
            NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
            NioEventLoopGroup workerGroup = new NioEventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 实现http长连接,需要http的编解码器
                            pipeline.addLast(new HttpServerCodec());
                            // 是以块方式写的,就是http请求中的chunked,所以需要添加chunkedWriterHandler
                            // chunkedWriterHandler是处理大数据传输的,不会占用大内存或导致内存一次的问题,它会维护管理大文件传输过程中时复杂的状态
                            pipeline.addLast(new ChunkedWriteHandler());
                            /*
                            1. http数据在传输大文件的时候是分段的,httpObjectAggregator,就是分段聚合的作用
                            2. 这就是为什么,当浏览器发送大量数据的时候,会发出多条http请求
                             */
                            pipeline.addLast(new HttpObjectAggregator(8192));
                            /*
                            1. webSocket是以帧的形式传递数据,就是数据链路层数据传输单位
                            2. webSocketFrame 下面有6个帧
                            3. 浏览器请求时, ws://localhost:7000/xxx 表示请求的uri ,xxx就是参数webSocketPath
                            .前端写webSocket对象需要按照webSocketPath参数来写.实际上这个webSocketPath参数就是资源路径,本例中就是hello.html的路径
                            4. WebSocketProtocolHandler 核心功能是将http协议升级为ws协议,保持长连接
                            5. WebSocketProtocolHandler是通过 状态码101 Switching Protocols,过程:
                                1. Connection: Upgrade该Connection头被设置为"Upgrade"以表示的升级要求。Upgrade:
                                protocols所述Upgrade标头指定的一个或多个以逗号分隔的协议名称。
                                2. 检查服务器是否支持客户端所需要的协议。
                                3. 服务器可拒绝升级,在这种情况下，它发送回一个普通。
                                4. 或接受升级，在这种情况下，它会发送一个"101 Switching
                                Protocols"带有升级标头的指定所选协议的标头。

                            */
                            pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));

                            /*
                            自定义一个handler做业务处理
                             */
                            pipeline.addLast(new MyWebSocketFrameHandler());
                        }
                    });
            ChannelFuture bindFuture = serverBootstrap.bind(remoteHost, remotePort).sync();
            bindFuture.channel().closeFuture().sync();
        }catch (Exception e) {

        } finally{

        }
    }

    public static void main(String[] args) {
        new NettyWebSocketServer("127.0.0.1",7000).start();
    }
}
