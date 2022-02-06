import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

interface UserMapper {
    List<Object> selectUserList();
}
public class ProxyTest {
    public static void main(String[] args) throws Throwable {
        UserMapper userMapper = (UserMapper) Proxy.newProxyInstance(
                ProxyTest.class.getClassLoader(),/*类加载器*/
                new Class<?>[]{UserMapper.class},/*让代理对象和目标对象实现相同接口*/
                new InvocationHandler(){/*代理对象的方法最终都会被JVM导向它的invoke方法*/
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //这里执行目标方法
                        Object result = method.invoke(proxy, args);
                        return result;
                    }
                }
        );
    }
}