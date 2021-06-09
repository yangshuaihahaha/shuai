# 简介
Netty是一个异步的，基于事件驱动的网络应用框架，用以快速开发高性能，高可靠的网络IO程序

# 封装结构是 
TCP --> java原生的io/网络编程 --> NIO --> Netty

# 应用场景
1）作为高性能的RPC框架的基础通信组件
2）大型网络游戏
3）大数据领域

# IO模型的基本说明
简单理解就是用什么样的通道进行数据的发送和接受，很大程度上决定了程序通信的性能
java支持三种网络编程模型/IO模式：BIO, NIO, AIO

1）BIO:
同步并阻塞，服务器的实现模式为一个连接一个线程，即客户端有连接请求时服务器端就需要启动一个线程处理
如果这个线程不做任何事情，就会造成资源浪费
场景适用于连接数目小，并且一次发送大量数据的场景，对服务器资源要求比较高
2）NIO:
同步非阻塞，服务器端实现为一个线程处理多个连接，即客户端发送的线程都会注册到多路
复用器上，多路复用器轮询有IO请求就进行处理
场景适用于连接数目多且连接比较短的框架，比如聊天服务器，弹幕系统
3）AIO:
异步非阻塞，有效的请求才启动线程，它的特点是由操作系统完成后才通知服务端程序启动
线程去处理，一般适用于连接数较多且连接时间较长的应用
场景适用于连接数目比较多且连接比较长的架构，比如相册服务器

# NIO的三大核心组件
Selector，Channel，Buffer
1）每个channel都会对应一个Buffer
2）Selector对应一个线程，一个线程对应多个channel
3）程序切换到哪个channel是由事件决定的，Event是一个重要的概念
4）Selector会根据不同的事件，在各个通道上进行切换
5）数据的读取或写入是通过Buffer，这个和BIO不同，BIO中要么是输入流要么是输出流，不能双向
但是NIO的Buffer是可以读也可以写的，但是要通过flip方法进行切换
6）channel是双向的，可以返回底层操作系统的情况，比如Linux底层的通道就是双向的

# Buffer的几个重要属性
Capacity 容量，即可以容纳的最大量数据，在缓冲区创建时被设定不能改变
Limit 表示缓冲区的当前终点，不能对缓冲区超过极限的位置进行读写操作，且极限是可以修改的
Position 位置，下一个要被读或写的元素的索引，每次读写缓冲区数据时会改变值，为下次读写做准备
Mark 标记

# 通道（Channel）
NIO的通道类似于流，但是有区别

通道可以进行同时的读写，而流只能读或者只能写
通道可以实现异步读写数据
通道可以从缓冲读取数据，也可以写数据到缓冲区

常用的Channel
FileChannel：用于文件的读写
DatagramChannel：用于UDP的数据读写
ServerSocketChannel和SocketChannel：用于TCP数据读写

# NIO线程模型
## 1）传统阻塞的IO服务模型
### 特点：
采用阻塞IO获取输入的数据，每个连接都需要独立的线程完成数据的输入，
业务处理，数据返回
### 缺点：
并发很大时会创建大量的线程，占用系统资源
连接创建后，如果当前线程没有数据可读，会阻塞read操作，造成资源浪费

## 2）Reactor反应模式
### 1, 单reactor单线程
#### 实现：
Reactor通过Selector监控客户端的请求，收到请求后创建Handler对象处理连接后续的处理
#### 优点
模型简单，没有多线程，进行通信，竞争的问题
#### 缺点
性能不足，只有一个线程，在Handler处理某个连接业务时，整个线程无法处理其他的事件
导致性能瓶颈。
可靠性不足，线程意外终止，导致整个系统通信模块不可用
####使用场景
客户端数量有限，业务处理速度非常快

### 2, 单reactor多线程
#### 实现
Reactor通过Selector监控客户端的请求，如果是连接请求通过Acceptor创建Handler对象，
处理完成连接后的事件。如果不是连接请求，则调用连接对应的Handler来处理，这里的Handler
只负责响应事件，不做具体的业务处理，会分发给后面的worker线程池处理具体的业务
#### 优点：
充分利用多核cpu的处理能力
####缺点
多线程数据共享访问比较复杂。
reactor处理所有的事件和响应，在单线程中运行，在高并发场景容易出现瓶颈

### 3, 主从reactor多线程
#### 实现
Reactor的主线程MainReactor会监听连接事件，收到事件后会通过Acceptor处理事件
连接事件后，MainReactor将连接分配给SubReactor，SubReactor将连接加入到连接队列监听
并创建Handler进行事件的处理，后面就会将具体的处理分发给worker线程池处理

#### 优点
响应快，不必为单个同步时间阻塞，虽然Reactor本身是同步的
避免复杂的线程和同步问题，并避免了多线程切换的开销
扩展性好，可以方便通过增加Reactor实例数充分利用CPU资源
复用性好，Reactor模型本身和事件处理逻辑无关，具有很高的复用性

# Netty基本模型
1）Netty抽象出两组线程池，BossGroup专门负责客户端的连接，WorkerGroup专门负责网络的读写
2）BossGroup和WorkerGroup类型都是NioEventLoopGroup
3）NioEventLoopGroup，相当于是一个事件循环组，这个组中有多个事件循环，每个事件循环是NioEventLoop
4）NioEventLoop表示一个不断循环处理任务的线程，每个NioEventLoop都有一个selector用于监听绑定
在其上的socket网络通讯

