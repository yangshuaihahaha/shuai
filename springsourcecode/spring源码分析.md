# 1， spring的初始化过程
- 在容器创建的时候，创建Environment存放系统的相关属性方便后续使用。
- 创建Bean工厂（BeanFactory）并初始化
- BeanDefinitionReader来读取定义好的bean对象信息的xml，解析成Bean的定义信息（BeanDefinition）
- BeanFactoryPostProcessor处理Bean的定义信息（BeanDefinition）
- 注册监听事件
- BeanDefinition传入到BeanFactory，在BeanFactory通过反射的方式实例化bean
- Bean填充属性
- BeanPostProcessor : Before （aop就是在这里实现的）
- 初始化Bean，执行init方法
- BeanPostProcessor : After
- 完成Bean对象


# 2， 循环依赖
首先需要了解两点
- spring是通过递归的方式来获取目标bean以及其依赖的bean
- spring实例化bean的时候是分两步进行的，首先实例化bean，然后添加属性

解决循环依赖是通过spring的三级缓存来解决的
1，Spring尝试通过ApplicationContext.getBean()方法获取A对象的实例，由于Spring容器中还没有A对象实例，因而其会创建一个A对象
2，然后发现其依赖了B对象，因而会尝试递归的通过ApplicationContext.getBean()方法获取B对象的实例，但是Spring容器中此时也没有B对象的实例，因而其还是会先创建一个B对象的实例
3，此时A对象和B对象都已经创建了，并且保存在Spring容器中了，只不过A对象的属性b和B对象的属性a都还没有设置进去
4，在前面Spring创建B对象之后，Spring发现B对象依赖了属性A，因而此时还是会尝试递归的调用ApplicationContext.getBean()方法获取A对象的实例，因为Spring中已经有一个A对象的实例，虽然只是半成品（其属性b还未初始化），但其也还是目标bean，因而会将该A对象的实例返回此时，B对象的属性a就设置进去了
5，然后还是ApplicationContext.getBean()方法递归的返回，也就是将B对象的实例返回，此时就会将该实例设置到A对象的属性b中。

# 3， FactoryBean和beanFactory区别
BeanFactory是个Factory，是ioc 容器最底层的接口，负责对bean的创建，spring用来管理和装配普通bean的ioc容器
FactoryBean是个bean，是一个可以生产对象和装饰对象的工厂bean

# 4， Environment
指的是spring所处的应用环境，而spring的应用环境主要分为两方面：
# 4.1，profiles
配置是一个被命名的，bean定义的逻辑组，这些bean只有在给定的profile配置激活时才会注册到容器。不管是XML还是注解，Beans都有可能指派给profile配置。Environment环境对象的作用，对于profiles配置来说，它能决定当前激活的是哪个profile配置，和哪个profile是默认。

bean定义profiles是核心容器内的一种机制，该机制能在不同环境中注册不同的bean。环境的意思是，为不同的用户做不同的事儿，该功能在很多场景中都非常有用，包括：开发期使用内存数据源，在QA或者产品上则使用来自JNDI的相同的数据源开发期使用监控组件，当部署以后则关闭监控组件，是应用更高效为用户各自注册自定义bean实现.

考虑一个实际应用中的场景，现在需要一个DataSource。开测试环境中，这样配置:

```
@Bean
public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.HSQL)
        .addScript("my-schema.sql")
        .addScript("my-test-data.sql")
        .build();
}
```
现在想一想，如何将应用部署到QA或者生产环境，假设生产环境中使用的JNDI。我们的dataSource bean看起来像这样:

```
@Bean(destroyMethod="")
public DataSource dataSource() throws Exception {
    Context ctx = new InitialContext();
    return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
}
```
我们需要在某些上下文环境中使用某些bean，在其他环境中则不用这些bean。你也许会说，你需要在场景A中注册一组bean定义，在场景B中注册另外一组。先看看我们如何修改配置来完成此需求。
@Profile注解的作用，是在一个或者多个指定profiles激活的情况下，注册某个组件。使用上面的样例，重写dataSource配置:
```$xslt
@Configuration
@Profile("dev")
public class StandaloneDataConfig {
 
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("classpath:com/bank/config/sql/schema.sql")
            .addScript("classpath:com/bank/config/sql/test-data.sql")
            .build();
    }
}
@Configuration
@Profile("production")
public class JndiDataConfig {
 
    @Bean(destroyMethod="")
    public DataSource dataSource() throws Exception {
        Context ctx = new InitialContext();
        return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
    }
}
```
开启profile

要修改配置，我们仍然需要指定要激活哪个文件。如果现在运行上面的样例应用，它会抛异常NoSuchBeanDefinitionException,因为容器找不到dataSourcebean。

