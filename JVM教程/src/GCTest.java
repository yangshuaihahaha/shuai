import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class GCTest {
    public static void main(String[] args) throws InterruptedException {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);
        list.add(7);
        list.add(8);
        list.add(9);


        list.add(4, 0);
        System.out.println(list);

        LinkedList<Integer> linkedList = new LinkedList<>();
        linkedList.add(1);
        linkedList.add(2);
        linkedList.add(3);
        linkedList.add(4);
        linkedList.add(5);
        linkedList.add(3, 99);
        linkedList.get(3);
        System.out.println(linkedList);



        HashSet<Integer> hashSet = new HashSet<>();
        hashSet.add(2);

        TreeSet<Integer> treeSet = new TreeSet<>();
        LinkedHashSet<Integer> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add(1);

        TreeMap<Integer, String> treeMap = new TreeMap<>();
        treeMap.put(1, "");

        HashMap<Integer, String> hashMap = new HashMap<>();
        hashMap.put(1, "");


        Hashtable<Integer, String> hashtable = new Hashtable<>();
        hashtable.put(1, "");

        CallableTest callableTest = new CallableTest();
        FutureTask<Integer> futureTask = new FutureTask<Integer>(callableTest);



//        while(true) {
//            byte[] arr = new byte[1024];//1kb
//            list.add(arr);
//            Thread.sleep(1000);
//        }
    }
}

class CallableTest implements Callable {
    @Override
    public Object call() throws Exception {
        int i = 1000;
        for ( ; i < 1010; i++) {
            System.out.println(i);
        }
        return 1111;
    }
}
