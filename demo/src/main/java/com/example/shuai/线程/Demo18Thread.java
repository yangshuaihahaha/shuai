package src.main.java.com.example.shuai.线程;

import java.util.EventListener;

//this逃逸
//在一个类的构造器创建了一个内部类（内部类本身是拥有对外部类的所有成员的访问权的），此时外部类的成员变量还没初始化完成。
//但是，同时这个内部类被其他线程获取到，并且调用了内部类可以访问到外部类还没来得及初始化的成员变量的方法。

public class Demo18Thread {
    public final int id;
    public final String name;

    public Demo18Thread(EventSource<EventListener> source) {
        id = 1;
        source.registerListener(new EventListener() {  //内部类是可以直接访问外部类的成员变量的（外部类引用this被内部类获取了）
            public void onEvent(Object obj) {
                System.out.println("id: " + Demo18Thread.this.id);
                System.out.println("name: " + Demo18Thread.this.name);
            }
        });
        name = "flysqrlboy";
    }
}
