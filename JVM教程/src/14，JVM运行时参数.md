# JVM参数选项类型
类型一：标准参数选项
    比较稳定，后续版本基本不会变化。以"-"开头
    各种选项    运行java或者java -help可以看到所有的标准选项
    补充内容：-server与-client
        Hotspot JVM有两种模式，分别是server和client，分别通过-server和-client模式设置
            1，在32位Windows系统上，默认使用Client类型的JVM。要想使用Server模式，则机器至少有2个以上的CPU和2G以上的物理内存
            client模式适用于对内存要求较小的桌面应用程序，默认使用Serial串行垃圾收集器。
            2，64位机器上只支持server模式的JVM，适用于需要大内存的应用程序，默认使用并行垃圾收集器
类型二：-X参数选项
    特点：
        非标准化参数
        功能还是比较稳定的。但官方说后续版本可能会变更
        以-X开头
        运行Java -X命令可以看到所有的X选项
    主要选项
        1，JVM的JIT编译模式相关的选项
            -Xint：禁用JIT，所有字节码都被解释执行，这个模式的速度最慢
            -Xcomp：所有字节码第一次使用就被编译成本地代码，然后执行
            -Xmixed：混合模式执行（默认）
        2，特别地
            -Xms<size>：设置初始Java堆大小，等价于-XX:initialHeapSize
            -Xmx<size>：设置最大Java堆大小，等价于-XX:MaxHeapSize
            -Xss<size>：设置Java线程堆大小，等价于-XX:ThreadStackSize
类型三：-XX参数选项
    特点：
        非标准化参数
        使用最多的参数类型
        这类选项属于实验性，不稳定
        以-XX开头
        用于开发和调试JVM
    分类：
        1，Boolean类型格式：
            -XX:+<option> 表示启用option属性
            -XX:-<option> 表示禁用option属性
            例如：
                -XX:-UseParalleGC：选择垃圾收集器为并行垃圾收集器
                -XX:UseG1GC：表示启用G1手机去
                -XX:UseAdaptiveSizePolicy：自动选择年轻代大小和相应的Survivor区比例
            说明：因为有的指令是默认开启的，所以使用-<option>关闭
        2，key-value类型：
            子类型1：数值型格式-XX:<option>=<number>
            子类型2：非数值型格式-XX:<name>=<string>
            例如：
                -XX:NewSize=1024m：表示设置新生代初始大小为1024兆
                -XX:MaxGCPauseMillis=500：表示设置GC停顿时间：500毫秒
                -XX:GCTimeRatio=19：表示设置吞吐量
                -XX:NewRatio=2：表示新生代与老年代的比例
                -XX:HeapDumpPath=/User/yangshuai/heapdump.hprof：用来指定heap转存文件的路径
        3，特别地：
            -XX:+PrintFlagsFinal：
                输出所有参数的名称和默认值，
                默认不包括Diagnostic和Exprimenta的参数   
                可以配合-XX:+UnlockDiagonsticVMOptions和-XX:UnlockExperimentalVMOptions使用
# 添加JVM参数选项
1，运行jar包
    java -Xms50m -Xmx50m -XX:-PrintGCDetails -XX:PrintGCTimeStamps -jar demo.jar
2，通过Tomcat运行jar包
    Linux系统下可以在tomcat/bin/catalina.sh中添加如下配置
        JAVA_OPTS="-Xms512M -Xmx1024M"
    Windows系统下在catalina.bat中添加类似如下配置：
        set "JAVA_OPTS=-Xms512M -Xmx1024M"
3，程序运行过程中
    针对boolean类型：jinfo -flag [+|-] 具体参数 PID
    针对非boolean类型：jinfo -flag 具体参数=具体参数 PID

# 常用的JVM参数选项
1，打印设置的XX选项信息
    -XX:+PrintCommandLineFlags：可以在程序运行前打印出用户手动设置或者JVM自动设置的XX选项
    -XX:+PrintFlagsInitial：表示打印出所有XX选项的默认值
    -XX:+PrintFlagsFinal：表示打印出XX选项在运行程序时生效的值
    -XX:+PrintVMOptions：打印JVM的参数
