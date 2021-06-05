package Chapter10_JavaSE_Collection;

import org.junit.Test;

/**
 * @Date 2021/6/5 14:30
 * @Author by pounds
 * @Description HashMap相关方法的测试
 */
public class HashMapTest {
    private int MAXIMUM_CAPACITY = 1 << 30;
    /**
     * hash
     */
    @Test
    public void test(){
        String input1 = "abc";
        String input2 = "abcd";
        System.out.println("输入abc  输出 --> " + input1.hashCode());
        System.out.println("输入abcd  输出 --> " + input2.hashCode());

    }

    /**
     * char相加
     */
    @Test
    public void testCharAdd(){
        char[] a  = {'a','b'};
        System.out.println('0' - 0);
        System.out.println(a[0] - 0);
        System.out.println(a[0] - '0');
    }

    /**
     * hashmap有参构造参数的table长度限制方法
     */
    private final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * tableSizeFor不同输入值输出结果的测试
     */
    @Test
    public void testTableSizeFor(){
        int[] lessThanEight = {1,2,3,4,7,8};
        int[] lessThanSixteen = {11,12,14,15,16};
        int[] lessThanThirtyTwo = {27,24,26,31,25,32};
        int[] lessThanSixtyFour = {33,43,54,61,63,64};
        System.out.println("8以下的值: ");
        for (int i : lessThanEight) {
            System.out.print(tableSizeFor(i) + " ");
        }
        System.out.println("---------------------------");
        System.out.println("16以下的值: ");
        for (int i : lessThanSixteen) {
            System.out.print(tableSizeFor(i) + " ");
        }
        System.out.println("---------------------------");
        System.out.println("32以下的值: ");
        for (int i : lessThanThirtyTwo) {
            System.out.print(tableSizeFor(i) + " ");
        }
        System.out.println("---------------------------");
        System.out.println("64以下的值: ");
        for (int i : lessThanSixtyFour) {
            System.out.print(tableSizeFor(i) + " ");
        }
        /**
         * 输出结果:
         * 8以下的值:
         * 1 2 4 4 8 8 ---------------------------
         * 16以下的值:
         * 16 16 16 16 16 ---------------------------
         * 32以下的值:
         * 32 32 32 32 32 32 ---------------------------
         * 64以下的值:
         * 64 64 64 64 64 64
         */
    }
}
