    1）Producer ：消息生产者，就是向 kafka broker 发消息的客户端；
    2）Consumer ：消息消费者，向 kafka broker 取消息的客户端；
    3）Consumer Group （CG）：消费者组，由多个 consumer 组成。消费者组内每个消费者负
    责消费不同分区的数据，一个分区只能由一个组内消费者消费；消费者组之间互不影响。所
    有的消费者都属于某个消费者组，即消费者组是逻辑上的一个订阅者。
    4）Broker ：一台 kafka 服务器就是一个 broker。一个集群由多个 broker 组成。一个 broker
    可以容纳多个 topic。
    5）Topic ：可以理解为一个队列，生产者和消费者面向的都是一个 topic；
    6）Partition：为了实现扩展性，一个非常大的 topic 可以分布到多个 broker（即服务器）上，
    一个 topic 可以分为多个 partition，每个 partition 是一个有序的队列；
    7）Replica：副本，为保证集群中的某个节点发生故障时，该节点上的 partition 数据不丢失，
    且 kafka 仍然能够继续工作，kafka 提供了副本机制，一个 topic 的每个分区都有若干个副本，
    一个 leader 和若干个 follower。
    8）leader：每个分区多个副本的“主”，生产者发送数据的对象，以及消费者消费数据的对
    象都是 leader。
    9）follower：每个分区多个副本中的“从”，实时从 leader 中同步数据，保持和 leader 数据
    的同步。leader 发生故障时，某个 follower 会成为新的 follower。

    基本命令
    安装目录
        /usr/local/Cellar/kafka/2.5.0
    启动命令
        kafka-server-start /usr/local/etc/kafka/server.properties &
        kafka-server-start /usr/local/etc/kafka/server1.properties &
        kafka-server-start /usr/local/etc/kafka/server2.properties &
        zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties &
    查看所有主题
        bin/kafka-topics --list --zookeeper localhost:2181
        bin/kafka-topics --describe --zookeeper localhost:2181 --topic mytopic
    创建主题
        bin/kafka-topics --create --zookeeper localhost:2181 --replication-factor 3 --partitions 2 --topic mytopic
        --replication-factor：副本数
        --partitions：分区数
    删除数据
        bin/kafka-topics --delete --zookeeper localhost:2181 --topic mytopic
    创建一个生产者：
        bin/kafka-console-producer --broker-list localhost:9092 --topic test
    创建一个消费者：
        kafka-console-consumer --bootstrap-server localhost:9092 --topic test --from-beginning



    Kafka生产者

    生产者分区策略
    分区的原因：
        1，方便在集群中进行扩展，每个partition可以通过调整以适应它所在的机器，而一个topic又可以有多个partition组成，因此整个集群就可以适应任何大小的数据了
        2，可以提高高并发，因为可以以partition为单位进行读写了
        3，多个 partition ，能够对 broker 上的数据进行分片，通过减少消息容量来提升 IO 性能；
        4，为了提高消费端的消费能力，一般情况下会通过多个 conusmer 去消费 同一个 topic 中的消息，即实现消费端的负载均衡。
    分区的原则
        我们需要将producer发送的数据封装成一个ProducerRecord对象
        1，指明partition的情况下，直降将指明的值作为partition
        2，没有指明的值，但是有key的情况下，将key的hash值与topic的partition数进行取余得到partition的值
        3，即没有partition值又没有key值的情况下，第一次调用时随机生成一个整数（后面每次调用在这个整数上自增），
        将这个值与topic可用的partition总数取余得到partition值，也就是常说的round-robin算法


    生产者数据可靠性
    为了保证数据的可靠性，能可靠的发送到指定的topic，topic的每个partition收到producer发送的数据，都要向producer发送ack（确认收到）
    如果producer收到ack，就会进行下一轮发送，否则重新发送数据
    那么何时发送ack？
    副本的同步策略：
        1，半数以上完成同步发送ack，优点是延迟低，缺点是选举新的leader的时候容忍n台节点故障，需要2n+1副本
        2，全部同步完成才发送ack，优点是选举新的leader时容忍n台节点故障，需要n+1个副本，缺点是延迟高
    kafka选择的是第二种方案，原因如下：
        1，Kafka的每个分区都有大量的数据，第一种方案会造成大量的数据冗余
        2，虽然第二种方案的网络延迟比较高，但网络延迟对Kafka的影响较小


    ISR
        采用第二种方案后，设想一下几种场景，所有的follower开始同步数据，但是有一个follower因为故障不能与leader同步
        那么leader就要一直等待下去，直到他同步完成，才能发送ack，那么这个问题是怎么解决的？

        leader维护了一个动态的ISR，意为和leader保持同步的follower集合。当ISR中的follower完成数据同步后
        leader就会向producer发送ack，如果follower长时间未向leader同步数据，则该follower将被提出ISR，该
        阀值由replica.lag.time.max.ms参数决定，leader发生故障后，就会从ISR中选举新的leader
    ack应答机制
        Kafka为用户提供三种可靠性级别，用户可对可靠性和延迟的要求进行权衡
        0：producer不等待broker的ack，这一操作提供了一个最低的延迟，broker一旦接收还没有写入磁盘就进行返回，当booker故障时就有可能丢失数据
        1：producer等待broker的ack，partition的leader写入磁盘就返回ack，如果在follower同步成功之前leader故障，那么将可能丢失数据
        -1/all：producer等待broker的ack，partition的leader和follower全部写入磁盘才会ack，但是如果在follower同步完成后，leader发生故障
                那么可能会造成数据重复


    故障的细节处理
        LEO：每个副本最后一个offset
        HW(高水位)：所有副本最小的LEO
        HW之前的数据才会对consumer可见
        (1) follower故障
            follower发生故障后会被提出ISR，等待follower恢复后，follower会读取本地磁盘上记录的上次的HW，并将log文件高于HW的部分截取掉
            从HW开始向leader进行同步，等该follower的LEO大于等于该partition的HW ,即follower追上leader之后，就可以重新加入ISR
        (2)leader故障
            leader发生故障后，会从ISR中选出一个新的leader，之后，为了保证多个副本之间的数据一致性，其余的follower会将各自高于HW的
            部分截取掉，然后从新的leader同步数据
        注意：这只能保证副本之间的数据一致性，并不能保证数据不丢失或者不重复


    Exactly once（At Least Once + 幂等性 = Exactly Once）
    即数据不重复，也不丢失
    Kafka的幂等只能保证单个生产者会话（session）中单分区的幂等
    启用
        要启用幂等性，只需要将Producer的参数中的enable.idompotence设置为true即可。
        Kafka的幂等性实现是将原来下游需要去做的去重复操作放在了上游
    原理
        开启幂等性的producer在初始化的时候会分配一个pid，发往同一个partition的消息会附带一个sequence number
        而broker端会对<PID,Partiotion,SeqNumber>做缓存，当具有主键的消息提交时，broker只会持久化一条



    Kafka消费者

    消费方式
        consumer 采用 pull(拉)模式从 broker 中读取数据。
        push(推)模式很难适应消费速率不同的消费者，因为消息发送速率是由 broker 决定的。
        它的目标是尽可能以最快速度传递消息，但是这样很容易造成 consumer 来不及处理消息，
        典型的表现就是拒绝服务以及网络拥塞。而 pull 模式则可以根据 consumer 的消费能力以适 当的速率消费消息。
        pull 模式不足之处是，如果 kafka 没有数据，消费者可能会陷入循环中，一直返回空数 据。针对这一点，Kafka 的消费者在消费数据时会传入一个时长参数 timeout，
        如果当前没有 数据可供消费，consumer 会等待一段时间之后再返回，这段时长即为 timeout。
    分区分配策略
        一个consumer group中有多个consumer，一个topic有多个partition，所以必然会涉及到partition的分配问题，即确定那个 partition由哪个consumer决定
        Kafka有两种分配策略，一种是RoundRobin，一种是Range
    (1) RoundRobin
        轮询分区策略，是把所有的 partition 和所有的 consumer 都列出来，然后按照 hascode 进行排序，最后通过轮询算法来分配 partition 给到各个消费者。
        条件：
            1. 每个消费者订阅的主题必须是相同的
            2. 每个主题的消费者实例具有相同数量的流
    (2) RangeAssignor
        Range 策略是对每个主题而言的，首先对同一个主题里面的分区按照序号进行排序，并对消费者按照字母顺序进行排序。假设我们有 10 个分区，3 个消费者，
        排完序的分区 将会是 0, 1, 2, 3, 4, 5, 6, 7, 8, 9;消费者线程排完序将会是 C1-0, C2-0, C3-0。然后将 partitions 的个数除于消费者线程的总数来决定每个消费者线程消费几个分区。
        如果除不尽，那么前面几个消费者线程将会多消费一个分区。在例子里面，我们有 10 个分区，3 个消费者线程， 10 / 3=3，而且除不尽，那么消费者线程 C1-0 将会多消费一个分区，
        所以最后分区分配的结果看起来是这样的:
        C1-0 将消费 0, 1, 2, 3 分区
        C2-0 将消费 4, 5, 6 分区
        C3-0 将消费 7, 8, 9 分区
        可以看出，C1-0 消费者线程比其他消费者线程多消费了 1 个 分区，这就是 Range strategy 的一个很明显的弊端。

     Kafka 高效读写数据
     1)顺序写磁盘
        Kafka 的 producer 生产数据，要写入到 log 文件中，写的过程是一直追加到文件末端， 为顺序写。
        官网有数据表明，同样的磁盘，顺序写能到 600M/s，而随机写只有 100K/s。这与磁盘的机械机构有关，顺序写之所以快，是因为其省去了大量磁头寻址的时间。
     2)零拷贝


     Kafka 事务
     保证了多回话的幂等性
     Kafka 从 0.11 版本开始引入了事务支持。事务可以保证 Kafka 在 Exactly Once 语义的基 础上，生产和消费可以跨分区和会话，要么全部成功，要么全部失败。

     1)Producer 事务
        为了实现跨分区跨会话的事务，需要引入一个全局唯一的 Transaction ID，并将 Producer 获得的 PID 和 Transaction ID 绑定。这样当 Producer 重启后就可以通过正在进行的 Transaction ID 获得原来的 PID。
        为了管理 Transaction，Kafka 引入了一个新的组件 Transaction Coordinator。Producer 就 是通过和 Transaction Coordinator 交互获得 Transaction ID 对应的任务状态。
        Transaction Coordinator 还负责将事务所有写入 Kafka 的一个内部 Topic，这样即使整个服务重启，由于 事务状态得到保存，进行中的事务状态可以得到恢复，从而继续进行。
     2)Consumer 事务
        上述事务机制主要是从 Producer 方面考虑，对于 Consumer 而言，事务的保证就会相对 较弱，尤其时无法保证 Commit 的信息被精确消费。
        这是由于 Consumer 可以通过 offset 访 问任意信息，而且不同的 Segment File 生命周期不同，同一事务的消息可能会出现重启后被 删除的情况。


     跨会话的幂等性写入：即使中间故障，恢复后依然可以保持幂等性；
     跨会话的事务恢复：如果一个应用实例挂了，启动的下一个实例依然可以保证上一个事务完成（commit 或者 abort）；
     跨多个 Topic-Partition 的幂等性写入，Kafka 可以保证跨多个 Topic-Partition 的数据要么全部写入成功，要么全部失败，不会出现中间状态


     Consumer端幂等性
     如上所述，consumer拉取到消息后，把消息交给线程池workers，workers对message的handle可能包含异步操作，又会出现以下情况：
        先commit，再执行业务逻辑：提交成功，处理失败 。造成丢失
        先执行业务逻辑，再commit：提交失败，执行成功。造成重复执行
        先执行业务逻辑，再commit：提交成功，异步执行fail。造成丢失
        对此我们常用的方法时，works取到消息后先执行如下code：

        if(cache.contain(msgId)){
          // cache中包含msgId，已经处理过
                continue;
        }else {
          lock.lock();
          cache.put(msgId,timeout);
          commitSync();
          lock.unLock();
        }
