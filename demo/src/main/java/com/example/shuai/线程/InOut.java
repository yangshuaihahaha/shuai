package src.main.java.com.example.shuai.线程;

public class InOut {
    String str = new String("Between");

    public void amethod(final int iArgs) {
        int it315 = 10;
        class Bicycle {
            public void sayHello() {
                System.out.println(str);
                System.out.println(iArgs);
                System.out.println(it315);//此处编译出错：InOut.java:13: local variable it315 is accessed from within inner class; needs to be declared final
            }
        }
    }
}
