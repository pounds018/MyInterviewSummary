package Chapter12_DesignPattern.singleton;

import java.util.Collection;
import java.util.HashMap;

/**
 * @Date 2021/4/3 17:21
 * @Author by pounds
 * @Description TODO
 */
public class HungrySingleton {
    private static final HungrySingleton HUNGRY_SINGLETON_INSTANCE = new HungrySingleton();

    private HungrySingleton(){
        System.out.println("饿汉式单例,私有化构造器了");
    }

    public static HungrySingleton getInstance(){
        return HUNGRY_SINGLETON_INSTANCE;
    }

    /**
     * 验证是否还是单例
     * @param args
     */
    public static void main(String[] args) {
        HashMap<HungrySingleton, Integer> map = new HashMap<>(16);
        for (int i = 0; i < 16; i++) {
            map.put(HungrySingleton.getInstance(),i);
        }

        // 如果都是单例模式的话,map应该只有一个元素,且value为15
        if (map.size() == 1 ){
            Collection<Integer> values = map.values();
            values.forEach(System.out::println);
        }

        /**
         * 饿汉式单例,私有化构造器了 --- 类在被加载的时候就会加载静态属性,然后通过构造器new一次
         * 15
         */
    }
}
