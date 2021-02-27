package Chapter10_JavaSE_Collection;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @Project: InterviewSummary
 * @Date: 2021/2/15 13:00
 * @author: by Martin
 * @Description: TODO
 */
public class ArrayListTest {
    private static final ArrayList<String> TEST_LIST = new ArrayList<String>();
    static {
        TEST_LIST.add("java");
        TEST_LIST.add("c");
        TEST_LIST.add("erlang");
        TEST_LIST.add("go");
        TEST_LIST.add("c++");
        TEST_LIST.add("python");
        TEST_LIST.add("ruby");
    }
    /**
     * 测试初始扩容时计算的新容量为多少
     */
    @Test
    public void test(){
        // 无参构造创建list
        ArrayList<Object> NoArgsList = new ArrayList<>();
        // 初始化长度位0,扩容至默认初始化长度10
        NoArgsList.add(1);
        NoArgsList.add(2);

        // 有参构造不指定初始化长度
        ArrayList<Object> NoInitArgsList = new ArrayList<>(0);
        NoInitArgsList.addAll(NoArgsList);
        // 初始化长度为0,扩容至原数组元素个数+1
        NoInitArgsList.add(1);

        // 有参构造指定初始化长度
        ArrayList<Object> listWithInitArgs = new ArrayList<>(5);
        // 初始化的长度就是5,不需要扩容
        listWithInitArgs.add(1);
    }

    /**
     * arrayList到底有什么副作用?
     */
    @Test
    public void testSubList(){
        List<String> subList = TEST_LIST.subList(2, 5);
        System.out.println(TEST_LIST);
        System.out.println(subList);
        /**
         * 未修改之前的TEST_LIST
         * [java, c, erlang, go, c++, python, ruby]
         * sublist:
         * [erlang, go, c++]
         */
        // subList方法演示

        // E set(int index, E e),将subList中第二个元素设置成"php"
        subList.set(1,"php");
        System.out.println(TEST_LIST);
        System.out.println(subList);
        /**
         * TEST_LIST:
         * [java, c, erlang, php, c++, python, ruby]
         * sublist
         * [erlang, php, c++]
         */

        //重置sublist
        subList = TEST_LIST.subList(2, 5);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        // add(int index, E e)
        subList.add("js");
        System.out.println(TEST_LIST);
        System.out.println(subList);
        /**
         * TEST_LIST:
         * [java, c, erlang, php, c++, js, python, ruby]
         * subList:
         * [erlang, php, c++, js]
         */
        subList.add(1,"c#");
        System.out.println(TEST_LIST);
        System.out.println(subList);
        /**
         * TEST_lIST:
         * [java, c, erlang, c#, php, c++, js, python, ruby]
         * sublist:
         * [erlang, c#, php, c++, js]
         */

        //重置sublist
        subList = TEST_LIST.subList(2, 5);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        // 删除指定元素,不会删除掉父list中的元素
        subList.remove("js");
        System.out.println(TEST_LIST);
        System.out.println(subList);
        /**
         * TEST_LIST:
         * [java, c, erlang, c#, php, c++, js, python, ruby]
         * subList:
         * [erlang, c#, php]
         */
        // 删除指定位置上的元素,会删除掉父list上的元素
        subList.remove(1);
        System.out.println(TEST_LIST);
        System.out.println(subList);
        /**
         * TEST_LIST
         * [java, c, erlang, php, c++, js, python, ruby]
         *  sublist
         * [erlang, php]
         */
    }

}
