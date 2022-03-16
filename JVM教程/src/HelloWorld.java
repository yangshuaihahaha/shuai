public class HelloWorld {
    public static void main(String[] args) {
        while (true) {
            sayHello();
        }
    }

    public static void sayHello() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Hello World");
    }
}
