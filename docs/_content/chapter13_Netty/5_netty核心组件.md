## 5.1 核心组件概述:  
1. Channel:  
   `Channel`是NIO 三大核心组件之一, Channel是输入输出硬件设备与内存之间的一个通道的抽象,channel当做是 数据传输的载体,因此 channel可以被打开或者关闭,
   可以连接或者断开连接.
2. ByteBuf:  
   `ByteBuf`是Netty在Nio的ByteBuffer基础上的扩展,Netty的核心数据容器.
3. ChannelHandler和ChannelPipeline:  
   `ChannelPipeline`: channel包裹的数据处理链,本质是个双向链表,结点元素是ChannelHandlerContext.而ChannelHandlerContext又与数据处理器 
   `ChannelHandler`关联.  
   `ChannelHandler`: 数据处理器,对数据的处理逻辑都在这个对象中完成.
4. EventLoopGroup和EventLoop:  
   `EventLoopGroup`事件循环组: 本质是一个线程池,里面的线程与`EventLoop`事件循环相关联.
5. Future和Promise:   
   `回调`: 本质是一个方法,一个指向已经被提供给其他方法的方法的引用   
   `Future`: future可以看做是一个异步操作结果的占位符,future会在未来的某一个时刻完成,并提供对一步操作结果访问的途径.  
   `Promise`: promise是对future的扩展,future本身是不提供对异步操作结果设置的途径,promise则提供了对异步操作设置结果的途径.

## 5.2 Channel:  
### 5.2.1 Channel概述:  

1. 基本的I/O操作(bind,connect,read,write)依赖于底层网络传输提供的原语(Socket).Netty提供了自己的Channel及其子类,大大的降低了直接使用socket的复杂性.
2. 通过channel可以获得当前网络连接的通道的状态
3. 通过channel可以获得当前网络连接的配置参数(比如: 接口缓冲区的大小等)
4. channel提供异步的网络I/O操作(建立连接,读写,绑定端口等),异步调用意味着任何I/O都将立即返回,并且不保证在调用结束时所有的I/O操作已经完成
5. channel支持关联I/O操作与对应的处理程序(即handler)
6. 不同的协议,不同阻塞类型的连接都有不同的channel与之对应,常见的Channel类型`不仅限与下列实现类`为:  
   |Channel实现类|解释|  
   |:--|---|  
   | NioSocketChannel   |异步的客户端TCP连接|  
   | NioServerSocketChannel   |异步的服务端TCP连接|  
   | NioDatagramChannel |异步udp连接|  
   | NioSctpChannel |异步客户端Sctp连接|  
   | NioSctpServerChannel  |异步服务端Sctp连接|  
   | OioSocketChannel  |阻塞的客户端tcp连接|  
   | EmbeddedChannel   |内置的channel 用于测试channel|  
   

### 5.2.2 Channel的层次结构、常用方法:  

#### 5.2.2.1 层次结构:
   ```java
      public interface Channel extends AttributeMap, ChannelOutboundInvoker, Comparable<Channel>
   ```
   ![channel的层次结构](../../_media/chapter13_Netty/5_netty核心组件/channel层次结构.png)  
   说明:  
   1. 每一个Channel在初始化的时候都将会被分配一个ChannelPipeLine和ChannelConfig.
   2. 每一个Channel都是独一无二的,Channel实现了java.lang.Comparable接口,从而保证了Channel的顺序.
   3. ChannelConfig包含了该channel的所有设置,并且支持了更新,可以通过`实现ChannelConfig的子类来给Channel设置某些特殊的设置`  
   4. ChannelPipeLine是实现Channel只执行I/O操作所有逻辑的容器,里面包含了许多 实际处理数据的handler了, 本质是一个 双向链表,表头表位分别表示入站出站的起点.
   5. Netty的Channel是线程安全的,所以可以使用多线程对channel进行操作
   6. 通过Channel.write()操作,数据将从链表表尾开始向链表表头移动,通过ChannelHandlerContext.write()是将数据传递给下一个ChannelHandler开始沿着链表移动.  

#### 5.2.2.2 常见方法
   1. `Channel read()`: 从当前Channel中读取数据到第一个inbound缓冲区,如果数据读取成功,触发`ChannelHandler.channelRead(ChannelHandlerContext ctx,
      Object msg)事件`.`read()操作`完毕之后,紧接着触发`ChannelHandler.channelReadComplete(ChannelHandlerContext ctx)事件`.
      如果该channel读请求被挂起,后续的读操作会被忽略.
   2. `ChannelFuture write(Object msg)`: 请求将当前的msg通过ChannelPipeLine(`从pipeline的链表尾开始流动`)写入到Channel中.
      > 注意: write只是将数据存放于channel的缓冲区中,并不会将数据发送出去.要发送数据必须使用flush()方法  
      
   3. `ChannelFuture write(Object msg, ChannelPromise promise)`: 与 `方法2` 作用相同,参数 `promise`是用来写入 `write方法`的执行结果.  
   4. `ChannelFuture writeAndFlush(Object msg)`: 与 `方法2` 作用类似, 不过 `会立即将msg发送出去`  
   5. `ChannelFuture writeAndFlush(Object msg, ChannelPromise promise)`: 与 `方法4` 作用相同, 参数 `promise`是用来写入 `write方法`的执行结果.
   6. `ChannelOutboundInvoker flush()`: 将所有带发送的数据(`存放于channel缓冲区中的数据`),发送出去
   7. `ChannelFuture close()`: 关闭`channel`无论 `close`操作是成功还是失败,都会通知一次`channelFuture对象`(即触发ChannelFuture.
      operationComplete方法). `close操作`会级联触发该channel关联的`channePipeLine`中所有 `出站handler(继承了xxxOutBoundHandler)的close方法`.
      > 注意: 一旦channel 被关闭之后,就无法再使用了
   8. `ChannelFuture disconnect()`: 断开与远程通信对端的连接. `disconnect方法`会级联触发该channel关联的`channePipeLine`中所有 `出站handler(继承了xxxOutBoundHandler)的close方法`
   9. `ChannelFuture disconnect(ChannelPromise promise)`: 断开与远程通信对端的连接,并级联触发所有出站handler的`disconnect方法`,参数`promise`用于设置 
      `diaconnect方法`的执行结果.  
   10. `ChannelFuture connect(SocketAddress remoteAddress)`: 客户端使用指定的服务端地址remoteAddress发起连接请求,如果连接因为应答超时而失败,
       ChannelFuture中的 `connect方法`执行结果就是`ConnectTimeoutException`,连接被拒绝就是`ConnectException`. 
       `connection方法`会级联触发该channel关联的`pipeline`中所有`出站handler`中的`connect方法`.  
       > connect方法有很多的重载方法,可以既连接远程,又绑定本地地址等...  
   11. `ChannelFuture bind(SocketAddress localAddress)`: 绑定本地的socket地址,并级联触发所有`出站handler`中的`bind
       (ChannelHandlerContext, SocketAddress, ChannelPromise)方法`  
       > 重载方法多了一个参数 `promise`,支持对bind操作执行结果的设置  
   
   12. channel信息获取方法:  
   ```java
        ChannelConfig config() // 获取channel的配置信息,如: 连接超时时间
        ChannelMetadata metadata() // 获取channel的元数据描述信息,如TCP配置信息等
        boolean isOpen()  // channel是否已经打开
        boolean isRegistered() // channel 是否已经注册到EventLoop中
        boolean isActive() // channel是否已经处于激活状态
        boolean isWritable() // channel是否可写
        SocketAddress localAddress();  // channel本地绑定地址
        SocketAddress remoteAddress(); // channel 远程通信的远程地址
        ChannelPipeline pipeline(); // 获取channel关联的pipeline  
        ByteBufAllocator alloc(); // 获取channel缓冲区的分配对象,用于分配缓冲区大小  
        EventLoop eventLoop(); // 获取channel绑定的eventLoop(唯一分配一个I/O事件的处理线程)
        ChannelId id(); // 获取channel的唯一标识
        Channel parent();  // serverChannel.parent()返回null,socketChannel返回serverSocketChannel 
   ```  

