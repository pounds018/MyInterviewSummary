package Chapter12_DesignPattern.singleton;

import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Date 2021/4/3 17:39
 * @Author by pounds
 * @Description 懒汉式单例
 */
public class LazySingletonTest {
    private volatile static LazySingletonTest INSTANCE;
    private static final ReentrantLock lock = new ReentrantLock();

    private LazySingletonTest(){
        System.out.println("懒汉式单例模式,私有化构造器了");
    }

    public static LazySingletonTest getInstance(){
        if (INSTANCE == null) {
            lock.lock();
            try {
                if (INSTANCE == null) {
                    INSTANCE = new LazySingletonTest();
                }
            }finally {
                lock.unlock();
            }
        }
        return INSTANCE;
    }

    public static void main(String[] args) {
        HashMap<LazySingletonTest, Integer> map = new HashMap<>(16);
        for (int i = 0; i < 16; i++) {
            map.put(LazySingletonTest.getInstance(),i);
        }

        // 如果都是单例模式的话,map应该只有一个元素,且value为15
        if (map.size() == 1 ){
            Collection<Integer> values = map.values();
            values.forEach(System.out::println);
        }
    }
}
