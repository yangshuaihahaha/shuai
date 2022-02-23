# 什么是零拷贝
CPU不需要为数据在内存之间拷贝消耗资源。java中常用的零拷贝有
## 1）mmap 内存映射
通过内存映射，将文件映射到内核缓冲区，同时，用户可以共享内核空间的数据
这样在进行网络传输时，就可以减少内核空间到用户控件的拷贝次数
## 2）sendFile
避免了从内核缓冲区拷贝到Socket缓冲区的操作，直接拷贝到协议栈，这里其实有一次cpu拷贝
但是，拷贝的信息很少，比如length，offset

## 区别
1）mmap适合小数据量的读写，sendFile适合大文件传输
2）mmap需要4次上下文切换，3次数据拷贝。snedFile需要3次上下文切换，最少两次数据拷贝
3）sendFile可以利用DMA方式，减少CPU拷贝，mmap则不能（必须从内核拷贝到Socket缓冲区）

# 什么是DMA
其实DMA技术很容易理解，本质上，DMA技术就是我们在主板上放⼀块独立的芯片。在进行内存和I/O设备的数据传输的时候，我们不再通过CPU来控制数据传输，而直接通过 DMA控制器（DMA?Controller，简称DMAC）。
这块芯片，我们可以认为它其实就是一个协处理器（Co-Processor）)

# sendfile"零拷贝"、mmap内存映射、DMA
## 先说说零拷贝
零拷贝并不是说不需要拷贝，而是减少不必要的拷贝次数。通常是在IO读写过程中。
### 传统的IO流程：
比如：读取文件，再用socket发送出去
传统实现方式：
先读取、再发送，实际经过1-4次copy。
1，第一次：将磁盘文件，读取到操作系统的内核缓冲区
2，第二次：将内核缓冲区的数据，拷贝到application应用程序的buffer
3，第三次：将application应用程序的buffer，拷贝到socket网络发送缓冲区（属于操作系统内核缓冲区）
4，第四次：将socket buffer数据，拷贝到网卡，由网卡进行网络传输
传统方式，读取磁盘文件进行网络发送，经过4次拷贝是非常繁琐的。实际IO读写，需要进行IO中断，需要CPU响应中断（带来上下文切换），尽管后来引入DMA来接管CPU的中断请求，但四次copy是存在“不必要的拷贝”的。

### 重新思考传统IO方式
我们发现实际上不需要第2、3次数据拷贝。
这种场景：读取磁盘文件后不需要做其他处理，直接使用网络发送出去。

### 为什么Kafka这么快
kafka作为MQ也好，作为存储层也好，无非是两个重要功能：
    1，是Producer生产的数据存到broker
    2，是 Consumer从broker读取数据
我们把它简化成如下两个过程：
    1、网络数据持久化到磁盘 (Producer 到 Broker)
    2、磁盘文件通过网络发送（Broker 到 Consumer）

下面，先给出“kafka用了磁盘，还速度快”的结论
1、顺序读写
    磁盘顺序读或写的速度400M/s，能够发挥磁盘最大的速度。
    随机读写，磁盘速度慢的时候十几到几百K/s。这就看出了差距。
    kafka将来自Producer的数据，顺序追加在partition，partition就是一个文件，以此实现顺序写入。
    Consumer从broker读取数据时，因为自带了偏移量，接着上次读取的位置继续读，以此实现顺序读。
    顺序读写，是kafka利用磁盘特性的一个重要体现。
2，零拷贝 sendfile(in,out)
    数据直接在内核完成输入和输出，不需要拷贝到用户空间再写出去
    kafka数据写入磁盘之前，数据先写到进程的内存空间
3，mmap映射
    虚拟映射只支持文件
    在进程的非堆内存开辟一块内存空间，和OS内核空间的一块内存进行映射
    kafka数据写入、是写入这块内存空间，但实际这块内存和OS内核内存有映射，也就是相当于写在内核内存空间了，且这块内核空间、内核直接能够访问到，直接落入磁盘。