### 5.2.3 Channel的源码分析: 
   TODO

## 5.3 ByteBuf:  
### 5.3.1 ByteBuf概述:  
1. ByteBuf优化:  
   NIO中ByteBuffer的缺点:  
      - `长度固定`: 一旦ByteBuffer分配完成,其容量就不能动态扩展或者收缩, 容易出现数组越界异常.
      - `操作繁琐`: ByteBuffer所有的读写操作都是基于`position`作为定位指针进行操作,读写操作切换的时候需要使用`flip()`或者`rewind()`方法
      - `功能有限`: ByteBuffer的API功能有限, 一些高级和实用的特性不支持,需要手动实现  
   
   Netty中ByteBuf的优点:  
      - 可以被用户自定义的缓冲区类型扩展
      - 通过内置的复合缓冲区实现了透明的零拷贝  
      - 容量可以按需增长(类似于StringBuilder)  
      - 读写操作使用不同的索引,读写都有专门的api无需来回切换
      - 支持方法的链式调用  
      - 支持引用计数
      - 支持池化  

### 5.3.2 ByteBuf工作原理:  
   1. ByteBuf的数据结构:  
      - `初始化时`:  
         ![ByteBuf数据结构](../../_media/chapter13_Netty/5_netty核心组件/ByteBuf数据结构.png)  
      - `写入部分数据之后`:  
         ![ByteBuf数据结构](../../_media/chapter13_Netty/5_netty核心组件/读写部分数据之后.png)  
      说明:  
         - ByteBuf维护了两个不同的指针: 一个用于读(readerIndex),一个用于写(writeIndex).
         - ReadIndex 和 WriteIndex 的起始位置都是数组下标为0的位置. 
         - 凡是 `read` 或者 `write`开头的api都会让 `readerIndex` 或者 `writeIndex`递增,而 `get` 或者 `set`开头的api不会.
         - ByteBuf有默认的最大长度限制 `Integter.MAX_VALUE`.在这个范围之内可以定义ByteBuf的最大容量,通过`capacity(int)
           `或者`ensureWritable(int)`方法扩容如果超出最大容量会抛出异常.
         - 试图读 `writeIndex`之后的数据,或者视图在`最大容量`之外写数据,会发生数组越界异常  
   2. ByteBuf的使用模式:  
      - `堆缓冲区`:  
        最常用模式,将数据存放在JVM的对空间里面.这种模式又被称为是 `支撑数组`.  
        `优点`: 能够在没有使用 `池化` 的情况下提供快速的分配和释放.`非常适合有遗留数据需要处理的情况`  
        `缺点`: 当需要发送堆缓冲区的数据时,JVM需要在内部把 `堆缓冲区` 中的数据复制到 `直接缓冲区`中.  
        使用示例:  
        ```java
           // 通过netty提供的工具类Unpooled获取Netty的数据容器,ByteBuf
           ByteBuf byteBuf = Unpooled.copiedBuffer("hello netty", Charset.forName("utf-8"));
   
           // 相关方法
           // public abstract boolean hasArray();判断ByteBuf的使用模式(hasArray判断的是 byteBuf是否在堆空间有一个支撑数组),
           // 即数据存放的位置在哪儿 堆/直接缓存区/复合缓冲区
           if (byteBuf.hasArray()){
               //  获取byteBuf的支撑数组
               byte[] array = byteBuf.array();
   
               // 把字节数组转换成字符串
               System.out.println( new String(array,Charset.forName("utf-8")));
   
               // 获取支撑数组的一些信息:
               // 获取支撑数组的偏移量
               System.out.println(byteBuf.arrayOffset());
               // 获取支撑数组的可读索引位置
               System.out.println(byteBuf.readerIndex());
               // 获取支撑数组的可写索引位置
               System.out.println(byteBuf.writerIndex());
               // 获取支撑数组的容量
               System.out.println(byteBuf.capacity());
               // 获取支撑数组中剩余可读元素占多少个字节,这个的大小是相对于readerIndex位置的,
               // 比如下面这个读取方法会导致readerIndex的移动,从而导致readableBytes()变化
               System.out.println(byteBuf.readByte());
               // 但是getByte方法是不会造成readerIndex移动的
               System.out.println(byteBuf.getByte(1));
               System.out.println(byteBuf.readableBytes());
           } 
        ```  
        
      - `直接缓冲区`:  
        `网络数据传输最理想的选择`,直接缓冲区中的数据是常驻在常规会被JVM进行垃圾回收的堆空间之外.
        优点: 由于数据是存储在JVM堆空间之外的直接内存中,在进行网络传输的时候,无需把数据从堆空间复制到直接内存中,提高网络传输时的性能.   
        缺点: 1.分配和释放的开销都十分昂贵;2.如果数据不仅仅是用作网络传输的数据,在服务端还可能对齐进行访问的话,必须要将数据从`直接内存`复制到`堆空间中`来.
        > 建议: 如果缓冲区中的数据,需要被访问的话,堆缓冲区是更好的选择.
        
        使用示例:  
        ```java
           ByteBuf directBuf = Unpooled.directBuffer();
           // public abstract boolean hasArray();判断ByteBuf的使用模式(hasArray判断的是 byteBuf是否在堆空间有一个支撑数组),
           // 如果不是那么就是直接缓冲区
           if (!directBuf.hasArray()){
               int len = directBuf.readableBytes();
               byte[] bytes = new byte[len];
               directBuf.getBytes(directBuf.readerIndex(),bytes);
               // 业务逻辑处理
               handleArray(bytes,0,len);
           }  
        ```

      - `复合缓冲区`:    
        `多个ByteBuf的聚合视图`,可以根据需要添加或者删除ByteBuf实例.Netty通过ByteBuf子类 --- `CompositeByteBuf`实现`复合缓冲区模式`,
        其提供一个将多个缓冲区表示为单个合并缓冲区的虚拟表示.
        > ps:  `CompositeByteBuf`中的byteBuf实例可能同时包含 `直接缓冲区`或者`非直接缓冲区`,此时`hasArray()`方法在只有一个实例的时候,回去判断该实例是否有支撑数组,
        > 存在多个实例的时候`hasArray方法`总是返回`false`

        使用示例:  模拟一个HTTP协议传输的消息,包含两部分 头部和主题,分别由不同的模块生成,在发送数据的时候进行组装.如图:`可以使用CompositeByteBuf来消除每条消息都重复创建这两个缓冲区`  
        ![Composite实例](../../_media/chapter13_Netty/5_netty核心组件/CompositeByteBuf示例.png)
        ```java
            //  -------------------------------  构造 复合缓冲区  -----------------------------
            CompositeByteBuf composited = Unpooled.compositeBuffer();
            // 也可以是堆缓冲区
            ByteBuf headerBuf = Unpooled.directBuffer();
            // 也可以是直接缓冲区,buffer()返回的是一个堆缓冲区
            ByteBuf bodyBuf = Unpooled.buffer();
    
            // 添加缓冲区到复合缓冲区
            composited.addComponents(headerBuf,bodyBuf);
    
            //  .... 业务逻辑
    
            // 删除某个buf,按照添加的顺序,在本例子中,0为headBuf
            composited.removeComponent(0);
            // 遍历获取每一个buf
            for (ByteBuf buf : composited) {
                System.out.println(buf.toString());
            }   
          
            // ------------------------------  访问复合缓冲区  --------------------------------
            public static void byteBufCompositeArray() {
                CompositeByteBuf compBuf = Unpooled.compositeBuffer();
                //获得可读字节数
                int length = compBuf.readableBytes();
                //分配一个具有可读字节数长度的新数组
                byte[] array = new byte[length];
                //将字节读到该数组中
                compBuf.getBytes(compBuf.readerIndex(), array);
                //使用偏移量和长度作为参数使用该数组
                handleArray(array, 0, array.length);
            }
        ```  

