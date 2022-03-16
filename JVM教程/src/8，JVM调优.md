# 背景说明
1，生产环境中的问题
    生产环境发生了内存益处该如何处理？
    生产环境应该给服务器分配多少内存合适？
    生产环境CPU负载飙高该如何处理？
    生产环境应该分配多少线程合适？
    不加log，如何确定请求是否执行了某一行代码
    不加log，如果实时查看，某个方法的入参与返回值
2，为什么要调优？
    防止出现OOM
    解决OOM  
    减少Full GC出现的频率
3，不同阶段的考虑
    上线前
    项目运行阶段
    线上出现OOM
# 调优概述
1，监控依据
    运行日志
    异常堆栈
    GC日志
    线程快照
    堆转储快照
2，调优大方向
    合理的编写代码
    充分并合理的使用硬件资源
    合理进行JVM调优

# 性能优化的步骤
第1步：性能监控
    GC频繁
    cpu load过高
    OOM
    死锁
    程序响应时间过长
第2步：性能分析
    打印GC日志，通过GCviewer或者http://gceasy.io来分析日志信息
    灵活运用命令行工具，jstack、jmap、jinfo等
    dump出堆文件，使用内存分析工具分析文件
    使用阿里Arthas，或jconsole、JVisualVM来实时查看JVM状态
    jstack查看堆栈信息
第3步：性能调优
    适当增加内存，根据业务背景选择垃圾回收器
    代码优化，控制内存使用
    增加机器，分散节点压力
    合理设置线程池数量
    使用中间件提高程序效率，比如缓存、消息队列
# 性能评价/测试指标
1，停顿时间（或响应时间）
2，吞吐量：对单位时间内完成的工作量的量度
3，并发数：同一时刻，对服务器有实际交互的请求书
4，内存占用：Java堆区的内存大小

# 诊断工具-命令行篇
1，jps: 查看正在运行中的进程
    即Java Process Status，显示指定系统内所有的HotSpot虚拟机进程，可用于查询正在运行的虚拟机进程
    说明：
        对于本地虚拟机进程来说，进程的本地虚拟机ID与操作系统的进程ID是一致的，是唯一的
    基本语法：
        jps -q: 只显示进程ID
        jps -l: 显示程序主类的全类名，如果进程执行的是jar包的话，则输出jar完整路径
        jps -m: 输出虚拟机进程启动时传递给主类main()的参数
        jps -v: 列出虚拟机进程启动时的JVM参数
    补充：
        如果Java进程关闭了默认开启的UsePerfData参数，那么jps命令（以及下面介绍的jstat）将无法探知该Java进程
