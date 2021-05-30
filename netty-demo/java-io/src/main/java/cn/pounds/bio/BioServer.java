package cn.pounds.bio;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Date 2021/4/17 16:04
 * @Author by pounds
 * @Description 模拟Bio模型
 * 1. 使用Bio模式编写一个服务器端程序,监听端口6666,当有客户端连接的时候,就启动一个线程与之通讯
 * 2. 要求使用线程池改善,可以连接多个客户端
 * 3. 服务端可以接受客户端发送的数据(telnet即可)
 */
public class BioServer {
    public static void main(String[] args) {
        // 创建线程池处理连接:
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                10,
                10,
                2 * 60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(100),
                new ThreadFactoryBuilder().setNameFormat("BIO-THREAD-POOL-%d").build(),
                new ThreadPoolExecutor.DiscardPolicy());
        // 打开socket连接:
        try(ServerSocket serverSocket = new ServerSocket(6666)){
            System.out.println("成功启动socket链接");
            while (true){
                // 有链接过来就建立简介
                final Socket socket = serverSocket.accept();
                System.out.println("客户端接入");
                // 开启 线程处理通信
                executor.execute(()->{
                    byte[] bytes = new byte[1024];
                    // 获取输入流
                    try (
                      final InputStream inputStream = socket.getInputStream()
                    ){
                        while (true){
                            final int read = inputStream.read(bytes);
                            if (read != -1){
                                System.out.println(Thread.currentThread().getName() + " : " +new String(bytes, "GBK"));
                            }else {
                                break;
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
