package Chapter11_MultiThread;

import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Date 2021/3/13 14:08
 * @Author by pounds
 * @Description 线程池的演示
 */
public class ThreadPoolExecutorTest {
    /**
     * 自定义拒绝策略
     */
    @Test
    public void test(){
        // 由于RejectedExecutionHandler是个函数式接口,所以直接使用lambda表达式实现
        ThreadPoolExecutor executor1 = new ThreadPoolExecutor(
                1,
                1,
                1000,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(10),
                Executors.defaultThreadFactory(),
                (r, executor) -> {
                    System.out.println("自定义拒绝策略开始了");
                }
        );
        // max + queue = 11,循环提交12个任务,第十二个任务将会被拒绝
        for (int i = 0; i < 12; i++) {
            executor1.execute(()->{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("提交任务了!!");
            });
        }

        // 防止主线程退出,不打印结果
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