有多种方式激活配置，但是最直接的方式是编程式的方式使用ApplicationContext API
```$xslt
AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
ctx.getEnvironment().setActiveProfiles("dev");
ctx.register(SomeConfig.class, StandaloneDataConfig.class, JndiDataConfig.class);
ctx.refresh();
```
此外，还可以使用spring.profiles.active激活配置，该属性可以配置在系统环境变量、JVM系统属性、web.xml
中JNDI中的servlet context上下文参数
注意配置文件不是单选；可能会同时激活多个配置文件，编程式的使用方法setActiveProfiles()，该方法接收String数组参数,也就是多个配置文件名

```$xslt
ctx.getEnvironment().setActiveProfiles("profile1", "profile2");//声明式的使用spring.profiles.active ，值可以为逗号分隔的配置文件名列表, -Dspring.profiles.active="profile1,profile2"

```
如果没有任何profile配置被激活，默认的profile将会激活。
# 4.2，properties
properties属性可能来源于properties文件、JVM properties、system环境变量、JNDI、servlet context parameters上下文参数、专门的properties对象，Maps等等。Environment对象的作用，对于properties来说，是提供给用户方便的服务接口、方便撰写配置、方便解析配置。

配置属性源。
从属性源中获取属性。
容器（ApplicationContext）所管理的bean如果想直接使用Environment对象访问profile状态或者获取属性，可以有两种方式
（1）实现EnvironmentAware接口。
（2）@Inject或者@Autowired一个Environment对象。
绝大数情况下，bean都不需要直接访问Environment对象，而是通过类似@Value注解的方式把属性值注入进来。
```$xslt
@Autowired
private Environment environment;
environment.getProperty("rabbitmq.address")
获取是否使用profile的
public boolean isDev(){
    boolean devFlag = environment.acceptsProfiles("dev");
    return  devFlag;
}
```





# 5，  Spring bean的生命周期
总结：

**（1）实例化Bean：**

对于BeanFactory容器，当客户向容器请求一个尚未初始化的bean时，或初始化bean的时候需要注入另一个尚未初始化的依赖时，容器就会调用createBean进行实例化。对于ApplicationContext容器，当容器启动结束后，通过获取BeanDefinition对象中的信息，实例化所有的bean。

**（2）设置对象属性（依赖注入）：**

实例化后的对象被封装在BeanWrapper对象中，紧接着，Spring根据BeanDefinition中的信息 以及 通过BeanWrapper提供的设置属性的接口完成依赖注入。

**（3）处理Aware接口：**

接着，Spring会检测该对象是否实现了xxxAware接口，并将相关的xxxAware实例注入给Bean：

**（4）BeanPostProcessor：**

如果想对Bean进行一些自定义的处理，那么可以让Bean实现了BeanPostProcessor接口，那将会调用postProcessBeforeInitialization(Object obj, String s)方法。

**（5）InitializingBean 与 init-method：**

如果Bean在Spring配置文件中配置了 init-method 属性，则会自动调用其配置的初始化方法。

**（6）如果这个Bean实现了BeanPostProcessor接口**，将会调用postProcessAfterInitialization(Object obj, String s)方法；由于这个方法是在Bean初始化结束时调用的，所以可以被应用于内存或缓存技术；

以上几个步骤完成后，Bean就已经被正确创建了，之后就可以使用这个Bean了。

**（7）DisposableBean：**

当Bean不再需要时，会经过清理阶段，如果Bean实现了DisposableBean这个接口，会调用其实现的destroy()方法；

**（8）destroy-method：**

最后，如果这个Bean的Spring配置中配置了destroy-method属性，会自动调用其配置的销毁方法。

# 6，Spring aware接口
感知到自身的一些属性，比如
实现了ApplicationContextAware接口的类，能够获取到ApplicationContext，
实现了BeanFactoryAware接口的类，能够获取到BeanFactory对象。

①如果这个Bean已经实现了BeanNameAware接口，会调用它实现的setBeanName(String beanId)方法，此处传递的就是Spring配置文件中Bean的id值；

②如果这个Bean已经实现了BeanFactoryAware接口，会调用它实现的setBeanFactory()方法，传递的是Spring工厂自身。

③如果这个Bean已经实现了ApplicationContextAware接口，会调用setApplicationContext(ApplicationContext)方法，传入Spring上下文；
```$xslt
package com.github.jettyrun.springinterface.demo.aware.beannameaware;

import org.springframework.beans.factory.BeanNameAware;

/**
 * Created by jetty on 18/1/31.
 */
public class User implements BeanNameAware{

    private String id;

    private String name;

    private String address;

    //实现了BeanNameAware接口，能够获取到Spring配置文件中Bean的id值
    public void setBeanName(String beanName) {
        id=beanName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
```
# 7， 三级缓存

# 8， ApplicationContext和BeanFactory区别

# 9， 设计模式


