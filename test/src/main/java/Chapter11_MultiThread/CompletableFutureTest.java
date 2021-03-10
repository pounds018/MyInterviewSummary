package Chapter11_MultiThread;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;

/**
 * @Date 2021/3/10 22:46
 * @Author by pounds
 * @Description TODO
 */
public class CompletableFutureTest {
    /**
     * 验证默认线程池产生的线程是不是守护线程
     */
    @Test
    public void test(){

        CompletableFuture.runAsync(()->{
            System.out.println("当前线程是不是守护线程: " + Thread.currentThread().isDaemon());
        });

        // 防止test线程退出导致jvm退出,从而造成异步线程输出语句不打印
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /**
         * 当前线程是不是守护线程: true
         */
    }
}
