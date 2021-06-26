package cn.pounds.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
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

    /**
     * 直接缓冲区
     */
    @Test
    public void testDirect(){
        ByteBuf directBuf = Unpooled.directBuffer();
        // public abstract boolean hasArray();判断ByteBuf的使用模式(hasArray判断的是 byteBuf是否在堆空间有一个支撑数组),
        // 如果不是那么就是直接缓冲区
        if (!directBuf.hasArray()){
            int len = directBuf.readableBytes();
            byte[] bytes = new byte[len];
            directBuf.getBytes(directBuf.readerIndex(),bytes);
            // 业务逻辑处理
//            handleArray(bytes,0,len);
        }
    }

    /**
     * 复合缓冲区
     */
    @Test
    public void testComposite(){
        CompositeByteBuf composited = Unpooled.compositeBuffer();
        // 也可以是堆缓冲区
        ByteBuf headerBuf = Unpooled.directBuffer();
        // 也可以是直接缓冲区,buffer()返回的是一个堆缓冲区
        ByteBuf bodyBuf = Unpooled.buffer();

        // 添加缓冲区到复合缓冲区
        composited.addComponents(headerBuf,bodyBuf);

        //  .... 业务逻辑

        // 删除某个buf,按照添加的顺序,在本例子中,0为headBuf
        composited.removeComponent(0);
        // 遍历获取每一个buf
        for (ByteBuf buf : composited) {
            System.out.println(buf.toString());
        }

    }


    /**
     * 测试派生缓冲区:  数据是否共享问题
     */
    @Test
    public void testDrive(){
        // ----------------------   非共享  -----------------------------------------

        Charset utf8 = Charset.forName("UTF-8");
        //创建 ByteBuf 以保存所提供的字符串的字节
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        //创建该 ByteBuf 从索引 0 开始到索引 15 结束的分段的副本
        ByteBuf copy = buf.copy(0, 15);
        //将打印"Netty in Action"
        System.out.println(copy.toString(utf8));
        //更新索引 0 处的字节
        buf.setByte(0, (byte)'J');
        //将会成功，因为数据不是共享的
        assert buf.getByte(0) != copy.getByte(0);

        // ----------------------   共享  -----------------------------------------
        //创建一个用于保存给定字符串的字节的 ByteBuf
        ByteBuf bufForSlice = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        //创建该 ByteBuf 从索引 0 开始到索引 15 结束的一个新切片
        ByteBuf sliced = bufForSlice.slice(0, 15);
        //将打印"Netty in Action"
        System.out.println(sliced.toString(utf8));
        //更新索引 0 处的字节
        bufForSlice.setByte(0, (byte)'J');
        //将会成功，因为数据是共享的，对其中一个所做的更改对另外一个也是可见的
        assert bufForSlice.getByte(0) == sliced.getByte(0);
    }

}