5）BossGroup循环执行的步骤
1，轮询accept事件
2，处理accept事件，建立连接
3，处理任务队列的任务，即runAllTasks

6）WorkerGroup循环执行的步骤
1，轮询read，write事件
2，处理事件，在对应的NioSocketChannel上处理
3，处理任务队列的任务，即runAllTasks


方案再说明
NioEventLoop表示一个不断循环的处理任务的线程，每个NioEventLoop都有一个
selector，用于监听绑定在其上的socket网络通道
NioEventLoop采用串行化设计，从消息读取->解码->编码->发送，始终由IO线程NioEventLoop负责


NioEventLoopGroup下包含多个NioEventLoop
每个NioEventLoop中包含有一个selector，一个taskQueue
每个NioEventLoop的selector上可以注册监听多个NioChannel
每个NioChannel只会绑定在唯一的一个NioEventLoop上
每个NioChannel都绑定有自己的一个ChannelPipeline


Channel
1）Netty网络通信的组件，能够用于执行网路的IO操作
2）通过Channel可获得当前网络的连接状态
3）通过Channel可获得网络连接的参数配置
4）channel提供异步的网路IO操作（如建立连接，读写，绑定端口），异步意味着任何IO调用将立即返回，并且不保证
在调用结束时所有请求IO操作完成
5）调用立即返回一个ChannelFuture实例，通过注册监听器到ChannelFuture上，可以在IO操作成功，失败，取消时回掉通知对方
6）支持关联IO操作与对应的处理程序
7）不同协议，不同阻塞类型的连接都有不同的channel类型与之对应，
常用的channel类型：
NioSocketCHannel 异步的客户端TCP Socket连接
NioServerSocketChannel 异步的服务端TCP Socket连接
NioDatagramChannel 异步的UDP连接
NioSctpChannel 异步的客户端Sctp连接
NioSctpServerChannel 异步的Sctp服务器端连接，这些通道覆盖了UDP和TCP网络IO以及文件IO


Selector
1）netty基于selector对象实现IO多路复用，通过selector一个线程可以监听多个连接的channel事件
2）当向一个selector注册了channel后，selector内部机制就可以自动不断的查询这些注册的channel是否有已就绪的IO事件
这样程序可以简单的使用一个线程管理多个channel


ChannelHandler
1）ChannelHandler是一个接口，处理IO事件或连接IO连接，并将其转发到其ChannelPipeline（业务处理）的下一个处理程序
2）ChannelHandler本身并没有提供很多的方法，因为这个接口的很多方法需要实现
ChannelInboundHandler 用于处理入站IO事件
ChannelOutboundHandler 用于处理出站IO事件
//适配器
ChannelInboundHandlerAdapter 用于处理入站IO事件
ChannelOutboundHandlerAdapter 用于处理入站IO事件
ChannelDuplexHandler处理入站和出站事件



ChannelPipeline
在netty中每个channel都有且仅有一个channelpipeline与之对应
1）一个channel包含了一个channelpipeline，而channelpipeline中又维护了一个由channelhandlercontext
组成的双向链表，并且那个channelhandlercontext中又关联着一个handler
2）入站事件和出站事件在一个双向链表中，入站事件会从链表head往后传递到最后一个入站的handler
出站事件会从链表tail往前传递到最前一个出站的handler，两种类型的handler互不干扰


ChannelHandlerContext
1）保存Channel相关的上下文信息，同时关联着一个ChannelHandler对象
2）即ChannelHandlerContext中具体包含着一个具体的事件处理器ChannelHandler
同时也绑定了对应的pipeline和channel信息，方便对ChannelHandler进行调用
ChannelFuture close() 关闭通道
ChannelOutboundInvoker flush() 刷新
ChannelFuture writeAndFlush(Object msg) 将数据写到ChannelPipeline
ChannelHandler 的下一个ChannelHandler开始处理


EventLoopGroup和其实现类NioEventLoopGroup
1）EventLoopGroup是一组EventLoop的抽象，Netty为了更好的利用多核CPU资源
一般会有多个EventLoop同时工作，每个EventLoop维护者一个Selector实例
2）EventLoopGroup提供next接口，可以从组里面按照规则获取其中一个
EventLoop来处理任务，在netty编程中，我们一般需要两个EventLoopGroup，比如
BossEventloopGroup和WorkerEventLoopGriup
3）通常一个服务端口即一个ServerSocketChannel对应一个Selector和一个EventLoop线程
1. BossEventLoop负责接受客户端的连接并将SocketChannel交给WorkerEventLoopGroup进行IO处理
BossEventLoopGroup通常是一个单线程的EventLoop，EventLoop维护者一个注册了ServerSocketChannel的
Selector实例BossEventLoop不断轮询Selector将连接事件分离出来
2. 通常是OP_ACCEPT事件，然后将接受到的SocketChannel交给WorkerEventLoopGroup
3. WorkerEventLoopGroup会由next选择其中一个EventLoopGroup将这个SocketChannel
注册到其维护的Selector并对其后续的IO事件进行处理


Unpooled