### 5.3.3 字节级别操作:

工作时通常ByteBuf的数据结构图:  
![ByteBuf数据结构](../../_media/chapter13_Netty/5_netty核心组件/读写部分数据之后.png)

#### 5.3.3.1 几种不同的字节区间:

1. `可丢弃字节区间`:  
   在ByteBuf中,已经被读取过的字节都会被视为是 `可丢弃的字节`, `0`到 `readerIndex`之间就是 `可丢弃区间`,通过`discardReadBytes()`可以丢弃他们并回收空间.
   `discardReadBytes()`方法会将 `readerIndex`重新指向 `byteBuf`数组开始元素的位置,`writerIndex`会减少相应的数量.
   `discardReadBytes()`调用之后的数据结构:  
   ![ByteBuf数据结构](../../_media/chapter13_Netty/5_netty核心组件/discardReadBytes调用之后.png)
   > 注意:
   > 1. 由于`discardReadBytes()`方法只是移动了`writerIndex`和`可读字节`,但是没有对其他数组空间进行数据擦除,所以可写部分的数据是`没有任何保证的`.
   > 2. 在非必要时刻(内存极低紧张)的时候,不要使用 `discardReadBytes()`将`可丢弃字节区间`转化为`可写区间`,因为该方法必须将`可读区间`中的数据转移到数组开始位置去,这个操作很有可能会发生`内存复制`

2. `可读字节区间`:  
   ByteBuf的可读字节是存储实际数据的区间.`新分配`、`包装`、`复制`的缓冲区默认的`readerIndex`为`0`.  
   任何 `read` 或者 `skip` 开头的方法都会将造成`readerIndex`增加已读字节. 如果 `readBytes(ByteBuf dest)`读取并写入到`dest`会造成 `dest`的`writerIndex`
   增加相应的数量.
   > 读取 `writerIndex`之外的数据将造成数组越界异常

3. `可写字节区间`:  
   可写字节区间是指一个拥有未定义内容的,写入就绪的内存区域.`新分配`的缓冲区默认的`readerIndex`为`0`.  
   任何以 `write` 开头的方法都会造成 `writerIndex`增加以写入字节数.
   如果 `writeBytes(Bytes dest)`读取并写入到`dest` 会造成 `调用` 这个方法的缓冲区的 `readerIndex`增加相应的数量.

#### 5.3.3.2 索引管理:

1. `随机访问索引`: `get或者set`开头的方法来随机`访问或者写入`数据 ByteBuf与普通数组一样,索引总是从 `0`开始,到`capacity - 1`截至.
   > 注意:
   > 1. `get或者set`开头的方法不会造成读写索引的移动
   > 2. 通过`需要索引值为入参`的方法来访问缓冲区,同样不会造成读写索引的移动
   > 3. 在需要的时候可以通过 readerIndex(index) 或者 writerIndex(index) 来移动读写索引

2. `顺序访问索引`: `read()或者write()`方法来顺序 `读取或者写入` 数据
3. `索引管理`:  
   调用 `markReaderIndex()`, `markWriterIndex()` 来标记读写索引  
   调用 `resetWriterIndex()`, `resetReaderIndex()` 来重置读写索引到标记的位置  
   调用 `readerIndex(index)`, `writerIndex(index` 来指定读写索引的位置  
   调用 `clear()` 将读写索引重新指向 `数组起始位置`  
   > 1. `clear()`方法仅仅是重置读写索引为0,不会对buf中的数据进行清除  
   > 2. 相对于 `discardReadBytes()`方法,`clear()`方法更为效率,因为它只是将读写索引重置为了0,不会引发任何的数据复制.

#### 5.3.3.3 派生缓冲区:
  
