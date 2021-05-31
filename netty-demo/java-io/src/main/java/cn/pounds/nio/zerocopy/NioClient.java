package cn.pounds.nio.zerocopy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * @Date 2021/5/9 12:43
 * @Author by pounds
 * @Description Nio中的零拷贝
 */
public class NioClient {
    public static void main(String[] args) throws IOException {
        SocketChannel client = SocketChannel.open();
        client.connect(new InetSocketAddress("localhost",7001));
        String file = "protoc-3.6.1-win32.zip";

        FileChannel fileChannel = new FileInputStream(file).getChannel();

        long startTime = System.currentTimeMillis();

        // windows系统中,transferTo方法每次只能传输8m,需要分段传输,linux系统则不需要,下面这个写法是linux的写法\
        // transferTo 就是使用的零拷贝
        fileChannel.transferTo(0,fileChannel.size(),client);

        System.out.println(String.format("总共耗时 %s ",System.currentTimeMillis() - startTime));

    }
}
