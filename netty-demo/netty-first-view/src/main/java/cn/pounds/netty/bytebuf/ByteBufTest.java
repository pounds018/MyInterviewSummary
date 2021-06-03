package cn.pounds.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.nio.charset.Charset;

/**
 * @Date 2021/6/3 22:24
 * @Author by pounds
 * @Description netty ByteBuf 相关功能演示类
 */
public class ByteBufTest {
    /**
     * netty的byteBuf会自动切换读取和写入模式.
     * 原因是ByteBuf底层维护了 readerIndex和writerIndex两个属性
     * 初始情况下各个属性的位置
     * / ------------ byte array ------------- /
     * 0                                       capacity
     * readerIndex
     * writerIndex
     * ps:
     * writeByte()会造成 writerIndex++
     * readByte()会造成 readerIndex++ ,如果readIndex + 1 > writerIndex会报数组越界异常.
     */
    @Test
    public void test(){
        // 创建一个ByteBuf,实际底层就是一个byte数组
        ByteBuf buffer = Unpooled.buffer(10);

        // 写入数据
        for (int i = 0; i < 11; i++) {
            buffer.writeByte(i);
        }

        // 读取数据
        for (int i = 0; i < buffer.capacity(); i++) {
            // 读取有两种读法

            // 1.getByte(index)不会影响readerIndex
            System.out.println(buffer.getByte(i));
            System.out.println("index ->"+buffer.readerIndex());
            System.out.println(buffer.readByte());
            System.out.println("index ->" + buffer.readerIndex());

            System.out.println(" --------------------------- ");
        }

    }


    @Test
    public void Test() {
        // 通过netty提供的工具类Unpooled获取Netty的数据容器,ByteBuf
        ByteBuf byteBuf = Unpooled.copiedBuffer("hello netty", Charset.forName("utf-8"));

        // 相关方法
        // public abstract boolean hasArray();判断ByteBuf的使用模式(hasArray判断的是 byteBuf是否在堆空间有一个支撑数组),
        // 即数据存放的位置在哪儿 堆/直接缓存区/复合缓冲区
        if (byteBuf.hasArray()){
            //  获取byteBuf的支撑数组
            byte[] array = byteBuf.array();

            // 把字节数组转换成字符串
            System.out.println( new String(array,Charset.forName("utf-8")));

            // 获取支撑数组的一些信息:
            // 获取支撑数组的偏移量
            System.out.println(byteBuf.arrayOffset());
            // 获取支撑数组的可读索引位置
            System.out.println(byteBuf.readerIndex());
            // 获取支撑数组的可写索引位置
            System.out.println(byteBuf.writerIndex());
            // 获取支撑数组的容量
            System.out.println(byteBuf.capacity());
            // 获取支撑数组中剩余可读元素占多少个字节,这个的大小是相对于readerIndex位置的,
            // 比如下面这个读取方法会导致readerIndex的移动,从而导致readableBytes()变化
            System.out.println(byteBuf.readByte());
            // 但是getByte方法是不会造成readerIndex移动的
            System.out.println(byteBuf.getByte(1));
            System.out.println(byteBuf.readableBytes());
        }
    }
}
