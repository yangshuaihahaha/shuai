# 线程和进程有什么区别
1，进程一个进程是独立的运行环境，不同的进程使用不同的内存空间，它可以被看作一个程序或者应用
2，而线程是进程中执行的一个任务，是进程的子集，一个进程可以有很多线程，所有的线程共享一片相同的内存空间

# 创建线程的几种方式
1，写一个类继承Thread类，重写run方法，用start启动线程。
2，写一个类实现Runnable接口，实现run方法，用start启动线程。
两者区别：
    （1）适合多个相同的程序代码的线程去处理同一个资源，也就是适合资源共享，但这是一个伪命题：
        class MyThread extends Thread {
            int things = 5;
            @Override
            public void run() {
                while(things > 0) {
                    System.out.println(currentThread().getName() + " things:" + things);
                    things--;
                }
            }
        }
        这里创建了两个MyThread，当然有两个things，所以会卖出10倍的票
        class MyRunnable implements Runnable {
            int things = 5;
            @Override
            public void run() {
                while(things > 0) {
        　　　　　　　things--;
                    System.out.println(name + " things:" + things);
                }
            }
        }
        new MyThread().start();
        new MyThread().start();
        new Thread(MyRunnable).start();
        new Thread(MyRunnable).start();  
        这里创建了两个MyThread，但是只创建了一个MyRunnable，所以things数量会差一倍，但是本质上还是应该加上锁来保证并发问题
    （2）实现Runnable接口可以避免java中的单继承的限制
    （3）实现Runnable接口增加程序的健壮性，把线程和任务拆分开了，代码可以被多个线程共享，代码和数据独立
    （4）实现Runnable接口更容易和线程池等高级API配合使用
3，通过Callable和Future创建线程
    背景：
        一般创建线程是通过Thread和Runnable，但是这两种方式又一个缺陷：在执行完任务之后无法获得执行结果
        如果想要获取结果就需要通过共享变量或者使用线程间通信的方式来达到效果，这样使用起来比较麻烦
        而自从Java 1.5开始，就提供了Callable和Future，通过它们可以在任务执行完毕之后得到任务执行结果。
    Callable和Future简介：
        Callable接口可以理解成一段可以调用并返回结果的代码
        Future接口表示异步任务，是还没有完成的任务给出的未来结果。
        所以说Callable用于产生结果，Future用于获取结果。
    源码分析：
        Runnable位于java.lang包下，它是一个接口，在它里面声明了一个方法叫做 run()：
            ```java
            @FunctionalInterface
            public interface Runnable {
                public abstract void run();
            }
            ```
        Callable位于java.util.concurrent包下，它也是一个接口，在它里面也只声明了一个方法，只不过这个方法叫做call()：
            ```
            @FunctionalInterface
            public interface Callable<V> {
                V call() throws Exception;
            }
            ```
        Future源码，Future类位于java.util.concurrent包下，它也是一个接口
            ```
            public interface Future<V> {
                /**
                 * 取消任务
                 */
                boolean cancel(boolean mayInterruptIfRunning);
                /**
                 * 任务是否被取消成功
                 */
                boolean isCancelled();
                /**
                 * 任务是否已经完成
                 */
                boolean isDone();
                /**
                 * 获取执行结果
                 */
                V get() throws InterruptedException, ExecutionException;
                /**
                 * 获取执行结果，支持超时
                 */
                V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;
            }
            ```
    使用：
        1，创建自定义类实现于Callable接口，并实现call()方法，该call()方法将作为线程执行体，并且有返回值。
        2，创建自定义类（Callable实现类）的实例，使用FutureTask类来包装Callable对象，该FutureTask对象封装了该Callable对象的call()方法的返回值。
        3，使用FutureTask对象作为Thread对象的target创建并启动新线程。
        4，调用FutureTask对象的get()方法来获得子线程执行结束后的返回值
        ```
        public class CallableTest implements Callable{
            @Override
            public Object call() throws Exception {
                int i = 1000;
                for ( ; i < 1010; i++) {
                    System.out.println(i);
                }
                return 1111;
            }
            public static void main(String[] args) {
                CallableTest callableTest = new CallableTest();
                FutureTask<Integer> futureTask = new FutureTask<Integer>(callableTest);
                Thread thread = new Thread(futureTask);
                thread.start();
                try {
                   System.out.println("Result："+futureTask.get());
                } catch (InterruptedException | ExecutionException e) {
                   e.printStackTrace();
                }
            }
        }
        ```
    总结：
        1，Future就是对于具体的Runnable或者Callable任务的执行结果进行取消、判断任务是否完成、获取结果。
        2，可以通过get方法获取执行结果，该方法会阻塞直到任务返回结果。

# 线程运行原理
我们都知道JVM中由堆、栈、方法区组成，那其中栈内存是给谁用的呢？其实就是线程
1，每个线程启动，虚拟机就会为其分配一块内存
2，每个栈由多个栈帧组成，对应着每次方法调用时所占用的内存
3，每个线程只能有一个活动的栈帧，对应着当前正在执行的那个方法
4，线程执行过程中因为一些原因导致cpu不再执行当前线程，会进行线程的上下文切换：
    （1）线程的cpu时间片用完
    （2）垃圾回收
    （3）有更高优先级的线程需要运行
    （4）线程自己调用了sleep、yield、wait、join、park、synchronized、lock等方法
5，当线程上下文切换发生时，需要由操作系统保存当前线程的状态，并恢复另一个线程的状态，
Java中对应的概念就是程序计数器（Program Counter Register），它的作用是记住下一条jvm指令的执行地址，是线程私有的


# 线程的生命周期
新建：就是刚使用new方法，new出来的线程；
    仅仅是在JAVA的这种编程语言层面被创建，而在操作系统层面，真正的线程还没有被创建。只有当我们调用了 start() 方法之后，该线程才会被创建出来，进入Runnable状态。
就绪：就是调用的线程的start()方法后，这时候线程处于等待CPU分配资源阶段，谁先抢的CPU资源，谁开始执行;
    Runnable状态的线程无法直接进入Blocked状态和Terminated状态的。只有处在Running状态的线程，换句话说，只有获得CPU调度执行权的线程才有资格进入Blocked状态和Terminated状态，Runnable状态的线程要么能被转换成Running状态，要么被意外终止
运行：当就绪的线程被调度并获得CPU资源时，便进入运行状态，run方法定义了线程的操作和功能;
    Running状态的线程能发生哪些状态转变？
        1，被转换成Terminated状态，比如调用 stop() 方法;
        2，被转换成Blocked状态，比如调用了sleep, wait 方法被加入 waitSet 中；
        3，被转换成Blocked状态，如进行 IO 阻塞操作，如查询数据库进入阻塞状态；
        4，被转换成Blocked状态，比如获取某个锁的释放，而被加入该锁的阻塞队列中；
        5，该线程的时间片用完，CPU 再次调度，进入Runnable状态；
        6，线程主动调用 yield 方法，让出 CPU 资源，进入Runnable状态
阻塞：在运行状态的时候，可能因为某些原因导致运行状态的线程变成了阻塞状态，比如sleep()、wait()之后线程就处于了阻塞状态，这个时候需要其他机制将处于阻塞状态的线程唤醒，比如调用notify或者notifyAll()方法。唤醒的线程不会立刻执行run方法，它们要再次等待CPU分配资源进入运行状态;
    Blocked状态的线程能够发生哪些状态改变？
        1，被转换成Terminated状态，比如调用 stop() 方法，或者是 JVM 意外 Crash;
        2，被转换成Runnable状态，阻塞时间结束，比如读取到了数据库的数据后；
        3，完成了指定时间的休眠，进入到Runnable状态；
        4，正在wait中的线程，被其他线程调用notify/notifyAll方法唤醒，进入到Runnable状态；
        5，线程获取到了想要的锁资源，进入Runnable状态；
        6，线程在阻塞状态下被打断，如其他线程调用了interrupt方法，进入到Runnable状态；
销毁：如果线程正常执行完毕后或线程被提前强制性的终止或出现异常导致结束，那么线程就要被销毁，释放资源;
    哪些情况下，线程会进入到Terminated状态呢？
        1，线程正常运行结束，生命周期结束；
        2，线程运行过程中出现意外错误；
        3，JVM 异常结束，所有的线程生命周期均被结束。

