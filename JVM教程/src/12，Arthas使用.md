# 为什么要引入arthas？
项目上线之后，如果想通过工具远程连接到项目进行，如果使用MAT或者JProfiler这样的工具很不方便。
例如：网络的环境是隔离的，本地监控环境根本连接不到线上环境。
那么有没有一款不需要远程连接，也不需要配置监控参数，同时也提供了丰富的性能监控数据呢？
阿里巴巴性能分析神器Arthas应运而生！

# 概述
arthas是一款开源的Java诊断工具。
    在线排查问题
    无需重启
    动态跟踪Java代码
    实时监控JVM状态
采用命令行交互模式
当遇到一下类似问题的时候，arthas可以帮你解决：
1，这个类是那个jar包加载的？为什么会报各种相关的Exception？
2，我改的代码为什么没有执行到，难道我没有commit？
3，遇到问题无法在线上debug，难道只能通过加日志再重新发布么？
4，线上遇到某个用户的数据处理有问题，但线上同样无法debug，线下无法重现
5，是否又一个全局视角来查看系统的运行状况
6，有什么办法可以监控JVM实时运行状态
7。怎么快速定位应用的热点，生成火焰图

# 基础指令
查看日志：cat ~/logs/arthas/arthas.log
查看帮助：java -jar arthas-boot.jar -h
help：查看命令帮助信息
cat：和Linux类似，打印文件内容
echo：和Linux类似，打印参数
grep：和Linux类似，匹配查找
tee：和Linux类似，复制标准输入到标准输出和指定的文件
pwd：和Linux类似，返回当前的工作目录
cls：和Linux类似，清空当前屏幕区域
session：查看当前的会话信息
reset：重制增强类，将被arthas增强过的类全部还原，arthas服务端关闭时会重制所有增强过的类
version：输出当前目标Java进程所加载的arthas版本号
history：打印命令历史
quit：退出当前arthas客户端，其他arthas客户端不受影响
stop：关闭arthas服务端，所有arthas客户端全部退出
keymap：arthas快捷列表及自定义快捷键

# jvm相关
dashboard：当前系统的实时数据面板
    dashboard -i 500：隔500毫秒打印一次
    dashboard -n 3：打印三次
thread：查看当前JVM线程堆栈信息
    thread 1：查看线程ID为1的线程详细情况
    thread -b：查看block的线程
    thread -i 5000：统计5秒钟之内的CPU利用率
    thread -n 2：线程CPU利用率的前两位
jvm：查看当前JVM信息
sysprop：查看和修改JVM系统属性
sysenv：查看JVM环境变量
vmoption：查看和修改JVM里诊断相关的option
perfcounter：查看当前JVM的 Perf Counter信息
logger：查看和修改logger
getstatic：查看类的静态属性
ognl：执行ognl表达式
mbean：查看Mbean的信息
heapdump：类似jmap命令heap dump

# 类加载相关
sc：查看JVM已加载的类信息
    class-pattern：类名表达式匹配
    sc -d org.apache.commons.lang.StringUtils || sc -d org/apache/commons/lang/StringUtils：
        输出当前类的详细信息，包括这个类加载的原始文件来源，类的声明、加载的ClassLoader等详细信息
        如果一个类被多个ClassLoader所加载，则会出现多次
    sc -d -f org.apache.commons.lang.StringUtils: 
        输出当前类的成员变量（需要配合-d使用）
    sc -E org\\.apache\\.commons\\.lang\\.StringUtils：
        开启正则表达式匹配，默认为通配符匹配
sm：查看已加载类的方法信息
    sm命令只能看到由当前类所声明的方法，父类的无法看到
    常用参数：类名表达式匹配
    method-pattern：方法名表达式匹配
    -d：展示每个方法的详细信息
    -E：开启正则表达式匹配，默认为通配符匹配
jat：反编译指定已加载类的源码
    反编译出来的Java代码可能会存在语法错误，但是并不映像阅读
mc、redefine：
    mc：Memory Compiler内存编译器，编译.java文件生成class
    redefine：加载外部的.class文件，redefine jvm已加载的类
    推荐使用retransform命令
jad & mc & redefine线上热更新
    1，反编译HelloWorld，保存到 /Users/yangshuai/arthas/HelloWorld.java 文件里。
        ```jad --source-only HelloWorld > /Users/yangshuai/arthas/HelloWorld.java```
    2，修改反编绎出来的代码
    3，sc查找加载UserController的ClassLoader
        ```sc -d *HelloWorld | grep classLoaderHash```
    4，mc内存编绎代码
        ```mc -c 18b4aac2 /Users/yangshuai/arthas/HelloWorld.java -d /Users/yangshuai/arthas```
    5，redefine热更新代码
        redefine /Users/yangshuai/arthas/HelloWorld.class
classloader：查看classloader的继承树、urls、类加载信息
    -t：查看ClassLoader的继承树
    -l：按类加载实例查看统计信息
    -c：用classloader对应的hashcode 来查看对应的jar urls

# monitor/watch/trace相关
monitor命令：方法执行监控（命令是一个非实时返回命令）
    例如：```monitor HelloWorld main```
    对匹配class-pattern/method-pattern的类、方法的调用进行监控。设计方法的调用次数、执行时间、失败率等
    class-pattern：类名表达式匹配
    method-pattern：方法名表达式匹配
    -c：统计周期，默认值是120秒
watch命令：方法执行数据观测
    让你能方便的观察到指定方法的调用情况。能观察到的范围为：
        返回值、抛出异常、入参，通过编写groovy表达式进行对应变量的查看
    常用参数：
        class-pattern：类名表达式匹配
        method-pattern：方法名表达式匹配
        express：观察表达式匹配
        condition-pattern：方法名表达式匹配
        -b：在方法调用之前观察（默认关闭）
        -e：在方法异常之后观察（默认关闭）
        -s：在方法返回之前观察（默认关闭）
        -f：在方法结束之后观察（默认开启）
        -x：指定输出结果的属性遍历深度，默认为0
    说明：
        这里重点说明的是观察表达式，观察表达式主要是由ognl表达式组成，
        所以你可以这样写"{params,returnObj}"，只要是合法的ognl表达式都能被识别
trace命令：方法内部调用路径，并输出方法路径上的每个节点的耗时
    补充说明：
        1，trace命令能主动搜索 class-pattern/method-pattern 对应的方法路径，渲染和统计整个调用链路上的所有性能开销和追踪调用链路
        2，trace能方便帮助你定位和发现因RT高而导致的性能问题缺陷，但每次只能追踪一级方法的调用链路
        3，trace在执行的过程中本身是有一定的性能开销，在统计的报告中并未像JProfiler一样预先减去自身的统计开销，所以统计出来的结果可能不准。
stack命令：输出当前方法被调用的调用路径
    常用参数：
        class-pattern：类名表达式匹配
        method-pattern：方法名表达式匹配
        condition-express：条件表达式
        -n：执行次数限制
tt命令：方法执行数据的时空隧道，记录下指定方法每次调用的入参和返回信息，并能对这些不同的时间下调用进行观测
    常用参数：
        -t：表明希望记录下类*Test的print方法的每次执行情况
        -n 3：指定你需要记录的次数
        -s：筛选指定方法的调用信息
        -i：参数后边跟着对应的INDEX编号查看到它的详细信息
        -p：重做一次调用。通过 --replay-times 指定调用次数，通过 --replay-interval指定多次调用间隔
Arthas实践系列：
https://blog.csdn.net/hengyunabc/article/details/87718469
