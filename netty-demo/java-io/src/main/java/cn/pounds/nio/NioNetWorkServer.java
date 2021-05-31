package cn.pounds.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @Date 2021/4/18 19:08
 * @Author by pounds
 * @Description NIO 网络编程快速入门
 * 要求:
 * 实现服务端和客户端之间的数据简单通讯
 *
 */
public class NioNetWorkServer {
    public static void main(String[] args) throws IOException {
        //1. 获取Selector
        Selector selector = Selector.open();
        //2. 开启serverSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //3. 绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(6666));
        //4. 将channel设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        //5. 注册serverSocketChannel,该channel只关心注册事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("注册后的selectionKey 数量=" + selector.keys().size());

        // 开始循环等待客户端链接
        while (true) {
            // 每一次都只阻塞监听端口1秒
            if (selector.select(1000) == 0 ){
                System.out.println("本次监听端口 6666 没有监听到任何事件 ");
                continue;
            }

            // 到这里表示监听到了事件,遍历keySet判断key的类型进行相应的处理
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            while (keyIterator.hasNext()){
                SelectionKey selectionKey = keyIterator.next();
                // 判断是不是监听事件的key
                if (selectionKey.isAcceptable()){
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    System.out.println("客户端连接成功 生成了一个 socketChannel " + socketChannel.hashCode());
                    // 将客户端channel也设置为非阻塞
                    socketChannel.configureBlocking(false);
                    // 注册客户端channel ,关注事件为 read,并绑定一个buffer
                    socketChannel.register(selector,SelectionKey.OP_READ,ByteBuffer.allocate(1024));
                    System.out.println("客户端连接后 ，注册的selectionkey 数量=" + selector.keys().size());
                    //上面sout的输出: 2,3,4..
                }
                // 判断是否是read是事件
                if (selectionKey.isReadable()){
                    SocketChannel channel = (SocketChannel)selectionKey.channel();
                    // 拿到关联的buffer
                    ByteBuffer buffer = (ByteBuffer)selectionKey.attachment();
                    // 往客户端的buffer里面写数据
                    channel.read(buffer);
                    System.out.println("form 客户端 " + new String(buffer.array()));
                }
                // 防止重复处理事件,及时删除事件
                keyIterator.remove();
            }
        }
    }
}
