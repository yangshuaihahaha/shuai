package src.main.java.com.example.shuai.线程;

//重入锁之子类方法调用父类的同步方法
public class Demo06Thread {

    public synchronized void m1() {
        System.out.println("父类中的m1");
    }

    public static void main(String[] args) {
        Demo06Thread demo06Thread = new Demo06Thread_1();
        demo06Thread.m1();
    }
}

class Demo06Thread_1 extends Demo06Thread {
    public synchronized void m1() {
        System.out.println("子类中的m1");
        super.m1();
    }
}
