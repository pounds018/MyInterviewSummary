package cn.pounds.netty.groupchat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @Date 2021/6/6 19:25
 * @Author by pounds
 * @Description bizHandler
 */
public class NettyGroupChatServerHandler extends SimpleChannelInboundHandler<String> {
    private static Map<String,Channel> channelMap = new ConcurrentHashMap<>(64);
    /**
     * channelGroup通常是用于群发通知
     * channel组用于管理所有的channel,因为是管理所有channel的所以将其定义为static共享channelGroup属性
     * GlobalEventExecutor.INSTANCE 是一个全局的事件执行器,单例对象
     */
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    /**
     * 转换时间使用的
     */
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * handlerAdd, 一旦有客户端连接建立,就会首先执行这个方法
     * 在这个例子中,这个方法是用来:
     * 1. 将客户端channel放入channelGroup中的.
     * 2. 通知其他客户端有客户端连接进来了
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel curChannel = ctx.channel();
        // 通知其他客户端,内部实现就是遍历所有channel,进行发送
        channelGroup.writeAndFlush(String.format("客户端 %s [%s]: 加入聊天室\n",
                curChannel.remoteAddress(),sdf.format(new Date())));

        // 加入group中
        channelGroup.add(curChannel);

        // 键入点对点的管理
        channelMap.put(curChannel.remoteAddress().toString() ,curChannel);
    }

    /**
     * channel激活就会调用这个方法
     * 向服务端提示 xxx上线
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.printf("客户端 %s [%S]: 上线了\n",ctx.channel().remoteAddress(),sdf.format(new Date()));
    }

    /**
     * channel不活跃就会调用这个方法
     * 向服务端提示 xxx离线
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.printf("客户端 %s [%S]: , 上线了\n",ctx.channel().remoteAddress(),sdf.format(new Date()));
    }

    /**
     * 当channel断开连接就调用这个方法,channelGroup会自动移除调用这个方法的channel
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        ArrayList<Channel> list = new ArrayList<>();
        // 验证channelGroup会不会移除调用这个方法的channel
        for (Channel channel : channelGroup) {
            list.add(channel);
        }
        System.out.println("移除之前 : "+ list);
        channelGroup.writeAndFlush(String.format("%s [%s]: 离开了聊天室\n",
                ctx.channel().remoteAddress(),sdf.format(new Date())));

    }

    /**
     * 真正处理业务逻辑的方法
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // 把当前channel发送过来的消息转发到其他channel
        Channel curChannel = ctx.channel();
//        channelGroup.forEach(ch -> {
//            // 不是当前channel(发消息的channel),就向对方发送当前channel要发送的消息
//            if (ch != curChannel){
//                // 向对方发送,就是用对方的channel去写入消息
//                ch.writeAndFlush(String.format("%s [%s]: %s\n",ctx.channel().remoteAddress(),sdf.format(new Date()),
//                        msg));
//            }
//        });
        // 群聊系统应该是都能看到消息的
        channelGroup.forEach(channel -> {
                channel.writeAndFlush(String.format("%s [%s]: %s\n",curChannel.remoteAddress(),
                        sdf.format(new Date()),
                        msg));
        });
        System.out.println(String.format("%s [%s]: %s\n",curChannel.remoteAddress(),
                sdf.format(new Date()),
                msg));
    }

}
