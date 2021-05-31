package cn.pounds.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Date 2021/5/5 17:31
 * @Author by pounds
 * @Description NIO 网络编程快速入门
 * 要求:
 *      实现服务端和客户端之间的数据简单通讯
 */
public class NioClient {
    public static void main(String[] args) {
        try {
            // 获取client端的channel
            SocketChannel socketChannel = SocketChannel.open();
            // 设置非阻塞
            socketChannel.configureBlocking(false);
            // 提供服务端的ip和端口
            InetSocketAddress serverInetSocketAddress = new InetSocketAddress("127.0.0.1", 6666);

            // 连接服务端
            if (!socketChannel.connect(serverInetSocketAddress)) {
                while (!socketChannel.finishConnect()) {
                    System.out.println("连接失败,但是客户端不会阻塞,可以做些其他工作");
                }
            }
            // 连接成功,发送数据
            String info = "hello netty";
            ByteBuffer byteBuffer = ByteBuffer.wrap(info.getBytes());
            socketChannel.write(byteBuffer);

            // 让代码停止
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
