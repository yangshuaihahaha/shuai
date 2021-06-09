package src.main.java.com.example.shuai.线程;

import java.util.concurrent.TimeUnit;

//锁定某对象o，如果o的属性发生改变，不影响锁的使用
//但是如果o变成另一个对象，则锁定的对象发生改变，
//应避免将锁定对象的引用变成另外一个对象
public class Demo11Thread {

    static Object o = new Object();

    public void m() {
        synchronized (o) {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName());
            }
        }
    }

    public static void main(String[] args) {
        Demo11Thread demo11Thread = new Demo11Thread();
        new Thread(demo11Thread::m, "T1").start();
        new Thread(demo11Thread::m, "T2").start();
        o = new Object();//锁定对象发生改变T2得以执行，如果注释掉这句话，T2将永远得不到执行
    }
}
