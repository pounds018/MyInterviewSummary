`ps: 只列出常用集合,本章节所有的代码都是基于 jdk1.8`
## 10.1 javaSe 集合框架结构

    - 单列集合:
        - Collection:
            - list: 元素位置有序,可重复
                - `ArrayList`: 变长数组,底层是Object[],线程不安全,有索引,`查询快,增删慢`
                - `Vector`: 变长数组,底层是Object[],线程安全,但是效率低
                - `LinkedList`: 双向链表,底层是Node[],无索引,`查询慢,增删快`
            - set: 元素位置无序,不可重复
                - `HashSet`: 元素位置无序,不可重复,允许存放null值`(底层就是一个value固定的hashmap)`
                    - LinkedHashSet: 用Node结点存放元素,形成一个双向链表,使得元素存取有序
                - `TreeSet`: 按照一定比较规则排序的set,`即元素大小有序`
    - 双列集合:
        - Map: key-value键值对
            - `HashMap`: key不能重复,但是允许存放一个`null`key
                - LinkedHashMap: 在HashMap的基础上维护了一个双向链表,可以控制元素是存取有序还是访问有序
            - `HashTable`: 线程安全的双列集合,`但是效率不行`
            - `TreeMap`: key按照一定比较规则排序,`即键值对按照key大小有序存放`
      
## 10.2 ArrayList源码概述:

### 10.2.1 几个重要的变量:

- int `modCount` = 0 : 继承自AbstractList的属性,表示 list发生结构性 变化的次数.
- int `DEFAULT_CAPACITY` = 10 : 在未指定ArrayList初始化长度时,默认的初始化长度.
- int `modCount` = 0 : arraylist在发生增删改等结构性变化的时候,modCount++
- Object[] `EMPTY_ELEMENTDATA` = {} : 有参构造创建ArrayList但是没有给初始化长度时,默认的Object数组
- Object[] `DEFAULTCAPACITY_EMPTY_ELEMENTDATA` = {} : 无参构造器创建ArrayList时,默认的Object数组
- Object[] `elementData` : 实际存放元素的element数组
```yaml
# EMPTY_ELEMENTDATA 和 DEFAULTCAPACITY_EMPTY_ELEMENTDATA 这两个是扩容时识别ArrayList是由什么构造器创建的
```

### 10.2.2 构造函数:

- 无参构造函数:
```java
public ArrayList() {
    // 通过 DEFAULTCAPACITY_EMPTY_ELEMENTDATA 变量将element数组标记为无参构造器创建
    this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
}
```
- 有参构造函数:
```java
public ArrayList(int initialCapacity) {
        /**
         * @Param initialCapacity --- 初始化容量
         * @Desc 有参数就按照参数长度初始化elementData 数组,没有就使用 EMPTY_ELEMENTDATA初始化
         */
    if (initialCapacity > 0) {
        
        // initialCapacity 合法,就按照传入参数初始化element数组
        this.elementData = new Object[initialCapacity];
    
    } else if (initialCapacity == 0) {
        
        // 未指定initialCapacity时,就是用 EMPTY_ELEMENTDATA数组作为 
        // 有参构造器创建ArrayList但是没有初始化数组的标记
        this.elementData = EMPTY_ELEMENTDATA;
    
    } else {
        throw new IllegalArgumentException("Illegal Capacity: "+initialCapacity);
    }
}
```
- 有参构造函数:
```java
public ArrayList(Collection<? extends E> c) // 就是将集合c中元素全部存入ArrayList中,不是本章节关注的重点
```

### 10.2.3 扩容流程:

- 从上面的构造器可以看出,在构造ArrayList时,未指定arrayList初始容量的时候,arraylist实际上就是一个`空object数组`
- 在jdk1.8 中ArrayList 实际上是一种`懒加载`的机制在进行初始化存放元素的elementData数组,`即在使用的时候才会去初始化数组`
- `扩容流程`:
    1. 入口方法: add(E e) ---> 先去对数组容量进行判断,判断当前数组容量是否允许插入新元素,容量不足就扩容
  ```java
  public boolean add(E e) {
      ensureCapacityInternal(size + 1);  // Increments modCount!!
      elementData[size++] = e;
      return true;
  }
  ```
    
    2. ensureCapacityInternal(int minCapacity): 计算期望最小容量
  ```java
    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }
  ```
    
    3. calculateCapacity(Object[] elementData, int minCapacity): 计算 期望的新容量
    [示例](https://pounds018.github.io/MyInterviewSummary/test/src/main/java/Chapter10_JavaSE_Collection/ArrayListTest.
       java) `从实例中debug可以看出,
       第一次存入数据的时候,有参构造(指定长度为0)和无参构造扩容的长度是不同的,有参构造(指定长度为0)构造出来的elementData数组长度为
       插入元素个数+原数组元素个数;无参构造构造出来的elementData数组长度为10`
  ```java
    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        /**
        从这个方法可以看出,在第一次添加元素的时候扩容分为两种情况:
        1. 如果是使用无参构造创建的list,在第一次add元素的时候,其计算出的期望容量为 默认初始化长度(10)和最小容量二者之间的最大值.
        2. 如果是使用有参构造创建的list,在第一次add元素的时候,其计算出的期望容量为 最小容量.
        */
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }
   ```
    - 如果是就比较 `指定容量`和 `默认容量`的大小,将大的返回,作为最小期望容量
    - 如果不是,就返回最小期望容量参数,作为计算结果返回
    
    4. ensureExplicitCapacity(int minCapacity): 修改modCount,并且保证 确实需要扩容
    ```java
        private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }
    ```
  
    5. grow(int minCapacity): 真正的扩容函数,大多数情况下 将容量扩容为旧容量的1.5倍.
    ```java
        
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);// 新容量 赋值 为旧容量的1.5倍
        // 扩容为1.5倍后,还不够存放,就将新容量设置为 minCapacity
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        // MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8,判断newCapacity是否超出容量上限
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
      
    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }  
    ```
  > 总结: 
  > 1. ArrayList在初次扩容的时候,只有在arraylist是由 无参构造器初始化出来的时候,才会扩容为10;其他情况都是将 elementData数组的
  > 的长度设置为 待插入元素长度+1.
  > 2. ArrayList在初次扩容完成之后,后续的扩容都是 将容量扩容为 elementData数组原长度的1.5倍.
  