派生缓冲区为ByteBuf提供以专门的方式呈现 `缓冲区数据` 的视图,常见方法为:  
- `duplicate()`: 返回一个与调用`duplicate()`的缓冲区`共享所有空间`的缓冲区  
- `slice()`: 返回调用`slice()`的缓冲区`整个可读字节区间`的切片  
- `slice(int,int)`: 返回调用`slice()`的缓冲区`部分可读字节区间`的切片
- `readSlice(int length)`: 返回调用`slice()`的缓冲区`可读字节区间中 readerIndex + length`的切片,会将`readerIndex`增加length长度.
- `order(ByteOrder)`:  
- `Unpooled.unmofifiableBuffer(..)`:  
> 注意:
> 1. 上面的方法只是将数据展示出来了,但是与原buffer使用的是同一份数据,修改派生缓冲区的数据也会改变原缓冲区的数据.(`如同ArrayList.subList`)
> 2. 如果想要复制一份独立的数据副本,请使用`copy()`,`copy(int,int)`  

测试:  
```java
        // ----------------------   非共享  -----------------------------------------

        Charset utf8 = Charset.forName("UTF-8");
        //创建 ByteBuf 以保存所提供的字符串的字节
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        //创建该 ByteBuf 从索引 0 开始到索引 15 结束的分段的副本
        ByteBuf copy = buf.copy(0, 15);
        //将打印"Netty in Action"
        System.out.println(copy.toString(utf8));
        //更新索引 0 处的字节
        buf.setByte(0, (byte)'J');
        //将会成功，因为数据不是共享的
        assert buf.getByte(0) != copy.getByte(0);

        // ----------------------   共享  -----------------------------------------
        //创建一个用于保存给定字符串的字节的 ByteBuf
        ByteBuf bufForSlice = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        //创建该 ByteBuf 从索引 0 开始到索引 15 结束的一个新切片
        ByteBuf sliced = bufForSlice.slice(0, 15);
        //将打印"Netty in Action"
        System.out.println(sliced.toString(utf8));
        //更新索引 0 处的字节
        bufForSlice.setByte(0, (byte)'J');
        //将会成功，因为数据是共享的，对其中一个所做的更改对另外一个也是可见的
        assert bufForSlice.getByte(0) == sliced.getByte(0);
```

#### 5.3.3.4 查找字节所在的位置:
![1](../../_media/chapter13_Netty/5_netty核心组件/查找操作.png)  
一般的字节可以通过 `indexOf()` 方法来查找指定的字节,或者通过传入 `ByteProcessor参数` 设定`中止字符`来配合`forEachByte()方法`帮助查找.
```java
    /**
     * Aborts on a {@code NUL (0x00)}.
     */
    ByteProcessor FIND_NUL = new IndexOfProcessor((byte) 0);

    /**
     * Aborts on a non-{@code NUL (0x00)}.
     */
    ByteProcessor FIND_NON_NUL = new IndexNotOfProcessor((byte) 0);

    /**
     * Aborts on a {@code CR ('\r')}.
     */
    ByteProcessor FIND_CR = new IndexOfProcessor(CARRIAGE_RETURN);

    /**
     * Aborts on a non-{@code CR ('\r')}.
     */
    ByteProcessor FIND_NON_CR = new IndexNotOfProcessor(CARRIAGE_RETURN);

    /**
     * Aborts on a {@code LF ('\n')}.
     */
    ByteProcessor FIND_LF = new IndexOfProcessor(LINE_FEED);

    /**
     * Aborts on a non-{@code LF ('\n')}.
     */
    ByteProcessor FIND_NON_LF = new IndexNotOfProcessor(LINE_FEED);

    /**
     * Aborts on a semicolon {@code (';')}.
     */
    ByteProcessor FIND_SEMI_COLON = new IndexOfProcessor((byte) ';');

    /**
     * Aborts on a comma {@code (',')}.
     */
    ByteProcessor FIND_COMMA = new IndexOfProcessor((byte) ',');

    /**
     * Aborts on a ascii space character ({@code ' '}).
     */
    ByteProcessor FIND_ASCII_SPACE = new IndexOfProcessor(SPACE);

    /**
     * Aborts on a {@code CR ('\r')} or a {@code LF ('\n')}.
     */
    ByteProcessor FIND_CRLF = new ByteProcessor() {
        @Override
        public boolean process(byte value) {
            return value != CARRIAGE_RETURN && value != LINE_FEED;
        }
    };

    /**
     * Aborts on a byte which is neither a {@code CR ('\r')} nor a {@code LF ('\n')}.
     */
    ByteProcessor FIND_NON_CRLF = new ByteProcessor() {
        @Override
        public boolean process(byte value) {
            return value == CARRIAGE_RETURN || value == LINE_FEED;
        }
    };

    /**
     * Aborts on a linear whitespace (a ({@code ' '} or a {@code '\t'}).
     */
    ByteProcessor FIND_LINEAR_WHITESPACE = new ByteProcessor() {
        @Override
        public boolean process(byte value) {
            return value != SPACE && value != HTAB;
        }
    };

    /**
     * Aborts on a byte which is not a linear whitespace (neither {@code ' '} nor {@code '\t'}).
     */
    ByteProcessor FIND_NON_LINEAR_WHITESPACE = new ByteProcessor() {
        @Override
        public boolean process(byte value) {
            return value == SPACE || value == HTAB;
        }
    };
```

### 5.3.4 ByteBuf常见API总结:  

#### 5.3.4.1 顺序读操作:  
   ![1](../../_media/chapter13_Netty/5_netty核心组件/顺序读操作1.png)  
   ![1](../../_media/chapter13_Netty/5_netty核心组件/顺序读操作2.png)  
   ![1](../../_media/chapter13_Netty/5_netty核心组件/顺序读操作3.png)  
   ![1](../../_media/chapter13_Netty/5_netty核心组件/顺序读操作4.png)   

#### 5.3.4.2 顺序写操作:  
   ![1](../../_media/chapter13_Netty/5_netty核心组件/顺序写操作1.png)  
   ![1](../../_media/chapter13_Netty/5_netty核心组件/顺序写操作2.png)  
   ![1](../../_media/chapter13_Netty/5_netty核心组件/顺序写操作3.png)  
   ![1](../../_media/chapter13_Netty/5_netty核心组件/顺序写操作4.png)  

#### 5.3.4.3 随机写操作:  
   ![1](../../_media/chapter13_Netty/5_netty核心组件/随机写操作.png)  

#### 5.3.4.4 随机读操作:  
   ![1](../../_media/chapter13_Netty/5_netty核心组件/随机读操作.png)

#### 5.3.4.5 其他操作:  
   ![1](../../_media/chapter13_Netty/5_netty核心组件/其他操作.png)

### 5.3.5 ByteBuf辅助工具类:

