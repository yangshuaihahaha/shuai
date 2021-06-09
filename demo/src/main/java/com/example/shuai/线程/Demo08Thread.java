package src.main.java.com.example.shuai.线程;

//volatile作用是一旦值改变后提醒其他线程缓存过期通知
//如果不加volatile，runing这个值在线程的缓冲区中一直存在，不会去主内存中读取刷新后的值
//Thread.sleep(10);貌似起作用了但是原因是sleep的时候cup空闲了所以去主线程中刷新了一下缓存
public class Demo08Thread {

    static Boolean runing = true;

    public void m1() {
        System.out.println(Thread.currentThread().getName() + "start");
        int i = 0;
        while (runing) {
            i++;
            System.out.println(runing.toString() + i);
        }
        System.out.println(Thread.currentThread().getName() + "end");
    }


    public static void main(String[] args) throws InterruptedException {
        Demo08Thread demo08Thread = new Demo08Thread();
        new Thread(demo08Thread::m1, "线程1").start();
        Thread.sleep(1000);
        runing = false;
    }
}
