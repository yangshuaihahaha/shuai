# 那些行业需要流处理
1，电商和市场营销
    数据报表、广告投放、业务流程需要
2，物联网
    传感器实时数据采集和显示、实时报警，交通运输业
3，电信业
    基站流量调配
4，银行和金融行业
    实时结算和通知推送，实时检测异常行为

# Flink主要特点
事件驱动
离线数据是有界的流，实时数据是一个没有界限的流：这就是所谓的有界流和无界流
分层api   
    sql/table api：最高级的api，把里面的数据都当成表来操作
    DataStream api：核心api，主要用来处理无界流
    DataSet api：核心api，主要用来处理有界流
    ProcessFunction：最底层api，能拿到事件，有状态计算，还可以进行时间操作
支持事件时间和处理时间
精确一次（exactly-once）的状态一致性保证
低延迟，每秒处理数百万个事件，毫秒级延迟
与众多常用存储系统连接
高可用，动态扩展、全天候运行

# Flink对比Spark Streaming
数据模型
    spark采用RDD模型，Spark streaming的DStream实际上也就是一组小批数据RDD的集合
    flink基本数据模型是数据流，以及事件（Event）序列
运行时架构
    spark是批计算，将DAG划分为不同的stage，一个完成后才可以计算下一个
    flink是标准的流执行模式，一个事件在一个节点处理完后可以直接发往下一个节点进行处理

# 运行时架构
运行时组件
    - 作业管理器JobManager
        1，控制一个应用程序执行的主进程，也就是说，每个应用程序都会被一个不同的JobManager所控制执行
        2，JobManager会先接收到要执行的应用程序，每个应用程序包括：作业图JobGraph、逻辑数据流图Logical dataflow graph和打包了所有的类、裤和其他资源jar包
        3，JobManager会把JobGraph转换成一个物理层面的数据流图，这个图叫做"执行图ExecutionGraph"，包含了所有可以并发执行的任务
        4，JobManager会向资源管理器ResourceManager请求执行任务必要的资源，也就是资源管理器上的插槽slot。一旦获取到了足够的资源，
        就会将执行图发分发到真正运行他们的TaskManager上。而在运行过程中，JobManager会负责所有需要中央协调的操作，比如检查点的协调
    - 任务管理器TaskManager
        1，flink的工作进程。通常flink会有多个taskmanager运行，每个TaskManager都包含了一定数量的插槽。插槽的数量限制了TaskManager能够执行的任务数量
        2，启动之后，TaskManager会向资源管理器注册它的插槽；收到资源管理器指令后，TaskManager就会将一个或者多个插槽提供给JobManager调用。JobManager就可以向插槽分配任务来执行
        3，执行过程中，一个TaskManager可以跟其他运行同一应用程序的TaskManager交换数据
    - 资源管理器ResourceManager
        1，主要负责任务管理器TaskManager和插槽slot
        2，Flink为不同的环境和资源管理器工具提供了不同的资源管理器，比如yarn、mesos、k8s、以及standalone部署
        3，当JobManager申请插槽资源时，ResourceManager会将有空闲的插槽的TaskManager分配给JobManager。
        如果ResourceManager没有足够的插槽来满足JobManager请求，它还可以向资源提供平台发起会话，以提供TaskManager进程容器
    - 分发器Dispacher
        1，可以跨作业运行，提供了rest接口
        2，当一个应用被提交执行时，分发器会启动并将应用交给JobManager
        3，Dispatcher也会启动一个Web UI，用来方便的展示和监控作业执行的信息
        4，Dispatcher在架构中可能不是必需的，这取决于应用提交运行的方式
任务提交流程
    1，app提交应用给分发器dispatcher
    2，分发器dispatcher启动并提交应用给JobManager（每个Job就有一个JobManager）
    3，JobManager（已经拿到了所有的作业图）向ResourceManager请求slots
    4，启动ResourceManager、注册slots、发出提供slot的指令
    5，然后由TaskManager给JobManager提供slots
    6，提交要在slots中执行的任务
    7，TaskManager执行任务