#### 5.3.5.1 ByteBufHolder接口:   
`ByteBufHolder`是`ByteBuf`的容器,除了实际数据装载之外,我们还需要存储各种属性值.比如HTTP的请求和响应都可以携带消息体,在Netty中消息体就是用`ByteBuf`来表示;
但是由于不同的协议之间会包含不同的协议字段和功能,这部分数据并不适合写在实际数据中,所以Netty抽象出了一个 `ByteBufHolder接口`持有一个`ByteBuf`用以装载实际数据,
同时携带不同协议的协议字段和功能. (ByteBufHolder的实现类实现不同协议的协议字段和功能描述).  
![1](../../_media/chapter13_Netty/5_netty核心组件/不同协议的ByteBufHolder实现类.png)  
说明:  
- `ByteBufHolder`为Netty的高级特性提供了支持,比如缓冲区池化,其中可以从池中借用ByteBuf,并且在需要时自动释放.  
- `通常用作需要存储实际数据的消息对象接口`
- `常用方法`:  
   ![1](../../_media/chapter13_Netty/5_netty核心组件/ByteBufHolder的常用方法.png)  

#### 5.3.5.2 ByteBuf内存空间分配:  

1. `按需分配 --- ByteBufAllocator接口`:  
    1. 为了降低分配和释放的内存开销,Netty通过`interface ByteBufAllocator`实现了缓冲区池化,`ByteBufAllocator`可以用来分配我们所描述过得任意类型的ByteBuf实例.
    `池化`不会改变`ByteBuf`api 的语义,只是新的数据需要使用ByteBuf来存储的时候,可以从缓冲池中取出一个 `ByteBuf实例` 来存储数据.
    2. 常用方法:  
       ![1](../../_media/chapter13_Netty/5_netty核心组件/ByteBufAllocator常用方法.png)  
       
    3. 说明:  
        - 可以通过 `channel`(每个都可以有一个不同的ByteBufAllocator实例) 或者 绑定到 
          `channelHandler`的`ChannelHandlerContext`获取到一个`ByteBufAllocator`的引用.  
        - Netty提供了  ByteBufAllocator 的实现:  
          `PooledByteBufAllocator`:  池化ByteBuf实例,提高性能并最大限度的减少内存碎片.使用的是jemalloc技术实现的内存分配.
          `UnpooledByteBufAllocator`:  非池化ByteBuf实例分配,每一次调用都会返回一个新的byteBuf实例.  
          > 1. netty4.1版本默认使用的是 `PooledByteBufAllocator`, 4.0版本默认使用的则是 `UnpooledByteBufAllocator`.
          > 2. 可以通过`ChannelConfig`或者`Bootstrap`配置 使用什么类型的 `ByteBufAllocator`.
        - `ioBuffer()`: 这个方法在使用的时候,如果当前运行环境有 sun.misc.Unsafe 支持的时候, 返回的是 `Direct ByteBuf`,否则返回的是 `Heap ByteBuf`.  
          当使用 `PreferHeapByteBufAllocator`的时候, 只会返回 `Heap ByteBuf`.
          
2. `Unpooled缓冲区`:  
在某些情况下,如果未能获取到一个 `ByteBufAllocator`的引用.可以通过工具类 `Unpooled` 来创建未池化的 `ByteBuf实例`.  
![1](../../_media/chapter13_Netty/5_netty核心组件/Unpooled工具类.png)  

3. `ByteBufUtil类`:  
   `ByteBufUtil` 提供了用于操作 `ByteBuf` 的静态的辅助方法。因为这个API是通用的，并且和池化无关，所以这些方法已然在分配类的外部实现。
   这些静态方法中最有价值的可能就是 hexdump()方法，它以十六进制的表示形式输出`ByteBuf` 的内容。这在各种情况下都很有用，例如，出于调试的目的记录 `ByteBuf` 的内容。十
   六进制的表示通常会提供一个比字节值的直接表示形式更加有用的日志条目，此外，十六进制的版本还可以很容易地转换回实际的字节表示。
   另一个有用的方法是 `boolean equals(ByteBuf, ByteBuf)`，它被用来判断两个 `ByteBuf`实例的相等性。

### 5.3.6 引用计数:  

- `引用计数`: 一种通过在某个对象所持有的资源不再被其他对象引用时,释放该对象所持有的资源来优化内存使用和性能的技术.(`类似于JVM的引用计数算法`)  
- Netty 在第 4 版中为 `ByteBuf` 和 `ByteBufHolder` 引入了引用计数技术，它们都实现了 `interface ReferenceCounted`.  
- `引用计数实现的大致思路`: 它主要涉及跟踪到某个特定对象的活动引用的数量。一个 `ReferenceCounted` 实现的实例将通常以活动的引用计数为 `1`作为开始。只要`引用计
  数`大于 `0`，就能保证对象不会被释放。当活动引用的数量减少到 0 时，该实例就会被释放。
  > 注意: 虽然释放的确切语义可能是特定于实现的，但是至少已经释放的对象应该不可再用了。
- `引用计数`对于 `池化实现`（如 PooledByteBufAllocator）来说是至关重要的，它降低了内存分配的开销。
- 试图访问一个已经被`释放(或者说是 引用计数为 0)`的引用计数的对象，将会导致一个 `IllegalReferenceCountException`。  
- `谁来释放`: 一般是由 最后访问资源的一方 来负责释放 资源.  
> 一个特定的（ReferenceCounted 的实现）类，可以用它自己的独特方式来定义它的引用计数规则。例如，我们可以设想一个类，其 `release()` 方法的实现总是将引用计数设为
零，而不用关心它的当前值，从而一次性地使所有的活动引用都失效。  


### 5.3.7 ByteBuf源码分析:  

TODO


## 5.4 ChannelHandler和ChannelPipeline:  

### 5.4.1 ChannelHandler和ChannelPipeline的概述:  
`Channel,ChannelPipeline,ChannelContext,ChannelHandler之间的关系`:  

![关系图](../../_media/chapter13_Netty/5_netty核心组件/channelhandler关系.png)

Netty的ChannelPipeline和ChannelHandler机制类似于Servlet和Filter过滤器,这类拦截器实际上是责任链模式的一种变形,主要是为了方便事件的拦截和用户业务逻辑的定制.  
`ChannelHandler`: 从应用程序开发人员的角度来看,Netty的主要组件是ChannelHandler,它充当了所有处理入站和出站数据的应用程序逻辑的容器,ChannelHandler可以适用于任何逻辑操作.  
`ChannelPipeline`: Netty将Channel的数据管道抽象为 `ChannelPipeline`,消息在 `ChannelPipeline`中流动和传递.
`ChannelPipeline`持有I/O事件拦截器`ChannelHandler`的链表, 由 `ChannelHandler` 对I/O事件进行拦截和处理,可以方便的通过增删handler来实现不同业务逻辑的定制,不需要对已有
handler修改,就能实现对修改封闭和对扩展的支持.  
`ChannelHandlerContext`: 当`ChannelHandler`每一次被分配到一个`ChannelPipeline`的时候,都会创建一个新的`ChannelHandlerContext`与`ChannelHandler`关联起来,
其表示`ChannelHandler`与`ChannelPipeline`的绑定关系.

