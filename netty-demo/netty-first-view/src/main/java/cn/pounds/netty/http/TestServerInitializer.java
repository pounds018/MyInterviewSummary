package cn.pounds.netty.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * @Date 2021/5/30 17:37
 * @Author by pounds
 * @Description TODO
 */
public class TestServerInitializer extends ChannelInitializer<SocketChannel> {
    /**
     * 添加handler的时候,一定不要把initializer添加成handler了,否则要出错,比如下面这个:
     * pipeline.addLast("MyTestHttpServerHandler",new TestServerInitializer());
     * @param ch
     * @throws Exception
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 获取管道
        ChannelPipeline pipeline = ch.pipeline();
        // 获取http编码解码器,name属性是给这个对象设定名称
        pipeline.addLast("MyHttpServerCodec1",new HttpServerCodec());
        // 向管道加入handler
        pipeline.addLast("MyTestHttpServerHandler",new TestHttpServerHandler());

        System.out.println("ok~~~~");
    }
}
