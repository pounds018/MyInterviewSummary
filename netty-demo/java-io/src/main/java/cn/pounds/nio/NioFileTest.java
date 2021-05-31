package cn.pounds.nio;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * @Date 2021/4/18 16:35
 * @Author by pounds
 * @Description NIO使用实例
 */
public class NioFileTest {
    /**
     * 使用NIO模拟一个BIO从内存中网磁盘写文件的流程
     */
    @Test
    public void test(){
        String info = "hello NIO";

        try(
            //1. 创建一个文件输出流
            final FileOutputStream fileOutputStream = new FileOutputStream("src/main/resources/NioOutPutFile.txt")
        ){
            //2. 通过流拿到FileChanel,channel实际上就是流使用了装饰者模式,将channel作为属性包裹在了流中
            FileChannel fileOutputStreamChannel = fileOutputStream.getChannel();
            //3. 创建buffer
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            //4. 向buffer里面写入数据
            byteBuffer.put(info.getBytes(StandardCharsets.UTF_8));
            //5. 切换buffer的模式,使用flip由写切换为读
            byteBuffer.flip();
            //6. 将buffer中数据写入channel中
            fileOutputStreamChannel.write(byteBuffer);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 使用NIO模拟一个BIO从磁盘往内存读文件的流程
     */
    @Test
    public void test1(){
        try(
                //1. 创建一个文件输出流
                FileInputStream fileOutputStream = new FileInputStream("src/main/resources/NioOutPutFile.txt")
        ){
            //2. 通过流拿到FileChanel,channel实际上就是流使用了装饰者模式,将channel作为属性包裹在了流中
            FileChannel fileOutputStreamChannel = fileOutputStream.getChannel();
            //3. 创建buffer
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            //4. 从channel里面读取数据
            fileOutputStreamChannel.read(byteBuffer);
            //5. 获取数据,array会将整个缓冲区都获取出来,就不需要切换模式
            System.out.print(new String(byteBuffer.array()));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 仅使用一个buffer完成文件的拷贝
     */
    @Test
    public void test3(){
        try(
            FileInputStream fileInputStream = new FileInputStream("src/main/resources/NioOutPutFile.txt");
            FileOutputStream fileOutputStream = new FileOutputStream("src/main/resources/NioOutPutFile1.txt");
        ){
            FileChannel inputStreamChannel = fileInputStream.getChannel();
            FileChannel outputStreamChannel = fileOutputStream.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(512);
            while (true){
                // 往缓冲区写数据
                int read = inputStreamChannel.read(byteBuffer);
                if (read == -1){
                    System.out.println("read == " + read + ",于是退出啦");
                    break;
                }
                // 写模式切换为读模式
                byteBuffer.flip();
                // 从缓冲区读数据
                outputStreamChannel.write(byteBuffer);
                //读模式转换为写模式,数据超出缓冲区的大小,就要多次读写
                byteBuffer.clear();
            }
        }catch (Exception e){

        }
    }

    /**
     * 通过transfer方法处理文件拷贝
     */
    @Test
    public void test4(){
        try(
                FileInputStream fileInputStream = new FileInputStream("src/main/resources/NioOutPutFile.txt");
                FileOutputStream fileOutputStream = new FileOutputStream("src/main/resources/NioOutPutFile2.txt");
        ){
            FileChannel inputStreamChannel = fileInputStream.getChannel();
            FileChannel outputStreamChannel = fileOutputStream.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(512);
            while (true){
                // 往缓冲区写数据
                int read = inputStreamChannel.read(byteBuffer);
                if (read == -1){
                    System.out.println("read == " + read + ",于是退出啦");
                    break;
                }
                inputStreamChannel.transferTo(0,read,outputStreamChannel);
            }
        }catch (Exception e){

        }
    }
}