### 5.4.2 ChannelHandler:  

#### 5.4.2.1 Channel的生命周期:  

`Channel`接口定义了一组和 `ChannelInBoundHandler`api 密切相关的简单但是功能强大的状态模型.`Channel的4个状态` :  
![channel生命周期](../../_media/chapter13_Netty/5_netty核心组件/Channel生命周期.png)  
![channel状态模型](../../_media/chapter13_Netty/5_netty核心组件/CHANNEL状态模型.png)  
说明:  
- 只要`Channel`没有关闭,`Channel`就可以再次被注册到`EventLoop`组件上.
- 当图片中的状态一旦发生改变的时候,就会生成对应的事件,这些事件会被转发给 `ChannelPipeline` 中的 `ChannelHandler`处理.  

#### 5.4.2.2 ChannelHandler的生命周期: 
`ChannelHandler`定义的生命周期操作,`ChannelHandler`被添加到`channelPipeline`或者从`channelPipeline`中移除的时候会触发这些操作.
每个方法都会接收一个`ChannelHandlerContext`作为参数
![ChannelHandler生命周期](../../_media/chapter13_Netty/5_netty核心组件/handler的生命周期.png)  
`channelHandler`两个重要的子接口:  
- `ChannelInboundHandler` : 处理入栈数据以及各种状态变化
- `ChannelOutboundHandler` : 处理出站数据并且允许拦截所有的操作

#### 5.4.2.3 `ChannelInboundHandler`接口:  
![ChannelInboundHandler](../../_media/chapter13_Netty/5_netty核心组件/inboundHandler生命周期.png)  
说明:  

- 上面是 `ChannelInboundHandler`的生命周期方法,channel中的`数据被读取`或者`channel状态发生变化`的时候被调用.
- 当 `ChannelInboundHandler`的子类类重写了`channelRead()`方法的时候,需要手动通过 `ReferenceCountUtil.release()`来手动释放与`池化ByteBuf有关的内存
(即参数msg)`
```java
         @Sharable
         public class DiscardHandler extends ChannelInboundHandlerAdapter{
             @Override
             public void channelRead(ChannelHandlerContext ctx,Object msg){
                 // 如果不手动释放,Netty会通过日志的形式记录msg未释放的实例
                 ReferenceCountUtil.release(msg);
             }  
         }
```
- 也可以通过 `SimpleChannelInboundHandler`来自动释放资源,ps: 不要试图把 `SimpleChannelInboundHandler`中的数据存放起来,以便后期使用.
```java
         @Sharable
         public class DiscardHandler extends ChannelInboundHandlerAdapter{
             @Override
             public void channelRead(ChannelHandlerContext ctx,Object msg){
                 // 在这里就不需要手动释放msg所占用的byteBuf空间了
             }  
         }
```

#### 5.4.2.3 `ChannelOutboundHandler`接口:  

出站操作和数据将由 `ChannelOutboundHandler`处理.`ChannelOutboundHandler`的方法将会被 `Channel` , `ChannelPipeline` ,以及 
`ChannelHandlerContext` 调用.  
`ChannelOutboundHandler` 可以按照需要`推迟操作` 或者 `推迟事件`, 这可以通过一些复杂的方法来处理请求.   
![ChannelOutboundHandler](../../_media/chapter13_Netty/5_netty核心组件/outbound生命周期.png)  

#### 5.4.2.4 `ChannelHandlerAadptor`:  

![ChannelHandlerAdaptor](../../_media/chapter13_Netty/5_netty核心组件/adaptor的层次结构.png)  
说明:  
- `ChannelInboundHandlerAdapter` 和 `ChannelOutboundHandlerAdapter` 类分别提供了 `ChannelInboundHandler`和 `ChannelOutboundHandler` 
  的基本实现。通过扩展抽象类 `ChannelHandlerAdapter`，它们获得了它们共同的超接口 ChannelHandler 的方法。  
- `ChannelHandlerAdapter` 还提供了实用方法 `isSharable()`。如果其对应的实现被标注为 `@Sharable`，那么这个方法将返回 `true`，表示它可以被添加到多个 
  `ChannelPipeline`中.
- 在 `ChannelInboundHandlerAdapter` 和 `ChannelOutboundHandlerAdapter` 中所提供的方法体调用了其相关联的 `ChannelHandlerContext` 上的等效方法(
  fireXXX())， 从而将事件转发到了 `ChannelPipeline` 中的下一个 `ChannelHandler` 中。

#### 5.4.2.5 防止内存泄露:  

- 每当通过调用 `ChannelInboundHandler.channelRead()`或者 `ChannelOutboundHandler.write()`方法来处理数据时，都需要保证不会出现资源泄漏(buf没有释放)。
Netty 使用引用计数来处理池化的 ByteBuf。所以在完全使用完某个 ByteBuf 后，调整其引用计数是很重要的。  
- Netty提供了class ResourceLeakDetector ， 它将对你应用程序的缓冲区分配做大约 1%的采样来检测内存泄露。  
  Netty定义的4种`泄漏检测级别`:  
  ![netty内存检测级别](../../_media/chapter13_Netty/5_netty核心组件/内存检测级别.png)
  > 可以通过启动命令: `java -Dio.netty.leakDetectionLevel=ADVANCED` 来设置内存检测级别
  
  检测结果: `存在内存泄漏`如图   
  ![netty内存检测结果](../../_media/chapter13_Netty/5_netty核心组件/检测结果.png)
  
### 5.4.3 ChannelPipeline:  

- `ChannelPipeline` : 是一个流经Channel的入站和出站事件的ChannelHandler实例双向链表.
- `ChannelPipeline`在`Channel`创建的时候,就会分配给`Channel`并与之关联,直到`Channel`关闭.在`Channel`整个生命周期中,`ChannelPipeline`都是唯一与之绑定的,
既不能 `新增pipeline`也不能`分离pipeline`.  
- 根据事件的类型,事件将会被 `ChannelInboundHandler`或者`ChannelOutboundHandler`处理.`ChannelHandler`之间通过上下文对象`ChannelHandlerContext`交互.
- `ChannelHandlerContext`可以让`ChannelHandler`之间进行交互,也可以动态的修改 `ChannelPipeline`中 handler的顺序.
- 入站口总是 `处理入站事件`的 `ChannelInboundHandler`(即链表的head总是指向`能够`处理入站事件的处理器),出站口总是 `处理出站事件`的`ChannelOutboundHandler`(即
  链表的tail总是指向`能够`处理出站事件的处理器)