2，堆、栈、方法区等内存大小设置
    栈：
        -Xss:128k：设置每个线程的大小为128k。等价于-XX:ThreadStackSize=128k
    堆：
        -Xms3550m：等价于-XX:InitialHeapSize，设置JVM初始堆内存为3550M
        -Xmx3550m：等价于-XX:MaxHeapSize，设置JVM最大堆内存为3550M
        -Xmn2g：设置年轻代大小为2g。官方推荐配置为整个堆大小的3/8
        -XX:NewSize=1024m：设置年轻代初始值为1024M
        -XX:MaxNewSize=1024m：设置年轻代最大值为1024M
        -XX:SurvivorRatio=8：设置年轻代中Eden区与一个Survivor区的比值，默认为8
        -XX:+UseAdaptiveSizePolicy：自动选择各区比例大小
        -XX:NewRatio=4：设置老年代与年轻代（包括一个Eden和2个Survivor区）比值
        -XX:PretenureSizeThreadshould=1024：设置让大于此阈值的对象直接分配在老年代，单位为字节。只对Serial、ParNew收集器有效
        -XX:MaxTenuringThreshold：默认值为15。新生代每次MiniorGC后，还存活的对象年龄+1，当对象的年龄大于设置的这个值时就进入老年代
        -XX:+PrintTenuringDistribution：表示MinorGC后打印出当前使用的Survivor中对象的年龄分布
        -XX:TargetSurvivorRatio：表示MinorGC结束后Survivor区域占用空间的期望比例
    方法区：
        永久代：
            -XX:PermSize=256m：设置永久代的初始值为256m
            -XX:MaxPermSize=256m：设置永久代最大值为256m
        元空间：
            -XX:MetaspaceSize：初始空间大小
            -XX:MaxMetaspaceSize：最大空间，默认没有限制
            -XX:+UseCompressedOops：压缩对象指针
            -XX:+UseCompressedClassPointers：压缩类指针
            -XX:CompressedClassSpaceSize：设置CLass Metaspace的大小，默认1G
    直接内存：
        -XX:MaxDirectMemorySize：指定DirectMemory容量，若未指定，则默认跟Java堆最大值一样

# OnOutOfMermory相关的选项
-XX:HeapDumpOnOutOfMemoryError：表示内存出现OOM的时候，把Heap转存到文件，以便后续分析
-XX:HeapDumpBeforeFullGC：表示出现FullGC之前，生成Heap转储文件
-XX:HeapDumpPath=<path>：指定heap转储文件路径
-XX:OnOutOfMemoryError：指定一个可行性程序或者脚本路径，当发生OOM的时候，去执行这个脚本

# 垃圾收集器相关选项
1，查看默认垃圾收集器
    -XX:+PrintCommandLineFlags：查看命令行相关参数（包含使用的垃圾收集器）
    使用命令行指令：jinfo -flag 相关垃圾收集器参数 进程ID
2，Serial回收器
    Serial收集器作为HotSpot中Client模式下默认的新生代垃圾收集器。Serial Old是运行在Client模式下默认的垃圾收集器。Serial OLD是运行在Client模式下默认的老年代垃圾回收器
    -XX:UseSerialGC：指定年轻代和老年代都使用串行收集器。等价于新生代用Serial GC，且老年代用Serial Old GC。可以获得更高的单线程收集效率
3，PerNew回收器
    -XX:UseParNewGC：手动指定使用PerNew收集器执行内存回收任务。他表示年轻代使用并行收集器，不影响老年代
    -XX:ParallelGCThreads=N：限制线程数量，默认开启和CPU数据相同的线程数
4，Parallel回收器
    -XX:UseParallelGC：手动指定年轻代使用Parallel并行收集器执行内存回收任务
    -XX:UseParallelOldGC：手动指定老年代使用并行回收器
    分别适用于新生代和老年代。默认jdk1.8是开启的
    上面两个参数，默认开启一个，另一个也会被开启（互相激活）
    -XX:ParallelGCThreads：
        设置年轻代并行垃圾收集器线程数。一般的，最好与CPU数量相等，以避免过多的线程数影响垃圾收集器性能
        在默认情况下，当CPU数量小于8个，ParallelGCThreads的值等于CPU数量
        当CPU数量大于8个，ParallelGCThreads的值等于 3+(5*CPU_Counts/8)
    -XX:MaxGCPauseMillis：设置垃圾收集器最大停顿时间（即STW时间）。单位是毫秒
        为了尽可能地把停顿时间控制在MaxGCPauseMills以内，收集器在工作时会调整Java堆大小或其他一些参数
        对于用户来说，停顿时间越短体验越好。但是在服务器端，我们注重高并发，整体的吞吐量。所以服务端更适合Parallel进行控制
        该参数使用需谨慎。因为Parallel回收器主打的是吞吐量，并不是低延迟
    -XX:GCTimeRatio：垃圾收集时间占总时间的比例(=1/(N+1))。用于衡量吞吐量的大小
        取值范围(0, 100)。默认值99，也就是垃圾回收时间不超过1%
        与前一个-XX:MaxGCPauseMillis参数有一定的矛盾性。暂停时间长，Ratio参数就容易超过设定的比例
        响应时间越短，吞吐量就越小
    -XX:+UseAdaptiveSizePolicy：设置Parallel Scavenge收集器具有自适应策略
        在这种模式下，年轻代的大小、Eden和Survivor的比例、今生老年代的对象年龄等参数会被自动调整
        以达到在堆大小、吞吐量和停顿时间之间的平衡点
        在手动调优比较困难的场合，可以直接使用这种自适应方式，仅指定虚拟机的最大堆、目标吞吐量和停顿时间，让虚拟机自己完成调优工作

