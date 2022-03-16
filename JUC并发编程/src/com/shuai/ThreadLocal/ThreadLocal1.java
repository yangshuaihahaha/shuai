package com.shuai.ThreadLocal;

public class ThreadLocal1 {
    volatile static Person p1 = new Person();

    static ThreadLocal<Person> tl = new ThreadLocal<>();

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                System.out.println(p1.name);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            p1.name = "lisi";
        }).start();
    }
}

class Person {
    String name = "zhangsan";
}
