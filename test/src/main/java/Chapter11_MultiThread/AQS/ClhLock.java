package Chapter11_MultiThread.AQS;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @Date 2021/3/28 11:40
 * @Author by pounds
 * @Description TODO
 */
public class ClhLock {
    /** 指向前驱结点 */
    private final ThreadLocal<ChlNode> preNode = ThreadLocal.withInitial(ChlNode::new);
    /** 指向当前结点 */
    private final ThreadLocal<ChlNode> curNode = ThreadLocal.withInitial(ChlNode::new);
    // 尾结点
    private final AtomicReference<ChlNode> tail = new AtomicReference<>(new ChlNode());


    private static class ChlNode {
        private volatile boolean locked;
    }

    /**
     * 加锁,没做重入
     */
    public void lock() {
        // 获取到当前结点,因为是ThreadLocal变量的关系,每个线程来获取到的都是新的item
        final ChlNode curNode = this.curNode.get();

        // 这就上锁了
        curNode.locked = true;
        // 将当前结点添加到原子引用中,并获取前驱引用(实际上就是原来的当前结点)
        ChlNode pre = this.tail.getAndSet(curNode);
        // 把前驱结点的引用保存到前驱结点上
        this.preNode.set(pre);
        // 轮训前驱结点的状态,当前驱结点的lock状态变成了false,lock方法结束,表示当前结点的lock成功,
        while (pre.locked){
            // 能执行到这里，说明当前线程没有获取到锁
            //            System.out.println("线程" + Thread.currentThread().getName() + "没能获取到锁,开始自旋等待！！！");
        }
        // 能执行到这里，说明当前线程获取到了锁
        //        System.out.println("线程" + Thread.currentThread().getName() + "获取到了锁！！！");

    }

    /**
     * 解锁
     */
    public void unlock() {
        final ChlNode curNode = this.curNode.get();
        curNode.locked = false;
        this.curNode.set(this.preNode.get());
    }
}

