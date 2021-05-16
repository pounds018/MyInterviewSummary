## 12.1.1 使用单例模式的几个原则:
    1. 私有化构造器: 防止通过构造器创建出不同的实例.
    2. 以静态方法或者枚举返回实例: 保证实例的唯一性.
    3. 确保实例只有一个,防止线程安全问题: 即保证创建实例的方法是没有线程安全问题的
    4. 确保实例无法通过反射创建对象,并保证实例在反序列化的时候返回的是同一个对象: 实际上都是反射的原因.
## 12.1.2 单例模式的几种实现方式:
1. 饿汉式单例: 
```java
   public class HungrySingleton {
        private static final HungrySingleton HUNGRY_SINGLETON_INSTANCE = new HungrySingleton();
    
        private HungrySingleton() {
            System.out.println("饿汉式单例,私有化构造器了");
        }
    
        public static HungrySingleton getInstance() {
            return HUNGRY_SINGLETON_INSTANCE;
        }

    }
```
- 特点: 
    1. 通过类加载的特性,只在类加载的时候通过构造器创建一个实例对象.
    2. 所有的对象都共用一个实例,避免了线程安全问题
    3. 无论用不用实例都会被创建,`如果这样的单例多了,却又没有使用,比较浪费空间`
    4. 无法解决反射和反序列话的问题
> 通常: 没有反射或者反序列话的需求的话,饿汉式单例是使用的比较多的一种单例模式,
2. 懒汉式单例: 延迟加载,在使用的时候才会去创建实例 
```java
   // todo to be continue
```