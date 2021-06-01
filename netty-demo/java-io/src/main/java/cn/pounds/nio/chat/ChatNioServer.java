package cn.pounds.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @Date 2021/5/5 20:56
 * @Author by pounds
 * @Description NIO非阻塞网络编程机制的联系
 * 1. 编写一个NIO群聊系统,实现服务器端与客户端之间的数据简单通讯(非阻塞)
 * 2. 实现多人群聊
 * 3. 服务器端: 可以监听用户上线,离线,并实现消息转发功能
 * 4. 客户端: 通过channel可以无阻塞发送消息给其他所有用户,同时可以收其他用户发送的消息(由服务器转发)
 *
 */
public class ChatNioServer {
    private static final int PORT = 6667;
    private Selector selector;
    private ServerSocketChannel listenChannel;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public ChatNioServer() {
        try {
            // 初始化selector
            selector = Selector.open();
            // 初始化serverSocketChannel
            listenChannel = ServerSocketChannel.open();
            listenChannel.configureBlocking(false);
            listenChannel.bind(new InetSocketAddress(PORT));
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void listen(){
        try {
            // 循环监听
            while (true){
                int eventCount = selector.select(2000);
                if (eventCount > 0){
                    // 遍历key处理事件
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> selectedKeyIter = selectedKeys.iterator();
                    while (selectedKeyIter.hasNext()){
                        // 获取key
                        SelectionKey currentKey = selectedKeyIter.next();

                        // accept事件
                        if (currentKey.isAcceptable()){
                            SocketChannel socketChannel = listenChannel.accept();
                            socketChannel.configureBlocking(false);
                            // 注册为可读事件
                            socketChannel.register(selector,SelectionKey.OP_READ);
                            String currentTime = DATE_FORMAT.format(new Date());
                            String userName = socketChannel.getRemoteAddress().toString().substring(1);
                            System.out.println(String.format("[%s] : %s ,上线了",currentTime,
                                   userName ));
                        }

                        // read事件
                        if (currentKey.isReadable()){
                            readData(currentKey);
                        }

                        // 删除currentKey防止重复操作
                        selectedKeyIter.remove();
                    }
                }else{
                    // 没有事件需要处理
                    System.out.println("当前没有事件需要处理,等待事件中.........");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }
    }

    /**
     * 读取客户端消息
     */
    private void readData(SelectionKey key){
        SocketChannel channel = null;
        try {
            channel = (SocketChannel) key.channel();
            // 创建buffer
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int count = channel.read(buffer);
            if (count > 0){
                String msg = new String(buffer.array());
                // 输出消息
                System.out.println(msg);

                sendInfo2Other(msg,channel);
            }
        }catch (Exception e){
            try {
                String currentTime = DATE_FORMAT.format(new Date());
                // 输出消息
                String userName = channel.getRemoteAddress().toString().substring(1);
                System.out.println(String.format("[%s] %s: 离线了",currentTime,
                        userName));
                // 取消注册
                key.cancel();
                // 关闭channel
                channel.close();
            }catch (Exception e1){
                e.printStackTrace();
            }
        }
    }

    private void sendInfo2Other(String msg, SocketChannel self) throws IOException {
        String currentTime = DATE_FORMAT.format(new Date());
        String userName = self.getRemoteAddress().toString().substring(1);
        // 输出消息
        System.out.println(String.format("[%s] %s: 服务器转发消息中.......",currentTime,
                userName));
        selector.keys().forEach(selectionKey -> {
            Channel targetChannel = selectionKey.channel();
            if (targetChannel instanceof SocketChannel && targetChannel != self){
                SocketChannel dest = (SocketChannel) targetChannel;
                // 将msg 存储到buffer
                ByteBuffer wrapBuffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
                try {
                    dest.write(wrapBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        ChatNioServer chatNioServer = new ChatNioServer();
        chatNioServer.listen();
    }
}