2，jstat：查看JVM统计信息呢
    即JVM Statistics Monitoring Tool，用于监视虚拟机各种运行状态信息的命令行工具。它可以显示本地或者远程虚拟机进程中的类装载、内存、垃圾收集、JIT编译等运行数据
    基本语法：
        option参数：
            jstat -class 进程ID：显示ClassLoader的相关信息：类的装载、卸载数量、总空间、类装载所消耗的时间等
            jstat -gc 进程ID：显示gc相关的信息。包括Eden区、两个Survivor区、老年代、永久代等情况
            jstat -gccapacity 进程ID：显示内容与gc基本相同，但输出主要关注Java堆各个区域使用到的最大、最小空间
            jstat -gcutil 进程ID：显示的内容与gc基本相同，但输出主要关注已使用空间占总空间的百分比
            jstat -gccause 进程ID：与-gcutil功能一样，但是会额外输出导致最后一次或当前正在发生gc产生的原因
            jstat -gcnew 进程ID：显示新生代的GC情况
            jstat -gcnewcapacity 进程ID：显示内容与-gcnew基本一致，输出主要关注使用到的最大、最小空间
            jstat -gcold 进程ID：显示老年代的gc情况
            jstat -gcoldcapacity 进程ID：显示内容与-gcold基本一致，输出主要关注使用到的最大、最小空间
            jstat -gcpermcapacity 进程ID：显示永久代使用到的最大、最小空间
            jstat -complier 进程ID：显示JIT编译器编译过的方法、耗时信息
            jstat -printcompilation 进程ID：输出已经被JIT编译的方法
        interval参数：
            jstat -class 进程ID 1000：指定输出统计时间的周期，单位为毫秒：即每隔1秒打印输出一次ClassLoader的相关信息
        count参数：
            jstat -class 进程ID 1000 10：每隔1秒打印输出一次，总共输出10次
        -t参数：
            jstat -class -t 进程ID：显示程序的运行时间，单位是秒
        -h参数：
            jstat -class -t -h3 进程ID：每隔3行打印一次表头
    表头参数含义：
        1，新生代相关：
            S0C是第一个幸存者区的大小（字节）
            S1C是第二个幸存者区的大小（字节）
            S0U是第一个幸存者区已使用的大小（字节）
            S1U是第二个幸存者区已使用的大小（字节）
            EC是Eden空间的大小（字节）
            EU是Eden区已使用的空间大小
        2，老年代相关
            OC是老年代的空间大小（字节）
            OU是老年代已使用的空间大小
        3，方法区相关
            MC是方法区的大小
            MU是方法区已使用的空间大小
            CCSC是压缩类空间的大小
            CCSU是压缩类已使用的大小
        4，其他
            YGC是指应用程序从启到采样的young gc的采样次数
            YGCT是指应用程序从启动到采样young gc消耗的时间
            FGC是只应用程序启动到采样full gc次数
            FGCT是指应用程序启动到采样时full gc消耗的时间
            GCT是指应用程序启动到采样时GC的总时间
2，jinfo：查看虚拟机的参数配置信息，也可以用于调整虚拟机的配置参数
    在很多情况下，Java应用程序不会指定所有的Java虚拟机参数。而此时，开发人员可能不知道某一个具体的Java虚拟机参数的默认数值
    在这种情况下，可能需要通过查找开发文档获取某个参数的默认值。而这个过程是非常艰难的。但有了jinfo工具，开发人员就可以很方便地找到Java虚拟机参数的当前值了
    基本语法：
        查看：
            jinfo -sysprops PID: 可以查看由System.getRpoperties()取得的参数
            jinfo -flags PID：查看曾经赋过值的一些参数
            jinfo -flag 具体参数 PID：查看某个Java进程的具体参数的值
        修改：
            针对boolean类型：jinfo -flag [+|-] 具体参数 PID
            针对非boolean类型：jinfo -flag 具体参数=具体参数 PID
    拓展：
        java -XX:+PrintFlagsInitial：查看所有jvm参数启动值
        java -XX:+PrintFlagsFinal：查看所有jvm参数的最终值
        java -XX:+PrintCommandLineFlags：查看哪些已经被用户或者JVM设置过的详细的XX参数的名称和值
