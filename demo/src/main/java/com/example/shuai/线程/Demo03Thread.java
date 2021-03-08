package src.main.java.com.example.shuai.线程;

//如果只对写方法加了锁，而读方法不加锁，也可能造成脏读
public class Demo03Thread {
    String name;

    Double balance;

    public synchronized void  setNameAndBalance(String name, Double balance) {
        this.name = name;

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.balance = balance;
    }

    public Double getBalance(String name) {
        return this.balance;
    }


    public static void main(String[] args) {
        Demo03Thread demo03Thread = new Demo03Thread();
        new Thread(() -> demo03Thread.setNameAndBalance("张三", 100.0)).start();
        System.out.println(demo03Thread.getBalance("张三"));
    }
}
