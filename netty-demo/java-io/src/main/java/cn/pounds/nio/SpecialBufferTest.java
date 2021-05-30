package cn.pounds.nio;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * @Date 2021/4/18 17:50
 * @Author by pounds
 * @Description TODO
 */
public class SpecialBufferTest {
    /**
     * readOnlyBuffer
     */
    @Test
    public void test(){
        ByteBuffer normalBuffer = ByteBuffer.allocate(64);
        for (int i = 0; i < 64; i++) {
            normalBuffer.put((byte)i);
        }
        // 写切换为读
        normalBuffer.flip();
        // 转换只读
        ByteBuffer readOnlyBuffer = normalBuffer.asReadOnlyBuffer();
        readOnlyBuffer.put((byte)1);
    }

    /**
     * MappedByteBuffer.可以让文件不在内存中进行修改,直接就是在物理内存中修改文件
     */
    @Test
    public void test2() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("src/main/resources/NioOutPutFile.txt","rw");
        FileChannel channel = randomAccessFile.getChannel();
        /*
        参数1: 转换成的MappedByteBufferChannel的模式,示例为读写模式
        参数2: 可以修改内容的起始位置
        参数3: 可以修改内容的大小,即可以修改byte[]中从起始位置开始,多少个元素
         */
        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);
        mappedByteBuffer.put(0,(byte)'H');
        randomAccessFile.close();
    }

    /**
     * Scattering: 将数据写入到buffer时,可以采用buffer数组,依次写入
     * Gathering: 将buffer读取数据时,可以采用buffer数组,依次读取
     */
    @Test
    public void test3() throws IOException {
        //使用 ServerSocketChannel 和 SocketChannel 网络

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(7000);

        //绑定端口到socket ，并启动
        serverSocketChannel.socket().bind(inetSocketAddress);

        //创建buffer数组
        ByteBuffer[] byteBuffers = new ByteBuffer[2];
        byteBuffers[0] = ByteBuffer.allocate(5);
        byteBuffers[1] = ByteBuffer.allocate(3);

        //等客户端连接(telnet)
        SocketChannel socketChannel = serverSocketChannel.accept();
        int messageLength = 8;   //假定从客户端接收8个字节
        //循环的读取
        while (true) {

            int byteRead = 0;

            while (byteRead < messageLength ) {
                long l = socketChannel.read(byteBuffers);
                //累计读取的字节数
                byteRead += l;
                System.out.println("byteRead=" + byteRead);
                //使用流打印, 看看当前的这个buffer的position 和 limit
                Arrays.asList(byteBuffers).stream().map(buffer -> "postion=" + buffer.position() + ", limit=" + buffer.limit()).forEach(System.out::println);
            }

            //将所有的buffer进行flip
            Arrays.asList(byteBuffers).forEach(buffer -> buffer.flip());

            //将数据读出显示到客户端
            long byteWirte = 0;
            while (byteWirte < messageLength) {
                long l = socketChannel.write(byteBuffers);
                byteWirte += l;
            }

            //将所有的buffer 进行clear
            Arrays.asList(byteBuffers).forEach(buffer-> {
                buffer.clear();
            });

            System.out.println("byteRead:=" + byteRead + " byteWrite=" + byteWirte + ", messagelength" + messageLength);
        }

    }
}
