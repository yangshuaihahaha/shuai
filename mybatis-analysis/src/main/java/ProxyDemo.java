import java.lang.reflect.Method;

interface Test {
    public void say();
}

interface InvokeHandler {
    Object invoke(Object obj, Method method, Object... arg);
}

public class ProxyDemo {
    public static void main(String[] args) {

    }

    public Test createProxyInstance(final InvokeHandler handler, final Class<?> clazz) {
        return new Test() {
            @Override
            public void say() {
                try {
                    Method sayMethod = clazz.getMethod("say");
                    handler.invoke(this, sayMethod);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}