package cn.pounds.netty.groupchat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.TimeUnit;

/**
 * @Date 2021/6/6 21:54
 * @Author by pounds
 * @Description TODO
 */
public class NettyGroupChatClientHandler extends SimpleChannelInboundHandler<String> {
    private NettyGroupChatClient chatClient = new NettyGroupChatClient("127.0.0.1",6668);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(" 掉线了.....");
        ctx.channel().eventLoop().schedule(()->{
            chatClient.run();
        },1, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg.trim());
    }

}
