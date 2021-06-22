## 5.1 核心组件概述:  
1. Channel:  
   Channel是NIO 三大核心组件之一, Channel是输入输出硬件设备与内存之间的一个通道的抽象,channel当做是 数据传输的载体,因此 channel可以被打开或者关闭,
   可以连接或者断开连接.
2. ByteBuf:  
   ByteBuf是Netty在Nio的ByteBuffer基础上的扩展,Netty的核心数据容器.
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
## 5.3 ByteBuf:  
## 5.4 ChannelHandler和ChannelPipeline:  
## 5.5 EventLoopGroup和EventLoop:  
## 5.6 Future和Promise:
