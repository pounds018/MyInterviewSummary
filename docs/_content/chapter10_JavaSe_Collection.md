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

- int `DEFAULT_CAPACITY` = 10 : 在未指定ArrayList初始化长度时,默认的初始化长度.
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
    3. calculateCapacity(Object[] elementData, int minCapacity): 判断elementData是否是由有参构造器创建,
       - 如果是就比较 `指定容量`和 `默认容量`的大小,将大的返回,作为最小期望容量
       - 如果不是,就返回最小期望容量参数,作为计算结果返回
  ```java
    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }
   ```