我们来重点探究 kafka两个重要过程、以及是如何利用两个零拷贝技术sendfile和mmap的。
    1，网络数据持久化到磁盘 (Producer 到 Broker)
        传统方式实现：
            先接收生产者发来的消息，再落入磁盘。
            数据落盘通常都是非实时的，kafka生产者数据持久化也是如此。kafka数据并不是实时的写入硬盘，它充分利用了现代操作系统分页存储来利用内存提高I/O效率；
            对于kafka来说，Producer生产的数据存到broker，这个过程读取到socket buffer的网络数据，其实可以直接在os内核缓冲区完成落盘。
            并没有必要将socket buffer的网络数据读取到应用进程缓冲区，在这里应用进程缓冲区其实就是broker，broker接收到生产者的数据，就是为了持久化
        Memory Mapped Files：在此特殊场景下：接收来自socket buffer的网络数据，应用进程不需要中间处理、直接进行持久化时。——可以使用mmap内存文件映射。
            简称mmap，简单描述其作用就是：将磁盘文件映射到内存, 用户通过修改内存就能修改磁盘文件；
            它的工作原理是直接利用操作系统的Page来实现文件到物理内存的直接映射。完成映射之后你对物理内存的操作会被同步到硬盘上（操作系统在适当的时候）。
            通过mmap，进程像读写硬盘一样读写内存（当然是虚拟机内存），也不必关心内存的大小有虚拟内存为我们兜底。
            使用这种方式可以获取很大的I/O提升，省去了用户空间到内核空间复制的开销。
        mmap缺陷：
            不可靠：写到mmap中的数据并没有被真正的写到硬盘，操作系统会在程序主动调用flush的时候才把数据真正的写到硬盘
            Kafka提供了一个参数——producer.type来控制是不是主动flush；如果Kafka写入到mmap之后就立即flush然后再返回Producer叫同步(sync)；写入mmap之后立即返回Producer不调用flush叫异步(async)。
        Java NIO对文件映射的支持：
            Java NIO，提供了一个 MappedByteBuffer 类可以用来实现内存映射。
            MappedByteBuffer只能通过调用FileChannel的map()取得，再没有其他方式。
            注意：
                mmap的文件映射，在full gc时才会进行释放。当close时，需要手动清除内存映射文件，可以反射调用sun.misc.Cleaner方法。
    2，磁盘文件通过网络发送（Broker 到 Consumer）
        传统方式实现：
            先读取磁盘、再用socket发送，实际也是进过四次copy。
        kafka中的实现：
            而Linux2.4+内核通过sendfile系统调用，提供了零拷贝。磁盘数据通过DMA拷贝到内核态buffer后，直接通过DMA拷贝到NIC Buffer（socket buffer），无需CPU拷贝。
            这也是零拷贝这一说法的来源。除了减少数据拷贝之外，整个文读文件-网络发送由一个sendfile调用完成，整个过程只有两次上下文切换，因此大大提高了性能。零拷贝过程如下：
                对传统IO 4步拷贝的分析，sendfile将第二次、第三次拷贝，一步完成。
                其实这项零拷贝技术，直接从内核空间（DMA的）到内核空间（Socket的)、然后发送网卡。
                简单理解 sendfile(in,out)就是，磁盘文件读取到操作系统内核缓冲区后、直接扔给网卡，发送网络数据。
        应用场景：
            如Tomcat、Nginx、Apache等web服务器返回静态资源等，将数据用网络发送出去，都运用了sendfile。
        Java NIO对sendfile的支持：
            FileChannel.transferTo()/transferFrom()。
        kafka中是怎么实现sendfile的：
            具体来看，Kafka 的数据传输通过 TransportLayer 来完成，其子类 PlaintextTransportLayer 通过Java NIO 的 FileChannel 的 transferTo 和 transferFrom 方法实现零拷贝。
        注意：
            transferTo 和 transferFrom 并不保证一定能使用零拷贝。实际上是否能使用零拷贝与操作系统相关，如果操作系统提供 sendfile 这样的零拷贝系统调用，则这两个方法会通过这样的系统调用充分利用零拷贝的优势，否则并不能通过这两个方法本身实现零拷贝。
总结Kafka快的原因：
    1、partition顺序读写，充分利用磁盘特性，这是基础；
    2、Producer生产的数据持久化到broker，采用mmap文件映射，实现顺序的快速写入；
    3、Customer从broker读取数据，采用sendfile，将磁盘文件读到OS内核缓冲区后，直接转到socket buffer进行网络发送。

### mmap 和 sendfile总结
1、都是Linux内核提供、实现零拷贝的API；
2、sendfile 是将读到内核空间的数据，转到socket buffer，进行网络发送；
3、mmap将磁盘文件映射到内存，支持读和写，对内存的操作会反映在磁盘文件上。
注意：RocketMQ 在消费消息时，使用了 mmap。kafka 使用了 sendFile。











        
    