5，CMS回收器
-XX:+UseConcMarkSweepGC：手动指定CMS收集器执行内存回收任务
    开启此参数后会自动将-XX:+UseParNewGC打开。即：ParNew(Young区) + CMS(Old区) + Serial Old的组合
-XX:CMSInitiatingOccupanyFraction：设置内存使用率的阈值，一旦达到该阈值，便开始回收
    jdk5及以前的版本默认值为68，即当老年代的空间使用率达到68%时会执行一次CMS回收
    jdk6及以上的版本默认值为92%。
    如果内存增长缓慢，则可以设置一个稍微大的值，大的阈值可以有效降低CMS触发频率，减少老年代回收的次数可以较为明显的改善应用的程序性能
    反之，如果应用程序内存使用率增长很快，则应该降低这个阈值，以避免频繁触发老年代的串行收集器，因此通过此选项便可以有效降低Full GC次数
-XX:UseCMSCompacAtFullCollection：用于指定在执行完Full GC后对内存空间进行压缩整理
    以此避免内存碎片的产生。不过由于内存整理压缩过程无法并发执行，所带来的问题就是停顿时间变得更长了
-XX:CMSFullGCsBeforeCompaction：设置在执行多少次Full GC后对内存空间进行压缩整理
-XX:ParallelCMSThreads：设置CMS的线程数量
    CMS默认自动线程数量是(ParallelGCThreads+3)/4
    ParallelGCTThreads是年轻代并行收集器的线程数。当CPU资源比较紧张时，受到CMS收集线程的影响，应用程序的性能在垃圾收集阶段可能会非常糟糕

6，G1回收器
和CMS类似，主打低延迟
-XX:MaxGCPauseMills：设置期望达到的最大GC停顿时间指标（JVM会尽力实现，但不保证达到），默认值是200毫秒
-XX:ParallelGCThread：设置STW时GC线程的值，最多设置为8
-XX:ConcGCThreads：设置并发标记的线程数，将n设置为并行垃圾回收器线程数（ParallelGCThreads）的1/4左右
-XX:InitiatingHeapOccupancyPercent：设置触发并发GC周期的Java堆占用率阈值。超出此值，就触发GC。默认是45。
-XX:G1NewSizePercent、-XX:C1MaxNewSizePercent：新生代占用整个堆内存的最小百分比（默认5%）、最大百分比（默认60%）
-XX:G1ReservePercent=10：保留内存区域、防止to space（Survivor中的to区）益处
Mined GC调优参数：
    注意：C1收集器主要涉及到Mixed GC，Mixed GC会回收young区和部分old区
    G1关于Mixed GC调优常用参数：
        -XX:InitiatingHeapOccupancyPercent：
            设置堆占用率的百分比（0到100）达到这个数值的时候触发global concurrent marking（全局并发标记）
            默认为45%。值为0表示间隔进行全局并发标记
        -XX:G1MixedGCLiveThresholdPercent：
            设置Old区的region被回收时候的对象占比，默认占用率为85%。
            只有Old区的region中存活的对象占用达到这个百分比，才会在Mixed GC中被回收
        -XX:G1HeapWastePercent：
            在global concurrent marking（全局并发标记）结束之后，可以知道所有的区有多少空间被回收，
            在每次young gc之后和再次发生Mixed GC之前，会检查垃圾占比是否达到此参数，只有达到了，下次才会发生Mixed GC
        -XX:G1MixedGCCountTarget：
            一次global concurrent marking（全局并发标记）之后，最多执行Mixed GC的次数，默认是8秒
        -XX:G1OldCSetRegionThresholdPercent：
            设置Mixed GC收集周期中要收集的Old region数的上限。默认值是Java堆的10%

# GC日志相关参数
-verbose:gc：输出gc日志信息，默认输出到标准输出
-XX:+PrintGC：等同于-verbose:gc，表示打开简化的GC信息
-XX:PrintGCDetails：在发生垃圾回收时打印内存回收的详细日志，并在进程推出时输出当前内存区域分配情况
-XX:PrintGCTimeStamps：输出发生时的时间戳
-XX:PrintGCDateStamps：输出GC发生时的时间戳（以日期形式）
-XX:PrintHeapAtGC：每一次GC前GC后打印堆信息
-Xloggc:<file>：把GC文件写入到一个文件中去，而不是打印到标准输出中