3，jmap：导出内存映像文件&内存使用情况
    即JVM Memory Map：
        作用一方面是获取dump文件（堆转储快照文件，二进制文件）
        它还可以获取目标Java进程的内存相关信息，包括Java堆各区域的使用情况，堆中对象的统计信息、类加载信息
    基本语法：
        -dump：
            生成Java堆转储快照：dump文件
            特别的：-dump:live只保存堆中的存活对象
        -heap
            输出整个堆空间的详细信息，包括GC的使用、堆配置信息，以及内存的使用信息
        -histo
            输出堆中对象的统计信息，包括类、实体数量和合集容量
            特别的：-histo:live只统计堆中的存活对象
        -permstat
            以ClassLoader为统计口径输出永久代的内存状态信息
            仅linux/solaris平台有效
        -finalizerinfo
            显示在F -Queue中等待Finalizer线程执行finalize方法的对象
            仅linux/solaris平台有效
        -F
            当虚拟机进程对-dump选项没有任何响应时，可使用此选项强制执行生成dump文件
            仅linux/solaris平台有效
    使用1：导出内存映像文件
        手动方式：
            jmap -dump:format=b,file=/Users/yangshuai/Desktop/ PID
            jmap -dump:live,format=b,file=/Users/yangshuai/Desktop/ PID
            由于生成dump文件比较耗时，因此大家需要耐心等待。
            我们一般会加上:live去导出存活的对象，来减少耗时。
        自动方式：
            -XX:+HeapDumpOnOutOfMemoryError//当程序出现OOM的时候，自动导出dump文件
            -XX:HeapDumpPath=<filename.hprof>//指定堆快照保存的位置
            在这种方式下，通常Heap Dump文件前会触发一次Full GC，所以heap dump文件保存的是FullGC后留下的对象信息
    使用2：显示堆内存相关的信息
        jmap -heap PID
        jmap -histo PID
    使用3：
        jmap -permstat PID
        jmap -finalizerinfo PID
    小结：
        由于jmap将访问堆中多有的对象，为了保证在此过程中不被应用线程干扰，jmap需要借助安全点机制，让所有线程停留在不改变数据的状态
        也就是说jmap导出的堆快照必定是安全点的位置，这可能导致基于该堆快照的分析结果存在偏差
        举个例子：
            假设在编译生成的机器码中，某些对象的生命周期在两个安全点之间，那么:live选项将无法探知到这些对象
        另外，如果某个线程长时间无法跑到安全点，jmap将一直等下去，与前面讲的jstat则不同，
        垃圾回收器会主动将jstat所需要的摘要数据保存至固定位置之中，而jstat只需直接读取即可
4，jhat：JDK自带的堆分析工具
    即JVM Heap Analysis Tool，jhat与jmap命令搭配使用，用于分析jmap生成的heap dump文件
    jhat内置了一个微型的HTTP/HTML服务器，生成dump文件的分析结果后，用户可以在浏览器中查看分析结果
    使用了jhat命令，就启动了一个http服务。端口是7000，即http://localhost:7000/
    说明：jhat命令在JDK9、JDK10中已经被删除，官方建议使用VisualVM代替
5，jstack：打印JVM中的线程快照
    即JVM Stack Trace，用于生成虚拟机指定进程当前时刻的线程快照（虚拟机堆栈跟踪）
    线程快照就是当前虚拟机内指定进程的每一条线程正在执行的方法堆栈集合。
    生成线程快照的作用，可用于定位线程出现长时间停顿的原因，如线程间死锁、死循环、请求外部资源导致长时间等待等问题，这些都是导致线程长时间停顿的常见原因
    在thread dump中，要留意下面几种状态：
        死锁，Deadlock（重点关注）
        等待资源，Waiting on condition（重点关注）
        等待获取监视器，Waiting on monitor entry（重点关注）
        阻塞，Blocked（重点关注）
        执行中，Runnable
        暂停，Suspended
6，在JDK1.7以后，新增了一个命令行工具jcmd
    它是一个多功能的工具，可以用来实现前面除了jstat之外的所有命令功能。
    比如用它导出堆、内存使用、查看java进程、导出线程信息、执行GC、JVM运行时间等
    官方也推荐使用jcmd来代替jmap
    基本语法：
        jcmd -l：列出所有JVM进程
        jcmd pid help：针对指定的进程，列出支持所有命令
        jcmd pid 具体命令：显示指定进程的指令命令的数据
            具体命令：The following commands are available:
                JFR.stop
                JFR.start
                JFR.dump
                JFR.check
                VM.native_memory
                VM.check_commercial_features
                VM.unlock_commercial_features
                ManagementAgent.stop
                ManagementAgent.start_local
                ManagementAgent.start
                VM.classloader_stats
                GC.rotate_log
                Thread.print
                GC.class_stats
                GC.class_histogram
                GC.heap_dump
                GC.finalizer_info
                GC.heap_info
                GC.run_finalization
                GC.run
                VM.uptime
                VM.dynlibs
                VM.flags
                VM.system_properties
                VM.command_line
                VM.version