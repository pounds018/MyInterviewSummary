> 主要是总结一下 AQS共享锁和排他锁的原理,暂时还无法理解 AQS条件队列相关的原理(3.27)
## 11.4.1 AQS概述
1. AQS是什么:  
    1. 源码注释中是这样写的:
        - 提供一种基于 FIFO等待队列实现的阻塞锁和类似同步器的框架,并且支持对同步状态的原子管理,阻塞和唤醒线程功能.
        - AQS的子类应该用作帮助实现某种同步机制的私有内部类.AQS没有实现任何同步接口,但是实现了支撑同步接口完成同步机制的剩余步骤方法.
        - AQS支持独占式和共享式两种方式的同步机制:  
            - 独占式: 一旦某个线程拿到了共享资源,那么其他线程就无法拿到共享资源
            - 共享式: 多个线程可以共同持有资源.  
        - AQS内部定义了一个ConditionObject类,这个内部类是用来实现条件队列的
    2. 个人理解:
        - AQS是一个使用了模板方法模式的同步框架,在其内部实现了一套能够实现不同同步机制的模板方法,具体要实现什么样的同步机制,就需要根据
        同步机制特点来具体实现特定的步骤,AQS中这些特定的步骤就是以下几个方法:  
        ![AQS预留方法](../../_media/chapter11_MultiThread/4_AQS/AQS预留方法.png)  
          AQS的子类通过实现这些方法来实现不同的同步逻辑,比如JUC包下: ReentrantLock,CountDownLatch...等
    3. AQS体系使用的继承结构:  
        ![AQS使用时继承结构](../../_media/chapter11_MultiThread/4_AQS/AQS使用时的继承结构.png)  
        > 通常在使用AQS框架的时候,都需要使用一个内部类来实现AQS完成某种同步机制的实现.
    4. AQS内部结构:  
        - 内部类:
            - Node: AQS实现FIFO队列的基础,一个Node结点表示一个线程
            - ConditionObject: 与AQS条件队列有关的一个内部类
        - 属性:
            - int state: 共享资源的同步状态, `取int类型: 是为了复用这个属性,不同的值有不同的含义`
            - Node head: 指向等待队列的头节点, `head结点的waitStatus一定不会是Cancelled`
            - Node Tail: 指向等待队列的尾结点, `主要作用是 等待结点入队 和 待激活节点的waitStatus为CANCELLED的时候,
              寻找队列中第一个waitStatus不为CANCELLED的结点` 
        > AQS里面还有一些属性没有列出,比如用来协助 Unsafe类CAS操作内存中head,tail,state变量的内存偏移量等.          
2.  
## 11.4.2 通过ReentrantLock理解基于AQS实现独占锁
## 11.4.3 通过CountDownLatch理解基于AQS实现共享锁