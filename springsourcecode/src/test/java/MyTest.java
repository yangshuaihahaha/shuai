import model.Name;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.StandardEnvironment;

public class MyTest {
    public static void main(String[] args) {
        //创建Spring上下文（加载bean.xml）
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
        Name name = (Name) context.getBean("name");
        System.out.println(name.getName());
    }
}
