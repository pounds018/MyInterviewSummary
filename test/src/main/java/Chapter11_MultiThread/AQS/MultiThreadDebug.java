package Chapter11_MultiThread.AQS;

/**
 * @Date 2021/3/28 11:43
 * @Author by pounds
 * @Description TODO
 */
// 测试代码:
public class MultiThreadDebug extends Thread{
    private static Integer num = 100;
    private static ClhLock lock = new ClhLock();

    @Override
    public void run() {
        while(true){
            lock.lock();
            try{
                if (num > 0){
                    Thread.sleep(100);
                    System.out.println(Thread.currentThread().getName() + " : num = " + --num);
                }else{
                    break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        MultiThreadDebug t1 = new MultiThreadDebug();
        MultiThreadDebug t2 = new MultiThreadDebug();
        MultiThreadDebug t3 = new MultiThreadDebug();

        t1.start();
        t2.start();
        t3.start();
    }
}
