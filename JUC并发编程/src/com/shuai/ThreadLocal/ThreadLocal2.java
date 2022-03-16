package com.shuai.ThreadLocal;

//ThreadLocal和Synchronized都是为了解决多线程中相同变量的访问冲突问题，不同的点是
//Synchronized是通过线程等待，牺牲时间来解决访问冲突
//ThreadLocal是通过每个线程单独一份存储空间，牺牲空间来解决冲突，并且相比于Synchronized，ThreadLocal具有线程隔离的效果，只有在线程内才能获取到对应的值，线程外则不能访问到想要的值。

//使用 ThreadLocal 的时候，最好不要声明为静态的；
//使用完 ThreadLocal ，最好手动调用 remove() 方法，例如上面说到的 Session 的例子，如果不在拦截器或过滤器中处理，不仅可能出现内存泄漏问题，而且会影响业务逻辑；

public class ThreadLocal2 {

    public static void main(String[] args) {
        ThreadLocal<Integer> tl = new ThreadLocal<>();

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(tl.get());
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tl.set(1);
        }).start();
    }
}