- `ChannelHandler`在`ChannelPipeline`执行顺序是根据`handler`被加入到双向链表中的顺序而决定的
  > 一个处理器,可能同时实现 `ChannelInboundHandler` 和 `ChannelOutboundHandler`.在`Netty`中事件流经pipeline的时候通过标志位判断当前处理器能不能处理该事件,直到找到一个
  > 能够处理该事件的handler为止.
#### 5.4.3.1 获取pipeline:  
- 通过Channel获取
```java
        bootstrap = new Bootstrap()
        .group(clientGroup)
        .channel(NioSocketChannel.class)
        // 这里实现的是中途断线重连,断线换成netty术语就是channel不活跃了
        .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                // ch.pipeline就是获取pipeline
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    // channel在运行过程中中断,就会触发这个(channelInactive)方法,然后通过这个方法去重新连接
                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        ctx.channel().eventLoop().schedule(()-> doConnect(),2, TimeUnit.SECONDS);
                    }
                });
            }
        });
```
- 通过ChannelHandlerContext获取: 
```java
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel curChannel = ctx.channel();  
        // 获取pipeline
        ChannelPipeline pipeline = ctx.pipeline();
    }
```

#### 5.4.3.2 处理ChannelPipeline中的handler:

1. `ChannelPipeline`的修改,实际上是修改双向链表上的handler,`ChannelPipeline`提供了一些对handler进行crud的方法:  
![修改pipeline中ChannelHandler](../../_media/chapter13_Netty/5_netty核心组件/修改pipeline中的handler.png)  
```java
ChannelPipeline pipeline = ..; // 通过channel或者channelHandlerContext可以获取pipeline
FirstHandler firstHandler = new FirstHandler(); 
pipeline.addLast("handler1", firstHandler); // 加入
pipeline.addFirst("handler2", new SecondHandler()); 加入
pipeline.addLast("handler3", new ThirdHandler()); 加入
        // 此时pipeline中的顺序为 handler2 -> handler1 -> handler3
...
pipeline.remove("handler3");
pipeline.remove(firstHandler);
pipeline.replace("handler2", "handler4", new ForthHandler());
// 此时pipeline中仅剩 handler4
```
> 由于整个pipeline中的channelHandler都是由与channel绑定的eventLoop来执行的,所以如果要在handler中使用阻塞api,为了不降低整体的I/O性能,在`添加handler`的时候,
> 可以使用pipeline中带有EventExecutorGroup的add()方法,`将handler交给ExecutorGroup中的线程去执行而不是有eventLoop线程执行`.
2. 访问handler:  
![访问handler](../../_media/chapter13_Netty/5_netty核心组件/访问handler.png)  

#### 5.4.3.3 事件的传递:  
`入站操作`:  实际上就是通知pipeline中下一个handler调用对应的接口:  
![访问handler](../../_media/chapter13_Netty/5_netty核心组件/ChannelPipeline传递事件.png)
> 调用channel或者pipeline对象的上述方法,会将整个事件沿着pipeline进行传播,但是通过handlerContext中的上述方法只会将事件传递给pipeline中下一个能够处理该事件的handler.

`出站操作`:  许多方法会造成底层套接字上发生一些列的动作  
![访问handler](../../_media/chapter13_Netty/5_netty核心组件/pipeline的出站操作.png)

### 5.4.4 ChannelHandlerContext:  

![访问handler](../../_media/chapter13_Netty/5_netty核心组件/context与handler之间的关系.png)

- `ChannelHandlerContext`代表 `handler` 和 `pipeline`之间的关联关系,只要有 `handler`分配到`pipeline`中来,就创建一个`context`与`handler关联`.
- `ChannelHandlerContext`主要作用是 用来与`context`关联的`handler`和其他`同一个pipeline中的handler`之间的交互 --- 将消息传递给下一个能够处理该消息的handler
- `ChannelHandlerContext`与`handler`的关联关系是永远不会改变的,缓存Context的引用是线程安全的.
- `ChannelHandlerContext`常用方法总结:  
  ![context常用方法](../../_media/chapter13_Netty/5_netty核心组件/context常用方法.png)

- `ChannelHandlerContext`与其他用同名方法的组件相比,产生的事件流更短,可以利用这个特性来获取最大的性能,`特性使用场景如下:`
    - 为了减少将事件传经对它不感兴趣的handler所带来的开销
    - 为了避免将事件传经那些可能会对他感兴趣的handler
    
- `ChannelHandlerContext`只能于一个handler绑定,但是handler可以绑定多个context实例.
    - 正确示例: 
    ```java
        @sharable
        public class SharableHandler extends ChannelInboundHandlerAdapter{
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object meg){ 
                ctx.fireChannelRead(msg);
            }         
        }
    ```
    - 错误示例:
    ```java
        @sharable
        public class SharableHandler extends ChannelInboundHandlerAdapter{
            private int count;
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object meg){ 
                // 对共享资源不保证线程安全的修改操作,会导致问题
                count++;
                ctx.fireChannelRead(msg);
            }         
        }
    ```
    > handler必须添加@Shareble注解,并且handler必须要是线程安全的.
  
- `ChannelHandlerContext`特殊用法:  
    保存`ChannelHandlerContext`在其他channel中使用或者以供稍后使用,完成一些特殊的操作
    ```java
        public class WriteHandler extends ChannelHandlerAdapter { 
            private ChannelHandlerContext ctx;
            @Override
            public void handlerAdded(ChannelHandlerContext ctx) {
                this.ctx = ctx;
            }
            public void send(String msg) {
                ctx.writeAndFlush(msg);
            }
        }
    ```  
  

### 5.4.5 异常处理:  

#### 5.4.5.1 入站异常处理:  
如果在处理入站事件的过程中,发生了异常,那么异常将会从当前handler开始在pipeline中的`InboundHandler`传递下去.
- 实现异常处理: 通过重写exceptionCaught方法
    ```java
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
    ```
    ```java
        public class EchoServerHandler extends ChannelInboundHandlerAdapter {
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                cause.printStackTrace();
                ctx.close();
            }
        }
    ```
- 注意事项:  
    1. 默认的exceptionCaught()方法只是简单的将当前异常转发给了pipeline中的handler
    2. 如果异常到达了pipeline的尾部仍然没有处理,netty会在日志这种记录该异常为未处理异常, `通常为了所有异常都会被处理,pipeline的最后都会存在一个实现了示例代码的handler`
    3. 通过重写exceptionCaught()来自定义自己的异常处理方法.  

