package src.main.java.com.example.shuai.线程池;

import java.util.concurrent.Executor;
//基本用法
public class DemoExecutor implements Executor {

    public static void main(String[] args) {
        new DemoExecutor().execute(() -> System.out.println("Hello Executor"));
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
