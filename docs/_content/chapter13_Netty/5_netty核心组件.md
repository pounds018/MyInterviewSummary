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
1. 层次结构:
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
   
2. 常见方法
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
       > 重载方法多了一个参变 `promise`,支持对bind操作执行结果的设置  
   
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
3. Channel的工作原理: 
   TODO

## 5.3 ByteBuf:  
## 5.4 ChannelHandler和ChannelPipeline:  
## 5.5 EventLoopGroup和EventLoop:  
## 5.6 Future和Promise:
