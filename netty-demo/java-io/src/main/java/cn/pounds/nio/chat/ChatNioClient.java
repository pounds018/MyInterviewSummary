package cn.pounds.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @Date 2021/5/5 21:56
 * @Author by pounds
 * @Description 群聊系统客户端
 */
public class ChatNioClient {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private final String HOST = "127.0.0.1";
    private final int PORT = 6667;
    private Selector selector;
    private SocketChannel socketChannel;
    private String userName;

    public ChatNioClient() throws IOException {
        selector = Selector.open();
        // 连接服务器
        socketChannel = SocketChannel.open(new InetSocketAddress(HOST,PORT));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        userName = socketChannel.getLocalAddress().toString().substring(1);
        System.out.println(userName + " is ok");
    }

    /**
     * 向服务端发送消息
     * @param msg
     */
    public void sendInfo2Server(String msg){
        String currentTime = DATE_FORMAT.format(new Date());
        msg = String.format("[%s] %s : %s",currentTime,userName,msg);

        try{
            socketChannel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 从服务端读取消息
     */
    public void readInfoFromServer(){
        try{
            int count = selector.select();
            if (count > 0){
                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()){
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    if (selectionKey.isReadable()){
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        channel.read(byteBuffer);
                        System.out.println(new String(byteBuffer.array()));
                    }
                    selectionKeyIterator.remove();
                }
            }else {
                System.out.println("当前没有可读通道,无消息可读");
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ChatNioClient chatNioClient = new ChatNioClient();
        new Thread(){
            @Override
            public void run() {
                while (true){
                    chatNioClient.readInfoFromServer();
                    try {
                        // 控制三秒钟读取一次
                        Thread.sleep(3000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        // 向服务端发送数据
        Scanner scanner = new Scanner(System.in);

        while(scanner.hasNext()){
            String msg = scanner.nextLine();
            chatNioClient.sendInfo2Server(msg);
        }
    }
}