# 关于Thread
基本方法：
    Thread.sleep(long)：强制线程睡眠一段时间。
    Thread.activeCount()：获取当前程序中存活的线程数。
    thread.start()：启动一个线程。
    Thread.currentThread()：获取当前正在运行的线程。
    thread.getThreadGroup()：获取线程所在线程组。
    thread.getName()：获取线程的名字。
    thread.getPriority()：获取线程的优先级。
    thread.setName(name)：设置线程的名字。
    thread.setPriority(priority)：设置线程的优先级。
    thread.isAlive()：判断线程是否还存活着。
    thread.isDaemon()：判断线程是否是守护线程。
    thread.setDaemon(true)：将指定线程设置为守护线程。
    thread.join()：在当前线程中加入指定线程，使得这个指定线程等待当前线程，并在当前线程结束前结束。
        场景：
            主线程创建并启动子线程，如果子线程中要进行大量的耗时运算，主线程将可能早于子线程结束。
            如果主线程需要知道子线程的执行结果时，就需要等待子线程执行结束了。
            主线程可以sleep(xx),但这样的xx时间不好确定，因为子线程的执行时间不确定，join()方法比较合适这个场景。
        示例：
            public class Counter {
                public volatile static int count = 0;
                public synchronized static void inc() {
                    //这里延迟1毫秒，使得结果明显
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }
                    count++;
                }
                public static void main(String[] args) {
                    //同时启动1000个线程，去进行i++计算，看看实际结果
                    List<Thread> threads = new ArrayList<>();
                    for (int i = 0; i < 1000; i++) {
                        threads.add(new Thread(Counter::inc, "thread" + i));
                    }
                    threads.forEach((t) -> t.start());
                    threads.forEach((t) -> {
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    //这里每次运行的值都有可能不同,可能为1000
                    System.out.println("运行结果:Counter.count=" + Counter.count);
                }
            }
    thread.yield()：使得当前线程退让出CPU资源，把CPU调度机会分配给同样线程优先级的线程。
        使当前线程从执行状态（运行状态）变为可执行态（就绪状态）。cpu会从众多的可执行态里选择，也就是说，当前也就是刚刚的那个线程还是有可能会被再次执行到的，并不是说一定会执行其他线程而该线程在下一次中不会执行到了。
        打个比方：现在有很多人在排队上厕所，好不容易轮到这个人上厕所了，突然这个人说：“我要和大家来个竞赛，看谁先抢到厕所！”，然后所有的人在同一起跑线冲向厕所，有可能是别人抢到了，也有可能他自己有抢到了。我们还知道线程有个优先级的问题，那么手里有优先权的这些人就一定能抢到厕所的位置吗? 不一定的，他们只是概率上大些，也有可能没特权的抢到了。
        示例：
            public class YieldTest extends Thread {
                public YieldTest(String name) {
                    super(name);
                }
                @Override
                public void run() {
                    for (int i = 1; i <= 50; i++) {
                        System.out.println("" + this.getName() + "-----" + i);
                        // 当i为30时，该线程就会把CPU时间让掉，让其他或者自己的线程执行（也就是谁先抢到谁执行）
                        if (i == 30) {
                            this.yield();
                        }
                    }
                }
                public static void main(String[] args) {
                    YieldTest yt1 = new YieldTest("张三");
                    YieldTest yt2 = new YieldTest("李四");
                    yt1.start();
                    yt2.start();
                }
            }
            运行结果：
            第一种情况：李四（线程）当执行到30时会CPU时间让掉，这时张三（线程）抢到CPU时间并执行。
            第二种情况：李四（线程）当执行到30时会CPU时间让掉，这时李四（线程）抢到CPU时间并执行。
    thread.interrupt()：使得指定线程中断阻塞状态，并将阻塞标志位置为true。
        在Java中，中断的作用被明显弱化了，更多的是指线程标志位的某个状态，而中断处理逻辑需要用户自己来定制
        首先，一个线程不应该由其他线程来强制中断或停止，而是应该由线程自己自行停止。所以，Thread.stop, Thread.suspend, Thread.resume 都已经被废弃了。
        Thread.interrupt()并不会中断线程的运行，它的作用仅仅是为线程设定一个状态而已，即标明线程是中断状态
        1，如果线程处于被阻塞状态（例如处于sleep, wait, join 等状态），那么线程将立即退出被阻塞状态，并抛出一个InterruptedException异常。仅此而已。
        2，如果线程处于正常活动状态，那么会将该线程的中断标志设置为 true，仅此而已。被设置中断标志的线程将继续正常运行，不受影响。
    thread.interrupted()：测试当前线程是否已经中断。线程的“中断状态”由该方法清除。换句话说，如果连续两次调用该方法，则第二次调用将返回 false。
    thread.isInterrupted()：测试线程是否已经中断。线程的“中断状态”不受该方法的影响。
    object.wait()、object.notify()、object.notifyAll()：Object类提供的线程等待和线程唤醒方法。
wait和sleep的区别：
    相同：
        1，wait和sleep都可以使线程进入阻塞状态
        2，wait和sleep方法都是可中断的，中断后都会受到中断异常
    不同点：
        1，所属类不同：wait是object方法，而sleep是thread特有的方法
        2，关于释放锁：wait会释放锁，sleep是休眠不会释放锁
        3，使用位置不同：wait方法执行必须在同步代码块中，而sleep可以放在任何位置
        4，sleep方法短暂休眠后会主动退出阻塞，而wait（没有指定等待时间的时候）则需要被其他线程唤醒
关于notify：
    1，唤醒等待对象监视器的单个线程
    2，如果等待对象监视器有多个线程，则选取其中一个线程唤醒
    3，唤醒哪一个线程是任意的，有cpu决定
关于notifyAll：
    唤醒等待监视器的所有线程

# 中断线程的三种方法
    1，使用标志位终止线程
        在 run() 方法执行完毕后，该线程就终止了。但是在某些特殊的情况下，run() 方法会被一直执行；
        比如在服务端程序中可能会使用 while(true) { ... } 这样的循环结构来不断的接收来自客户端的请求。
        此时就可以用修改标志位的方式来结束 run() 方法
        public class ServerThread extends Thread {
            //volatile修饰符用来保证其它线程读取的总是该变量的最新的值
            public volatile boolean exit = false; 
            @Override
            public void run() {
                ServerSocket serverSocket = new ServerSocket(8080);
                while(!exit){
                    serverSocket.accept(); //阻塞等待客户端消息
                    ...
                }
            }
            public static void main(String[] args) {
                ServerThread t = new ServerThread();
                t.start();
                ...
                t.exit = true; //修改标志位，退出线程
            }
        }
    2，使用 stop() 终止线程（已废弃）
    3，使用 interrupt() 中断线程
        interrupt() 方法并不像在 for 循环语句中使用 break 语句那样干脆，马上就停止循环。调用 interrupt() 方法仅仅是在当前线程中打一个停止的标记，并不是真的停止线程。
        也就是说，线程中断并不会立即终止线程，而是通知目标线程，有人希望你终止。至于目标线程收到通知后会如何处理，则完全由目标线程自行决定
        public class InterruptThread1 extends Thread{
            public static void main(String[] args) {
                try {
                    InterruptThread1 t = new InterruptThread1();
                    t.start();
                    Thread.sleep(200);
                    t.interrupt();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void run() {
                super.run();
                for(int i = 0; i <= 200000; i++) {
                    //判断是否被中断
                    if(Thread.currentThread().isInterrupted()){
                        //处理中断逻辑
                        break;
                    }
                    System.out.println("i=" + i);
                }
             }
        }

# 线程的优先级
高优先级的线程会优先运行，但是高优先级的线程未必会最先运行完。
    有时候各种结果看起来与优先级相违背，可能的原因是：
        运行日志无法完美展示JVM的运行过程，写日志也会消耗资源。
        多核处理器的影响。在多核处理器上运行多线程，不仅会产生并发效果，也会产生并行效果。
        线程进入JVM的时间不同。JVM并不是在同一时刻启动所有的线程，线程本身的启动也有先后顺序。
# 守护线程
1，守护线程一般用于周期性的执行某个任务或等待某个事件的发生。
2，守护线程是一种低级别的线程，当 Java 虚拟机中仅剩下守护线程的时候 JVM 会退出。
3，我们可以通过方法 Thread.setDaemon(true) 将线程设置为守护线程，该方法必须在 Thread.start()方法之前调用。
4，在守护线程中创建的线程也是守护线程；守护线程可能因为没有用户线程而随时退出，所以不建议使用守护线程进行资源访问或者某些重要的定时任务。
5，守护线程应该永远不去访问固有资源，如文件、数据库，因为它会在任何时候甚至在一个操作的中间发生中断。
应用场景：
    你还想让一个不太重要的线程执行任务，但是又想等主要线程退出时不太重要的线程也立刻结束。或许你想到了使用中断的方式，但是如果将不太重要的线程设置为守护进程使用则更加简单。
    守护线程的角色就像“服务员”，而用户线程的角色就像“顾客”，当“顾客”全部走了之后（全部执行结束），那“服务员”（守护线程）也就没有了存在的意义，所以当一个程序中的全部用户线程都结束执行之后，那么无论守护线程是否还在工作都会随着用户线程一块结束，整个程序也会随之结束运行。
    典型应用场景：
        1，守护线程的典型应用场景就是垃圾回收线程
        2，比如服务器端的健康检测功能，对于一个服务器来说健康检测功能属于非核心非主流的服务业务，像这种为了主要业务服务的业务功能就非常合适使用守护线程，当程序中的主要业务都执行完成之后，服务业务也会跟随者一起销毁。
    项目中会用到类似守护线程的场景，就是Listener中的代码是没有开启session的，就会将这部分代码新开启一个类似守护线程来运行
    在这个线程中设置ThreadLocal里面当前登陆的用户，开启session等等。

# CAS算法
即compare and swap（比较与交换），是一种有名的无锁算法。即不使用锁的情况下实现多线程之间的变量同步，也就是在没有线程被阻塞的情况下实现变量的同步，所以也叫非阻塞同步（Non-blocking Synchronization）。
    CAS算法涉及到三个操作数
    当前内存值V
    旧的预期值A
    即将更新的值B
当且仅当 V 的值等于 A时，CAS通过原子方式用新值B来更新V的值，否则不会执行任何操作（比较和替换是一个原子操作）。一般情况下是一个自旋操作，即不断的重试。
基本原理：
    1，当内存地址V当中，存储着值为10的变量
    2，此时线程1想要把变量的值加1。对于线程1来说，旧的预期值A=10，要修改的新值B=11
    3，在线程1要提交更新之前，另一个线程2抢先一步，把内存中V中的变量值更新成了11
    4，此时线程1开始提交更新，首先A和实际地址值比较，发现A不等于V的实际值，提交失败
    5，线程1重新获取内存地址V的当前值，并重新计算想要修改的新值。此时对线程1来说，A=11，B=12。这个重新尝试的过程成为自旋
    6，这一次比较幸运，没有其他线程改变地址V值。线程1进行compare，发现A和地址V实际值相等，那么就更新成功
CAS与synchronized对比
    1，从思想上来说cas属于乐观锁，而synchronized属于悲观锁。但是并不是说就不使用悲观锁了
    2，简单的来说CAS适用于写比较少的情况下（多读场景，冲突一般较少），synchronized适用于写比较多的情况下（多写场景，冲突一般较多）
    3，对于资源竞争较少（线程冲突较轻）的情况，使用synchronized同步锁进行线程阻塞、唤醒切换、用户态内核态间的切换操作浪费消耗cpu资源；
    而CAS基于硬件实现，不需要进入内核，不需要切换线程，操作自旋几率较少，因此可以获得更高的性能。
    4，对于资源竞争严重（线程冲突严重）的情况，CAS自旋的概率会比较大，从而浪费更多的CPU资源，效率低于synchronized。
    在并发量比较高的情况下，反而使用同步锁更适合
应用场景：
    Atomic、Lock系列类以及Synchronized转变为重量级锁之前，都会使用cas机制
缺点：
    1，CPU开销较大，循环时间长
        在并发量比较高的情况下，如果许多线程反复尝试更新某一个变量，却又一直更新不成功，循环往复，会给CPU带来很大的压力。
    2，不能保证代码块的原子性
        CAS机制所保证的只是一个变量的原子性操作，而不能保证整个代码块的原子性。比如需要保证3个变量共同进行原子性的更新，就不得不使用Synchronized了。
        解决：
            但是从 JDK 1.5开始，提供了AtomicReference类来保证引用对象之间的原子性，你可以把多个变量放在一个对象里来进行 CAS 操作.
            所以我们可以使用锁或者利用AtomicReference类把多个共享变量合并成一个共享变量来操作。
    3，ABA 问题
        如果一个变量V初次读取的时候是A值，并且在准备赋值的时候检查到它仍然是A值，那我们就能说明它的值没有被其他线程修改过了吗？
        很明显是不能的，因为在这段时间它的值可能被改为其他值，然后又改回A，那CAS操作就会误认为它从来没有被修改过。这个问题被称为CAS操作的 "ABA"问题。
        解决：
            我们在compare阶段不仅需要比较内存地址V中的值是否和旧的期望值A相同，还需要比较变量的版本号是否一致。
            JDK 1.5 以后的 AtomicStampedReference 类就提供了此种能力，其中的 compareAndSet 方法就是首先检查当前引用是否等于预期引用，并且当前标志是否等于预期标志，
            如果全部相等，则以原子方式将该引用和该标志的值设置为给定的更新值。

# 并发
1，线程内存模型
    1，所有的共享变量都存储于主内存，这里所说的变量指的是实例变量和类变量，不包含局部变量，因为局部变量是线程私有的，因此不存在竞争问题。
    2，每一个线程还存在自己的工作内存，线程的工作内存，保留了被线程使用的变量的从主存中拷贝的变量副本。
    3，线程对变量的所有的操作(读，取)都必须在工作内存中完成，而不能直接读写主内存中的变量。
    4，不同线程之间也不能直接访问对方工作内存中的变量，线程间变量的值的传递需要通过主内存中转来完成
2，并发三要素：
    原子性：在一个操作中，CPU 不可以在中途暂停然后再调度，即不被中断操作，要么执行完成，要么就不执行。
    可见性：多个线程访问同一个变量时，一个线程修改了这个变量的值，其他线程能够立即看得到修改的值。
    有序性：程序执行的顺序按照代码的先后顺序执行。

3，怎么做，才能解决并发问题？
    （1）volatile：
        保证可见性，不保证原子性
            可见性：
                当一个线程修改了变量的值 其他线程会立即得知
            怎么保证 变量在线程中的可见性的
                当写一个volatile变量时，jvm会把本地内存变量强制刷新到主内存中
                这个写操作导致其他线程中缓存无效，其他线程读，会从主内存读。volatile的写操作对其他线程实时可见
                一致性协议MESI：
                    写操作： 当一个线程对共享变量发生写操作的时候 会把其它线程中的对应的共享变量副本置为无效状态。
                    读操作： 当一个线程使用共享变量的时候它会先判断当前副本变量的状态 如果是无效状态的话 会向总线发送read 消息读取变量最新数据 总线贯穿这个变量用到的所有缓存以及主存 读取到的最新数据可能来自主存 也可能来自其他线程 
                    代码方面看 在修改共享变量后会有一个 lock 指令 这个指令会将本线程缓存写入内存 并 使得其他线程中的缓存失效（缓存锁）
        禁止指令重排序
            什么是指令重排？
                比如 两个处理器同时处理两个共享变量  
                处理器1  变量a++ 变量b++
                处理器2  变量a++ 变量b++
                这个时候两个处理器都先执行a++操作 为了保证数据的安全 肯定要一
                个一个来，一个一个来的话那么势必要有一个处理器处于闲置状态等
                待另一个处理器操作共享变量完成它才能运行，这样的话就造成了cpu资源的浪费。
                当进行指令重排优化后 可能会变成这样啦
                处理器1  变量a++ 变量b++
                处理器2  变量b++ 变量a++ 
                当处理器1操作a的时候 处理器2在操作b 
                通过重排了指令的执行顺序 避免了cpu资源的浪费
                （真实情况比这个要复杂多哈哈哈）
            禁止指令重排 处理器不会对代码进行乱序优化 就按写入的顺序执行，在操作变量后会有一个 内存屏障  内存屏障后的指令不能重排序到内存屏障之前的位置 。
            假设volatile 没有指令重排功能  变量只会保证该方法执行过程中所有依赖值结果的地方都能获得到正确的结果  但不能保证该方法的执行顺序 与写入代码的顺序一样 这样在多线程环境下就会出现问题
        volatile原理：
            在JVM底层volatile是采用内存屏障来实现的，内存屏障会提供3个功能：
                1，它确保指令重排序时不会把其后面的指令排到内存屏障之前的位置，也不会把前面的指令排到内存屏障的后面；即在执行到内存屏障这句指令时，在它前面的操作已经全部完成；
                2，它会强制将缓存的修改操作立即写到主内存
                3，写操作会导致其它CPU中的缓存行失效，写之后，其它线程的读操作会从主内存读。
        volatile 的局限性
            volatile 只能保证可见性，不能保证原子性写操作对其它线程可见，但是不能解决多个线程同时写的问题。
    （2）synchronized
        特性：
            1，保证原子性
            2，保证可见性
            3，保证有序性
            4，拥有可重入特性
            5，拥有不可中断特性
        Synchronized 可以保证同一时刻，只有一个线程可执行某个方法或某个代码块。可以解决同时写的问题
        对应的锁：
            1，如果synchronized修饰的是实例方法，对应的锁就是对象实例
            2，如果synchronized修饰的是静态方法，对应的锁就是当前类的class实例
            3，如果synchronized修饰的是代码块，对应的锁就是传入synchronized的对象实例
        基本原理：
            Synchronized是Monitor对象监视器来实现代码块同步和方法同步的。
            任何一个对象都有一个与其关联的对象监视器monitor，存放在对象头中。
            当一个对象的monitor被持有后，就锁住了一个对象。
            代码块同步使用的是monitorenter和 monitorexit 指令实现的。
            方法同步是通过Access flags后面的标识来确定该方法是否为同步方法。
            流程：
                获取Monitor和释放Monitor的流程：
                    （1）多个线程访问同步代码块的时候，首先会进入EntryList。
                    （2）然后会通过cas的方式尝试将monitor的owner设置为当前线程
                    （3）如果设置之前owner指向当前线程，说明当前线程再次进入owner，即重入锁，执行进入数+1
                    （4）如果当前线程第一次进入该monitor，设置monitor进入数为1，owner为当前线程
                    （5）如果获取锁失败，线程进入EntryList等待锁释放
                    （6）当持有monitor的线程调用wait()方法的时候，会将owner设置为null，进入数-1，然后把当前线程加入到WaitSet中，等待被唤醒
            为什么只有在同步代码块中才能调用wait/notify？
                通过以上synchronized底层原理可以得知，wait/notify只能在同步代码块中使用
        Synchronized 锁的优化：
            Java1.6对 Synchronized 进行了各种锁优化，比如：偏向锁、轻量级锁、适应性自旋、锁消除、锁粗化
                偏向锁：
                    目的：偏向锁的目标是，减少无竞争且只有一个线程使用锁的情况下，使用轻量级锁产生的性能消耗
                    问题：大多数情况下，锁不存在多线程竞争，而是由同一线程多次获得，为了让线程获得锁代价更低引入偏向锁
                    解决：当线程a访问加了锁的代码块时，会在对象头中存储当前线程id，后续线程a进入和退出这段加了锁的同步代码块时不需要加锁和释放锁
                         轻量级锁每次申请、释放锁都至少需要一次CAS，但偏向锁只有初始化时需要一次CAS。
                轻量级锁：
                    目的：轻量级锁的目标是，减少无实际竞争情况下，使用重量级锁产生的性能消耗
                    在偏向锁的情况下，如果线程b也访问了同步代码块，比较对象头的线程id不一样，会升级为轻量级锁，并通过自旋的方式来获取轻量级锁
                    具体过程：
                        在代码进入同步代码块的时候，如果同步对象的锁状态为无锁状态，虚拟机会首先在当前线程的栈帧中建立一个名为锁记录（Lock Record）的空间，用于存储锁对象Mark Record的拷贝
                        拷贝对象头的Mark Word到锁记录（Lock Record）中
                        拷贝成功过后，虚拟机使用cas操作尝试将锁对象的Mark Word更新为指向Lock Record的指针，并将线程栈帧中的Lock Record里的owner指针指向Object的Mark Word
                        如果这个更新动作成功了，那么这个线程就拥有了该对象的锁，并且对象Mark Word的锁标志位设置为“00”，表示对象属于轻量级锁状态。
                        如果这个更新失败了，虚拟机会检查对象的Mark Record是否指向当前线程栈帧，如果是就说明当前线程已经拥有了这个对象的锁，那就可以直接进入同步块继续执行
                        否则就说明多个线程竞争锁，轻量级锁膨胀为重量级锁，锁标志的状态值变为“10”
                重量级锁：
                    在轻量级锁状态下，当前线程会在栈帧下创建lock record，lock record会把mark word信息拷贝进去，且有个owner指针指向加锁对象
                    线程执行到同步代码时，怎用cas试图将mark word指向线程栈帧的lock record，假设cas修改成功，则获取轻量级锁，修改失败则自旋重试，一定次数后升级为重量级锁
                    如果线程a和线程b同时访问同步代码块，则轻量级锁升级为重量级锁，线程a获取到重量级锁的情况下，线程b只能入队等待，进入block
                锁消除：
                    在JIT编译的时候，对运行上下文进行扫描，去除不可能存在竞争的锁
                    比如这两段代码的执行效率是一样的：
                    ```java
                    public class SynchronizedTest {
                        public static void main(String[] args) {
                            SynchronizedTest test = new SynchronizedTest();
                            for (int i = 0; i < 100000000; i++) {
                                test.append("abc", "def");
                            }
                        }
                        public void append(String str1, String str2) {
                            StringBuffer sb = new StringBuffer();
                            sb.append(str1).append(str2);
                        }
                    }
                    ```
                    虽然 StringBuffer 的 append 是一个同步方法，但是这段程序中的 StringBuffer 属于一个局部变量，并且不会从该方法中逃逸出去（即StringBuffer sb的引用没有传递到该方法外，不可能被其他线程拿到该引用），所以其实这过程是线程安全的，可以将锁消除。
                锁粗化：
                    扩大加锁范围，避免反复加锁
                    ```java
                    public String test(String str){
                           int i = 0;
                           StringBuffer sb = new StringBuffer():
                           while(i < 100){
                               sb.append(str);
                               i++;
                           }
                           return sb.toString():
                    }
                    ```
                    我们无法把锁提取到循环体外，如果没有锁粗化，此处要进行100次加锁。
                    此时JVM会检测到这样一连串的操作都对同一个对象加锁，JVM就会将锁的范围粗化到这一连串的操作的外部（比如while的虚幻体外）
                    使得这一连串的操作只需要加一次锁即可。
        Synchronized锁升级过程：
            无锁 --> 偏向锁 --> 轻量级锁 --> 适应性自旋 --> 重量级锁
            具体过程：
                当一个对象开始实例化的时候，他认为可能只有一个线程访问他，所以当第一个线程访问他的时候，他会偏向这个线程
                这个线程使用cas操作将对象头中的ThreadID改成自己的ID，之后再访问这个对象时只需要对比ID，不在需要cas操作
                一旦有第二个线程访问这个对象，第二个线程可以看到对象时偏向状态，这时表明在这个对象上已经存在竞争了，然后检查原来持有该对象锁的线程是否存活
                如果挂了，就将对象改为无锁状态，然后重新偏向新的线程，如果依然存活，则马上执行那个线程的操作数栈，检查对象的使用情况
                如果仍然需要持有偏向锁，则偏向锁升级为重量级锁，如果不再使用，则将对象恢复成无锁状态，重新偏向
                轻量级锁认为竞争存在，但是竞争的程度很轻，一般两个线程对于同一个锁的操作都会错开，或者说稍微等待一下（自旋），另一个线程就会释放锁。 
                但是当自旋超过一定的次数，或者一个线程在持有锁，一个在自旋，又有第三个来访时，轻量级锁膨胀为重量级锁，重量级锁使除了拥有锁的线程以外的线程都阻塞，防止CPU空转。
        锁是不能降级的（这里说的是有线程占有锁的情况，如果没有任何一个线程占有锁，锁是可以降级成无锁的）
        总结：
            Synchronized原本只有重量级锁，依赖操作系统的mutex指令，需要用户态和内核态切换，性能损耗十分明显
            重量级锁用到monitor，
            偏向锁则在mark word记录线程id进行比较
            轻量级锁则是拷贝mark word到栈帧的lock word，用cas+自选的方式获取
            只有一个进入临界区：偏向锁
            多个线程交替进入临界区：轻量锁
            多个线程同时进入临界区：重量级锁
        

# Synchronized和Lock的区别
                    

                    
# AQS
什么是AQS？
    AQS是一个抽象的队列同步器，它定义了一套多线程访问共享资源的同步框架。
    许多同步类都依赖于他，比如ReentrantLock、Semaphore、ReentrantReadWriteLock、CountDownLatch、CyclicBarrier、Exchanger都依赖aqs才能实现各种锁机制
    它维护了一个volatile的state变量和一个先进先出的队列（双向链表结构）。其中state代表的是资源竞争标识，队列代表的是竞争资源失败的线程排队时存放的容器
AQS定义了两种资源共享模式：
    1，独占式
        每次只能有一个线程持有锁。 例如ReentrantLock
        实现过程：
            ReentrantLock初始化的时候state=0，表示资源未锁定。AQS内部还有一个关键变量用来记录当前加锁的是那一个线程。
            线程执行lock方法的时候，会调用tryAcquire()把AQS模式设置为独占，并将独占线程设置为当前线程，然后把state+1。
            当又有新线程来尝试加锁的时候，会判断当前加锁线程是否是新线程，如果是的话，那就是重入加锁，只需将state+1即可
            如果不是新线程会通过cas操作尝试把state改为1，这时候cas操作失败，就把新线程放入队列来等待，等待原来的线程释放锁之后，重新尝试加锁
            释放锁的过程就是将state-1，当state为0的时候释放锁，并把当前加锁线程设置为null。
            接下来就会唤醒等待队列的队头线程，其余节点线程继续沉睡。以此实现独占功能。
    2，共享式
        允许多个线程持有锁。例如CountDownLatch
        实现过程：
            CountDownLatch把AQS的state为共享锁的标记位，state>0就是加锁，等于0就是释放锁。每调用一次countDown()，state减1。线程会阻塞在await()，直到countDown()将state置为0
            CountDownLatch的await()方法不会只在一个线程中调用，多个线程可以同时等待await()方法返回，这些线程放在AQS队列中被刮起，锁在所有调用await()方法的线程间共享，所以叫做共享锁。
            当countDown()将state为0的时候，唤醒自身节点对应线程的同时，继续唤醒AQS队列中的下一个节点的线程，每个节点在唤醒自身的同时还会唤醒下一个节点对应的线程，以实现共享状态的"向后传播"，从而实现共享功能。
核心部件：
    （1）公平非公平锁
        非公平锁的lock方法的处理方式：在lock的时候先直接CAS修改一次state变量（尝试获取锁），成功就返回，不成功再排队，从而达到不排队直接抢占的目的。
        而对于公平锁：则是老老实实的开始就走AQS的流程排队获取锁。如果前面有人调用过其lock方法，则排在队列中前面，也就更有机会更早的获取锁，从而达到“公平”的目的。
        非公平锁中, 拥有锁的线程在释放锁资源的时候, 当前尝试获取锁资源的线程可以和等待队列中的第一个线程竞争锁资源, 这就是ReentrantLcok中非公平锁的含义; 
        但是已经进入等待队列的线程, 依然是按照先进先出的顺序获取锁资源
    （2）Condition队列
        主要用于实现条件锁。这个队列主要用于等待条件的成立，当条件成立时，才将节点移动到AQS的队列中，等待占有锁的线程释放锁后被唤醒
        典型的运用场景是在BlockingQueue中的实现，当队列为空时，获取元素的线程阻塞在notEmpty条件上，一旦队列中添加了一个元素，将通知notEmpty条件，将其队列中的元素移动到AQS队列中等待被唤醒。
        与Object的wait和notify/notifyAll不同：
            Condition能够支持不响应中断，而通过使用Object方式不支持；
            Condition能够支持多个等待队列（new 多个Condition对象），而Object方式只能支持一个；
            Condition能够支持超时时间的设置，而Object不支持
        实现分析：
            在这里一定要把条件队列和同步队列进行区分清楚！！
            条件队列/等待队列：即 Condition 的队列
            同步队列：AQS 的队列。
            每个condition对象包含一个等待队列，该队列是condition实现等待通知的关键。等待队列是一个先进先出的队列
            队列中每个节点都包含了一个线程引用，该线程就是在condition对象上等待的线程。
            如果一个线程调用了Condition.await()方法，那么该线程就会释放锁，然后构造节点加入等待队列进入等待状态
            然后当调用condition中的signal()方法，就会唤醒等待队列的首节点，唤醒首节点之前，会将节点移到同步队列中
    （4）模板方法；
        定义一个操作中算法的框架，将一些步骤延迟到子类中，使得子类可以不改变算法的结构即可重定义该算法中的某些特定步骤。

# ReentrantLock
ReentrantLock是一种重入锁。支持重入性，表示能够对共享资源能够重复加锁，即当前线程获取该锁再次获取不会被阻塞。
ReentrantLock还支持公平锁和非公平锁两种方式
主要特性：
    独占锁且可重入的:
        public class ReentrantLockTest {
            public static void main(String[] args) throws InterruptedException {
                ReentrantLock lock = new ReentrantLock();
                for (int i = 1; i <= 3; i++) {
                    lock.lock();
                }
                for(int i=1;i<=3;i++){
                    try {
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }
    公平锁:
        ReentrantLock lock = new ReentrantLock(true);
    可响应中断：
        ReentrantLock的中断和非中断加锁模式的区别在于：
            线程尝试获取锁操作失败后，在等待过程中，如果该线程被其他线程中断了，它是如何响应中断请求的。
            lock方法会忽略中断请求，继续获取锁直到成功；
            lockInterruptibly则直接抛出中断异常来立即响应中断，由上层调用者处理中断；
        适用场景：
            lock()适用于锁获取操作不受中断影响的情况，此时可以忽略中断请求正常执行加锁操作，因为该操作仅仅记录了中断状态（通过Thread.currentThread().interrupt()操作，只是恢复了中断状态为true，并没有对中断进行响应)。
            lockInterruptibly()适用于被中断线程不能参与锁的竞争操作，一旦检测到中断请求，立即返回不再参与锁的竞争并且取消锁获取操作（即finally中的cancelAcquire操作）
        public class ReentrantLockTest {
            static Lock lock1 = new ReentrantLock();
            static Lock lock2 = new ReentrantLock();
            public static void main(String[] args) throws InterruptedException {
                Thread thread = new Thread(new ThreadDemo(lock1, lock2));//该线程先获取锁1,再获取锁2
                Thread thread1 = new Thread(new ThreadDemo(lock2, lock1));//该线程先获取锁2,再获取锁1
                thread.start();
                thread1.start();
                thread.interrupt();//是第一个线程中断
            }
            static class ThreadDemo implements Runnable {
                Lock firstLock;
                Lock secondLock;
                public ThreadDemo(Lock firstLock, Lock secondLock) {
                    this.firstLock = firstLock;
                    this.secondLock = secondLock;
                }
                @Override
                public void run() {
                    try {
                        firstLock.lockInterruptibly();
                        TimeUnit.MILLISECONDS.sleep(10);//更好的触发死锁
                        secondLock.lockInterruptibly();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        firstLock.unlock();
                        secondLock.unlock();
                        System.out.println(Thread.currentThread().getName()+"正常结束!");
                    }
                }
            }
        }
    获取锁时限时等待
        public class ReentrantLockTest {
            static Lock lock1 = new ReentrantLock();
            static Lock lock2 = new ReentrantLock();
            public static void main(String[] args) throws InterruptedException {
                Thread thread = new Thread(new ThreadDemo(lock1, lock2));//该线程先获取锁1,再获取锁2
                Thread thread1 = new Thread(new ThreadDemo(lock2, lock1));//该线程先获取锁2,再获取锁1
                thread.start();
                thread1.start();
            }
            static class ThreadDemo implements Runnable {
                Lock firstLock;
                Lock secondLock;
                public ThreadDemo(Lock firstLock, Lock secondLock) {
                    this.firstLock = firstLock;
                    this.secondLock = secondLock;
                }
                @Override
                public void run() {
                    try {
                        while(!lock1.tryLock()){
                            TimeUnit.MILLISECONDS.sleep(10);
                        }
                        while(!lock2.tryLock()){
                            lock1.unlock();
                            TimeUnit.MILLISECONDS.sleep(10);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        firstLock.unlock();
                        secondLock.unlock();
                        System.out.println(Thread.currentThread().getName()+"正常结束!");
                    }
                }
            }
        }
        线程通过调用tryLock()方法获取锁,第一次获取锁失败时会休眠10毫秒,然后重新获取，直到获取成功。第二次获取失败时,首先会释放第一把锁,再休眠10毫秒,然后重试直到成功为止。
        线程获取第二把锁失败时将会释放第一把锁，这是解决死锁问题的关键,避免了两个线程分别持有一把锁然后相互请求另一把锁。
    结合condition实现等待通知机制：
        使用synchronized结合Object上的wait和notify方法可以实现线程间的等待通知机制。ReentrantLock结合Condition接口同样可以实现这个功能。而且相比前者使用起来更清晰也更简单。
        使用：
            Condition由ReentrantLock对象创建,并且可以同时创建多个
            Condition接口在使用前必须先调用ReentrantLock的lock()方法获得锁。之后调用Condition接口的await()将释放锁,并且在该Condition上等待,直到有其他线程调用Condition的signal()方法唤醒线程。使用方式和wait,notify类似。
                static Condition notEmpty = lock.newCondition();
                static Condition notFull = lock.newCondition();
            示例：
                public class ConditionTest {
                    static ReentrantLock lock = new ReentrantLock();
                    static Condition condition = lock.newCondition();
                    public static void main(String[] args) throws InterruptedException {
                        lock.lock();
                        new Thread(new SignalThread()).start();
                        System.out.println("主线程等待通知");
                        try {
                            condition.await();
                        } finally {
                            lock.unlock();
                        }
                        System.out.println("主线程恢复运行");
                    }
                    static class SignalThread implements Runnable {
                        @Override
                        public void run() {
                            lock.lock();
                            try {
                                condition.signal();
                                System.out.println("子线程通知");
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                }

# ReentrantReadWriteLock
ReentrantReadWriteLock允许多个读线程同时访问，但不允许写线程和读线程、写线程和写线程同时访问
在实际应用中，大部分对共享数据的读操作远多于写操作，这时读写锁相对于排他锁，有更好的并发性。
特性：
    1，支持公平和非公平获取锁的方式
    2，支持可重入：读线程获取了读锁之后还可以获取读锁；写线程获取了写锁之后既可以获取写锁又可以获取读锁
    3，支持锁降级：
        实现方式：先获取写锁，然后获取读锁，最后释放写锁。但是不允许从读锁升级到写锁
        场景：假设一个事物要执行10秒，写操作占用1秒，其余的都是读操作。那么在获取完写锁执行1秒的情况下，降级成读锁，并释放写锁，这样能提高并发
    4，读锁和写锁都支持锁获取间的中断
    4，当加了写锁时，其他线程对该锁加读锁或者写锁都会阻塞（不是失败）。
    5，当加了读锁时，其他线程对该锁加写锁会阻塞，加读锁会成功。
获取锁的过程：
    读写锁普通互斥锁的主要区别在于，使用AQS中的state同时记录读锁状态和写锁状态。
    他将int类型的state分为高16位和低16位，高16位记录读锁状态，低16位记录写锁状态
    同步队列中使用不同的mode描述来区分读锁等待节点和写锁等待节点
    在获取读锁时：
        如果写锁已经被其他线程获取，那么此线程将会加入到同步队列，挂起等待唤醒。
        如果写锁没有被其他线程获取，但是同步队列的第一个节点是写线程的节点，那么此线程让位给写线程，挂起等待唤醒
        如果获取读锁的线程，已经持有了写锁，那么即使同步队列的第一个节点是写线程的节点，它也不会让位给同步队列中的写线程，而是自旋去获取读锁。因为此时让位会造成死锁
    获取写锁时：
        如果读锁已经被获取，那么不允许获取写锁。将此线程加入到同步队列，挂起等待唤醒
        如果读锁没有被获取，但是写锁已经被其他线程抢占，那么还是将此线程加入到同步队列，挂起等待唤醒
        如果写锁已经被此线程持有，那么重入，即写状态+1
        如果读锁和写锁都没有被获取，那么CAS尝试获取写锁
为什么不能由读锁升级为写锁？
    如果两个线程的读锁同时升级为写锁，只有一个成功。这两个线程同时拥有读锁，其中一个线程还在申请写锁，这就会造成死锁。
    当时锁降级是可以的，因为只有一个线程持有写锁。
主要应用场景：
    1，利用重入来执行升级缓存后的锁降级
        class CachedData {
            Object data;
            //缓存是否有效
            volatile boolean cacheValid;
            ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
            public void processCachedData() {
                //获取读锁
                rwl.readLock().lock();
                //如果缓存无效，更新cache;否则直接使用data
                if (!cacheValid) {
                    //获取写锁前须释放读锁
                    rwl.readLock().unlock();
                    rwl.writeLock().lock();
                    //重新检查状态，因为另一个线程可能已获取
                    if (!cacheValid) {
                        data = ...
                        cacheValid = true;
                    }
                    //锁降级，在释放写锁前获取读锁
                    rwl.readLock().lock();
                     //释放写锁，仍然保留读锁
                    rwl.writeLock().unlock();
                }
                use(data);
                rwl.readLock().unlock();    //释放读锁
            }
        }
    2，使用 ReentrantReadWriteLock 来提高 Collection 的并发性
        通常在 collection 数据很多，读线程访问多于写线程并且 entail 操作的开销高于同步开销时尝试这么做。
        class RWDictionary {
            private final Map<String, Data> m = new TreeMap<String, Data>();
            private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
            private final Lock r = rwl.readLock();//读锁
            private final Lock w = rwl.writeLock();//写锁
            public Data get(String key) {
                r.lock();
                try { return m.get(key); }
                finally { r.unlock(); }
            }
            public String[] allKeys() {
                r.lock();
                try { return m.keySet().toArray(); }
                finally { r.unlock(); }
            }
            public Data put(String key, Data value) {
                w.lock();
                try { return m.put(key, value); }
                finally { w.unlock(); }
            }
            public void clear() {
                w.lock();
                try { m.clear(); }
                finally { w.unlock(); }
            }
        }
写锁的获取：
    实现读写锁以及公平性是依赖：
        读锁：public static class ReadLock implements Lock, java.io.Serializable {}
        写锁：public static class WriteLock implements Lock, java.io.Serializable {}
        同步器-公平性：abstract static class Sync extends AbstractQueuedSynchronizer {}
    接下来我们通过构造函数创建一个非公平性的ReentrantReadWriteLock读写锁的来看看其实现原理：
        // 默认构造方法*public ReentrantReadWriteLock() {this(false);}
        public ReentrantReadWriteLock(boolean fair) {  
            //默认非公平锁       
            sync = fair ? new FairSync() : new NonfairSync();  
            //创建读锁       
            readerLock = new ReadLock(this);  
            //创建写锁       
            writerLock = new WriteLock(this);  
        }
    读写锁ReadLock和WriteLock其都是使用同一个Sync，
        //读锁上锁
        protected ReadLock(ReentrantReadWriteLock lock) {           
            sync = lock.sync;      
        }
        //写锁上锁    
        protected WriteLock(ReentrantReadWriteLock lock) {
            sync = lock.sync;      
        }
    在Sync中32位的同步状态state，划分成2部分，高16位表示读，低16位表示写，假设当前同步状态为s：
        读状态为 s >>> 16，即无符号补0右移16位，读状态增加1，等于 s + (1 << 16)
        写状态为 s & 0000FFFF，即将高16位全部抹去，写状态增加1，等于 s + 1
    readLock::lock()实现:
        尝试获取同步状态tryAcquireShared(arg):
        1，如果是无锁状态，获取读锁，记录读者当前线程
        2，当已经是写锁状态，写锁的不是当前线程，那么直接返回失败（进入等待队列）
        3，已有读锁-->当前线程是队列中第一个读线程，其重入次数加1
        4，已有读锁时-->当前线程非首个获取读锁，先判断readHolds是否存在当前线程，放入线程本地变量
    writeLock::lock()实现：
        1，当为无锁状态-->获取写锁。返回false。
        2，当为有锁状态-->读锁。返回false。
        3，当为有锁状态-->写锁（当前线程）。判断当前线程获取写锁是否超过最大次数，若超过，抛异常，否则返回true
        3，当为有锁状态-->写锁（非当前线程）。返回false
        这里的返回false指的是阻塞：判断是否需要阻塞（公平和非公平方式实现不同），如果不需要阻塞，则CAS更新同步状态，若CAS成功则返回true，否则返回false。如果需要阻塞则返回false
    小结：
        在获取读锁时：
            如果写锁已经被其他线程获取，那么此线程将会加入到同步队列，挂起等待唤醒。
            如果写锁没有被其他线程获取，但是同步队列的第一个节点是写线程的节点，那么此线程让位给写线程，挂起等待唤醒
            如果获取读锁的线程，已经持有了写锁，那么即使同步队列的第一个节点是写线程的节点，它也不会让位给同步队列中的写线程，而是自旋去获取读锁。因为此时让位会造成死锁
        获取写锁时：
            如果读锁已经被获取，那么不允许获取写锁。将此线程加入到同步队列，挂起等待唤醒
            如果读锁没有被获取，但是写锁已经被其他线程抢占，那么还是将此线程加入到同步队列，挂起等待唤醒
            如果写锁已经被此线程持有，那么重入，即写状态+1
            如果读锁和写锁都没有被获取，那么CAS尝试获取写锁
    readLock.unlock()：
        释放锁
            如果第一个读线程是当前线程，将重入的次数减1，当减到0了就把第一个读者置为空
            如果第一个读线程不是当前线程，也需要将重入的次数减1，
            共享锁获取的次数减1， 如果减为0了说明完全释放了，才返回true
        释放成功，唤醒下一个节点
    WriteLock.unlock()：
        释放锁
            state状态变量的值减1，是否完全释放锁，如果完全释放锁则设置独占为null，
            设置state状态变量的值
        释放成功，唤醒下一个节点
        
原理：
    读写锁中Sync类是继承于AQS，并且主要使用上文介绍的数据结构中的state及waitState变量进行实现。 
    实现读写锁与实现普通互斥锁的主要区别在于需要分别记录读锁状态及写锁状态，并且等待队列中需要区别处理两种加锁操作。 
    AQS 的状态state是32位（int 类型），Sync使用state变量同时记录读锁与写锁状态，将int类型的state变量分为高16位与低16位，高16位记录读锁状态，低16位记录写锁状态
    sync使用不同的mode描述等待队列中的节点以区分读锁等待节点和写锁等待节点。mode取值包括SHARED及EXCLUSIVE两种，分别代表当前等待节点为读锁和写锁。
    代码过程：
        1，写锁加锁
            写锁加锁最终调用Sync类的acquire函数（继承自AQS）
            //获取写锁
            public void lock() {
                sync.acquire(1);
            }
            //AQS实现的独占式获取同步状态方法
            public final void acquire(int arg) {
                if (!tryAcquire(arg) &&
                    acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
                    selfInterrupt();
            }
            //自定义重写的tryAcquire方法
            protected final boolean tryAcquire(int acquires) {
                /*
                 * Walkthrough:
                 * 1. If read count nonzero or write count nonzero
                 *    and owner is a different thread, fail.
                 * 2. If count would saturate, fail. (This can only
                 *    happen if count is already nonzero.)
                 * 3. Otherwise, this thread is eligible for lock if
                 *    it is either a reentrant acquire or
                 *    queue policy allows it. If so, update state
                 *    and set owner.
                 */
                Thread current = Thread.currentThread();
                int c = getState();
                int w = exclusiveCount(c);    //取同步状态state的低16位，写同步状态
                if (c != 0) {
                    // (Note: if c != 0 and w == 0 then shared count != 0)
                    //存在读锁或当前线程不是已获取写锁的线程，返回false
                    if (w == 0 || current != getExclusiveOwnerThread())
                        return false;
                    //判断同一线程获取写锁是否超过最大次数，支持可重入
                    if (w + exclusiveCount(acquires) > MAX_COUNT)    //
                        throw new Error("Maximum lock count exceeded");
                    // Reentrant acquire
                    setState(c + acquires);
                    return true;
                }
                //此时c=0,读锁和写锁都没有被获取
                if (writerShouldBlock() ||
                    !compareAndSetState(c, c + acquires))
                    return false;
                setExclusiveOwnerThread(current);
                return true;
            }
            大致步骤：
                步骤1：判断同步状态state是否为0。如果state!=0，说明已经有其他线程获取了读锁或者写锁，执行步骤2，否则执行步骤5
                步骤2：判断同步状态state的低16位是否为0。如果w=0，说明其他线程获取了读锁，返回false；如果w!=0，说明其他线程获取了写锁，执行步骤3
                步骤3：判断获取了写锁是否是当前线程，若不是返回false，否则执行4
                步骤4：判断当前线程获取写锁是否超过最大次数，若超过，抛异常；反之更新同步状态（此时当前线程以获取写锁，更新是线程安全的），返回true。
                步骤5：此时读锁或写锁都没有被获取，判断是否需要阻塞（公平和非公平方式实现不同），如果不需要阻塞，则CAS更新同步状态，若CAS成功则返回true，否则返回false。如果需要阻塞则返回false
        2，写锁释放
            //写锁释放
            public void unlock() {
                sync.release(1);
            }
            //AQS提供独占式释放同步状态的方法
            public final boolean release(int arg) {
                if (tryRelease(arg)) {
                    Node h = head;
                    if (h != null && h.waitState != 0)
                        unparkSuccessor(h);
                    return true;
                }
                return false;
            }
            //自定义重写的tryRelease方法
            protected final boolean tryRelease(int releases) {
                if (!isHeldExclusively())
                    throw new IllegalMonitorStateException();
                int nextc = getState() - releases;    //同步状态减去releases
                //判断同步状态的低16位（写同步状态）是否为0，如果为0则返回true，否则返回false.
                //因为支持可重入
                boolean free = exclusiveCount(nextc) == 0;
                if (free)
                    setExclusiveOwnerThread(null);
                setState(nextc);    //以获取写锁，不需要其他同步措施，是线程安全的
                return free;
            }
        3，读锁加锁
            public void lock() {
                sync.acquireShared(1);
            }
            //使用AQS提供的共享式获取同步状态的方法
            public final void acquireShared(int arg) {
                if (tryAcquireShared(arg) < 0)
                    doAcquireShared(arg);
            }
            //自定义重写的tryAcquireShared方法，参数是unused，因为读锁的重入计数是内部维护的
            protected final int tryAcquireShared(int unused) {
                /*
                 * Walkthrough:
                 * 1. If write lock held by another thread, fail.
                 * 2. Otherwise, this thread is eligible for
                 *    lock wrt state, so ask if it should block
                 *    because of queue policy. If not, try
                 *    to grant by CASing state and updating count.
                 *    Note that step does not check for reentrant
                 *    acquires, which is postponed to full version
                 *    to avoid having to check hold count in
                 *    the more typical non-reentrant case.
                 * 3. If step 2 fails either because thread
                 *    apparently not eligible or CAS fails or count
                 *    saturated, chain to version with full retry loop.
                 */
                Thread current = Thread.currentThread();
                int c = getState();
                //exclusiveCount(c)取低16位写锁。存在写锁且当前线程不是获取写锁的线程，返回-1，获取读锁失败。
                if (exclusiveCount(c) != 0 &&
                    getExclusiveOwnerThread() != current)
                    return -1;
                int r = sharedCount(c);    //取高16位读锁，
                //readerShouldBlock（）用来判断当前线程是否应该被阻塞
                if (!readerShouldBlock() &&
                    r < MAX_COUNT &&    //MAX_COUNT为获取读锁的最大数量，为16位的最大值
                    compareAndSetState(c, c + SHARED_UNIT)) {
                    //firstReader是不会放到readHolds里的, 这样，在读锁只有一个的情况下，就避免了查找readHolds。
                    if (r == 0) {    // 是 firstReader，计数不会放入  readHolds。
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader == current) {    //firstReader重入
                        firstReaderHoldCount++;
                    } else {
                        // 非 firstReader 读锁重入计数更新
                        HoldCounter rh = cachedHoldCounter;    //读锁重入计数缓存，基于ThreadLocal实现
                        if (rh == null || rh.tid != current.getId())
                            cachedHoldCounter = rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                    }
                    return 1;
                }
                //第一次获取读锁失败，有两种情况：
                //1）没有写锁被占用时，尝试通过一次CAS去获取锁时，更新失败（说明有其他读锁在申请）
                //2）当前线程占有写锁，并且有其他写锁在当前线程的下一个节点等待获取写锁，除非当前线程的下一个节点被取消，否则fullTryAcquireShared也获取不到读锁
                return fullTryAcquireShared(current);
            }
            大致步骤：
                步骤1：通过同步状态低16位判断，如果存在写锁且当前线程不是获取写锁的线程，返回-1，获取读锁失败；否则执行步骤2）。
                步骤2：通过readerShouldBlock判断当前线程是否应该被阻塞，如果不应该阻塞则尝试CAS同步状态；否则执行3）
                步骤3：第一次获取读锁失败，通过fullTryAcquireShared再次尝试获取读锁。

# CountDownLatch
应用场景：
    一个线程等待另外的一些线程完成完成各自的工作后再继续执行其任务
CountDownLatch是一个同步工具类，用来协调多个线程之间的同步，或者说起到线程之间的通信
CountDownLatch能够是一个线程等待另外一些线程完成各自工作后，再继续执行。使用一个计数器进行实现。计数器初始值为线程数量。当每一个线程完成自己的任务后，计数器值就会减一。
当计数器值为0时，表示所有的线程都已经完成一些任务，然后在CountDownLatch上等待的线程就可以恢复执行接下来的任务
经典用法：
    1，某一线程在开始运行前等待n个线程执行完毕。将CountDownLatch的计数器初始化为new CountDownLatch(n)，每当一个任务线程执行完毕，就将计数器减1 
    countdownLatch.countDown()，当计数器的值变为0时，在CountDownLatch上await()的线程就会被唤醒。一个典型应用场景就是启动一个服务时，主线程需要等待多个组件加载完毕，之后再继续执行
    2，实现多个线程开始执行任务的最大并行性。注意是并行性，不是并发，强调的是多个线程在某一时刻同时开始执行。类似于赛跑，将多个线程放到起点，等待发令枪响，然后同时开跑。
    做法是初始化一个共享的CountDownLatch(1)，将其计算器初始化为1，多个线程在开始执行任务前首先countdownlatch.await()，当主线程调用countDown()时，计数器变为0，多个线程同时被唤醒。
基本原理：
   CountDownLatch内部通过AQS的state来完成计数器的功能，接下来通过源码来进行详细分析：
   1，new CountDownLatch(int count)
        用来创建一个AQS同步队列，并将计数器赋值给AQS的state
   2，countDown()
        内部使用了自旋+CAS操将计数器的值减1，当减为0时，方法返回true，将会调用doReleaseShared()方法
   3，await()
        调用countDownLatch.wait()的时候，会创建一个节点，加入到AQS阻塞队列（双向链表），并同时把当前线程挂起。

CountDownLatch把AQS的state为共享锁的标记位，state>0就是加锁，等于0就是释放锁。每调用一次countDown()，state减1。线程会阻塞在await()，直到countDown()将state置为0
CountDownLatch的await()方法不会只在一个线程中调用，多个线程可以同时等待await()方法返回，这些线程放在AQS队列中被刮起，锁在所有调用await()方法的线程间共享，所以叫做共享锁。
当countDown()将state为0的时候，唤醒自身节点对应线程的同时，继续唤醒AQS队列中的下一个节点的线程，每个节点在唤醒自身的同时还会唤醒下一个节点对应的线程，以实现共享状态的"向后传播"，从而实现共享功能。

        
# CyclicBarrier
应用场景：
    一个组线程需要等待所有线程完成任务后再继续执行下一次任务
构造方法：
   public CyclicBarrier(int parties)
   public CyclicBarrier(int parties, Runnable barrierAction)
   parties 是参与线程的个数
   第二个构造方法有一个 Runnable 参数，这个参数的意思是最后一个到达线程要做的任务
重要方法：
    public int await() throws InterruptedException, BrokenBarrierException
    public int await(long timeout, TimeUnit unit) throws InterruptedException, BrokenBarrierException, TimeoutException
    线程调用 await() 表示自己已经到达栅栏
    BrokenBarrierException 表示栅栏已经被破坏，破坏的原因可能是其中一个线程 await() 时被中断或者超时
CyclicBarrier 原理：
    CyclicBarrier 基于  ReentrantLock 和 Condition 来实现的。
    CyclicBarrier内部有一个计数器，每个线程在到达屏障点的时候都会调用await方法将自己加入condition的等待队列阻塞，此时计数器会减1
    当计数器减为0的时候，就唤醒等待队列中的所有线程，然后重置拦截的线程数量
    wait重要方法：
        可以看到在dowait方法中每次都将count减1，减完后立马进行判断看看是否等于0，如果等于0的话就会先去执行之前指定好的任务，执行完之后再调用nextGeneration方法将栅栏转到下一代
        在该方法中会将所有线程唤醒，将计数器的值重新设为parties，最后会重新设置栅栏代次，在执行完nextGeneration方法之后就意味着游戏进入下一局
        如果计数器此时还不等于0的话就进入for循环，根据参数来决定是调用trip.awaitNanos(nanos)还是trip.await()方法，这两方法对应着定时和非定时等待。如果在等待过程中当前线程被中断就会执行breakBarrier方法，该方法叫做打破栅栏，意味着游戏在中途被掐断
        设置generation的broken状态为true并唤醒所有线程。同时这也说明在等待过程中有一个线程被中断整盘游戏就结束，所有之前被阻塞的线程都会被唤醒。线程醒来后会执行下面三个判断，看看是否因为调用breakBarrier方法而被唤醒，如果是则抛出异常
        看看是否是正常的换代操作而被唤醒，如果是则返回计数器的值；看看是否因为超时而被唤醒，如果是的话就调用breakBarrier打破栅栏并抛出异常。这里还需要注意的是，如果其中有一个线程因为等待超时而退出，那么整盘游戏也会结束，其他线程都会被唤醒
代码：
    public class CyclicBarrierTest {  
        public static void main(String[] args) throws IOException, InterruptedException {  
            //如果将参数改为4，但是下面只加入了3个选手，这永远等待下去  
            //Waits until all parties have invoked await on this barrier.   
            CyclicBarrier barrier = new CyclicBarrier(3);  
            ExecutorService executor = Executors.newFixedThreadPool(3);  
            executor.submit(new Thread(new Runner(barrier, "1号选手")));  
            executor.submit(new Thread(new Runner(barrier, "2号选手")));  
            executor.submit(new Thread(new Runner(barrier, "3号选手")));  
            executor.shutdown();  
        }  
    }  
    class Runner implements Runnable {  
        // 一个同步辅助类，它允许一组线程互相等待，直到到达某个公共屏障点 (common barrier point)  
        private CyclicBarrier barrier;  
        private String name;  
        public Runner(CyclicBarrier barrier, String name) {  
            super();  
            this.barrier = barrier;  
            this.name = name;  
        }  
        @Override  
        public void run() {  
            try {  
                Thread.sleep(1000 * (new Random()).nextInt(8));  
                System.out.println(name + " 准备好了...");  
                // barrier的await方法，在所有参与者都已经在此 barrier 上调用 await 方法之前，将一直等待。  
                barrier.await();  
            } catch (InterruptedException e) {  
                e.printStackTrace();  
            } catch (BrokenBarrierException e) {  
                e.printStackTrace();  
            }  
            System.out.println(name + " 起跑！");  
        }  
    }
# CyclicBarrier 与 CountDownLatch 区别
1，CountDownLatch 是一次性的，CyclicBarrier 是可循环利用的
2，CountdownLatch适用于所有线程通过某一点后通知方法,而CyclicBarrier则适合让所有线程在同一点同时执行
3，对应的线程都完成工作之后再进行下一步动作，也就是大家都准备好之后再进行下一步
4，然而两者最大的区别是，进行下一步动作的动作实施者是不一样的。这里的“动作实施者”有两种，一种是主线程（即执行main函数），另一种是执行任务的其他线程，后面叫这种线程为“其他线程”，区分于主线程。对于CountDownLatch，当计数为0的时候，下一步的动作实施者是main函数；对于CyclicBarrier，下一步动作实施者是“其他线程”。
 CountDownLatch
 比如英雄联盟，主线程为控制游戏开始的线程。在所有的玩家都准备好之前，主线程是处于等待状态的，也就是游戏不能开始。当所有的玩家准备好之后，下一步的动作实施者为主线程，即开始游戏。
 对于CyclicBarrier，假设有一家公司要全体员工进行团建活动，活动内容为翻越三个障碍物，每一个人翻越障碍物所用的时间是不一样的。但是公司要求所有人在翻越当前障碍物之后再开始翻越下一个障碍物，也就是所有人翻越第一个障碍物之后，才开始翻越第二个，以此类推。类比地，每一个员工都是一个“其他线程”。当所有人都翻越的所有的障碍物之后，程序才结束。而主线程可能早就结束了，这里我们不用管主线程。
 CyclicBarrier
 我们使用代码来模拟上面的过程。我们设置了三个员工和三个障碍物。可以看到所有的员工翻越了第一个障碍物之后才开始翻越第二个的，下面是运行结果
 


代码示例：
    /**
     * 1，主线程等待子线程执行完成再执行
     */
    public class CountdownLatchTest1 {
        public static void main(String[] args) {
            ExecutorService service = Executors.newFixedThreadPool(3);
            final CountDownLatch latch = new CountDownLatch(3);
            for (int i = 0; i < 3; i++) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("子线程" + Thread.currentThread().getName() + "开始执行");
                            Thread.sleep((long) (Math.random() * 10000));
                            System.out.println("子线程"+Thread.currentThread().getName()+"执行完成");
                            latch.countDown();//当前线程调用此方法，则计数减一
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                service.execute(runnable);
            }
            try {
                System.out.println("主线程"+Thread.currentThread().getName()+"等待子线程执行完成...");
                latch.await();//阻塞当前线程，直到计数器的值为0
                System.out.println("主线程"+Thread.currentThread().getName()+"开始执行...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public class CountdownLatchTest2 {
        public static void main(String[] args) {
            ExecutorService service = Executors.newCachedThreadPool();
            final CountDownLatch cdOrder = new CountDownLatch(1);
            final CountDownLatch cdAnswer = new CountDownLatch(4);
            for (int i = 0; i < 4; i++) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("选手" + Thread.currentThread().getName() + "正在等待裁判发布口令");
                            cdOrder.await();
                            System.out.println("选手" + Thread.currentThread().getName() + "已接受裁判口令");
                            Thread.sleep((long) (Math.random() * 10000));
                            System.out.println("选手" + Thread.currentThread().getName() + "到达终点");
                            cdAnswer.countDown();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                service.execute(runnable);
            }
            try {
                Thread.sleep((long) (Math.random() * 10000));
                System.out.println("裁判"+Thread.currentThread().getName()+"即将发布口令");
                cdOrder.countDown();
                System.out.println("裁判"+Thread.currentThread().getName()+"已发送口令，正在等待所有选手到达终点");
                cdAnswer.await();
                System.out.println("所有选手都到达终点");
                System.out.println("裁判"+Thread.currentThread().getName()+"汇总成绩排名");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            service.shutdown();
        }
    } 

# Semaphore
用来控制同时访问特定资源的线程数量，它通过协调各个线程，保证合理的使用公共资源。线程可以通过acquire()方法来获取信号量的许可
当信号量中没有可用的许可的时候，线程阻塞，直到有可用的许可为止。线程可以通过release()方法释放它持有的信号量的许可
应用场景：
    可以用Semaphore实现限行的功能，比如限制任务执行一次并发只能有N个（定义一个具有N个permit的Semaphore）
主要方法：
    void acquire() throws InterruptedException	获取一个许可，允许中断
    void acquireUninterruptibly()	同acquire()获取一个许可，不同的是该方法不响应中断
    boolean tryAcquire()	尝试获取一个许可，立刻返回，成功返回true，失败返回false
    boolean tryAcquire(long timeout, TimeUnit unit)	同tryAcquire()，尝试获取一个许可，不同的是该方法支持超时机制
    void acquire(int permits) throws InterruptedException	acquire高级版，尝试一次获取多个许可
    void acquireUninterruptibly(int permits)	acquireUninterruptibly高级版，尝试一次获取多个许可
    void tryAcquire(int permits)	tryAcquire高级版
    boolean tryAcquire(int permits, long timeout, TimeUnit unit)	tryAcquire(long timeout, TimeUnit unit)高级版
    void release()	释放一个许可
    void release(int permits)	释放多个许可
代码示例：
    public class SemaphoreTest {
        public static void main(String[] args) {
            final Semaphore res = new Semaphore(2);
            Thread[] thds = new Thread[10];
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        res.acquire();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        return ;
                    }
                    try {
                        System.out.println("thread : " + Thread.currentThread().getName() + " got permit");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        res.release();
                    }
                }
            };
            for (int i = 0; i < thds.length; i++) {
                thds[i] = new Thread(r, "thd" + i);
                thds[i].start();
            }
            for (int i = 0; i < thds.length; i++) {
                try {
                    thds[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
原理：
    是基于共享锁实现的，和ReentrantLock一样，Semaphore内部也有一个Sync对象继承自AQS，Sync对象具有两个子类FairSync和NonFairSync，分别对应公平锁和非公平锁的实现。
    Semaphore使用state变量来保存目前可用的permits数。获取共享锁计算state变量减去消耗的permits数。如果结果小于0，说明permits不够用，线程阻塞等待，如果大于等于0，使用CAS操作更新state变量。
    当释放信号量的时候，线程会尝试将state+1，释放成功后，会唤醒同步队列中所有的共享节点线程（向后传播），被唤醒的节点会重新尝试state-1操作。
    线程在尝试获取信号量许可时，对于公平信号量而言，如果当前线程不在CLH队列的头部，则排队等候；而对于非公平信号量而言，无论当前线程是不是在CLH队列的头部，它都会直接获取信号量。
    
# CopyOnWriteArrayList
Vector作为线程安全版的ArrayList，存在感总是比较低。因为无论是add、remove还是get方法都加上了synchronized锁，所以效率低下。
JDK1.5引入的J.U.C包中，又实现了一个线程安全版的ArrayList——CopyOnWriteArrayList。
适用场景：
    读多写少的并发场景
原理：
    CopyOnWriteArrayList容器允许并发读，读操作是无锁的，性能较高。
    至于写操作，比如向容器中添加一个元素，则首先将当前容器复制一份，然后在新副本上执行写操作，结束之后再将原容器的引用指向新容器。
    将容器拷贝一份，写操作则作用在新的副本上，这个过程需要加锁。此过程如果有读会作用在原容器上
    将原容器引向新副本，使用volatile保证切换过程对读过程立即可见
优点：
　　 读操作性能很高，因为无需任何同步措施，比较适用于读多写少的并发场景。Java的list在遍历时，若中途有别的线程对list容器进行修改，则会抛出ConcurrentModificationException异常。
    而CopyOnWriteArrayList由于其"读写分离"的思想，遍历和修改操作分别作用在不同的list容器，所以在使用迭代器进行遍历时候，也就不会抛出ConcurrentModificationException异常了
缺点：
    一是内存占用问题，毕竟每次执行写操作都要将原容器拷贝一份，数据量大时，对内存压力较大，可能会引起频繁GC；
    二是无法保证实时性，Vector对于读写操作均加锁同步，可以保证读和写的强一致性。而CopyOnWriteArrayList由于其实现策略的原因，写和读分别作用在新老不同容器上，在写操作执行过程中，读不会阻塞但读取到的却是老容器的数据。

# 并发队列
阻塞队列和非阻塞队列对比：
LinkedBlockingQueue 多用于任务队列（单线程发布任务，任务满了就停止等待阻塞，当任务被完成消费少了又开始负载 发布任务）
ConcurrentLinkedQueue  多用于消息队列（多个线程发送消息，不计并发的-cas特点）
多个生产者，对于LinkedBlockingQueue性能还算可以接受；但是多个消费者就不行了mainLoop需要一个timeout的机制，否则空转，cpu会飙升的。LinkedBlockingQueue正好提供了timeout的接口，更方便使用
如果ConcurrentLinkedQueue，那么我需要收到处理sleep
单生产者，单消费者  用 LinkedBlockingqueue
多生产者，单消费者   用 LinkedBlockingqueue
单生产者 ，多消费者   用 ConcurrentLinkedQueue
多生产者 ，多消费者   用 ConcurrentLinkedQueue
## ConcurrentLinkedQueue
是一个高性能线程安全的非阻塞队列，底层数据结构使用单项链表实现，对于入队和出队操作使用cas来实现线程安全
原理：
    首先有两个节点，头节点head和尾节点tail，然后每个节点由节点元素item和指向下一节点的指针组成，默认情况下head节点存储的元素为null，tail节点等于head节点
    入队的时候：
        入队的时候主要做三件事：第一是定位出尾节点，第二是通过cas指令将入队节点更新为尾节点的next节点，如果不成功就重试，第三就是重新定位tail节点
        tail节点并不总是尾节点，更新tail节点的时候，如果tail节点的next节点为空，那么就将入队节点设置为tail节点的next节点
        如果如果tail节点的next节点不为空，那么就将入队节点设置为tail节点。
        这样做的主要目的是，减少cas更新tail节点的次数，提高入队效率。
        虽然增加了查询尾节点的次数，但是读操作的开销远远小于更新tail节点写操作的开销。
        设置入队节点为尾节点：
            p.casNext(null, n)方法用于将入队节点设置为当前队列尾节点的next节点，p如果是null表示p是当前队列的尾节点，如果不为null表示有其他线程更新了尾节点，则需要重新获取当前队列的尾节点。
    出队的时候：
        并不是每次出队都会更新head节点，当head节点有元素的时候，直接弹出head节点，而不更新head节点
        只有head节点没有元素的时候，出队才会更新head节点。同理也是为了减少cas更新head节点的次数，提高出队效率
        详细步骤：
            1，首先获取头节点的元素，然后判断头节点元素是否为空，如果为空，表示另外一个线程已经进行了一次出队操作将该节点的元素取走；
            2，如果不为空，则使用CAS的方式将头节点的引用设置成null，如果CAS成功，则直接返回头节点的元素，如果不成功；
            3，表示另外一个线程已经进行了一次出队操作更新了head节点，导致元素发生了变化，需要重新获取头节点。
    hops（tail节点不总是尾节点）的设计意图：
        让tail节点永远作为队列的尾节点，这样实现代码量非常少。但是这么做有个缺点就是每次都需要使用循环CAS更新tail节点；
        如果能减少CAS更新tail节点的次数，就能提高入队的效率，所以doug lea使用hops变量来控制并减少tail节点的更新频率；
        并不是每次节点入队后都将 tail节点更新成尾节点，而是当 tail节点和尾节点的距离大于等于常量HOPS的值（默认等于1）时才更新tail节点；
        tail和尾节点的距离越长使用CAS更新tail节点的次数就会越少，但是距离越长带来的负面效果就是每次入队时定位尾节点的时间就越长，因为循环体需要多循环一次来定位出尾节点；
        但是这样仍然能提高入队的效率，因为从本质上来看它通过增加对volatile变量的读操作来减少了对volatile变量的写操作，而对volatile变量的写操作开销要远远大于读操作，所以入队效率会有所提升。
        private static final int HOPS = 1;
        主要目的是为了减少频繁更新tail节点的次数，虽然增加了查询尾节点的次数，但是读操作的开销远远小于更新tail节点写操作的开销
非阻塞却线程安全的原因？
    它的入队和出队操作都是通过cas操作实现的，如果进行入队或出队的时候如果其他线程更改了tail或者head节点，那么就会进行失败重试，直到成功为止
    在这个过程中并不会出现类似多个线程抢夺插入同一节点的情况。
## LinkedBlockingQueue
是一个基于链表的阻塞队列，如果不指定容量就是无界队列。为了避免内存爆满，最好手动穿一个队列的大小。
主要用于平衡负载，减少两边处理速度的差距。
它的内部由两个ReentrantLock分别保证入队和出队的线程安全，
然后由各自的条件队列（notEmpty、notFull）结合Condition对象实现线程的等待唤醒。
在判断队列是否为空的时候最好不要使用size()方法，因为他会遍历整个队列，而是使用isEmtpy。
每次存储/取出数据都会有Node对象的新建和移除，所以也存在由于GC影响性能的可能
## ArrayBlockingQueue
是一个基于数组的阻塞队列，它是一个有界队列，在创建的时候必须指定其大小。
它的内部只有一个ReentrantLock锁，线程的入队、出队都需要获取该锁。
另外有两个条件队列notEmpty、notFull结合condition线程的等待唤醒。
因为只有一把全局独占锁，所以ArrayBlockingQueue基本没有什么并发性能。
## PriorityBlockingQueue
是一个无界带优先级的阻塞队列，它每次出队的都是最高优先级的元素
与普通队列不同，它里面添加的元素必须实现了Comparator或者Comparable，这些元素必须是排好序的。
因为是无界阻塞队列所以可以一直向队列中插入元素，除非资源耗尽。可以说是一个半阻塞队列。
它使用ReentrantLock和Condition来确保多线程环境下的同步问题。多线程环境下，保证只有一个线程能进行入队或出队操作。
内部通过使用一个二叉树最小堆算法来维护内部数组，这个数组是可扩容的，
扩容的时候会先释放锁，然后通过cas操作保证只有一个线程可以扩容成功。
## DelayQueue
是一个无界阻塞队列，队列中每个元素都有一个过期时间，只有过期元素才会出队。
队列中的元素必须实现Delayed接口，Delayed接口又实现了Comparable，所以我们要同时实现compareTo方法和getDelay方法。
它使用ReentrantLock实现线程同步。
他的应用场景比如30分钟没有付款，自动取消订单。以及缓存中的应用，超出了空闲时间，就从缓存中移除。
它的内部是通过一个优先队列来存放元素，当线程put元素的时候，DelayQueue会通过其本身的compareTo方法进行排序，延时时间越短的顺序越靠近队列头部
当取出元素的时候，检测当前是否有线程已经在等待队头元素了，如果有那么只能阻塞当前线程等待，
如果没有，会查询队头元素还有多少delay时间，若果小于0，则直接取出队头元素，
如果大于0，会并将当前线程设置为队头等待线程，并让当前线程wait剩余的delay时间后，再来获取队头元素
## SynchronousQueue
是一个没有容量的阻塞队列，生产者和消费者相互等待对方，然后一起离开
数据在生产者和消费者之间直接传递。它的内部没有使用AQS而是直接使用cas。
使用场景主要是任务转接：
1，在缓存线程池中有使用到，线程池直接向SynchronousQueue提交任务，如果有空闲线程，就直接取出任务执行
如果没有空闲线程，就新建一个。执行完任务的线程有60秒的生存时间。超时销毁
2，会话转交，通常坐席需要进行会话转交，如果有坐席在线那么会为我们分配一个客服，但是如果没有，那么阻塞请求线程，
一段时间后会超时或者提示当前坐席已满。
有人说1个容量大小的BlockingQueue也能实现该操作
    它们之间有着本质的不同：
    SynchronousQueue在put时，如果另一个线程没有执行take操作，put线程会一直阻塞；而BlockingQueue在put一个元素时，第一次是不会阻塞的，只有第二次因为容量满了put操作才阻塞，而SynchronousQueue在第一次就阻塞；
https://www.zhenchao.io/2018/09/18/java/juc-synchronous-queue/

特性：
    1，是一个没有数据缓冲的BlockingQueue，生产者线程对其插入操作put，必须等待消费者移除操作take
    2，你不能调用peek()方法来看队列中是否有数据元素，因为数据元素只有当你试着取走的时候才可能存在，不取走而只想偷窥一下是不行的
    3，当然遍历这个队列的操作也是不允许的。队列头元素是第一个排队要插入数据的线程，而不是要交换的数据。
    4，数据是在配对的生产者和消费者线程之间直接传递的，并不会将数据缓冲数据到队列中。
    5，生产者和消费者互相等待对方，握手，然后一起离开。
    6，SynchronousQueue直接使用CAS实现线程的安全访问
    7，SynchronousQueue由于其独有的线程一一配对通信机制
    8，内部没有使用AQS，而是直接使用CAS
原理：
    SynchronousQueue是一个内部只能包含一个元素的队列。插入元素到队列的线程被阻塞，直到另一个线程从队列中获取了队列中存储的元素。同样，如果线程尝试获取元素并且当前不存在任何元素，则该线程将被阻塞，直到线程将元素插入队列。
    将这个类称为队列有点夸大其词。这更像是一个点。
    它内部没有容器，一个生产线程，当它生产产品（即put的时候），如果当前没有人想要消费产品(即当前没有线程执行take)，此生产线程必须阻塞，等待一个消费线程调用take操作，take操作将会唤醒该生产线程，同时消费线程会获取生产线程的产品（即数据传递），这样的一个过程称为一次配对过程(当然也可以先take后put,原理是一样的)
    公平模式下的模型：
        1，公平模式下，底层实现使用的是TransferQueue这个内部队列，它有一个head和tail指针，用于指向当前正在等待匹配的线程节点。
        2，线程put1执行 put(1)操作，由于当前没有配对的消费线程，所以put1线程入队列，自旋一小会后睡眠等待，这时队列状态如下：
        3，接着，线程put2执行了put(2)操作，跟前面一样，put2线程入队列，自旋一小会后睡眠等待，这时队列状态如下：
        4，这时候，来了一个线程take1，执行了
        take操作，由于tail指向put2线程，put2线程跟take1线程配对了(一put一take)，这时take1线程不需要入队，但是请注意了，这时候，要唤醒的线程并不是put2，而是put1。为何？
        大家应该知道我们现在讲的是公平策略，所谓公平就是谁先入队了，谁就优先被唤醒，我们的例子明显是put1应该优先被唤醒。
        至于读者可能会有一个疑问，明明是take1线程跟put2线程匹配上了，结果是put1线程被唤醒消费，怎么确保take1线程一定可以和次首节点(head.next)也是匹配的呢？其实大家可以拿个纸画一画，就会发现真的就是这样的。
        公平策略总结下来就是：队尾匹配队头出队。
        执行后put1线程被唤醒，take1线程的 take()方法返回了1(put1线程的数据)，这样就实现了线程间的一对一通信
        5，最后，再来一个线程take2，执行take操作，这时候只有put2线程在等候，而且两个线程匹配上了，线程put2被唤醒
    非公平模式下的模型：
        非公平模式底层的实现使用的是TransferStack
        1，线程put1执行 put(1)操作，由于当前没有配对的消费线程，所以put1线程入栈，自旋一小会后睡眠等待，这时栈状态如下
        2，接着，线程put2再次执行了put(2)操作，跟前面一样，put2线程入栈，自旋一小会后睡眠等待，这时栈状态如下
        3，这时候，来了一个线程take1，执行了take操作
        这时候发现栈顶为put2线程，匹配成功，但是实现会先把take1线程入栈，然后take1线程循环执行匹配put2线程逻辑，一旦发现没有并发冲突，就会把栈顶指针直接指向 put1线程
        4，最后，再来一个线程take2，执行take操作，这跟步骤3的逻辑基本是一致的，take2线程入栈，然后在循环中匹配put1线程，最终全部匹配完毕，栈变为空，恢复初始状态

5，DelayQueue
一个无界阻塞的队列，队列中的每个元素都有一个过期时间，当要从队列中取出数据时，只有过期元素才会出队
原理：
    1，DelayQueue是通过Delayed，使得不同元素之间能按照剩余的延迟时间进行排序
    2，然后通过PriorityQueue，使得超时的元素能最先被处理
    3，然后利用BlockingQueue，将元素处理的操作阻塞住。
    DelayQueue内部使用PriorityQueue存放元素，又用ReentrantLock实现线程同步
    因为DelayQueue内部要获取元素的剩余时间，所以我们的数据类需要继承Delayed接口，Delayed又继承Comparable接口，实现排序
    自身只有一个getDelay()方法，用来获取元素的剩余时间，如果getDelay()返回<=0的值，则表示这个元素过期，通过take()方法即可取出他，如果没有过期值，则take()会一直阻塞
    主要方法：
        take()：
            获取并移除队列中过期元素，没有则等待。
            首先也是获取独占锁，然后使用peek获取一个元素(peek不移除元素)。
            如果first为null，则把当前线程放入available中阻塞等待(available是Condition)。
            如果first不为null，则调用这个元素的getDelay获取还有多长时间要过期(参数是NANOSECONDS，纳秒)，如果<=0，则表示已经过期，直接出队返回
            否则查看leader是否为null，不为null则说明其他线程也在执行take()，则把线程放入条件队列
            如果leader为null，则让leader为当前线程，然后执行等待，剩余过期时间到达后，然后重置leader线程为null，重新进入循环
            重新进入后就可以发现队列中的头部元素已经过期，然后返回他
            如果判断结果为true，则说明当前线程从队列移除过期元素后，又有其他线程执行了入队，就会调用条件变量的signal方法，激活条件队列里面的等待线程。
        poll()：
            获取并移除队头过去元素，如果没有过期元素则返回null。
            较简单，只有一个if判断队列是否为空，不为空的话如果队头元素没有过期则返回null。
        size()：
            返回队列元素个数

ConcurrentLinkedQueue
    特性：
        1，先进先出
        1，无阻塞、无锁、高性能、无界、线程安全
            无阻塞指的是队列为空时，不会等到队列有内容才返回，直接返回null，程序继续向下运行
        2，性能优于 BlockingQueue
        3，不允许插入null值，如果插入null值，抛出空指针异常
        5，添加元素会添加到队列的尾部,获取元素会返回队列头部元素.
        6，无界非阻塞队列，底层数据结构使用单向链表实现，对于入队和出队操作使用CAS来实现线程安全。
        7，由于使用非阻塞CAS算法，没有加锁，所以在计算size时有可能进行了offer、poll等操作，导致计算的元素个数不精确，所以在并发情况下size函数不是很有用。
    主要方法：
        offer(E e) ：将指定元素插入此队列的尾部
        poll() ：获取并移除此队列的头，如果此队列为空，则返回 null
        contains(Object o) ：如果此队列包含指定元素，则返回 true
        isEmpty() ：如果此队列不包含任何元素，则返回 true
        iterator() ：返回在此队列元素上以恰当顺序进行迭代的迭代器。
        offer(E e) ：将指定元素插入此队列的尾部。
        peek() ：获取但不移除此队列的头；如果此队列为空，则返回 null。
        poll() ：获取并移除此队列的头，如果此队列为空，则返回 null。
        remove(Object o) ：从队列中移除指定元素的单个实例（如果存在）。
        size() ：返回此队列中的元素数量。
    结构：
        1，有两个结点:头节点(head)和尾节点(tail).
        2，每个节点由结点元素(item)和指向下一个节点的指针(next)组成.
        3，默认情况下head节点存储的元素为空,tail节点等于head节点.
    入队原理：
        1，定位尾节点。
            tail节点并不总是尾节点，所以每次入队都必须先通过tail节点来找到尾节点，尾节点可能就是tail节点，也可能是tail节点的next节点
        2，更新tail节点
            如果tail节点的next节点为空，则将入队节点设置成tail的next节点；
            如果tail节点的next节点不为空，则将入队节点设置成tail节点；
            所以tail节点不总是尾节点，理解这一点对于我们研究源码会非常有帮助
        3，设置入队节点为尾节点
            p.casNext(null, n)方法用于将入队节点设置为当前队列尾节点的next节点，p如果是null表示p是当前队列的尾节点，如果不为null表示有其他线程更新了尾节点，则需要重新获取当前队列的尾节点。
    出队原理：
        1，并不是每次出队时都更新head节点，当head节点里有元素时，直接弹出head节点里的元素，而不会更新head节点；
        2，只有当head节点里没有元素时，出队操作才会更新head节点。这种做法也是通过hops变量来减少使用CAS更新head节点的消耗，从而提高出队效率
        详细步骤：
            1，首先获取头节点的元素，然后判断头节点元素是否为空，如果为空，表示另外一个线程已经进行了一次出队操作将该节点的元素取走；
            2，如果不为空，则使用CAS的方式将头节点的引用设置成null，如果CAS成功，则直接返回头节点的元素，如果不成功；
            3，表示另外一个线程已经进行了一次出队操作更新了head节点，导致元素发生了变化，需要重新获取头节点。
    
    
阻塞队列：
    1，应用场景：
        1，队列会自动平衡负载，即那边（生产与消费两边）处理快了就会被阻塞掉，从而减少两边的处理速度差距
        2，在多线程领域：所谓的阻塞，在某些情况下会挂起线程（即阻塞），一旦条件满足，被挂起的线程又会自动唤醒，不需要额外的同步机制
    2，LinkedBlockingQueue:
        LinkedBlockingQueue是一个基于链表的阻塞队列，其内部维持一个基于链表的数据队列。遵循先进先出。
        如果不指定容量，默认为Integer.MAX_VALUE，也就是无界队列。所以为了避免队列过大造成机器负载或者内存爆满的情况出现，我们在使用的时候建议手动传一个队列的大小。
        基本方法：
            添加数据
                take()：首选。当队列为空时阻塞
                poll()：弹出队顶元素，队列为空时，返回空
                peek()：和poll烈性，返回队队顶元素，但顶元素不弹出。队列为空时返回null
                remove(Object o)：移除某个元素，队列为空时抛出异常。成功移除返回true
            取数据
                put()：首选。队满是阻塞
                offer()：队满时返回false
            判断队列是否为空
                size()方法会遍历整个队列，时间复杂度为O(n),所以最好选用isEmtpy
        原理：
            内部分别使用了takeLock 和 putLock 对并发进行控制，也就是说，添加和删除操作并不是互斥操作，可以同时进行
            维持两把锁和两个条件，同一时刻可以有两个线程在队列的两端操作，但同一时刻只能有一个线程在一端操作
            线程安全的集合，通过ReentrantLock锁来实现的，在并发情况下可以保持数据的一致性(只有一把锁)
            内部由两个ReentrantLock来实现出入队列的线程安全，由各自的Condition对象的await和signal来实现等待和唤醒功能
            容量有限，大小初始化固定，不会随着队列元素增加而出现扩容现象，也就是说他是一个’有界缓存区’
        它和ArrayBlockingQueue的不同点在于：
            1，队列大小有所不同，ArrayBlockingQueue是有界的初始化必须指定大小，而LinkedBlockingQueue可以是有界的也可以是无界的(Integer.MAX_VALUE)，对于后者而言，当添加速度大于移除速度时，在无界的情况下，可能会造成内存溢出等问题。
            2，数据存储容器不同，ArrayBlockingQueue采用的是数组作为数据存储容器，而LinkedBlockingQueue采用的则是以Node节点作为连接对象的链表。
            3，由于ArrayBlockingQueue采用的是数组的存储容器，因此在插入或删除元素时不会产生或销毁任何额外的对象实例，而LinkedBlockingQueue则会生成一个额外的Node对象。这可能在长时间内需要高效并发地处理大批量数据的时，对于GC可能存在较大影响。
            4，两者的实现队列添加或移除的锁不一样，ArrayBlockingQueue实现的队列中的锁是没有分离的，即添加操作和移除操作采用的同一个ReenterLock锁，而LinkedBlockingQueue实现的队列中的锁是分离的，其添加采用的是putLock，移除采用的则是takeLock，这样能大大提高队列的吞吐量，也意味着在高并发的情况下生产者和消费者可以并行地操作队列中的数据，以此来提高整个队列的并发性能。
    3，ArrayBlockingQueue：
        概述：
            线程安全
            先进先出队列
            是一个支持并发操作的有界阻塞队列。此队列按照先进先出（FIFO）的原则对元素进行排序。底层以数组的形式保存数据(在逻辑上可看作一个循环数组，即队列头部和尾部的位置不是固定的，但是头部和尾部之间在逻辑上是连续的元素)
            ArrayBlockingQueue创建时必须传入容量，且一旦创建，容量不能改变。如果向已满的队列继续塞入元素，将导致当前的线程阻塞。如果向空队列获取元素，那么也将导致当前线程阻塞。
            ArrayBlockingQueue内部是采用一个items数组进行数据存储的。使用ReentrantLock作为锁，使用notEmpty、notFull两个Condition来分别实现消费者、生产者的等待唤醒，
            当获消费者线程被阻塞时会将该线程放置到notEmpty条件队列中，当插入数据的生产者线程被阻塞时，会将该线程放置到notFull条件队列中。
            使用count来保存元素的数量，使用takeIndex来保存将取元素的下标索引，代表逻辑头部，使用putIndex将存元素的下标索引，代表队列逻辑尾部，这里的“逻辑”表示队列元素在数组中“逻辑上是连续的”，就队列头尾没有空余的空间，但是物理上可能不连续，比如起始索引在数组尾部，结束索引在数组头部，这里的数组相当于一个逻辑环形数组，有利于空间的利用！
            ArrayBlockingQueue是一个底层使用逻辑环形数组实现的阻塞队列，其容量大小不能更改。它的属性均没有volatile修饰，内部只有一把全局独占锁lock，线程的入队、出队都需要获取该锁。另外具有两个条件队列notEmpty、notFull分别消费线程和生产线程在不满足条件的情况下挂起和唤醒（等待-通知）。
            相比于其他的并发容器，它的size 操作的结果也是精确的，因为同样加了全局锁lock。但是它的缺点很明显，那就是由于只有一个全局独占锁，相当于给整个ArrayBlockingQueue对象都加了一把大锁，锁粒度太大，因此它的基本没什么并发性能了。
            ArrayBlockingQueue的源码非常简单，主要是因为对于线程的获取锁、释放锁、等待、唤醒等基本操作的源码都交给ReentrantLock、AQS和Concition来实现了，比如最开始说的公平和非公平模式，就是通过ReentrantLock的不同模式来实现的
        原理：
            底层基于定长的数组，容量有限制。
            只有一把锁和两个条件，同一时刻只能有一个线程在队列的一端操作
            是一个使用了链表完成队列操作的阻塞队列，是个单向链表。
            采用了两锁队列算法，读只操作对头，而写只操作队尾
            对于put和offer采用了putLock，对于take和poll采用了takeLock 即为写锁和读锁
            对于生产者和消费者分别采用了独立的锁来控制数据同步，避免了读写时相互竞争锁的问题。
            数据存储容器不同，ArrayBlockingQueue采用的是数组作为数据存储容器，而LinkedBlockingQueue采用的则是以Node节点作为连接对象的链表。
        主要方法：
            非阻塞队列中的方法：
                抛出异常的方法 Exception in thread "main" java.lang.IllegalStateException: Queue full
                1.add(e) throw exception：将元素e插入到队列的末尾，如果插入成功，则返回true，如果插入失败 (队列已经满) 抛出异常
                2.remove(e) throw exception：移除队首元素，若移除成功，则返回true；若移除失败（队列为空）则抛出异常
                3.element() throw exception：获取队列首元素，若获取成功，则返回首元素，否则抛出异常 java.util.NoSuchElementException
            返回特定值的方法
                1.offer(E e)：将元素e插入到队列末尾，如果插入成功，则返回true，如果插入失败（队列已满）,返回false
                2.poll(E e)：移除并获取队首元素，若成功，则返回队首元素，否则返回null
                3.peek(E e)：获取队首元素，若成功，则返回队首元素，否则则返回null
            可以指定TimeOut
                3.offer(E e,long timeout, TimeUnit unit)：向队列尾部存入元素e，如果队列满，则等待一定的时间，当达到timeout时候，则返回false，否则返回true
                4.poll(long timeout, TimeUnit unit)：从队首获取元素，如果队列为空，则等待一定时间，当达到timeout时，如果没有取到，则返回null，如果取到则返回取到的元素
            阻塞队列中的几个重要方法：
                1.put(E e) :用于队列尾部存入元素e，如果对满，则等待。
                2.take()：用于从对列首取出元素e，如果队列为空，则等待
            注意：阻塞队列包括了非阻塞队列中的大部分方法，上面列举的5个方法在阻塞队列中都存在，但是要注意这5个方法在阻塞队列中都进行了同步措施，都是线程安全的。
    4，PriorityBlockingQueue
        是一个无界带优先级的阻塞队列，内部使用堆算法保证每次出队都是优先级最高的元素
        1，与标准队列不同，不能增加任意类型元素，元素必须满足两个条件：
            实现了Comparator 或Comparable的元素，PriorityBlockingQueue中的元素是排好序的。
            实现比较器的目的是让最高优先级的元素排在第一位，因此当从队列中删除元素时，总是删除最高优先级的元素
        2，使用ReentrantLock和Condition来确保多线程环境下的同步问题。
            前面介绍的ArrayBlockingQueue和LinkedBlockingQueue都是具有notEmpty和notFull两个Condition，那PriorityBlockingQueue为什么只有一个notEmpty呢？
            因为PriorityBlockingQueue是一个无界阻塞队列，可以一直向队列中插入元素，除非系统资源耗尽，所以该队列也就不需要notFull了。
        3，PriorityBlockingQueue内部有一个数组queue.
        4，PriorityBlockingQueue内部是一个数组,但是其实数据结构是使用数组实现的一个最小堆.压入队列时需要计算最小堆,弹出队列时需要重新调整根节点.
        5，带优先级的队列(带排序功能).
        6，无界队列,默认队列大小为11,当队列满了之后会自动扩容(和ArrayList类似的扩容数组)
        7，take队列为空时阻塞.但是PriorityBlockingQueue队列是无界的,put方法不存在队列满的时候阻塞的情况,所以put方法是不阻塞的.可以说PriorityBlockingQueue是一个半阻塞的队列.
        8，和ArrayBlockingQueue类似,是独占锁来控制的, 就是说多线程访问时只能有一个线程可以进行入队或出队操作.
        8，PriorityBlockingQueue其实是一个无界阻塞队列.但只是读取时为阻塞的,在写入时是无阻塞的.
        9，使用独占锁来保证读取和写入的多线程安全性,读写同时只能有一个线程操作
        10，但是在扩容时使用CAS锁来进行扩容提高读写性能. 
        11，使用最小堆算法来保证队列的有序性,从而达到优先级队列.
    5，DelayQueue
        一个无界阻塞的队列，队列中的每个元素都有一个过期时间，当要从队列中取出数据时，只有过期元素才会出队
        原理：
            1，DelayQueue是通过Delayed，使得不同元素之间能按照剩余的延迟时间进行排序
            2，然后通过PriorityQueue，使得超时的元素能最先被处理
            3，然后利用BlockingQueue，将元素处理的操作阻塞住。
            DelayQueue内部使用PriorityQueue存放元素，又用ReentrantLock实现线程同步
            因为DelayQueue内部要获取元素的剩余时间，所以我们的数据类需要继承Delayed接口，Delayed又继承Comparable接口，实现排序
            自身只有一个getDelay()方法，用来获取元素的剩余时间，如果getDelay()返回<=0的值，则表示这个元素过期，通过take()方法即可取出他，如果没有过期值，则take()会一直阻塞
        主要方法：
            take()：
                获取并移除队列中过期元素，没有则等待。
                首先也是获取独占锁，然后使用peek获取一个元素(peek不移除元素)。
                如果first为null，则把当前线程放入available中阻塞等待(available是Condition)。
                如果first不为null，则调用这个元素的getDelay获取还有多长时间要过期(参数是NANOSECONDS，纳秒)，如果<=0，则表示已经过期，直接出队返回
                否则查看leader是否为null，不为null则说明其他线程也在执行take()，则把线程放入条件队列
                如果leader为null，则让leader为当前线程，然后执行等待，剩余过期时间到达后，然后重置leader线程为null，重新进入循环
                重新进入后就可以发现队列中的头部元素已经过期，然后返回他
                如果判断结果为true，则说明当前线程从队列移除过期元素后，又有其他线程执行了入队，就会调用条件变量的signal方法，激活条件队列里面的等待线程。
            poll()：
                获取并移除队头过去元素，如果没有过期元素则返回null。
                较简单，只有一个if判断队列是否为空，不为空的话如果队头元素没有过期则返回null。
            size()：
                返回队列元素个数
    6，SynchronousQueue
        特性：
            1，是一个没有数据缓冲的BlockingQueue，生产者线程对其插入操作put，必须等待消费者移除操作take
            2，你不能调用peek()方法来看队列中是否有数据元素，因为数据元素只有当你试着取走的时候才可能存在，不取走而只想偷窥一下是不行的
            3，当然遍历这个队列的操作也是不允许的。队列头元素是第一个排队要插入数据的线程，而不是要交换的数据。
            4，数据是在配对的生产者和消费者线程之间直接传递的，并不会将数据缓冲数据到队列中。
            5，生产者和消费者互相等待对方，握手，然后一起离开。
            6，SynchronousQueue直接使用CAS实现线程的安全访问
            7，SynchronousQueue由于其独有的线程一一配对通信机制
            8，内部没有使用AQS，而是直接使用CAS
        原理：
            SynchronousQueue是一个内部只能包含一个元素的队列。插入元素到队列的线程被阻塞，直到另一个线程从队列中获取了队列中存储的元素。同样，如果线程尝试获取元素并且当前不存在任何元素，则该线程将被阻塞，直到线程将元素插入队列。
            将这个类称为队列有点夸大其词。这更像是一个点。
            它内部没有容器，一个生产线程，当它生产产品（即put的时候），如果当前没有人想要消费产品(即当前没有线程执行take)，此生产线程必须阻塞，等待一个消费线程调用take操作，take操作将会唤醒该生产线程，同时消费线程会获取生产线程的产品（即数据传递），这样的一个过程称为一次配对过程(当然也可以先take后put,原理是一样的)
            公平模式下的模型：
                1，公平模式下，底层实现使用的是TransferQueue这个内部队列，它有一个head和tail指针，用于指向当前正在等待匹配的线程节点。
                2，线程put1执行 put(1)操作，由于当前没有配对的消费线程，所以put1线程入队列，自旋一小会后睡眠等待，这时队列状态如下：
                3，接着，线程put2执行了put(2)操作，跟前面一样，put2线程入队列，自旋一小会后睡眠等待，这时队列状态如下：
                4，这时候，来了一个线程take1，执行了
                    take操作，由于tail指向put2线程，put2线程跟take1线程配对了(一put一take)，这时take1线程不需要入队，但是请注意了，这时候，要唤醒的线程并不是put2，而是put1。为何？
                    大家应该知道我们现在讲的是公平策略，所谓公平就是谁先入队了，谁就优先被唤醒，我们的例子明显是put1应该优先被唤醒。
                    至于读者可能会有一个疑问，明明是take1线程跟put2线程匹配上了，结果是put1线程被唤醒消费，怎么确保take1线程一定可以和次首节点(head.next)也是匹配的呢？其实大家可以拿个纸画一画，就会发现真的就是这样的。
                    公平策略总结下来就是：队尾匹配队头出队。
                    执行后put1线程被唤醒，take1线程的 take()方法返回了1(put1线程的数据)，这样就实现了线程间的一对一通信
                5，最后，再来一个线程take2，执行take操作，这时候只有put2线程在等候，而且两个线程匹配上了，线程put2被唤醒
            非公平模式下的模型：
                非公平模式底层的实现使用的是TransferStack
                1，线程put1执行 put(1)操作，由于当前没有配对的消费线程，所以put1线程入栈，自旋一小会后睡眠等待，这时栈状态如下
                2，接着，线程put2再次执行了put(2)操作，跟前面一样，put2线程入栈，自旋一小会后睡眠等待，这时栈状态如下
                3，这时候，来了一个线程take1，执行了take操作
                    这时候发现栈顶为put2线程，匹配成功，但是实现会先把take1线程入栈，然后take1线程循环执行匹配put2线程逻辑，一旦发现没有并发冲突，就会把栈顶指针直接指向 put1线程
                4，最后，再来一个线程take2，执行take操作，这跟步骤3的逻辑基本是一致的，take2线程入栈，然后在循环中匹配put1线程，最终全部匹配完毕，栈变为空，恢复初始状态



# 并发Map
为什么HashMap线程不安全：
    在jdk1.7中，并发扩容的时候，会将原数组中的内容扩容到新数组中，在扩容的时候数组中的链表采用头插法，可能会造成链表闭环，从而造成死循环。也有可能造成数据丢失的问题。
        闭环详细解释：如果有两个线程同时进行扩容，一个线程扩容到一半，线程刮起。另一个线程继续扩容，把原来的链表采用头插法倒过来放入到链表中。旧链表与新链表的顺序是反的，就会造成链表的闭环。
    在jdk1.8中，采用尾插法避免了造成闭环的问题，但是并发扩容的时候会有数据覆盖的问题。
        数据覆盖详细解释：如果两个线程put的hash值相同，线程A获取了插入位置X，但是还未插入。此时线程B也计算出了插入位置X，无论线程A或者B谁先插入，都会造成一个数据丢失
## ConcurrentHashMap
ConcurrentHashMap是一个线程安全的Map，是HashMap的升级版，相对于HashTable有更高的并发性能。

在JDK1.7中，ConcurrentHashMap把哈希桶切割成数组Segment，每个Segment有n个HashEntry，HashEntry里面包含的是一个个链表，将数据分段存储，然后给每一段数据加一把锁。
多线程访问容器里的不同数据段的数据就不会存在锁竞争，提高并发效率。
Segment继承了ReentrantLock，是一种可重入锁。
然后用volatile修饰HashEntry数据的value和下一个节点next，保证多线程环境下获取数据的可见性


在JDK1.8中，数据结构上选择了与HashMap相同的Node数组+链表+红黑树的结构
在锁的设计上，摒弃了分段锁的概念，使用了 CAS + Synchronized 来实现更细粒度的锁
将锁的级别控制在了数组元素级别，直接使用链表的头节点（或者说红黑树的跟节点）作为锁，使得同一时间只能有一个线程对某一链表或红黑树执行put操作
但是并不影响其他哈希桶数组元素的读写，大大提高了并发。
同时Node节点中的value和Next通过volatile修饰，保证取数据时的可见性

1，put方法逻辑
    计算key的哈希值
    通过hash定位索引坐标，判断是否有node节点，如果没有则使用cas进行添加
    如果检查到正在扩容，就帮助它一块扩容
    如果node节点不为空，就使用synchronized锁住它的链表头节点或者红黑树的跟节点，然后执行添加操作
    添加之后判断长度看是否需要链表和红黑树之间的转换
    然后调用addCount()方法统计size，并检查是否需要扩容
2，为什么要使用Synchronized取代ReentrantLock
    因为锁被细化到这种程度，出现并发争抢的可能性不高，即使出现了线程也可以在自旋一会儿也可以拿到锁
    Synchronized并不会升级为重量级锁，等待的线程不会被刮起和唤醒，减少了上下文切换过程的开销
    但是如果使用ReentrantLock,，只要线程没有抢到锁就会刮起，这样容易多出很多上下文切换的开销
3，ConcurrentHashMap迭代器是强一致性还是弱一致性？HashMap呢？
    弱一致性，hashmap强一直性。
    ConcurrentHashMap可以支持在迭代过程中，向map添加新元素，而HashMap则抛出了ConcurrentModificationException，
4，ConcurrentHashMap是怎么扩容的？
    在jdk1.7中扩容的时候会对Segment加锁，不同的Segment还是可以并发的。
    在Segment里面先对数组的长度增加一倍，然后遍历原来的table数组，把每一个数组元素迁移到新的数组里面
    迁移完毕后直接替换旧的。
    在jdk1.8中，并发扩容的时候，由于操作的table是同一个，并不能想jdk1.7中那样分段控制
    所以这里必须等待扩容操作完成之后，读写操作才能继续进行，所以扩容效率成了影响整个并发的瓶颈点
    但是jdk1.8做了优化，一个线程在扩容的时候，其他线程的读写会阻塞，然后帮助这个线程进行扩容
5，ConcurrentHashMap的size原理？
    在jdk1.7中1，会使用不加锁的模式去尝试多次计算ConcurrentHashMap的size，最多三次，比较前后两次计算的结果，结果一致就认为当前没有元素加入，计算的结果是准确的
    如果第一种方案不符合，他就会给每个Segment加上锁，然后计算ConcurrentHashMap的size返回
    在jdk1.8中，使用一个volatile类型的变量baseCount记录元素个数，当插入新数据或者删除数据的时候，会通过addCount()方法更新baseCount
    然后在高并发场景下，cas更新baseCounter值的时候失败时，线程会继续执行方法体中的逻辑，这些失败的对象会放在counterCells中
    最终通过baseCount和遍历CounterCell数组得出size
## ConcurrentSkipListMap
是一个key值有序的线程安全的map，适用于高并发场景
内部是通过跳表实现的，跳表的本质是维护了多个链表，并且链表是分层的
最底层的链表，维护了跳表内所有的元素，每上一层链表，都是下面一层链表的子集
跳表内所有链表的元素都是排序的。查找时，可以从顶级链表开始找。一旦发现被查找的元素大于当前节点，就会转入下一层链表继续找
也就是说在搜索的过程中是跳跃式的，是一种空间换时间的算法，本质还是二分查找的思想
对于跳表的插入和删除只需要对局部进行操作加锁即可。



在JDK1.7中
    1，ConcurrentHashMap
        JDK1.7
            实现
                数组（Segment）-> 数组（HashEntry）-> 链表（HashEntry节点）
                Segment数组的意义就是将一个大的table分割成多个小的table来进行加锁，也就是上面的提到的锁分离技术，而每一个Segment元素存储的是HashEntry数组+链表，这个和HashMap的数据存储结构一样
                每一把锁只锁容器其中一部分数据，多线程访问容器里不同数据段的数据，就不会存在锁竞争，提高并发访问率
            初始化
                Segment的大小会通过位与运算来初始化，用size来表示。Segment的大小取值都是以2的N次方，Segment的大小size默认为16
                每一个Segment元素下的HashEntry的初始化也是按照位于运算来计算，用cap来表示，cap的初始值为1，所以HashEntry最小的容量为2
            put操作
                Segment实现了ReentrantLock,也就带有锁的功能
                当执行put操作时，会进行第一次key的hash来定位Segment的位置
                如果该Segment还没有初始化，即通过CAS操作进行赋值初始化
                然后进行第二次hash操作，找到相应的HashEntry的位置，这里会利用继承过来的锁的特性，在将数据插入指定的HashEntry位置时（链表的尾端）
                会通过继承ReentrantLock的tryLock（）方法尝试去获取锁，如果获取成功就直接插入相应的位置
                如果已经有线程获取该Segment的锁，那当前线程会以自旋的方式去继续的调用tryLock（）方法去获取锁，超过指定次数就挂起，等待唤醒
            get操作
                ConcurrentHashMap的get操作跟HashMap类似，只是ConcurrentHashMap第一次需要经过一次hash定位到Segment的位置，然后再hash定位到指定的HashEntry，
                遍历该HashEntry下的链表进行对比，成功就返回，不成功就返回null
            size操作
                计算ConcurrentHashMap的元素大小是一个有趣的问题，因为他是并发操作的，就是在你计算size的时候，他还在并发的插入数据，可能会导致你计算出来的size和你实际的size有相差（在你return size的时候，插入了多个数据），要解决这个问题，JDK1.7版本用两种方案
                    1，第一种方案他会使用不加锁的模式去尝试多次计算ConcurrentHashMap的size，最多三次，比较前后两次计算的结果，结果一致就认为当前没有元素加入，计算的结果是准确的
                    2，第二种方案是如果第一种方案不符合，他就会给每个Segment加上锁，然后计算ConcurrentHashMap的size返回
        JDK1.8
            实现
                Node数组 + 链表 + 红黑树
                桶中的结构可能是链表，也可能是红黑树，红黑树是为了提高查找效率。
            初始化
                初始化操作并不是在构造函数实现的，而是在put操作中实现
            常量设计和数据结构
                1，Node
                    是ConcurrentHashMap存储结构的基本单元，继承于HashMap中的Entry，用于存储数据
                    数据结构很简单，就是一个链表，但是只允许对数据进行查找，不允许进行修改
                2，TreeNode
                    TreeNode继承与Node，但是数据结构换成了红黑树。当链表的节点数大于8时会转换成红黑树的结构
                3，TreeBin
                    TreeBin就是封装TreeNode的容器，它提供转换黑红树的一些条件和锁的控制
            put操作：
                1，如果没有初始化就先调用initTable()方法来进行初始化过程
                2，如果没有hash冲突就直接CAS插入
                3，如果还在进行扩容操作就先进行扩容
                4，如果存在hash冲突，就加锁来保证线程安全，这里有两种情况，一种是链表形式就直接遍历到尾端插入，一种是红黑树就按照红黑树结构插入
                5，最后一个如果Hash冲突时会形成Node链表，在链表长度超过8，Node数组超过64时会将链表结构转换为红黑树的结构，break再一次进入循环
                6，如果添加成功就调用addCount()方法统计size，并且检查是否需要扩容
                大致原理就是使用的是乐观锁，当有冲突的时候才进行并发处理，而且流程步骤很清晰
                基于CAS+synchronized实现，空节点插入使用CAS，有Node节点则使用synchronized加锁
                通过key对象的hashcode计算出数组的索引，如果没有Node，则使用CAS尝试插入元素，失败则无条件自旋，直到插入成功为止，
                如果存在Node，则使用synchronized锁住该Node元素（链表/红黑树的头节点），再执行插入操作。
            扩容
                当需要进行扩容的时候，调用多个工作线程一起帮助进行扩容，这样的效率就会更高，而不是只有检查到要扩容的那个线程进行扩容操作，其他线程就要等待扩容操作完成才能工作
            get操作
                1，计算hash值，定位到该table索引位置，如果是首节点符合就返回
                2，如果遇到扩容的时候，会调用标志正在扩容节点ForwardingNode的find方法，查找该节点，匹配就返回
                3，以上都不符合的话，就往下遍历节点，匹配就返回，否则最后就返回null
        总结：
            JDK1.8版本的ConcurrentHashMap的数据结构已经接近HashMap，相对而言，ConcurrentHashMap只是增加了同步的操作来控制并发
            1，JDK1.8的实现降低锁的粒度，JDK1.7版本锁的粒度是基于Segment的，包含多个HashEntry，而JDK1.8锁的粒度就是HashEntry
            2，JDK1.8版本的数据结构变得更加简单，使得操作也更加清晰流畅，因为已经使用synchronized来进行同步，所以不需要分段锁的概念，也就不需要Segment这种数据结构了，由于粒度的降低，实现的复杂度也增加了
            3，JDK1.8使用红黑树来优化链表，基于长度很长的链表的遍历是一个很漫长的过程，而红黑树的遍历效率是很快的，代替一定阈值的链表，这样形成一个最佳拍档
            4，JDK1.8为什么使用内置锁synchronized来代替重入锁ReentrantLock，我觉得有以下几点
                1，因为粒度降低了，在相对而言的低粒度加锁方式，synchronized并不比ReentrantLock差，在粗粒度加锁中ReentrantLock可能通过Condition来控制各个低粒度的边界，更加的灵活，而在低粒度中，Condition的优势就没有了
                2，JVM的开发团队从来都没有放弃synchronized，而且基于JVM的synchronized优化空间更大，使用内嵌的关键字比使用API更加自然
                3，在大量的数据操作下，对于JVM的内存压力，基于API的ReentrantLock会开销更多的内存，虽然不是瓶颈，但是也是一个选择依据                
    2，ConcurrentSkipListMap
        特性
            1，线程安全，适用于高并发场景
            2，key是有序的，
            3，ConcurrentSkipListMap 的存取时间是log（N），和线程数几乎无关。也就是说在数据量一定的情况下，并发的线程越多，ConcurrentSkipListMap越能体现出他的优势
            4，是TreeMap的并发版本
        和TreeMap对比
            1，TreeMap是非线程安全的，而ConcurrentSkipListMap是线程安全的
            2，ConcurrentSkipListMap是通过跳表实现的，而TreeMap是通过红黑树实现的。
            3，非多线程的情况下，应当尽量使用TreeMap。
            4，此外对于并发性相对较低的并行程序可以使用Collections.synchronizedSortedMap将TreeMap进行包装，也可以提供较好的效率。
            5，对于高并发程序，应当使用ConcurrentSkipListMap，能够提供更高的并发度
        数据结构：
            跳表的本质，是同时维护了多个链表，并且链表是分层的
            最低层的链表，维护了跳表内所有的元素，每上面一层链表，都是下面一层的子集。
            跳表内所有链表的元素都是排序的。查找时，可以从顶级链表开始找。一旦发现被查找的元素大于当前链表中的取值，就会转入下一层链表继续找
            这也就是说在查找过程中，搜索是跳跃式的
            跳表是一种利用空间换时间的算法。
        属性：
            表头（head）：负责维护跳跃表的节点指针。
            跳跃表节点：保存着元素值，以及多个层
            层：保存着指向其他元素的指针。高层的指针越过的元素数量大于等于低层的指针，为了提高查找的效率，程序总是从高层先开始访问，然后随着元素值范围的缩小，慢慢降低层次
            表尾：全部由 NULL 组成，表示跳跃表的末尾
        ConcurrentSkipListMap是线程安全的有序的哈希表，适用于高并发的场景。
        对平衡树的插入和删除往往很可能导致平衡树进行一次全局的调整；而对跳表的插入和删除，只需要对整个数据结构的局部进行操作即可
        在高并发的情况下，需要一个全局锁，来保证整个平衡树的线程安全；而对于跳表，则只需要部分锁即可
      
# Exchanger
Exchanger可以在两个线程之间交换数据，只能有两个线程，他不支持更多线程之间互换数据
它提供了一个同步点，在这个同步点的两个线程可以交换彼此的数据。这两个线程通过exchange方法交换数据
如果第一个线程执行exchange方法，他会一直等到第二个线程也执行exchange方法，当两个线程都到达同步点
这两个线程交换数据。
    

# Synchronized和lock区别
也就是所谓的显式锁和隐式锁（Synchronized和ReentrantLock）
1，原始构成：
    sychronized是java中的关键字，是由jvm来维护的，是jvm层面的锁
    lock是jdk5以后才出现的具体类。是API层面的锁
2，使用方法上：
    sychronized不需要手动释放锁，当同步代码块执行完毕后，系统自动让线程释放对锁的占用
    lock需要手动释放锁
3，等待是否可中断
     Sync是不可中断的。除非抛出异常或者正常运行完成
     Lock可以中断的。中断方式：
             1：调用设置超时方法tryLock(long timeout ,timeUnit unit)
             2：调用lockInterruptibly()放到代码块中，然后调用interrupt()方法可以中断
4，加锁的时候是否可以公平
    Sync：非公平锁
    lock：两者都可以的。默认是非公平锁。在其构造方法的时候可以传入Boolean值。
        true：公平锁；false：非公平锁
5，唤醒方式
    Sync:要么随机唤醒一个线程；要么是唤醒所有等待的线程。
    Lock:可以分组唤醒他们需要唤醒的线程，可以精确唤醒 

# CompletableFuture异步编排
CompletableFuture提供了非常强大的Future扩展功能，可以帮助我们简化异步编程的复杂性，并且提供了函数式编程的能力
它提供了4个静态方法，其中两个runAsync的重载方法是没有返回值的，两个supplyAsync重载方法是有返回值的
可以指定线程池，如果没有就使用默认的线程池
主要方法有：
    whenComplete：方法完成后感知，可以获取到处理结果和异常信息，但是无法修改返回结果
    whenCompleteAsync：与whenComplete类似，区别是会将这个任务交给线程池处理，而不是像whenComplete那样都当前线程处理
    execptionally：获取异常信息，修改返回结果
    handle：可以获取到处理结果和异常信息，也可以修改返回结果
    当一个线程依赖另一个线程时，比如B线程需要A线程执行完成之后才能继续执行：  
        主要有三个方法：
            thenRun系列方法：没有返回值，也不需要依赖上一个线程的返回结果
            thenAccept系列方法：没有返回值，需要依赖上一个线程的返回结果
            thenApply系列方法：有返回值，也需要依赖上一个线程的返回结果
            这三个方法不带Async，是在主线程中执行，带Async是异步执行
    两任务组合，完成后才执行后面的线程：
        两任务组合，完成后执行后面的线程
        runAfterBothAsync：没有返回值，不依赖前面两个线程的返回值
        thenAcceptBothAsync：没有返回值，依赖前面两个线程的返回值
        thenCombineAsync：有返回值，依赖前面两个线程的返回值
    两个任务有一个完成就执行后面的结果，两个任务的返回结果类型必须一致
        runAfterEitherAsync：感知不到前面两个任务的结果，没有返回值
        acceptEitherAsync：感知前面两个任务的结果，没有返回值
        applyToEitherAsync：感知前面两个任务的结果，有返回值
    多任务组合：
        allof：组合多个任务，调用allof.get()，阻塞等待所有结果完成
        anyOf：组合多个任务，调用allof.get()，阻塞等待有任何一个结果完成就返回


应用场景：查询商品详情页的逻辑比较复杂，有些数据还需要远程调用，必然需要话费更多时间。
假如商品详情页的每个查询，需要如下标注时间才能完成，那么，用户需要6.5s后才能看到商品详情页内容，很显然是不能接受的
如果有多个线程同时完成这6步操作，也许只需要1.5s即可完成
1，四个静态方法来创建一个异步操作。
    1，static CompletableFuture<void> runAsync(Runnable runnable);
    2，public static CompletableFuture<void> runAsync(Runnable runnable, Executor executor);
    3，public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier);
    4，public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor);
    runXxx都是没有返回结果的，supplyXxx是可以获取返回结果的
    可以传入自定义线程池，否则就用默认的线程池
2，基本使用
    public static ExecutorService executor = Executors.newFixedThreadPoll(10);
    CompletableFuture<void> future = CompletableFuture.runAsync(() -> {
        int i = 10/2;
    }, executor);
    CompletableFuture<void> future = CompletableFuture.supplyAsync(() -> {
        int i = 10/2;
        return i;
    }, executor);
    Integer interger = future.get();
3，whenComplete（方法完成后的感知）
    CompletableFuture<void> future = CompletableFuture.supplyAsync(() -> {
        int i = 10/2;
        return i;
    }, executor).whenComplete((res, excption) -> {
        //虽然能得到异常信息，但是不能修改返回数据
        System.out.println("异步任务完成了。。。结果是" + res);
        System.out.println("异步任务完成了。。。异常是" + exception);
    }).execptionally((throwable) -> {
        //可以感知异常，同时返回默认值
        return 10;
    });
    whenComplete: 执行当前任务的线程继续执行whenComplete任务
    whenCompleteAsync: 执行把whenCompleteAsync这个任务继续提交给线程池
    方法不以async结尾，意味这action使用相同的线程执行，而Async可能会使用其他线程执行
4，handle（方法完成后的处理）
    CompletableFuture<void> future = CompletableFuture.supplyAsync(() -> {
        int i = 10/2;
        return i;
    }, executor).handle((res, excption) -> {
        //虽然能得到异常信息，还可以修改返回数据
        System.out.println("异步任务完成了。。。结果是" + res);
        System.out.println("异步任务完成了。。。异常是" + exception);
        if (res != null) {
            return res*2;
        }
        if (excption != null) {
            return 0;
        }
        return 0;
    });
5，线程串行化
    当一个线程依赖另一个线程时，获取上一个任务返回的结果，并返回当前任务的返回值
    带有Async默认是异步执行的。同之前，以上都要前置任务成功完成。
    //thenRun: 不能获取到上一步的执行结果
    CompletableFuture<void> future1 = CompletableFuture.supplyAsync(() -> {
        int i = 10/2;
        return i;
    }, executor).thenRunAsync(() -> {
        System.out.println("任务2启动了。。。");
    }, executor);
    //thenAccept: 能获取到上一步的执行结果，但是无返回值
    CompletableFuture<void> future1 = CompletableFuture.supplyAsync(() -> {
        int i = 10/2;
        return i;
    }, executor).thenAccept((res) -> {
        System.out.println("任务2启动了。。。" + res);
    }, executor);
    //thenApplyAsync: 既能获取到上一步的执行结果，又能有返回值
    CompletableFuture<void> future1 = CompletableFuture.supplyAsync(() -> {
        int i = 10/2;
        return i;
    }, executor).thenApplyAsync((res) -> {
        System.out.println("任务2启动了。。。" + res);
        return res*2;
    }, executor);
5，并行化（两任务组合 - 都要完成再执行后面的结果）
    CompletableFuture<void> future1 = CompletableFuture.supplyAsync(() -> {
        return 10/2;
    }, executor);
    CompletableFuture<void> future2 = CompletableFuture.supplyAsync(() -> {
        return "Hello";
    }, executor);
    //这样感知不到前两个的执行结果
    future1.runAfterBothAsync(future2, () -> {
        System.out.println("任务1、2都完成后，任务3启动了。。。");
    }, executor);
    //可以感知到前面线程的执行结果
    future1.thenAcceptBothAsync(future2, (f1, f2) -> {
        System.out.println("任务1、2都完成后，任务3启动了。。。之前的执行结果：" + f1 + f2);
    }, executor);
    //可以感知到前面线程的执行结果，并处理后返回
    future1.thenCombineAsync(future2, (f1, f2) -> {
        System.out.println("任务1、2都完成后，任务3启动了。。。");
        return f1 + f2;
    }, executor);
5，两个任务有一个完成就执行后面的结果
    CompletableFuture<void> future1 = CompletableFuture.supplyAsync(() -> {
        return 10/2;
    }, executor);
    CompletableFuture<void> future2 = CompletableFuture.supplyAsync(() -> {
        return 10/1;
    }, executor);
    //这样感知不到前两个的执行结果，无返回值
    future1.runAfterEitherAsync(future2, () -> {
        System.out.println("任务1、2任何一个完成后，任务3启动了。。。");
    }, executor);
    //可以感知到前面线程的执行结果，任务1、任务2的执行结果类型必须一样
    future1.acceptEitherAsync(future2, (res) -> {
        System.out.println("任务1、2任意一个完成后，任务3启动了。。。之前的执行结果：" + res);
    }, executor);
    //可以感知到前面线程的执行结果，并处理后返回，任务1、任务2的执行结果类型必须一样
    future1.applyToEitherAsync(future2, (res) -> {
        System.out.println("任务1、2任意一个完成后，任务3启动了。。。");
        return res + "hahaha";
    }, executor);
6，多任务组合
    CompletableFuture<void> future1 = CompletableFuture.supplyAsync(() -> {
        System.out.println("任务1执行。。。");
        return "任务1"；
    }, executor);
    CompletableFuture<void> future2 = CompletableFuture.supplyAsync(() -> {
        System.out.println("任务2执行。。。");
        return "任务2"；
    }, executor);
    CompletableFuture<void> future3 = CompletableFuture.supplyAsync(() -> {
        System.out.println("任务3执行。。。");
        return "任务3"；
    }, executor);
    //所有都执行成功后
    CompletableFuture<void> allof = CompletableFuture.allOf((future1, future2, future3));
    allof.get();//这里阻塞等待所有结果完成
    System.out.println("每个任务的结果。。。" + future1.get() + future2.get() + future3.get());
    //任何一个执行成功
    CompletableFuture<void> anyof = CompletableFuture.anyOf((future1, future2, future3));
    anyof.get();//阻塞等待有一个完成就好
    System.out.println("最先成功的任务的结果。。。" + anyof.get());
    
# ThreadLocal
1，概述
    ThreadLocal提供了线程内存储变量的能力，这些变量不同之处在于每一个线程读取的变量是对应的互相独立的。通过get和set方法就可以得到当前线程对应的值
    ThreadLocal相当于维护了一个map，key就是当前的线程，value就是需要存储的对象。
    ThreadLocal和Synchronized都是为了解决多线程中相同变量的访问冲突问题，不同的点是
    - Synchronized是通过线程等待，牺牲时间来解决访问冲突
    - ThreadLocal是通过每个线程单独一份存储空间，牺牲空间来解决冲突，并且相比于Synchronized，ThreadLocal具有线程隔离的效果，只有在线程内才能获取到对应的值，线程外则不能访问到想要的值。
2，实现原理
    ThreadLocal的静态内部类ThreadLocalMap为每个Thread都维护了一个数组table，ThreadLocal确定了一个数组下标，而这个下标就是value存储的对应位置。
    每个线程持有一个ThreadLocalMap对象。每一个新的线程Thread都会实例化一个ThreadLocalMap并赋值给成员变量threadLocals，使用时若已经存在threadLocals则直接使用已经存在的对象。
    //在某一线程声明了ABC三种类型的ThreadLocal
    ThreadLocal<A> sThreadLocalA = new ThreadLocal<A>();
    ThreadLocal<B> sThreadLocalB = new ThreadLocal<B>();
    ThreadLocal<C> sThreadLocalC = new ThreadLocal<C>();
    1）对于某一ThreadLocal不同线程来讲，他的索引值i是确定的，在不同线程之间访问时访问的是不同的table数组的同一位置即都为table[i]，只不过这个不同线程之间的table是独立的。
    2）对于同一线程的不同ThreadLocal来讲，这些ThreadLocal实例共享一个table数组，然后每个ThreadLocal实例在table中的索引i是不同的。
3，简单使用
    ```
        package test;
        public class ThreadLocalTest {
            static ThreadLocal<String> localVar = new ThreadLocal<>();
            static void print(String str) {
                //打印当前线程中本地内存中本地变量的值
                System.out.println(str + " :" + localVar.get());
                //清除本地内存中的本地变量
                localVar.remove();
            }
            public static void main(String[] args) {
                Thread t1  = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //设置线程1中本地变量的值
                        localVar.set("localVar1");
                        //调用打印方法
                        print("thread1");
                        //打印本地变量
                        System.out.println("after remove : " + localVar.get());
                    }
                });
                Thread t2  = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //设置线程1中本地变量的值
                        localVar.set("localVar2");
                        //调用打印方法
                        print("thread2");
                        //打印本地变量
                        System.out.println("after remove : " + localVar.get());
                    }
                });
                t1.start();
                t2.start();
            }
        }
    ```
4，应用场景
    1）ThreadLocal 用作保存每个线程独享的对象，为每个线程都创建一个副本，这样每个线程都可以修改自己所拥有的副本, 而不会影响其他线程的副本，确保了线程安全。
        场景通常用于保存线程不安全的工具类，典型的需要使用的类就是 SimpleDateFormat。
        每个任务都创建了一个 simpleDateFormat 对象，也就是说，1000 个任务对应 1000 个 simpleDateFormat 对象，这显然是不合理的。
        我们可能会想到，要不所有的线程共用一个 simpleDateFormat 对象？但是simpleDateFormat 又不是线程安全的，我们必须做同步，比如使用synchronized加锁。到这里也许就是我们最终的一个解决方法。但是使用synchronized加锁会陷入一种排队的状态
        使用ThreadLocal：
            ```
            public class ThreadLocalDemo04 {
                public static ExecutorService threadPool = Executors.newFixedThreadPool(16);
                public static void main(String[] args) throws InterruptedException {
                    for (int i = 0; i < 1000; i++) {
                        int finalI = i;
                        threadPool.submit(() -> {
                            String data = new ThreadLocalDemo04().date(finalI);
                            System.out.println(data);
                        });
                    }
                    threadPool.shutdown();
                }
                private String date(int seconds){
                    Date date = new Date(1000 * seconds);
                    SimpleDateFormat dateFormat = ThreadSafeFormater.dateFormatThreadLocal.get();
                    return dateFormat.format(date);
                }
            }
            class ThreadSafeFormater{
                public static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("mm:ss"));
            }
        ```
    2）每个线程内需要保存类似于全局变量的信息（例如在拦截器中获取的用户信息），可以让不同方法直接使用，避免参数传递的麻烦却不想被多线程共享（因为不同线程获取到的用户信息不一样）。
        例如，用 ThreadLocal 保存一些业务内容（用户权限信息、从用户系统获取到的用户名、用户ID 等），这些信息在同一个线程内相同，但是不同的线程使用的业务内容是不相同的。
        在线程生命周期内，都通过这个静态 ThreadLocal 实例的 get() 方法取得自己 set 过的那个对象，避免了将这个对象（如 user 对象）作为参数传递的麻烦。
        ```
        package com.kong.threadlocal;
        public class ThreadLocalDemo05 {
            public static void main(String[] args) {
                User user = new User("jack");
                new Service1().service1(user);
            }
        
        }
        class Service1 {
            public void service1(User user){
                //给ThreadLocal赋值，后续的服务直接通过ThreadLocal获取就行了。
                UserContextHolder.holder.set(user);
                new Service2().service2();
            }
        }
        class Service2 {
            public void service2(){
                User user = UserContextHolder.holder.get();
                System.out.println("service2拿到的用户:"+user.name);
                new Service3().service3();
            }
        }
        class Service3 {
            public void service3(){
                User user = UserContextHolder.holder.get();
                System.out.println("service3拿到的用户:"+user.name);
                //在整个流程执行完毕后，一定要执行remove
                UserContextHolder.holder.remove();
            }
        }
        class UserContextHolder {
            //创建ThreadLocal保存User对象
            public static ThreadLocal<User> holder = new ThreadLocal<>();
        }
        class User {
            String name;
            public User(String name){
                this.name = name;
            }
        }
        ```
# BlockingQueue
表示一个可以存取元素，线程安全的队列。换句话说，当线程从BlockingQueue中插入、获取元素的时候，不会导致任何并发问题
从BlockingQueue可以引申出一个概念：阻塞队列，指队列本身可以阻塞线程向队列插入元素插入元素，或者阻塞队列里面获取元素
比如：当一个线程尝试去一个空队列里面获取元素的时候，这个线程将被阻塞直到队列元素数量不为空
当然，线程是否会被阻塞取决于你调用什么方法从BlockingQueue获取元素，有的方法会阻塞线程，有的方法会抛出异常等等，下文我们会详细介绍。
接口类：
    ArrayBlockingQueue ：一个由数组结构组成的有界阻塞队列。
    LinkedBlockingQueue ：一个由链表结构组成的有界阻塞队列。
    PriorityBlockingQueue ：一个支持优先级排序的无界阻塞队列。
    DelayQueue：一个使用优先级队列实现的无界阻塞队列。
    SynchronousQueue：一个不存储元素的阻塞队列。
    LinkedTransferQueue：一个由链表结构组成的无界阻塞队列。
    LinkedBlockingDeque：一个由链表结构组成的双向阻塞队列。
应用场景：
    生产者线程不断的生产新的对象，并将他们插入到BlockingQueue，直到队列中object的数量达到队列存储容量的上限。也就是说当队列中对象达到容量上限的时候，生产者线程将被阻塞，不能再向队列中插入新的对象。生产者线程将保持阻塞等待状态，直到消费者线程从队列中拿走Object，让队列有空余位置放入新的对象。
    消费者线程不断的从BlockingQueue取出对象并将其进行处理。如果消费者线程尝试从一个空队列中获取一个对象，消费者线程将被阻塞处于等待状态，直到生产者向队列中放入一个新的对象。
    所以BlockingQueue经常被用于生产消费的缓冲队列，如果你不想用分布式的或者中间件消息队列（redis、kafka）等（因为对于一个小功能会增加比较大的独立中间件运维成本），BlockingQueue可以能是一个备选的选项。
方法介绍：
    JavaBlockingQueue 提供了四组不同的方法用于向队列中插入、移除、检查队列中包含某一元素对象。每一组方法在被调用之后的响应行为上有所不同，如下：
    （1）add(o)
    BlockingQueueadd() 方法可以将o对象以参数的形式插入到队列里面，如果队列里面有剩余空间，将被立即插入；如果队列里面没有剩余空间，add()方法将跑出 IllegalStateException.
    （2）offer(o)
    BlockingQueueoffer() 方法可以将o对象以参数的形式插入到队列里面，如果队列里面有剩余空间，将被立即插入；如果队列里面没有剩余空间，offer()方法将返回特定的值false.
    （3）offer(o, long millis, TimeUnit timeUnit)
    BlockingQueueoffer() 方法有另外一个版本的实现，存在超时时间的设置参数。这个版本的offer()方法将o对象以参数的形式插入到队列里面，如果队列里面有剩余空间，将被立即插入；如果队列里面没有剩余空间，调用offer方法的线程在超时时间内将被阻塞处于等到状态，当阻塞时间大于超时时间之后，队列内如果仍然没有剩余空间放入新对象，offer()方法将返回false.
    （4）put(o)
    BlockingQueueput() 方法可以将o对象以参数的形式插入到队列里面，如果队列里面有剩余空间，将被立即插入；如果队列里面没有剩余空间，调用put的方法的线程将被阻塞，直到BlockingQueue里面腾出新的空间可以放入对象为止。
    （5）take()
    BlockingQueuetake() 方法取出并移除队列中的第一个元素（对象），如果BlockingQueue队列中不包含任何的元素，调用take()方法的线程将被阻塞，直到有新的元素对象插入到队列中为止。
    （6）poll()
    BlockingQueuepoll() 方法取出并移除队列中的第一个元素（对象），如果BlockingQueue队列中不包含任何的元素，poll()方法将返回null.
    （7）poll(long timeMillis, TimeUnit timeUnit)
    BlockingQueuepoll(long timeMillis, TimeUnit timeUnit)方法同样存在一个超时时间限制的版本，正常情况下该方法取出并移除队列中的第一个元素（对象）。如果BlockingQueue队列中不包含任何的元素，在超时时间范围内，如果仍然没有新的对象放入队列，这个版本的poll()方法将被阻塞处于等待状态；当阻塞时间大于超时时间之后，poll(long timeMillis, TimeUnit timeUnit)返回null
    （8）remove(Object o)
    BlockingQueueremove(Object o) 方法可以从队列中删除一个以参数形式给定的元素对象，remove()方法使用o.equals(element)将传入参数o与队列中的对象进行一一比对，从而判定要删除的对象是否在队列中存在，如果存在就从队列中删除并返回true，否则返回false。
    需要注意的是：如果队列中有多个与传入参数equals相等的对象，只删除其中一个，不会将队列中所有匹配的对象都删除。
    （9）peek()
    BlockingQueuepeek() 方法将取出队列中的第一个元素对象，但是并不会将其从队列中删除。如果队列中目前没有任何的元素，也就是空队列，peek()方法将返回null.
    （10）element()
    BlockingQueueelement()方法将取出队列中的第一个元素对象，但是并不会将其从队列中删除。如果队列中目前没有任何的元素，也就是空队列，element()方法将抛出 NoSuchElementException.
    （11）contains(Object o)
    BlockingQueuecontains(Object o) 方法用来判断当前队列中是否存在某个对象，该对象与传入参数o相等（Objects.equals(o, element)被用来判定对象的相等性）。遍历队列中的所有元素，一旦在队列中发现匹配的元素对象，该方法将返回true；如果没有任何的元素匹配相等，该方法返回false。
    （12）drainTo(Collection dest)
    drainTo(Collection dest)方法一次性的将队列中的所有元素取出到集合类Collection dest对象中保存。
    （13）drainTo(Collection dest, int maxElements)
    drainTo(Collection dest)方法一次性的从队列中取出maxElements个元素到集合类Collection dest对象中保存。
    （14）size()
    BlockingQueuesize() 方法返回队列中目前共有多少个元素
    （15）remainingCapacity()
    BlockingQueueremainingCapacity() 方法将返回队列目前还剩多少个可用空间用于放入新的对象。剩余空间容量=队列的总容量-已经被占用的空间数量
    
# ThreadLocalRandom
Random类随机数的产生是由一个种子变量计算出的，再多线程的环境下种子变量的竞争会降低并发性能
ThreadLocalRandom应运而生，原理是：
    让每一个线程都应有自己的种子变量，则每个线程都会根据自己的老种子计算新种子，并使用新种子计算随机数，
    这样就不存在竞争问题了，可以极大的提高并发

# 原子操作类AtomicInteger
原子操作类可以帮助我们在不使用synchronized同步锁的情况下，实现多线程场景下int数值操作的线程安全，操作的原子性。
而且使用AtomicInteger来实现int数值原子操作，远比使用synchronized效率高
适用场景：
    1，多线程并发场景下操作一个计数器，需要保证计数器操作的原子性。
        - addAndGet()- 将给定的值加到当前值上，并在加法后返回新值，并保证操作的原子性。
        - getAndAdd()- 将给定的值加到当前值上，并返回旧值，并保证操作的原子性。
        - incrementAndGet()- 将当前值增加1，并在增加后返回新值。它相当于++i操作，并保证操作的原子性。
        - getAndIncrement()- 将当前值增加1并返回旧值。相当于++i操作，并保证操作的原子性。
        - decrementAndGet()- 将当前值减去1，并在减去后返回新值，相当于i--操作，并保证操作的原子性。
        - getAndDecrement()- 将当前值减去1，并返回旧值。它相当于 --i操作，并保证操作的原子性。
    2，进行数值比较，如果给定值与当前值相等，进行数值的更新操作，并实现操作的非阻塞算法。
        compareAndSet操作将一个内存位置的内容与一个给定的值进行比较，只有当它们相同时，才会将该内存位置的内容修改为一个给定的新值。这个过程是以单个原子操作的方式完成的。
        - boolean compareAndSet(int expect, int update)
          - expect是预期值
          - update是更新值
        public class Main {
            public static void main(String[] args) {
                //初始值为100的atomic Integer
                AtomicInteger atomicInteger = new AtomicInteger(100);
                //当前值100 = 预期值100，所以设置atomicInteger=110
                boolean isSuccess = atomicInteger.compareAndSet(100,110);  
                System.out.println(isSuccess);      //输出结果为true表示操作成功
                //当前值110 = 预期值100？不相等，所以atomicInteger仍然等于110
                isSuccess = atomicInteger.compareAndSet(100,120);  
                System.out.println(isSuccess);      //输出结果为false表示操作失败
            }
        }
扩展：
    AtomicBoolean布尔原子操作类、
    AtomicLong长整型布尔原子操作类、
    AtomicReference对象原子操作类、
    AtomicIntegerArray整型数组原子操作类、
    AtomicLongArray长整型数组原子操作类、
    AtomicReferenceArray对象数组原子操作类
原理：
    CAS

# 线程池
为什么使用线程池：
    每创建一个线程都需要在堆上分配内存空间，同时需要分配本地方法栈、虚拟机栈、程序计数器等线程私有方法空间。
    当线程被可达性分析算法标记为不可用时被GC回收，这样频繁的创建销毁需要大量额外的开销
    1，可以复用线程池中的线程，不需要每次都创建新线程，减少创建和销毁线程的开销
    2，线程池具有队列缓冲策略、拒绝机制、动态管理线程个数、特定的线程还有定时执行、周期执行的功能
    3，线程池还可以实现线程环境的隔离、例如分别定义支付功能和优惠券功能的相关线程池，其中一个运行有问题不会影响另一个
常用的几种线程池
    newFixedThreadPool(int nThreads)：创建固定大小的线程池（可以用于已知并发压力的情况下，对线程数做限制）
    newSingleThreadExecutor()：创建只有一个线程的线程池（可以用于需要保证顺序执行的场景，并且只有一个线程在执行。）
    newCachedThreadPool()：创建一个不限线程数上限的线程池，任何提交的任务都将立即执行（比较适合处理执行时间比较小的任务）
    newScheduledThreadPool：可以延时启动，定时启动的线程池，（适用于需要多个后台线程执行周期任务的场景）
    newWorkStealingPool：一个拥有多个任务队列的线程池（可以减少连接数，创建当前可用cpu数量的线程来并行执行）
线程池参数
    corePoolSize(int)
        核心线程数量。创建了线程池后，线程池中的线程数为0，当有任务来之后，就会创建一个线程去执行任务，当线程池中的线程数目达到corePoolSize后，就会把到达的任务放到任务队列当中
        线程池将长期保证这些线程处于存活状态，即使线程已经处于闲置状态。
        除非配置了allowCoreThreadTimeOut=true，核心线程数的线程也将不再保证长期存活于线程池内，在空闲时间超过keepAliveTime后被销毁。
    workQueue
        阻塞队列，存放等待执行的任务，线程从workQueue中取任务，若无任务将阻塞等待。
        当线程池中线程数量达到corePoolSize后，就会把新任务放到该队列当中。
        JDK提供了四个可直接使用的队列实现：
            基于数组的有界队列ArrayBlockingQueue
            基于链表的无界队列LinkedBlockingQueue
            只有一个元素的同步队列SynchronousQueue
            优先级队列PriorityBlockingQueue。
            在实际使用时一定要设置队列长度。
    maximumPoolSize(int)
        线程池内的最大线程数量，线程池内维护的线程不得超过该数量，大于核心线程数量小于最大线程数量的线程将在空闲时间超过keepAliveTime后被销毁。
        当阻塞队列存满后，将会创建新线程执行任务，线程的数量不会大于maximumPoolSize。
    keepAliveTime(long)
        线程存活时间，若线程数超过了corePoolSize，线程闲置时间超过了存活时间，该线程将被销毁。
        除非配置了allowCoreThreadTimeOut=true，核心线程数的线程也将不再保证长期存活于线程池内，在空闲时间超过keepAliveTime后被销毁。
    RejectedExecutionHandler
        拒绝策略，当任务队列存满并且线程池个数达到maximunPoolSize后采取的策略。
        ThreadPoolExecutor中提供了四种拒绝策略，分别是
            AbortPolicy（终止策略）：抛RejectedExecutionException异常
            CallerRunsPolicy（调用者运行策略）：直接用调用者所在线程来运行任务
            DiscardOldestPolicy（丢弃最旧策略）：丢弃队列中最久的任务，然后再调用 execute()
            DiscardPolicy（丢弃策略）：直接丢弃任务
        除了JDK默认提供的四种拒绝策略，我们可以根据自己的业务需求去自定义拒绝策略，自定义的方式很简单，直接实现RejectedExecutionHandler接口即可。
    threadFactory
        创建线程的工厂，虽说JDK提供了线程工厂的默认实现DefaultThreadFactory
        但还是建议自定义实现最好，这样可以自定义线程创建的过程，例如线程分组、自定义线程名称等。
核心任务原理：
    1，先判断线程池中当前线程数量是否达到了corePoolSize，若未达到，则新建线程运行此任务，且任务结束后将该线程保留在线程池中，不做销毁处理
    2，若当前线程数量已达到corePoolSize，判断工作队列(workQueue)是否已满，未满则将新的任务提交到工作队列中
    3，如果工作队列也满了，判断线程池中的线程数量是否达到了maxumunPoolSize，如果未达到，则新建一个工作线程来执行这个任务
    4，如果达到了maxumunPoolSize，达到了则使用饱和策略来处理这个任务
    注意： 在线程池中的线程数量超过corePoolSize时，每当有线程的空闲时间超过了keepAliveTime，这个线程就会被终止
线程池几种状态
    RUNNING：接收新任务，处理队列任务。
    SHUTDOWN：不接收新任务，但处理队列任务。
    STOP：不接收新任务，也不处理队列任务，并且中断所有处理中的任务。
    TIDYING（整理）：所有任务都被终结，有效线程为0。会触发terminated()方法。
    TERMINATED：当terminated()方法执行结束。
构造函数
    构造函数很简单，就是把线程池的配置信息赋值，没有实际开线程预热
execute(Runnable command) 提交任务
    当前运行中的线程数(工作线程数) < 核心线程数时，调用addWorker执行任务。
    当前运行中的线程数(工作线程数) >= 核心线程数时，暂时先放到一个阻塞队列里面，等待有空闲的线程了，就取这个阻塞队列去执行。
    如果上面阻塞队列存储满了，在调用一次addWorker去执行任务，如果addWorker不成功时，就会拒绝这个任务。
重点addWorker
    1，客户端创建线程池对象后，调用execute方法提交一个runnable任务
    2，execute方法内会调用addWorker方法创建一个worker对象
    3，addWorker方法内会调用Worker.thread.start方法，这时候实际调用的是Worker对象内的run方法
    4，Worker中的run方法委托给runWorker方法执行
    5，runWorker中有while循环体，不断地调用getTask获取新任务
    6，getTask方法中通过bolckQueue的take方法来获取队列中的任务，如果队列为空，则一直阻塞当前线程
        如果当前活动线程数大于核心线程数，当去缓存队列中取任务的时候，如果缓存队列中没任务了，则等待keepAliveTime的时长，此时还没任务就返回null，这就意味着runWorker()方法中的while循环会被退出，其对应的线程就要销毁了，也就是线程池中少了一个线程了。因此只要线程池中的线程数大于核心线程数就会这样一个一个地销毁这些多余的线程。
        如果当前活动线程数小于等于核心线程数，同样也是去缓存队列中取任务，但当缓存队列中没任务了，就会进入阻塞状态，直到能取出任务为止，因此这个线程是处于阻塞状态的，并不会因为缓存队列中没有任务了而被销毁。这样就保证了线程池有N个线程是活的，可以随时处理任务，从而达到重复利用的目的。
    因此，线程池是通过队列的take方法阻塞核心线程Worker的run方法，保证核心线程不会因为执行完run方法而被系统终止
线程参数调优
    CPU 密集型
        CPU密集型也叫计算密集型，指的是系统的硬盘、内存性能相对CPU要好很多，此时，系统运作大部分的状况是CPU Loading 100%，CPU要读/写I/O(硬盘/内存)，I/O在很短的时间就可以完成，而CPU还有许多运算要处理，CPU Loading很高。
        这种任务消耗的主要是 CPU 资源，可以将线程数设置为 N（CPU 核心数）+1，比 CPU 核心数多出来的一个线程是为了防止线程偶发的缺页中断，或者其它原因导致的任务暂停而带来的影响。一旦任务暂停，CPU 就会处于空闲状态，而在这种情况下多出来的一个线程就可以充分利用 CPU 的空闲时间。
    I/O 密集型
        IO密集型指的是系统的CPU性能相对硬盘、内存要好很多，此时，系统运作，大部分的状况是CPU在等I/O (硬盘/内存) 的读/写操作，此时CPU Loading并不高。
        I/O 密集型 这种任务应用起来，系统会用大部分的时间来处理 I/O 交互，而线程在处理 I/O 的时间段内不会占用 CPU 来处理，这时就可以将 CPU 交出给其它线程使用。因此在 I/O 密集型任务的应用中，我们可以多配置一些线程，具体的计算方法是 2N。
execute和submit的区别？
    execute适用于不需要关注返回值的场景，只需要将线程丢到线程池中去执行就可以了。
    submit方法适用于需要关注返回值的场景
线程池的关闭
    shutdownNow：对正在执行的任务全部发出interrupt()，停止执行，对还未开始执行的任务全部取消，并且返回还没开始的任务列表。
    shutdown：当我们调用shutdown后，线程池将不再接受新的任务，但也不会去强制终止已经提交或者正在执行中的任务。
1，线程池中的几种重要的参数及流程
    以上已经详解
2，线程池的异常处理？
    （1）在runnable里面使用try-catch捕获异常
    （2）当使用submit执行，通过Future.get捕获异常
    （3）重写ThreadPoolExecutor.afterExecute方法，处理传递的异常引用
    （4）传入自己的ThreadFactory，设置Thread.UncaughtExecptionHandler处理异常
3，使用无界队列的线程池会导致内存飙升吗？
    会的
5，线程池的工作原理
    当任务被提交到线程池，会先判断当前线程数量是否小于collPollSize，如果小于就创建线程来执行提交的任务
    否则就放入workQueue队列，如果workQueue满了，则判断当前线程数量是否小于maximunPollSize，
    如果小于则创建线程执行任务，否则就调用handler，执行拒绝策略。
6，如何保证核心线程不会因为执行完run方法而被系统终止？
    通过队列的take方法阻塞核心线程Worker的run方法


# CAS算法（CompareAndSwap）
概述：
    一个线程失败或挂起并不会导致其他线程也失败或挂起，那么这种算法就被称为非阻塞算法。
    而CAS就是一种非阻塞算法实现，也是一种乐观锁技术，它能在不使用锁的情况下实现多线程安全，所以CAS也是一种无锁算法。
    CAS（CompareAndSwap）比较并交换，是一种实现并发算法时常用到的技术，Java并发包中的很多类都使用了CAS技术。
具体实现
    CAS具体包括三个参数：当前内存值V、旧的预期值A、即将更新的值B，
    当且仅当预期值A和内存值V相同时，将内存值修改为B并返回true，否则什么都不做，并返回false。
    CAS 有效地说明了“ 我认为位置 V 应该包含值 A，如果真的包含A值，则将 B 放到这个位置，
    否则，不要更改该位置，只告诉我这个位置现在的值(A)即可。
    整个比较并交换的操作是原子操作。
原子更新的基本类型包括：
    AtomicBoolean：原子更新布尔变量；
    AtomicInteger：原子更新整型变量；
    AtomicLong：原子更新长整型变量；
CAS的缺点：
    1，循环时间长、开销很大。
        当某一方法比如：getAndAddInt执行时，如果CAS失败，会一直进行尝试。如果CAS长时间尝试但是一直不成功，可能会给CPU带来很大的开销。
    2，只能保证一个共享变量的原子操作。
        当操作1个共享变量时，我们可以使用循环CAS的方式来保证原子操作，但是操作多个共享变量时，循环CAS就无法保证操作的原子性，这个时候就需要用锁来保证原子性。
    3，存在ABA问题
        ABA问题是CAS中的一个漏洞。CAS的定义，当且仅当内存值V等于就得预期值A时，CAS才会通过原子方式用新值B来更新V的值，否则不会执行任何操作。
        那么如果先将预期值A给成B，再改回A，那CAS操作就会误认为A的值从来没有被改变过，这时其他线程的CAS操作仍然能够成功，但是很明显是个漏洞，因为预期值A的值变化过了。如何解决这个异常现象？
        java并发包为了解决这个漏洞，提供了一个带有标记的原子引用类“AtomicStampedReference”，它可以通过控制变量值的版本来保证CAS的正确性，
        即在变量前面添加版本号，每次变量更新的时候都把版本号+1，这样变化过程就从“A－B－A”变成了“1A－2B－3A”。



