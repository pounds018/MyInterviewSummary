package cn.pounds.netty.heartbeat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @Date 2021/6/7 22:56
 * @Author by pounds
 * @Description TODO
 */
public class HearBeatEventHandler extends ChannelInboundHandlerAdapter {

    /**
     *
     * @param ctx ---- 上下文
     * @param evt ----  触发的事件
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 判断触发事件是什么类型
        if (evt instanceof IdleStateEvent){
            // 向下转型
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            // 在对应的分支做相应的逻辑处理
            switch (idleStateEvent.state()){
                case READER_IDLE:
                    System.out.println("发生了读空闲事件.........");
                    break;
                case WRITER_IDLE:
                    System.out.println("发生了写空闲事件.........");
                    break;
                case ALL_IDLE:
                    System.out.println("发生了读写空闲事件.........");
                    break;
            }
        }
    }
}
