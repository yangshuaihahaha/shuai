import java.util.ArrayList;

public class GCTest {
    public static void main(String[] args) throws InterruptedException {
        ArrayList<byte[]> list = new ArrayList<>();
        while(true) {
            byte[] arr = new byte[1024];//1kb
            list.add(arr);
            Thread.sleep(1000);
        }
    }
}