#### 5.4.5.2 出站异常处理:
通过异步通知机制,来处理出站异常:  
- 每个出站操作都会返回一个ChannelFuture.注册到ChannelFuture的ChannelFutureListener将在操作完成的时候通知该操作是否成功
- 几乎所有的ChannelOutboundHandler上的方法都会传入一个ChannelPromise的实例.channelPromise是channelFuture的子类,可以用于异步通知也可以用于立即通知的可写方法
    ```java
        ChannelPromise setSuccess();
        ChannelPromise setFailure(Throwable cause);
    ```
  
- 处理异常示例:  
    方法1: 通过向出站操作返回的ChannelFuture中添加具体的监听器
    ```java
        ChannelFuture future = channel.write(someMessage)
        future.addListener(new ChanelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture f){
                if(!f.isSuccess()){
                    f.cause().printStackTrace();
                    f.channel().close();
                }     
            }     
        })
    ```
    <font color=red>**启动重连的示例**</font>
    ```java
        // 注意这里一定不要调用sync()
        ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(remoteHost, port));
        // 添加链接事件的监听器,当连接事件触发的时候就会调用监听器里面的operationComplete方法
        connectFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()){
                System.out.println(" 连接成功........");
            }else{
                System.out.println(String.format(" 连接失败,正在尝试重连 ..... 当前为 第 %s 次重连",retryTime.intValue()));
                future.channel().eventLoop().schedule(()->doConnect(),1,TimeUnit.SECONDS);
            }
        });
    ```
    方法2: 给出站操作的参数 promise添加listener.
    ```java
        public class OutboundExceptionHandler extends ChannelOutboundHandlerAdapter {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                promise.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture f) {
                        if (!f.isSuccess()) {
                            f.cause().printStackTrace();
                            f.channel().close();
                        } 
                    }
                });
            } 
        }
    ```
    > 两种方法如何选择: 不同出站事件的细节异常处理使用`方法一`更好, `方法二`更适合于一般的异常处理


## 5.5 EventLoopGroup和EventLoop:  

**继承结构**  
![NioEventLoop继承结构](../../_media/chapter13_Netty/5_netty核心组件/NioEventLoop继承结构.png)  
### 5.5.1 EventLoop: 

#### 5.5.1.1 概述:
运行任务来处理在连接的生命周期内发生的事件是任何网络框架的基本功能.这种功能在编程上的构造称为`事件循环`,Netty使用`EventLoop`表示.  
事件循环的大致思路:  
```java
    // 只要线程没有被关闭,就一直循环执行
    while (!terminated) {
        // 阻塞获取就绪事件
        List<Runnable> readyEvents = blockUntilEventsReady();
        // 遍历就绪事件,逐一执行
        for (Runnable event : readEvents) {
            ev.run();
        }
    }
```  
1. 在Netty所提供的线程模型中 `EventLoop` 将由一个永远不会改变的 线程 驱动(即由这个线程来处理`EventLoop`中就绪的事件).   
2. 任务可以直接提交给`EventLoop的实现类`,根据具体需求直接直接执行或者调度执行.  `EventLoop`继承了JUC中的延迟任务线程池`ScheduledExecutorService`,但是
   只定义了一个用来返回 `EventLoop`属于哪一个`EventLoopGroup`的方法(`parent()`)
3. 根据配置和cpu核心数量不同,可能会创建多个EventLoop实例来优化资源的使用率,当个 `EventLoop`可以被分配给多个 `Channel`.  
#### 5.5.1.2 I/O事件处理:
1. Netty3 中的I/O事件处理:
   - Netty3 中使用的线程模型只保证了入站事件会在I/O线程(Netty4中的`EventLoop`)中执行.但是出站事件都是由调用线程处理,该线程可以能是`EventLoop`或者其他线程,这样一来
   handler中执行的代码就有可能被多个线程并发执行,就需要在handler中处理线程安全问题.  
     比如: 在不同的线程中调用Channel.write(),同一个Channel中同时触发了出站事件.
   - Netty3 模型中如果出站事件触发了入站事件,可能造成一次额外的线程上下文切换.  
     比如: Channel.write()方法造成异常的时候,需要生成并触发一个exceptionCaught事件.在Netty3 模型中 exceptionCaught是一个入站事件,需要在调用线程中执行,然后将事件\
     交给I/O线程处理,造成依次不必要的线程上下文切换.
     
2. Netty4 中的I/O事件处理:  
   - 所有的I/O操作和事件都交给 驱动`EventLoop`永远不会改变的线程处理.解决了 `handler需要注意线程安全的问题` 和 `不必要的线程切换`

#### 5.5.1.3 I/O事件处理:
1. JDK的任务调度API:  通过 `ScheduledExecutorService`来完成
2. `EventLoop`来调度任务:  
   - 延迟多少时间后执行一次:  
   ```java
        Channel ch = ...
        // 60 秒之后执行一次,之后不再执行
        ScheduledFuture<?> future = ch.eventLoop().schedule( new Runnable() {
                @Override
                public void run() {
                    System.out.println("60 seconds later");
                }
        }, 60, TimeUnit.SECONDS);
   ```  
   - 定时任务:  
   ```java
        Channel ch = ...
        // 60秒后,执行第一次,之后每60秒执行一次.
        ScheduledFuture<?> future = ch.eventLoop().scheduleAtFixedRate(
            new Runnable() {
                @Override
                public void run() {
                System.out.println("Run every 60 seconds");
            }
        }, 60, 60, TimeUnit.Seconds);
   ```
   - 任务的取消: 通过future来取消或者检查任务的执行状态  
   ```java
        ScheduledFuture<?> future = ch.eventLoop().scheduleAtFixedRate(...);
        // Some other code that runs...
        // 任务的取消
        boolean mayInterruptIfRunning = false;
        future.cancel(mayInterruptIfRunning);
   ```
   说明:
   1. netty的任务调度是否立即执行是取决于调用任务的线程是否是 与`EventLoop`绑定的线程,即`负责处理`该channel事件的线程.如果是那么将会立即执行,如果不是那么将会把任务放入延时队列中,
   在 `EventLoop` 下次处理它的事件的时候,会去处理这些任务.`保证了任务不会被多个线程同时执行`.  
   2. `EventLoop`的任务队列,是 `EventLoop独有的`,独立于其他EventLoop.
   3. 调度任务执行的逻辑:  
   ![调度任务执行逻辑](../../_media/chapter13_Netty/5_netty核心组件/eventLoop调度任务执行的逻辑.png)
### 5.5.2 EventLoopGroup:

从上面的继承结构图中可以看出 `EventLoopGroup`实际上就是一个 `ScheduledExecutorService` 延时任务线程池.
`EventLoopGroup`主要功能:  
1. 作为一个线程池管理 `EventLoop实例`
2. 将channel注册到eventLoop的selector上
3. 
## 5.6 Future和Promise:
