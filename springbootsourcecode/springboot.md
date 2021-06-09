# spring boot的自动装配
spring boot的自动装配是通过@EnableAutoConfiguration来进行开启

在refreshContext的时候，也就是BeanFactoryPostProcessors阶段

注解@EnableAutoConfiguration中有一个AutoConfigurationImportSelector类

这个类会搜寻所有的META-INF/spring.factories配置文件

然后将这些文件中的配置项通过反射实例化为配置类，并加载到ioc容器中

当然，这些配置类想生效的话，pom.xml必须有对应功能的jar包才行，并且配置类中注入了默认的属性值

还有就是自定义优先，没有自定义类时才会使用自动装配的类。

# 什么是 Spring Boot Stater 
是一套方便的依赖，它可以放在程序中，一站式的获取所需的spring以及相关的技术
例如，如果你想使用 Sping 和 JPA 访问数据库，只需要你的项目包含 spring-boot-starter-data-jpa 依赖项，你就可以完美进行。

# spring boot如何定义自己的starter

# spring boot注解
## @Configuration  
标记这个类为注解类==配置文件
- @Bean
作用于方法或者注解类上，表示在config配置类中注入相应的bean类。@Bean给容器注入bean时候，类型为方法的返回类型，默认id是方法名，但是可以通过别名来改变默认id，比如上面例子可以改为@Bean("user1")。
```
@Configuration//标记这个类为注解类==配置文件
public class UserConfig {
    /*
     * @Bean代表给容器中注入一个Bean，类型为返回值得类型，id默认是方法名
     * id也可以自己定义比如@Bean("user1")
     */
    @Bean
    public User user() {
        User user=new User();
        user.setUserName("qiuzhangwei");
        user.setPassword("123456");
        user.setAge(55);
        user.setBrithday(new Date());
        return user;
    }
}
```
## @ComponentScan
自动扫描符合条件的bean，并把这些bean加载到容器中

## @EnableAutoConfiguration
开启spring boot自动装配

## @Scope
springIoc容器中的一个作用域
在 Spring IoC 容器中具有以下几种作用域：
- singleton单例模式(默认):全局有且仅有一个实例
- prototype原型模式:每次获取Bean的时候会有一个新的实例
- request: request表示该针对每一次HTTP请求都会产生一个新的bean，同时该bean仅在当前HTTP request内有效
- session :session作用域表示该针对每一次HTTP请求都会产生一个新的bean，同时该bean仅在当前HTTP session内有效
- global session : global session作用域类似于标准的HTTP Session作用域，不过它仅仅在基于portlet的web应用中才有意义

## @Lazy bean
标识bean是否需要延迟加载，默认是true

## @Conditional
代码中设置的条件装载不同的bean
需求：按照当前操作系统来注入相应的Bean，如果系统是windows，给容器中注册("bill")，如果是linux系统，给容器中注册("linus")
```
1、判断是否是linux系统
public class LinuxCondition implements Condition{
    /**
     * ConditionContext：判断条件能使用的上下文（环境）
     * AnnotatedTypeMetadata：注释信息
     */
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) 
{
        //1.能获取到ioc使用的Beanfactory
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        //2、获取到类加载器
        ClassLoader classLoader = context.getClassLoader();
        //3.获取到当前环境信息
        Environment environment = context.getEnvironment();
        //4.获取到bean定义的注册类信息
        BeanDefinitionRegistry registry = context.getRegistry();
        //=============这里主要是为了获取当前系统的环境变脸
        String property=environment.getProperty("os.name");
        if (property.contains("linux")) {
            return true;//放行
        }
        return false;
    }

}

2、判断是否是windows系统
public class WindowsCondition implements Condition{
    /**
     * ConditionContext：判断条件能使用的上下文（环境）
     * AnnotatedTypeMetadata：注释信息
     */
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata)
 {
        Environment environment = context.getEnvironment();
        String property=environment.getProperty("os.name");
        if (property.contains("Windows")) {
            return true;//放行
        }
        return false;
    }
}
3、@Conditional:按照一定的逻辑进行判断，满足条件给容器注入bean
public class ConditionalConfig {
//注入windows
    @Conditional(value= {WindowsCondition.class})
    @Bean
    public User user1() {
        User user=new User();
        user.setUserName("bill");
        return user;
    }
//注入linux
    @Conditional(value= {LinuxCondition.class})
    @Bean
    public User user2() {
        User user=new User();
        user.setUserName("linus");
        return user;
    }
4、idea中更换操作系统方法：-Dos.name=linux
```

## @Import
@Import通过快速导入的方式实现把实例加入spring的IOC容器中
加入IOC容器的方式有很多种，@Import注解就相对很牛皮了，@Import注解可以用于导入第三方包
@Import的三种用法:
### 第一种用法：直接填class数组
```
@Import({ 类名.class , 类名.class... })
public class TestDemo {

}
```
### ImportSelector方式
这种方式的前提就是一个类要实现ImportSelector接口
```
public class Myclass implements ImportSelector {
//既然是接口肯定要实现这个接口的方法
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        return new String[]{"com.yc.Test.TestDemo3"};
    }
}
```
分析实现接口的selectImports方法中的：
- 返回值： 就是我们实际上要导入到容器中的组件全类名【重点 】
- 参数： AnnotationMetadata表示当前被@Import注解给标注的所有注解信息【不是重点】

### 第三种用法：ImportBeanDefinitionRegistrar方式
类似于第二种ImportSelector用法，相似度80%，只不过这种用法比较自定义化注册，具体如下：
```
public class Myclass2 implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        //指定bean定义信息（包括bean的类型、作用域...）
        RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(TestDemo4.class);
        //注册一个bean指定bean名字（id）
        beanDefinitionRegistry.registerBeanDefinition("TestDemo4444",rootBeanDefinition);
    }
}
```
- 第一个参数：annotationMetadata 和之前的ImportSelector参数一样都是表示当前被@Import注解给标注的所有注解信息
- 第二个参数表示用于注册定义一个bean

## @Bean
用于注册Bean的注解： 比如@Component , @Repository , @Controller , @Service , @Configration

### 为什么要有@Bean注解？
类似@Component , @Repository , @ Controller , @Service 这些注册Bean的注解存在局限性，只能局限作用于自己编写的类，如果是一个jar包第三方库要加入IOC容器的话，这些注解就手无缚鸡之力了，是的，@Bean注解就可以做到这一点！

### @Bean注解总结
1、Spring的@Bean注解用于告诉方法，产生一个Bean对象，然后这个Bean对象交给Spring管理。 产生这个Bean对象的方法Spring只会调用一次，随后这个Spring将会将这个Bean对象放在自己的IOC容器中。

2、@Component , @Repository , @ Controller , @Service 这些注解只局限于自己编写的类，而@Bean注解能把第三方库中的类实例加入IOC容器中并交给spring管理。

3、@Bean注解的另一个好处就是能够动态获取一个Bean对象，能够根据环境不同得到不同的Bean对象。

4、、记住，@Bean就放在方法上，就是让方法去产生一个Bean，然后交给Spring容器，剩下的你就别管了。


## @ResponseBody
示该方法的返回结果直接写入HTTP response body中

## @Controller
用于标注控制层组件

## @RestController
用于标注控制层组件，@ResponseBody和@Controller的合集

## @RequestMapping
提供路由信息，负责URL到Controller中的具体函数的映射。

## @ImportResource
用来加载xml配置文件。

## @Service
一般用于修饰service层的组件

## @Repository
使用@Repository注解可以确保DAO或者repositories提供异常转译，这个注解修饰的DAO或者repositories类会被ComponetScan发现并配置，同时也不需要为它们提供XML配置项。


## @Value
注入Spring boot application.properties配置的属性的值。
```
@Service
public class HelloWorldServiceImpl implements IHelloWorldService {

    @Autowired
    private MyConfig myConfig;

    @Value(value = "${user.userName}")
    private String userName;
    @Value("${user.sex}")
    private String sex;
    @Value("${user.age}")
    private String age;

    @Override
    public String getMessage() {
        return "姓名："+userName+" 性别："+sex+" 年龄："+ age +" 作者&身高："+myConfig.getName()+" "+myConfig.getHeight();
    }
}
```

## @Inject
等价于默认的@Autowired，只是没有required属性；

## @Component
泛指组件，当组件不好归类的时候，我们可以使用这个注解进行标注。

## @Qualifier
当有多个同一类型的Bean时，可以用@Qualifier(“name”)来指定。与@Autowired配合使用。@Qualifier限定描述符除了能根据名字进行注入，但能进行更细粒度的控制如何选择候选者，具体使用方式如下：
@Resource(name=”name”,type=”type”)：没有括号内内容的话，默认byName。与@Autowired干类似的事。

## @Order
和注解 @Componerent一起使用，如果涉及到多个，用@Order()注解去设置执行顺序

为什么要使用CommandLineRunner
- 实现在应用启动后，去执行相关代码逻辑，且只会执行一次
- spring batch批量处理框架依赖这些执行器去触发执行任务
- 可以使用依赖，bean对象，因为它们已经初始化好了

```
@Component
@Order(value = 1)
public class CLR1 implements CommandLineRunner{
public void run(String... strings) throws Exception {
     //do something
   }
}

@Component
@Order(value = 2)
public class CLR2 implements CommandLineRunner{
public void run(String... strings) throws Exception {
     //do something
   }
}
```

## @JsonBackReference, @JsonManagedReference, @JsonIgnore
@JsonBackReference和@JsonManagedReference这两个标注通常配对使用，通常用在父子关系中。
@JsonBackReference标注的属性在序列化（serialization，即将对象转换为json数据）时，会被忽略（即结果中的json数据不包含该属性的内容）。
@JsonManagedReference标注的属性则会被序列化。在序列化时，@JsonBackReference的作用相当于@JsonIgnore，此时可以没有@JsonManagedReference。
但在反序列化（deserialization，即json数据转换为对象）时，如果没有@JsonManagedReference，则不会自动注入@JsonBackReference标注的属性（被忽略的父或子）.
如果有@JsonManagedReference，则会自动注入自动注入@JsonBackReference标注的属性。   
@JsonIgnore：直接忽略某个属性，以断开无限递归，序列化或反序列化均忽略。


## @RepositoryRestResource
作用：设置rest请求路径。通过匹配路径中的参数完成对数据库的访问。配合JPA使用

## JPA注解
@Entity：@Table(name=”“)：表明这是一个实体类。一般用于jpa这两个注解一般一块使用，但是如果表名和实体类名相同的话，@Table可以省略
@MappedSuperClass:用在确定是父类的entity上。父类的属性子类可以继承。
@NoRepositoryBean:一般用作父类的repository，有这个注解，spring不会去实例化该repository。

@Column：如果字段名与列名相同，则可以省略。

@Id：表示该属性为主键。

@GeneratedValue(strategy = GenerationType.SEQUENCE,generator = “repair_seq”)：表示主键生成策略是sequence（可以为Auto、IDENTITY、native等，Auto表示可在多个数据库间切换），指定sequence的名字是repair_seq。

@SequenceGeneretor(name = “repair_seq”, sequenceName = “seq_repair”, allocationSize = 1)：name为sequence的名称，以便使用，sequenceName为数据库的sequence名称，两个名称可以一致。

@Transient：表示该属性并非一个到数据库表的字段的映射,ORM框架将忽略该属性。如果一个属性并非数据库表的字段映射,就务必将其标示为@Transient,否则,ORM框架默认其注解为@Basic。@Basic(fetch=FetchType.LAZY)：标记可以指定实体属性的加载方式

@JsonIgnore：作用是json序列化时将Java bean中的一些属性忽略掉,序列化和反序列化都受影响。

@JoinColumn（name=”loginId”）:一对一：本表中指向另一个表的外键。一对多：另一个表指向本表的外键。

@OneToOne、@OneToMany、@ManyToOne：对应hibernate配置文件中的一对一，一对多，多对一。

## @ControllerAdvice
包含@Component。可以被扫描到。统一处理异常。
## @ExceptionHandler（Exception.class）
用在方法上面表示遇到这个异常就执行以下方法。

# RequestMapping 和 GetMapping 的不同之处在哪里？
- RequestMapping 具有类属性的，可以进行 GET,POST,PUT 或者其它的注释中具有的请求方法。
- GetMapping 是 GET 请求方法中的一个特例。它只是 ResquestMapping 的一个延伸，目的是为了提高清晰度。

# springboot
react响应式编程，以及web flux




