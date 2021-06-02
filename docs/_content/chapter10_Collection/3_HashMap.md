## 1. 重要属性:
1. 存放数据的结点: HashMap本质上是一个`结点数组`(就是一个数组,元素为Node类型),但是由于`hash冲突`(多个元素定位到结点数组中相同
   的位置,后面来的元素如果存放到该位置,会覆盖结点数组原有的元素)的原因,HashMap解决冲突的办法是让发生冲突的元素形成一个链表,但是
   如果链表过长会造成查询时间变慢,所以出现了将链表转化成红黑树的优化方式.因此,存储元素的结点,主要分成 `普通结点` 和 `树型结点`:
    - 普通结点:   
    ```java
    static class Node<K,V> implements Map.Entry<K,V> {
        // hash值,与计算该结点因该处于hash桶数组的哪个位置有关
        fianl int hash;
        // 存放 元素key
        fianl K key;
        // 存放 元素value
        V value;
        // 解决hash冲突使用,使hash桶链话
        Node<K,V> next;
   
        /**  ...  省略结点方法  ... */
    }
    // 父类中没有属性,只是一个借口,提供很多方法
    ```
    普通结点的属性总结:  
       |状态|类型|含义|    
       |:--|---|---|  
       | hash | int | 计算元素在结点数组中的位置相关属性 |  
       | key | 泛型K | key,可以看做是元素的唯一标志符 |
       | value | 泛型V | value,元素的值 |  
       | next | Node<K,V> | 当前结点的下一个结点,主要是将结点形成链表使用 |
     - 树型结点:  
    ```java
    static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
        // 红黑树结点父结点的引用
        TreeNode<K,V> parent; 
        // 红黑树结点左儿子引用 
        TreeNode<K,V> left;
        // 红黑树结点右儿子引用
        TreeNode<K,V> right;
        // 删除后需要取消连接
        TreeNode<K,V> prev; 
        // 红黑树结点颜色
        boolean red;
        
        /**  ...  省略结点方法  ... */
    }
    // 父类 LinkedHashMap.Entry<K,V>,Entry的父类就是 上面的 普通结点
    static class Entry<K,V> extends HashMap.Node<K,V> {
        // 
        Entry<K,V> before, after;
        Entry(int hash, K key, V value, Node<K,V> next) {
            super(hash, key, value, next);
        }
    }
    ```   
    树型结点属性总结:   
       |状态|类型|含义|  
       |:--|---|---|  
       | hash | int | 计算元素在结点数组中的位置相关属性 |  
       | key | 泛型K | key,可以看做是元素的唯一标志符 |
       | value | 泛型V | value,元素的值 |  
       | next | Node<K,V> | 当前结点的下一个结点,主要是将结点形成链表使用 |
       | before | Entry<K,V> | linkedHashMap链表中指向当前结点前一个结点的引用 |  
       | next | Entry<K,V> | linkedHashMap链表中指向当前结点后一个结点的引用 |  
       | parent | TreeNode<K,V> | 红黑树父结点的引用 |  
       | left | TreeNode<K,V> | 红黑树左子结点的引用 |  
       | right | TreeNode<K,V> | 红黑树右子结点的引用 |  
       | prev | TreeNode<K,V> | 红黑树上一个结点的引用,用来删除下一个节点用的 |  
       | red | boolean | 红黑树结点颜色标记,主要是用于判断属性结构是否满足红黑树的性质 |  
   
2. 属性:  
    - `结点数组`: 又称hash桶数组  
    ```java
        // hashMap的基础结构,存放不同hash值的hash桶
        transient Node<K,V>[] table;
    ```
    > 下面提到的table就是这个节点数组
    - `结点数组默认初始化长度`:  
    ```java
       // 1 << 4 表示二进制0000000001,向左移动4位,即 1 * 2^4 = 16
       static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; 
    ```
    - `结点数组最大长度`:  
    ```java
        //最大容量,必须是2^N并且小于2^30
        static final int MAXIMUM_CAPACITY = 1 << 30;
    ```  
    - `默认负载因子`: 用于判断当前结点数组中元素个数是否即将到达结点数组的容量上线,table.capacity() * 0.75 < table.size()
    的话,表示hash桶数组将要满了,需要扩容来容纳更多的hash桶
    ```java
        //在没有任何特殊指定的时候,默认负载因子为0.75f,计算下面这个属性使用的
        static final float DEFAULT_LOAD_FACTOR = 0.75f;
    ```  
    - `扩容阈值`:  
    ```java
        /*
        判断resize的阈值,即capacity * load factor,也就是扩容的阈值
        另外,如果HashMap还没有被扩容过,这个字段,就是HashMap的初始容量
        (指定初始容量构造器创建),或者0(没有指定初始容量的构造器创建)
        */
        int threshold;
    ```
    - `负载因子`: 可以手动指定负载因子是多少,用这个属性来存手动指定的负载因子
    ```java
        //负载因子
        final float loadFactor;
    ```
    - `hash桶树形化条件之一[hash链表最小要元素个数]`: 前面说过为了提高元素在hash桶(链表)中的查询速度会将链表转化成红黑树,`如果链表长度>=8`
    表示hash桶达到了树型化的条件`之一`,还要必须满足`table容量>=64` , 链表才能转化成树
    ```java
        //元素桶树形化的阈值,元素桶中元素个数>8,元素桶采用红黑树结构
        static final int TREEIFY_THRESHOLD = 8;
    ```
    - `hash桶树形化条件之一[table最小容量]`:  即使`hash链表长度>=8`了,如果这个条件没有达到也只会对`table`进行扩容.
    ```java
        //元素桶链表化的阈值,即导致元素桶树型化的元素桶数组的最小容量
        static final int MIN_TREEIFY_CAPACITY = 64;  
    ```
    - `hash桶树形化之后重新变回链表的条件[红黑树节点个数]`:    
    ```java
        //hash桶链表化的阈值,hash桶中元素个数<6,元素桶采用链表结构
        static final int UNTREEIFY_THRESHOLD = 6;    
    ```  
    - `存放key-value键值对的set`:  多用于遍历元素
    ```java
        //存放entry的set
        transient Set<Map.Entry<K,V>> entrySet;
    ```  
    - `元素个数`:  
    ```java
        //entry个数
        transient int size;
    ```
    