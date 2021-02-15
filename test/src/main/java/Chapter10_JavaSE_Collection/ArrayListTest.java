package Chapter10_JavaSE_Collection;

import org.junit.Test;

import java.util.ArrayList;

/**
 * @Project: InterviewSummary
 * @Date: 2021/2/15 13:00
 * @author: by Martin
 * @Description: TODO
 */
public class ArrayListTest {

    /**
     * 测试初始扩容时计算的新容量为多少
     */
    @Test
    public void test(){
        // 无参构造创建list
        ArrayList<Object> NoArgsList = new ArrayList<>();
        // 有参构造不指定初始化长度
        ArrayList<Object> NoInitArgsList = new ArrayList<>(0);
        // 有参构造指定初始化长度
        ArrayList<Object> listWithInitArgs = new ArrayList<>(5);

        NoArgsList.add(1);// 初始化长度位0,扩容至默认初始化长度10
        NoArgsList.add(2);
        NoInitArgsList.addAll(NoArgsList);
        NoInitArgsList.add(1);// 初始化长度为0,扩容至原数组元素个数+1

        listWithInitArgs.add(1);// 初始化的长度就是5,不需要扩容
    }

}