### 10.2.4 ArrayList的subList方法:
- 先上源码: 实际上就是新建了一个 list的内部类subList返回
  
  `ps: 传入参数fromIndex,toIndex是Arraylist的下标,并且截取出来的subList范围是一个左闭右开区间,即[formIndex,toIndex),`
  ```java
    public List<E> subList(int fromIndex, int toIndex) {
        // 检查范围
        subListRangeCheck(fromIndex, toIndex, size);
        return new SubList(this, 0, fromIndex, toIndex);
    }
  
      //检查范围是否合法
    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");
    }
  ``` 
- 返回的SubList究竟是什么?
  - SubList: `private class SubList extends AbstractList<E> implements RandomAccess ` 继承了AbstractList的内部类
  - 重要属性:
  ```java
        private final AbstractList<E> parent; // 指向subList的父List,即调用subList()的ArrayList
        private final int parentOffset;// 父list中截取区间的起始索引
        private final int offset;// 子list的起始索引
        int size;// 记录当前sublist的元素个数
  ```
  - 常用方法:
    > 说明:  下面的所有方法都是在下面这个list,sublist中完成</br>
    `未修改之前的TEST_LIST` : [java, c, erlang, go, c++, python, ruby]<br/>
    `sublist` : [erlang, go, c++]
    - subList构造函数,这个parent实际上是指向 父类list,`即调用subList()方法的list`
    ```java
    SubList(AbstractList<E> parent,int offset, int fromIndex, int toIndex) {
        this.parent = parent;
        this.parentOffset = fromIndex;
        this.offset = offset + fromIndex;
        this.size = toIndex - fromIndex;
        this.modCount = ArrayList.this.modCount;
    }
    ```
    - `set方法`: `E set(int index, E e)`将subList中index位置上的元素设置成 e,返回旧址
      
        `ps: 注意index指的是sublist中的元素下标,不要越界`
        ```java
        public E set(int index, E e) {
            rangeCheck(index);// 检查待处理元素是否位于sublist内
            checkForComodification();// 检查
            E oldValue = ArrayList.this.elementData(offset + index);
            ArrayList.this.elementData[offset + index] = e;
            return oldValue;
        }
        ```
        示例:
        ```java
            // E set(int index, E e),将subList中第二个元素设置成"php"
            subList.set(1,"php");
            System.out.println(TEST_LIST);
            System.out.println(subList);
            /**
             * TEST_LIST:
             * [java, c, erlang, php, c++, python, ruby]
             * sublist:
             * [erlang, php, c++]
             */
        ```
      > 原因: 
      >    ArrayList.this.elementData[offset + index] = e,实际上是修改了父list中的元素, 
      > 而这个sublist对象,实际上只是一个父list的视图. 
    - `get方法`: 从父list的elementData数组中获取到指定元素,返回 
        ```java
        public E get(int index) {
            rangeCheck(index);
            checkForComodification();
            return ArrayList.this.elementData(offset + index);
        }  
        ```
    - `add方法`: `add(int index, E e)`
      ```java
      public void add(int index, E e) {
        rangeCheckForAdd(index);
        checkForComodification();
        parent.add(parentOffset + index, e);
        this.modCount = parent.modCount;
        this.size++;
      }
      ```
      示例:
      ```java
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
      ```
      > 原因:<br/>
      parent.add(parentOffset + index, e);前面说过parent指向的是父list,parentOffset实际上就是fromIndex,<br/>
      所以sublist最终还是通过父list在对list进行修改
      
    - `remove方法` : 
      ```java
              public E remove(int index) {
            rangeCheck(index);
            checkForComodification();
            E result = parent.remove(parentOffset + index);
            this.modCount = parent.modCount;
            this.size--;
            return result;
        }
      ```
      ````java
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
      ````
      > 原因:<br/>
      1.remove(Object o)删除某个指定元素是继承自 abstractList的方法(并且SubList没有重写)
      , 所以其删除操作是在sublist自身上操作
      2.remove(index i)删除某个指定位置上的元素,在SubList类中重写了方法,
      并且最终是通过parent属性所指向的父list来执行的一个删除操作.
      
> 总结: `谨慎使用SubList中会造成元素发生更改的方法.`
> 1. sublist方法拿到的子list,仅仅是一个视图效果,并不代表重新生成了一个与父list没有关联的list.
> 2. sublist对象根据索引来执行的操作,实际上都是通过父list来完成的.最终都会影响两个list
> 3. 虽然add(Object o)方法不是根据索引的来执行的,但是他内部最终还是调用的根据索引操作的方法 - add(size(),o)
> 由于SubList重写了根据索引来执行的add操作,所以这个方法他也会造成`子父list` 都发生变化.