# spring的初始化过程
- 在容器创建的时候，创建Environment存放系统的相关属性方便后续使用。
- 然后创建BeanFactory并初始化
- 接下来使用BeanDefinitionReader来读取定义好的bean对象信息，然后解析成BeanDefinition
- 然后通过Bean工厂后置处理器BeanFactoryPostProcessor处理BeanDefinition，
    1，对使用到占位符的元素标签进行解析，得到最终匹配值
    2，通过Java反射机制找出所有属性编辑器的Bean（实现java.beans.PropertyEditor接口的Bean），并自动将它们注册到Spring容器的属性编辑器注册表中
- 接下来会注册监听事件
- 然后把BeanDefinition传入到BeanFactory，在BeanFactory通过反射的方式实例化bean
    1，推断构造方法，实例化bean
    2，Bean属性注入，依赖注入
- 接下来就是初始化过程
    1，调用初始化前方法：@PostContruct
    2，初始化Bean：InitializingBean的afterPropertiesSet，
- 调用BeanPostProcessor初始化后方法（AOP），生成一个代理对象
- 把代理对象放入单例池

# 什么是单例池？作用是什么？
单例池就是一个CurrentHashMap，key就是bean的名字，value就是bean对象
如果是一个多例bean（每次返回不同的对象），那么就不需要存放在单例池里面了

# @PostContruct主要做了什么？
会在bean初始化前执行一次，比如获取数据库连接

# Bean的实例化和初始化有什么区别
bean实例化就是通过无参构造方法得到一个对象
bean的初始化就是执行你所实现的afterPropertiesSet方法

# 什么是Spring
什么是Spring Framework？
    Spring是一个轻量级的IoC和AOP框架，它提供了非常多的组件，让我们开发Java应用程序的过程变得更加容易，
    同时弹性的支持其他软件框架，其他软件很简单的就能和Spring结合在一起使用，
    在整个 Spring 应用上下文的生命周期和 Spring Bean 的生命周期的许多阶段提供了相应的扩展点，供开发者自行扩展，使得框架非常的灵活。
    主要包含7个模块：
        Spring Core：核心模块，所有功能都依赖该类库，提供了IOC和DI服务
        Spring Context：上下文，提供框架式的Bean访问方式，以及企业级功能（JNDI、定时任务等）；
        Spring AOP：AOP服务
        Spring DAO：对JDBC的抽象封装，简化了数据访问异常的处理，并能统一管理JDBC事务；
        Spring Web：提供了基本的面向Web的综合特性，提供对常见框架如Struts2的支持，Spring能够管理这些框架，将Spring的资源注入给框架，也能在这些框架的前后插入拦截器；
        Spring MVC：提供面向Web应用的Model-View-Controller，即MVC实现。
        Spring ORM：对现有的ORM框架的支持；
对IoC的理解
    控制反转是一种编程思想。
    在传统方式中，当依赖一个对象，那么就需要主动去创建他并进行赋值，然后才能使用
    对于IoC来说，我们不需要关心对象的创建、属性的赋值，这些工作都是由IoC容器来完成
    简单来说就是把获取依赖对象的方式，交给IoC容器来实现，由主动拉去变为被动拉去
IoC和DI的区别
    依赖注入并不等同于控制反转，应该说依赖注入是控制反转的一种实现方式
    依赖查找个依赖注入都是IoC的实现策略。
    依赖查找就是在应用程序里面主动调用 IoC 容器提供的接口去获取对应的 Bean 对象
    而依赖注入是在 IoC 容器启动或者初始化的时候，通过构造器注入、setter 方法注入或者接口注入等方式注入依赖
    依赖查找相比于依赖注入对于开发者而言更加繁琐，具有一定的代码入侵性，需要借助 IoC 容器提供的接口，所以我们总是强调后者。
    依赖注入在 IoC 容器中的实现也是调用相关的接口获取 Bean 对象，只不过这些工作都是在 IoC 容器启动时由容器帮你实现了，在应用程序中我们通常很少主动去调用接口获取 Bean 对象。
IoC容器职责
    依赖处理，通过依赖查找或者依赖注入
    管理托管的资源（Java Bean 或其他资源）的生命周期
    管理配置（容器配置、外部化配置、托管的资源的配置）
什么是Spring IoC？
    Spring是IoC容器的实现，DI依赖注入是它的一个实现原则，提供依赖查找和依赖注入两种依赖处理
    管理者Bean的生命周期
BeanFactory和ApplicationContext谁才是Spring IoC容器？
    BeanFactory是Spring底层IoC容器，ApplicationContext是BeanFactory的子接口，是BeanFactory的一个超集
    提供除了IoC容器以外的更多功能。ApplicationContext除了扮演IoC容器角色，还提供了这些企业特性：
        **面向切面**、配置元信息、资源管理、**事件机制**、**国际化**、注解、**Environment抽象**等
    我们一般称ApplicationContext是Spring的应用上下文，BeanFactory是Sprig底层IoC容器
以Aware结尾的类是干嘛用的？
    Aware翻译过来是感知的意思，是Spring Bean生命周期的各个阶段留下来的回调接口
    我们可以通过实现这些Aware接口来访问Spring容器
    比如：
        1，BeanNameAware ：可以获取容器中bean的名称
            void setBeanName(String name);
        2）BeanFactoryAware : 获取当前bean factory这也可以调用容器的服务
            void setBeanFactory(BeanFactory beanFactory)
            throws BeansException;
        3）ApplicationContextAware： 当前的applicationContext， 这也可以调用容器的服务
            void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException;
        4） MessageSourceAware：获得message source，这也可以获得文本信息
            void setMessageSource(MessageSource messageSource);
        5）ResourceLoaderAware： 获得资源加载器，可以获得外部资源文件的内容
            void setResourceLoader(ResourceLoader resourceLoader);
    这些实现 Aware 接口的xxxAware类是如何实现感应并设置xxx属性的值呢？
        在spring容器中，工厂类创建实例后使用instanceof判断实例是否属于 xxxAware 接口的实例
        如果是的话，那么 spring 容器类就会调用实例的 setXxx() 方法给实例的 xxx 属性设置值。
Spring中的Hook钩子函数
    Spring作为优秀的企业级框架，在设计上针对Bean的生命周期各个阶段预留需要了许多hook，
    通过这些hook可以帮助快速实现自定义的框架设计，动态加载Bean，管理容器等功能，提供了快速扩展的能力
    比如：
        1，Aware接口族
            Spring中提供了各种Aware接口，方便从上下文中获取当前的运行环境，比较常见的几个子接口有：
            BeanFactoryAware,BeanNameAware,ApplicationContextAware,EnvironmentAware，BeanClassLoaderAware等
        2，InitializingBean接口和DisposableBean接口
            InitializingBean接口
                只有一个方法#afterPropertiesSet，作用是：当一个Bean实现InitializingBean，#afterPropertiesSet方法里面可以添加自定义的初始化方法或者做一些资源初始化操作
            DisposableBean接口
                只有一个方法#destroy，作用是：当一个单例Bean实现DisposableBean，#destroy可以添加自定义的一些销毁方法或者资源释放操作
            ```java
            @Component
            public class ConcreteBean implements InitializingBean,DisposableBean {
                @Override
                public void destroy() throws Exception {
                    System.out.println("释放资源");
                }
                @Override
                public void afterPropertiesSet() throws Exception {
                    System.out.println("初始化资源");
                }
            }
            ```
        3，ImportBeanDefinitionRegistrar接口
            ImportBeanDefinitionRegistrar接口提供了一个动态注入bean的方法，所以我们可以通过实现这个接口，然后自己手动注入bean
            使用的时候需要结合@Import(DkRegister.class)来使用
            ```java
            //我们自己实现一个ImportBeanDefinitionRegistrar：相信解释请看代码注释
            /**
             * @author 戴着假发的程序员
             * @company http://www.boxuewa.com
             * @description  我们自己实现的ImportBeanDefinitionRegistrar
             */
            public class DkRegister implements ImportBeanDefinitionRegistrar {
                /**
                 * @param annotationMetadata 当前类的注解信息
                 * @param beanDefinitionRegistry 实现BeanDefinitionRegistry接口的对象，用于创建scanner对象，scanner对象可以扫描我们的类
                 **/
                @Override
                public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
                    //创建ClassPathBeanDefinitionScanner对象，用于扫描我们的类，有两个参数
                    //参数1：实现BeanDefinitionRegistry接口的对象
                    //参数2：是否使用默认的类扫描过滤器，所谓默认类扫描过滤器就是指扫描spring的标准注解（@Component，@Controller，@Service,@Repository）我们这里就让spring扫描默认的注解
                    ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(beanDefinitionRegistry,true);
                    //扫描对应的包，找到有标准注解的类，加载并且注册到spring中。
                    scanner.scan("com.sp.beans");
                }
            }
            //配置类：
            /**
             * @author 戴着假发的程序员
             * @company http://www.boxuewa.com
             * @description
             */
            @Configuration
            @Import(DkRegister.class)//使用Import引入我们自己定义的类
            public class Appconfig {
            }
            //使用
            @Test
            public void tesSpringImport(){
                ApplicationContext ac = new AnnotationConfigApplicationContext(Appconfig.class);
            }
            ```
        4，BeanPostProcessor接口和BeanFactoryPostProcessor接口
            一般我们叫这两个接口为Spring的Bean后置处理器接口,作用是为Bean的初始化前后提供可扩展的空间
            BeanFactoryPostProcessor
                可以对bean的定义（配置元数据）进行处理。也就是说，Spring IoC容器允许BeanFactoryPostProcessor在容器实际实例化任何其它的bean之前读取配置元数据，并有可能修改它。
                如果你愿意，你可以配置多个BeanFactoryPostProcessor。你还能通过设置'order'属性来控制BeanFactoryPostProcessor的执行次序
            BeanPostProcessor
                接口可以在Bean(实例化之后)初始化的前后做一些自定义的操作，但是拿到的参数只有BeanDefinition实例和BeanDefinition的名称，也就是无法修改BeanDefinition元数据
                这里说的Bean的初始化是：
                    bean实现了InitializingBean接口，对应的方法为afterPropertiesSet
                    在bean定义的时候，通过init-method设置的方法
        5，BeanDefinitionRegistryPostProcessor 接口
            BeanDefinitionRegistryPostProcessor 接口可以看作是BeanFactoryPostProcessor和ImportBeanDefinitionRegistrar的功能集合，既可以获取和修改BeanDefinition的元数据，也可以实现BeanDefinition的注册、移除等操作。
        6，FactoryBean接口
            Spring通过反射机制利用bean的class属性指定实现类来实例化bean ，实例化bean过程比较复杂
            FactoryBean接口就是为了简化此过程，把bean的实例化定制逻辑下发给使用者。
        7，ApplicationListener
            ApplicationListener是一个接口，里面只有一个onApplicationEvent(E event)方法，这个泛型E必须是ApplicationEvent的子类
            而ApplicationEvent是Spring定义的事件，继承于EventObject，构造要求必须传入一个Object类型的source，这个source可以作为一个存储对象。将会在ApplicationListener的onApplicationEvent里面得到回调
            如果在上下文中部署一个实现了ApplicationListener接口的bean，那么每当在一个ApplicationEvent发布到 ApplicationContext时，这个bean得到通知。其实这就是标准的Oberver设计模式。
Spring Bean的生命周期？
    1，Bean元信息配置阶段
        可以通过面向资源（xml或properties）、面向注解、面向API进行配置
    2，Bean元信息解析阶段
        对上一步的元信息进行解析，解析成一个BeanDefinition对象，该对象包含定义Bean的所有信息，用于实例化一个Bean
    3，Bean元信息注册阶段
        将BeanDefinition配置信息保存至BeanDefinitionRegistry的CurrentHashMap
    4，BeanDefinition合并阶段
        定义的Bean可能存在层次关系，需要将它进行合并，存在相同配置则覆盖父属性，最终生成一个RootBeanDefinition对象
    5，Bean的实例化阶段
        首先通过类加载器加载出一个Class对象，通过这个Class对象的构造器创建一个实例对象，
        构造器注入会在此处完成。
        在实例化阶段Spring提供了实例化后两个扩展点：postProcessBeforeInstantiation，postProcessAfterInstantiation方法
    6，Bean属性赋值阶段
        在Spring实例化后，需要对其相关属性赋值，注入依赖的对象
        首先获取该对象所有属性和属性映射，可能已经定义，也可能需要注入。在这里都需要进行赋值（反射机制）
        提示一下，依赖注入的实现通过 CommonAnnotationBeanPostProcessor（@Resource、@PostConstruct、@PreDestroy）和 AutowiredAnnotationBeanPostProcessor（@Autowired、@Value）两个处理器实现的。
    7，Aware接口回调阶段
        如果Bean是Spring提供的Aware接口类型（如BeanNameAware、ApplicationContextAware）
        在这里会进行接口回调，注入相关对象，例如beanName、ApplicaionContext
    8，Bean初始化阶段
        这里会调用Bean配置的初始化方法，执行顺序：
            @PostConstruct标注的方法、实现InitializingBean接口的afterPropertiesSet()方法、自定义的初始化方法
        在初始化阶段提供了初始化前后的两个扩展点：postProcessBeforeInitialization、postProcessAfterInitialization
    9，Bean初始化完成阶段
        在所有的Bean初始化后，Spring会再次遍历所有初始化好的单例Bean对象
        如果是SmartInitializingSingleton类型，则调用afterSingletonInstantiated()方法，这里也属于Spring的扩展点
    10，Bean的销毁阶段
        当Spring应用上下文关闭或者你主动销毁某个Bean时，则进入Bean的销毁阶段
        执行顺序：
            @PreDestroy注解的销毁动作
            实现了DisposableBean接口的Bean回调
            destroy-method自定义的销毁方法
        这里也有一个销毁前阶段，属于Spring提供的一个扩展点，@PreDestroy就是基于这个实现的
Bean的作用域有哪些？
    singleton：默认的作用域，一个BeanFactory中只有一个实例
    prototype：每次获取bean的时候都会创建一个新的bean
    request：作用域在当前Http Request有效，每次http请求返回一个bean，不同的http请求返回不同的bean
    session：在同一次http session中容器会返回同一个实例。
    global Session：用于 Portlet 有单独的 Session,GlobalSession 提供了一个全局性的 HTTP Session。
    其中最常用的就是singleton和prototype
    singleton对于无会话状态的bean来说是最理想的选择，比如Service层、Dao层
    prototype适用于那些需要保持会话状态的Bean，比如Status2的action类
依赖查找和依赖注入的来源是否相同
    不相同，依赖查找来源仅限于Spring Definition单例对象，
    而依赖注入的来源还包括Spring应用上下文定义的可以注入对象以及@Value所标注的外部化配置






# Spring的事件机制
事件机制可以用于系统的解耦，事件源发布个事件，事件监听器消费这个事件，
事件源不再关注发布的事件有那些监听器，从而方便功能的修改和添加。
它是基于观察者模式实现的。
Spring中对事件的定义如下：
    ApplicationEvent：继承EventObject类，自定义事件源应该继承该类
    ApplicationEventListener：继承EventListener接口，自定义监听者实现该接口
    ApplicationEventPublisher：封装了事件发布的方法，通知所有在Spring中注册的监听着进行处理
Spring事件使用分为3步：
    1，事件定义：
        自定义事件，需要继承 ApplicationEvent
    2，事件监听：
        自定义监听事件，需要实现 ApplicationListener，这个接口有个onApplicationEvent需要实现，用来处理感兴趣的事件
    3，创建事件广播器
        创建事件广播器ApplicationEventMulticaster，这是个接口，可以实现这个接口
        也可以直接使用系统给我们提供的SimpleApplicationEventMulticaster
        ```ApplicationEventMulticaster applicationEventMulticaster = new SimpleApplicationEventMulticaster();```
    4，向广播器中注册事件监听
        ```
        ApplicationEventMulticaster applicationEventMulticaster = new SimpleApplicationEventMulticaster();
        applicationEventMulticaster.addApplicationListener(new SendEmailOnOrderCreateListener());
        ```
    5，通过广播器发布事件
        调用ApplicationEventMulticaster#multicastEvent方法广播事件
        此时广播器中对这个事件感兴趣的监听器会处理这个事件。
        ```applicationEventMulticaster.multicastEvent(new OrderCreateEvent(applicationEventMulticaster, 1L));```
Spring事件具体使用：
    （1）事件类（继承ApplicationEvent）
        ```
        public class UserRegisterEvent extends ApplicationEvent {
            private String userName;
            public UserRegisterEvent(Object source, String userName) {
                super(source);
                this.userName = userName;
            }
            public String getUserName() {
                return userName;
            }
        }
        ```
    （2）监听类（实现ApplicationListener接口）
        ```
        @Configuration
        public class SendEmailListener implements ApplicationListener<UserRegisterEvent> {
            @Override
            public void onApplicationEvent(UserRegisterEvent event) {
                System.out.println(String.format("给用户【%s】发送注册成功邮件!", event.getUserName()));
            }
        }
        ```
    （3）事件发布
        1，实现ApplicationEventPublisherAware接口，spring容器会把ApplicationEventPublisher注入进来，然后我们就可以使用这个来发布事件了。
            ```
            @Component
            public class UserRegisterService implements ApplicationEventPublisherAware {
                private ApplicationEventPublisher applicationEventPublisher;
                public void registerUser(String userName) {
                    //用户注册(将用户信息入库等操作)
                    System.out.println(String.format("用户【%s】注册成功", userName));
                    //发布注册成功事件
                    this.applicationEventPublisher.publishEvent(new UserRegisterEvent(this, userName));
                }
                @Override
                public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
                    this.applicationEventPublisher = applicationEventPublisher;
                }
            }
            ```
        2，也可以使用 Spring 上下文 ApplicationContext 进行发布。
            ```applicationContext.publishEvent(new UserRegisterEvent(this, user));```
    （4）测试用例
        ```
        @ComponentScan
        public class MainConfig2 { }

        public void test2() throws InterruptedException {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            context.register(MainConfig2.class);
            context.refresh();
            //获取用户注册服务
            UserRegisterService userRegisterService = Context.getBean(UserRegisterService.class);
            //模拟用户注册
            userRegisterService.registerUser("路人甲Java");
        }
        ```
        Spring容器在创建Bean的过程中，会判断Bean是否为ApplicationListener类型
        进而会将其作为监听器注册到AbstractApplicationContext#applicationEventMulticaster中 
监听类的配置：
    一，不使用注解
        1，实现ApplicationListener接口
        2，实现SmartApplicationListener接口
            实现了SmartApplicationListener的监听器中，我们通过重写GetOrder方法来修改不同监听器的顺序
            ```
            public class StartWorkflowListener implements SmartApplicationListener {
                @Override
                public int getOrder() {
                    return 2;
                }
                /**
                 *  指定支持哪些类型的事件
                 */
                @Override
                public boolean supportsEventType(Class<? extends ApplicationEvent> aClass) {
                    return aClass == StartWorkflowEvent.class;
                }
                /**
                 *  指定支持发生事件所在的类型
                 */
                @Override
                public boolean supportsSourceType(Class<?> aClass) {
                    return true;
                }
                @Override
                public void onApplicationEvent(ApplicationEvent applicationEvent) {
                    StartWorkflowEvent event = (StartWorkflowEvent) applicationEvent;
                    //发送短信
                    System.out.println(event.getTimestamp());
                }
            }
            ```
        注册监听器
            1，SpringApplication中直接添加监听器```pplication.addListeners(new MyApplicationListener());```，
            2，或者spring.factories中配置监听器：```org.springframework.context.ApplicationListener=com.roy.applicationListener.MyApplicationListener```
    二，使用注解
        1，实现ApplicationListener接口
            ```
            @EnableAsync//配合@Async，开启异步执行
            @Component("startWorkflowListener")//将监听器自动注册到IoC容器
            @Order(1)//事件监听顺序
            public class StartWorkflowListener implements ApplicationListener<StartWorkflowEvent> {
                @Async
                @Override
                public void onApplicationEvent(StartWorkflowEvent event) {
                    //监听到事件，开始执行
                    event.myevent();
                }
            }
            ```
        2，使用@EventListener注解，无需实现ApplicationListener接口
            ```
            @Component
            @EnableAsync//配合@Async，开启异步执行
            @Order(1)//事件监听顺序
            public class TestApplicationListener2{
                @Async
                @EventListener//标明和注册监听器
                public void onApplicationEvent(StartWorkflowEvent event) {
                    System.out.println("step2：code is:\t" + event.getCode() + ",\tmessage is:\t" + event.getMessage());
                }
            }
            ```
事件机制的原理：
    Spring中的事件通知机制是观察者模式的一种实现。观察者是ApplicationListener，可以实现接口定义观察者，也可以使用注解定义观察者
    观察者感兴趣的是某中状态的变化，这种状态的变化使用ApplicationEvent来传达，也就是事件对象ApplicationEvent
    在事件中，被观察者被认为是发出事件的一方，只有在状态发生变化时才发出事件。
    当有状态变化时，发布者调用ApplicationEventPublisher的publishEvent方法发布一个事件
    Spring容器广播器广播事件给所有的观察者，调用观察者的onApplicationEvent方法把事件对象传递给观察者
关于容器广播器applicationEventMulticaster
    1，容器初始化的时候查找是否有name为applicationEventMulticaster的bean，如果有放到容器里，如果没有初始化一个放到容器里
    2，查找手动设置的applicationListeners，添加到applicationEventMulticaster里
    3，查找定义的类型为ApplicationListener的bean，设置到applicationEventMulticaster
    4，初始化完成后，对earlyApplicationEvents里的事件进行通知（此容器仅仅是广播器未建立的时候保存通知信息，一旦容器建立完成，以后均直接通知）
    5，在系统操作的时候，遇到的各种bean的通知事件进行通知
    applicationEventMulticaster是一个标准的观察者模式，对于内部的监听者，每次事件到来都会一一进行通知
监听器对于事件的处理，系统中有三种方式：
    （a）继承SmartApplicationListener的情况下，根据supportsEventType返回结果判断
    （b）根据监听器泛型判断
    （c）如果没有继承父类也没有泛型的情况下，我么可以在广播器的onApplicationEvent方法中获取event，然后自行过滤
注意    
    1，事件通知往往是独立于整个运行程序之外的补充，所以另外开启线程进行工作是一个不错的办法，listener中提供了executor的注入来实现多线程
    2，事件类型不仅仅支持ApplicationEvent类型，也支持其他类型，Spring自动转化为PayloadApplicationEvent，我们调用hyePayload获取具体数据
    3，可以注入一个errorHandler，完成异常的处理
    4，Spring处理事件是同步的，监听者的执行事件最好不要过长，以免影响主流程。如果事件的处理一定要耗费比较长的时间可以使用异步的方式进行处理
Spring内置事件：
    ContextClosedEvent：当ApplicationContext被关闭时触发该事件。容器被关闭时，其管理的所有单例Bean都被销毁。
    ContextRefreshedEvent：在调用ConfigurableApplicationContext 接口中的refresh()方法时被触发。
    ContextStartedEvent：当容器调用ConfigurableApplicationContext的Start()方法开始/重新开始容器时触发该事件。
    ContextStoppedEvent：当容器调用ConfigurableApplicationContext的Stop()方法停止容器时触发该事件。
    ServletRequestHandledEvent：Spring MVC 请求完成之后推送会触发该事件监听器
    ```
    /*比如监听ContextRefreshedEvent事件，当所有的bean都初始化完成并被成功装载后会触发该事件*/
    @Component
    public class TestApplicationListener implements ApplicationListener<ContextRefreshedEvent>{
        @Override
        public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
            System.out.println(contextRefreshedEvent);
            System.out.println("TestApplicationListener............................");
        }
    }
    ```
@EventListener 的工作原理？
    Spring内部有一个处理器EventListenerMethodProcessor，他实现了SmartInitializingSingleton
    在所有的Bean初始化后，Spring会再次遍历所有初始化好的单例Bean对象
    在 EventListenerMethodProcessor 中会对标注了 @EventListener 注解的方法进行解析，如果符合条件则生成一个 ApplicationListener 事件监听器并注册。

# Spring 中的注解
Spring模式注解：
    @Component：通用组件注解
    @Repository：数据访问层注解
    @Service：业务层注解
    @Controller：控制器层注解
    @Configuration：配置类注解
    所有的模式注解都是@Component的派生注解，@Component 注解是一个通用组件注解，标注这个注解后表明你需要将其作为一个 Spring Bean 进行使用，
    而其他注解都有各自的作用，例如 @Controller 及其派生注解用于 Web 场景下处理 HTTP 请求，
    @Configuration 注解通常会将这个 Spring Bean 作为一个配置类，也会被 CGLIB 提供，帮助实现 AOP 特性。这也是领域驱动设计中的一种思想。
Spring装配注解
    @ImportResource：引入spring配置文件.xml
    @Import：引入带有@Configuration的java类。
依赖注入注解
    @Autowired：Bean依赖注入，支持多种依赖查找方式
    @Qualifier：细粒度的@Autowired依赖查找
@Enable模块驱动
    @EnableWebMvc：启动整个Web MVC模块
    @EnableTransactionManagement：启动整个事务管理模块
    @EnableCaching：启动整个缓存模块
    @EnableAsync：启动整个异步处理模块
    所谓“模块”是指具备相同领域的功能组件集合，组合所形成一个独立的单元。比如 Web MVC 模块、AspectJ 代理模块、Caching（缓存）模块、JMX（Java 管理扩展）模块、Async（异步处理）模块等。
    这类注解底层原理就是通过@Import注解导入相关类，来实现某个模块或功能
条件注解
    @Conditional：是按照一定的条件进行判断，满足条件给容器注册bean。
        比如根据不同的操作系统环境，注入不同的Bean
        ```
        @Configuration
        public class BeanConfig {
            //只有一个类时，大括号可以省略
            //如果WindowsCondition的实现方法返回true，则注入这个bean    
            @Conditional({WindowsCondition.class})
            @Bean(name = "bill")
            public Person person1(){
                return new Person("Bill Gates",62);
            }
            //如果LinuxCondition的实现方法返回true，则注入这个bean
            @Conditional({LinuxCondition.class})
            @Bean("linus")
            public Person person2(){
                return new Person("Linus",48);
            }
        }
        ```
    @Profile：指定类或方法在特定的 Profile 环境生效
        在不同的环境使用不同的dataSource
        ```
        @Configuration
        public class AppConfig {
            @Bean("dataSource")
            @Profile("dev")
            public DataSource standaloneDataSource() {
                return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.HSQL)
                    .addScript("classpath:com/bank/config/sql/schema.sql")
                    .addScript("classpath:com/bank/config/sql/test-data.sql")
                    .build();
            }
            @Bean("dataSource")
            @Profile("prod")
            public DataSource jndiDataSource() throws Exception {
                Context ctx = new InitialContext();
                return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
            }
        }
        ```
        profile激活：application.properties中配置：spring.profiles.active=dev
@Configuration 和 @Component 区别
    一句话概括就是 @Configuration 中所有带 @Bean 注解的方法都会被动态代理，因此调用该方法返回的都是同一个实例。

# Spring Environment
统一Spring配置属性存储，用于占位符处理和类型转换，还支持丰富的配置属性资源
通过Environment Profiles信息，帮助Spring容器提供条件化的装配Bean
在Spring应用上下文进入刷新阶段之前，可以通过setEnvironment方法设置Environment对象
在刷新阶段如果没有Environment对象则会创建一个新的Environment

# Spring应用上下文
什么是Spring应用上下文
    Spring应用上下文就是我们常说的Spring容器，除了作为基础的BeanFactory
    还提供了更多企业级的特性，比如资源管理、事件发布、国际化等
Spring应用上下文具有层次性设计，生命周期管理主要在AbstractApplicationContext，生命周期的相关方法均有实现的接口提供
各生命周期阶段并非完整的从上而下执行，部分阶段是可选的，具体如下：
    刷新阶段：org.springframework.context.ConfigurableApplicationContext#refresh
    启动阶段：org.springframework.context.Lifecycle#start
    停止阶段：org.springframework.context.Lifecycle#stop
    关闭阶段：org.springframework.context.ConfigurableApplicationContext#close
1，刷新阶段：
    刷新阶段是Spring应用上下文生命周期主要阶段，刷新阶段完成后Spring非延迟单例Bean即实例化完成，
    依赖注入也完成，正常可以调用bean方法
    （a）上下文刷新准备阶段
        设置启动时间及状态标识、初始化PropertySource（操作系统环境变量、启动参数等，比如放在default.properties中的属性）及Environment
        初始化事件监听集合，初始化早期事件集合
    （b）BeanFactory创建阶段
        创建BeanFactory，设置BeanFactory的ID，设置BeanFactory是否允许BeanDefinition重复注册和循环引用
    （c）BeanFactory准备阶段
        设置加载bean的ClassLoader，设置解析BeanDefinition中字符串的表达式处理器，添加几个BeanPostProcessor处理器
    （d）BeanFactory后置处理阶段
        主要执行BeanFactoryPostProcessor后置处理，是Spring应用上下文的一个扩展点
    （e）BeanPostProcessor注册阶段
        初始化 BeanPostProcessor 类型的 Bean，将其添加到BeanFactory内部持有的BeanPostProcessor列表中
    （f）初始化内建Bean
        包括国际化相关的MessageSource，ApplicationEventMulticaster 事件广播器对象等
    （g）Spring事件监听器注册阶段
        主要获取到所有的 ApplicationListener 事件监听器进行注册，并广播早期事件
    （h）BeanFactory 初始化完成阶段
        主要是初始化所有还未初始化的 Bean
2，Spring 应用上下文刷新完成阶段
    清除当前 Spring 应用上下文中的缓存，发布Spring应用上下文已刷新的事件-ContextRefreshedEvent
3，Spring应用上下文启动阶段
    调用所有 Lifecycle 的 start() 方法，最后会发布上下文启动事件-ContextStartedEvent
4，Spring应用上下文停止阶段
    调用所有 Lifecycle 的 stop() 方法，最后会发布上下文停止事件
5，Spring应用上下文关闭阶段
    发布当前 Spring 应用上下文关闭事件，销毁所有的单例 Bean，关闭底层 BeanFactory 容器；
    注意这里会有一个钩子函数（Spring 向 JVM 注册的一个关闭当前 Spring 应用上下文的线程），当 JVM “关闭” 时，会触发这个线程的运行
# BeanFactory、FactoryBean、ObjectFactory区别
BeanFactory：
    是Spring底层的IoC容器，里面保存了所有的单例Bean
FactoryBean：
    FactoryBean就是一个Bean。但这个Bean不是一个简单的Bean，而是一个能产生或者修饰对象生成的工厂Bean
    它的实现与设计模式中的工厂模式和修饰器模式类似
    设计目的：
        一般情况下，Spring通过反射机制实例化Bean，在某些情况下，实例化Bean过程比较复杂
        如果按照传统的方式，则需要在<bean>中提供大量的配置信息。配置方式的灵活性是受限的
        这时采用编码方式会得到一个简单的答案。Spring为此提供了一个FactoryBean的工厂类接口，用户可以通过实现该接口定制实例化Bean的逻辑。
    FactoryBean接口对于Spring框架来说占有重要的地位，Spring自身就提供了70多个FactoryBean的实现，它们隐藏了实例化一些复杂Bean的细节，给上层应用带来了便利。
    获取方式：
        如果想要获取FactoryBean这个对象，在beanName前面加上"&"即可
    应用：
        例如在Spring集成MyBatis项目中
        Mapper接口没有实现类是如何被注入的？
            其实Mapper接口就是一个FactoryBean对象，当你注入该接口的时候，实际得到的就是其getObject方法返回的一个代理对象
            关于对数据库的操作都是通过该代理对象来完成的
ObjectFactory：
    提供延迟依赖查找，想要获取某一类型的Bean，则需要调用其getObject()方法才能依赖查找到目标Bean对象
    ObjectFactory就是一个对象二厂，想要获取该类型对象，需要调用其getObject()方法
    特性：
        ObjectFactory可关联某一类型的Bean，仅提供一个getObject()方法用于返回目标bean对象
        ObjectFactory对象被依赖注入或者依赖查找时并未实时查找到关联的类型的目标Bean对象，在调用getObject()方法才会依赖查找目标Bean对象。
        根据ObjectFactory的特性，可以说它是提供延迟依赖查找。
    解决循环依赖的问题：
        在某个Bean还没有完全初始化好的时候，会先缓存一个ObjectFactory对象（调用其getObject()方法可返回当前正在初始化的Bean对象）
        如果初始化过程中依赖的对象又依赖于当前Bean，会先通过缓存ObjectFactory对象获取到当前正在初始化的Bean，这样依赖就解决了循环依赖的问题
    注意
        注意这里是延迟依赖查找而不是延迟初始化，ObjectFactory无法决定是否延迟初始化
        而需要通过配置Bean的lazy属性来决定这个Bean对象是否需要延迟初始化，非延迟初始化的Bean在Spring应用上下文刷新的过程中就会初始化
    提示：
        如果是ObjectFactory类型的Bean，在依赖注入或者依赖查找的时候返回的是DefaultListableBeanFactory#DependencyObjectProvider 私有内部类
        实现了 ObjectProvider<T> 接口，关联的类型为 Object。
ObjectProvider：
    在Spring4.3之前，如果你构造函数中要依赖另外一个bean，你必须显示依赖@Autowired
    而在4.3版本之后，已经不需要这么做了，只要我们只提供了一个构造函数，并且构造函数所需要的参数都在Spring容器中
    那么不需要进行精确的指定使用@Autowired
    但是这种隐式的注入仍然存在一些不足，例如，构造函数中的参数在容器中存在了一个以上的FooRepository甚至一个都没有的情况下，抛出异常
    那么我们有什么办法解决它呢？基于这个原因，ObjectProvider就出场了。
        如果注入实例为空时，使用ObjectProvider则避免了强依赖导致的依赖对象不存在异常；
        如果有多个实例，ObjectProvider的方法可以根据Bean实现的Ordered接口或@Order注解指定的先后顺序获取一个Bean。从而了提供了一个更加宽松的依赖注入方式
    
# Spring 循环依赖
Spring中的一、二、三级缓存：
    singletonObjects：一级缓存，存放完整的 Bean。
    earlySingletonObjects：二级缓存，存放提前暴露的Bean，Bean 是不完整的，未完成属性注入和执行 初始化（init） 方法。
    singletonFactories：三级缓存，存放的是 Bean 工厂，主要是生产 Bean，存放到二级缓存中。
    除了一级缓存是ConcurrentHashMap，其余的都是HashMap
1，存在循环引用时，Bean在这三个缓存之间的流转顺序为：
    （a）通过反射创建Bean实例。是单例Bean，并且IoC容器允许Bean之间循环引用，保存到三级缓存中
    （b）当发生循环引用时，从三级缓存中取出Bean对应的ObjectFactory实例，调用其getObject方法获取早期曝光的Bean，从三级缓存中移除，保存到二级缓存
    （c）Bean初始化完成后，生命周期的相关方法执行完毕，保存到一级缓存中，从二级缓存以及三级缓存中移除
2，没有循环引用时，Bean在这三个缓存之间的流转顺序为：
    （a）通过反射创建Bean。是单例Bean，并且IoC容器允许Bean之间循环引用，保存到三级缓存中
    （b）Bean初始化完成，生命周期的相关方法执行完毕，保存到一级缓存中，从二级缓存以及三级缓存中移除
总结：
    Bean在一级缓存、二级缓存、三级缓存中的流转顺序为：三级缓存 --> 二级缓存 --> 一级缓存
    但并不是所有Bean都会经历这个过程，例如对于原型Bean（Prototype），IoC容器也不会保存到任何一个缓存中
    另外即便是单例Bean，如果没有循环引用关系，也不会保存到二级缓存中。
循环依赖
    原理：
        创建对象的生命周期：
            AService生命周期：
                1，创建一个AService普通对象
                2，填充bService属性-->从单例池中查找BService对象-->找不到就创建BService对象
                3，填充其他属性
                4，其他操作
                5，初始化后
                6，放入单例池
            BService生命周期：
                1，创建一个BService普通对象
                2，填充aService属性-->从单例池中查找AService对象-->找不到就创建AService对象
                3，填充其他属性
                4，其他操作
                5，初始化后
                6，放入单例池
        在生命周期的步骤2中就会出现循环依赖的问题，因为初始化还没有完成，单例池中一直找不到。
        此时我们只需要新增一个缓存Map就可以解决这个问题：
            AService生命周期：
                1，创建一个AService普通对象 ---> 放入一个CacheMap
                2，填充bService属性 ---> 从单例池中查找BService对象 ---> 找不到就创建BService对象
                3，填充其他属性
                4，其他操作
                5，初始化后
                6，放入单例池
            BService生命周期：
                1，创建一个BService普通对象
                2，填充aService属性 ---> 从单例池中查找AService对象 ---> 找不到从CacheMap中查找 ---> 找不到就创建AService对象
                3，填充其他属性
                4，其他操作
                5，初始化后
                6，放入单例池
        当BService从单例池中找不到AService的时候，就从CacheMap中查找，这时候CacheMap存放的是实例化，但是还没有初始化的AService对象
        就解决了循环依赖的问题，在CacheMap中的代理对象还是会完成初始化。
        循环依赖指的是"单例模式"下的Bean字段注入时出现的循环依赖，构造器注入对于Spring来说无法自动解决，可以通过延迟初始化来解决
            当通过getBean依赖查找时会首先依次从上面三个map中获取（一级-->二级-->三级），存在就返回，不存在则进行初始化，这三个map是处理循环依赖的关键
            假如两个Bean出现循环依赖，A依赖B，B依赖A，在实例化后初始化前会生成一个ObjectFactory对象（可获取当前正在初始化A）保存在上面的singletonFactories中
            初始化的过程中需要注入B；接下来去查找B，初始B的时候又要去注入A，又去查找A，由于可以通过singletonFactories拿到真正初始化的A，那么就可以完成B的初始化，最后完成A的初始化
    为什么需要上面的二级Map？
        为了避免重复处理，当一个Bean被多个Bean依赖，三级缓存保存的ObjectFactory实现类会被调用两次，会生成两个对象。这就有问题了
        二级缓存在这里就做了一个中间的代理缓存，先从二级缓存找，没有再去三级缓存调用getobject方法生成。
    为什么不直接调用ObjectFactory#getObject() 方法放入 二级Map 中，而需要上面的 三级 Map？
        如果直接使用getObject()，那么进行依赖注入的就是原始对象，而不是代理对象，此时就无法进行AOP
        那么为什么不能提前进行AOP呢，比如在实例化的时候就进行AOP？
            这样违背了 Spring 的设计原则。因为创建的代理对象需要关联目标对象，在拦截处理的过程中需要根据目标对象执行被拦截的方法，所以这个目标对象最好是一个“成熟态”，而不是仅实例化还未初始化的一个对象。
# 循环依赖失效的问题
1，@Async导致循环依赖解决不了
    1.context.getBean(A)开始创建A，A实例化完成后给A的依赖属性b开始赋值
    2.context.getBean(B)开始创建B，B实例化完成后给B的依赖属性a开始赋值
    3.重点：此时因为A支持循环依赖，所以会执行A的getEarlyBeanReference方法得到它的早期引用。而执行getEarlyBeanReference()的时候因为@Async根本还没执行，所以最终返回的仍旧是原始对象的地址
    4.B完成初始化、完成属性的赋值，此时属性field持有的是Bean A原始类型的引用
    5.完成了A的属性的赋值（此时已持有B的实例的引用），继续执行初始化方法initializeBean(...)，在此处会解析@Aysnc注解，从而生成一个代理对象，所以最终exposedObject是一个代理对象（而非原始对象）最终加入到容器里
    6.尴尬场面出现了：B引用的属性A是个原始对象，而此处准备return的实例A竟然是个代理对象，也就是说B引用的并非是最终对象（不是最终放进容器里的对象）
    7.执行自检程序：由于allowRawInjectionDespiteWrapping默认值是false，表示不允许上面不一致的情况发生，so最终就抛错了
2，构造方法导致循环依赖失效
    AService要通过构造方法注入BService，BService要通过构造方法注入AService
    AService注入BService的时候，发现没有，那么就要创建BService
    而创建BService的时候，又需要注入AService，这时就会报错
    解决：
        在构造方法加上@Lazy注解，加上注解后就会返回一个Lazy的代理对象

# Spring 中几种初始化方法的执行顺序？
1，Aware 接口：实现了 Spring 提供的相关 XxxAware 接口，例如 BeanNameAware、ApplicationContextAware，其 setXxx 方法会被回调，可以注入相关对象
2，@PostConstruct 注解：该注解是 JSR-250 的标准注解，Spring 会调用该注解标注的方法
3，InitializingBean 接口：实现了该接口，Spring 会调用其 afterPropertiesSet() 方法
4，自定义初始化方法：通过 init-method 指定的方法会被调用
    
# Spring事物
事物的特性：
    原子性：事物是一个原子操作，由一系列动作组成。事物的原子性确保要么全部完成，要不完全不起作用
    一致性：一旦事物完成，不管成功失败，系统必须确保处于一致性状态，而不是部分完成部分失败
    隔离性：可能会有许多事物会处理相同的数据，因此每个事物与其他事物都应该隔离开，防止数据损坏
    持久性：一旦事务完成，无论发生什么系统错误，它的结果都不应该受到影响，这样就能从任何系统崩溃中恢复过来。通常情况下，事务的结果被写到持久化存储器中。
事物的配置方式
    1，编程式事物
        编程式事物是入侵性事物管理，使用TransactionTemplate或者PlatformTransactionManager
        Spring推荐TransactionTemplate
        ```xml
        <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
            <property name="dataSource" ref="dataSource"/>
        </bean>
        <bean id="transactionTemplate" class="org.springframework.transaction.support.TransactionTemplate">
            <property name="transactionManager">
                <ref bean="transactionManager"/>
            </property>
        </bean>
        ```
        ```java
        public class BankServiceImpl implements BankService {
            private BankDao bankDao;
            private TransactionTemplate transactionTemplate;
            public boolean transfer(final Long fromId, final Long toId, final double amount) {
                return (Boolean) transactionTemplate.execute(new TransactionCallback(){
                    public Object doInTransaction(TransactionStatus status) {
                        Object result;
                        try {
                            result = bankDao.transfer(fromId, toId, amount);
                        } catch (Exception e) {
                            status.setRollbackOnly();//回滚
                            result = false;
                            System.out.println("Transfer Error!");
                        }
                        return result;
                    }
                });
            }
        }
        ```
    2，声明式事物
        声明式事物是使用注解@Transactional实现的，属于无入侵式的事物。可以用于接口、接口方法、类和类方法上
        原理：
            Spring事物底层是基于数据库事物和AOP实现的，对于使用了@Transactional的Bean，Spring会创建一个代理对象作为Bean
            当调用代理对象方法时，会先判断该方法上是否加了@Transactional注解
            如果加了，则利用事物管理器创建一个数据库连接，并且修改数据库连接的autocommit属性为false，禁止此连接的自动提交。这是非常重要的一步
            然后Bean的方法（在代理对象方法中执行Bean的当前方法），方法中会执行sql
            执行完Bean当前方法之后，如果没有出现异常就直接提交事物
            如果出现了异常需要回滚就回滚事物，否则仍然提交事物
            实现原理代码实现：
            ```
            class UserServiceProxy extends UserService {
                UserService target;
                public void test() {
                    //有@Transactional，就创建一个数据库连接
                    //设置autocommit为false
                    conn.autocommit = false;
                    //执行目标方法
                    target.test()
                    //提交事物或者rollback
                }
            }
            ```
事物失效：
    1，自身调用的问题
        ```java
        @Service
        public class OrderServiceImpl implements OrderService {
            @Transactional
            public void update(Order order) {
                updateOrder(order);
            }
            @Transactional(propagation = Propagation.REQUIRES_NEW)
            public void updateOrder(Order order) {
                // update order
            }
        }
        ```
        上面的代码在update方法上加了@Transactional，updateOrder加了REQUIRES_NEW开启一个全新的事物，那么新开的事物管用么？
        是不管用的，因为发生了自身调用，没有经过Spring的代理类
    2，方法不是public
        事物方法按理来说是对外部调用的
        @Transactional 只能用于 public 的方法上，否则事务不会失效，如果要用在非 public 方法上，可以开启 AspectJ 代理模式。
    3，没有被Spring管理
        ```java
        // @Service
        public class OrderServiceImpl implements OrderService {
            @Transactional
            public void updateOrder(Order order) {
                // update order
            }
        }
        ```
        如果此时把 @Service 注解注释掉，这个类就不会被加载成一个 Bean，那这个类就不会被 Spring 管理了，事务自然就失效了。
    4，数据库不支持事物
        用mysql且引擎是MyISAM，则事务会不起作用，原因是MyISAM不支持事务
    5，数据源没有配置事务管理器
        ```java
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
        ```
        如上面所示，当前数据源若没有配置事务管理器，那也是白搭！
    6，异常被吃了
        ```java
        @Service
        public class OrderServiceImpl implements OrderService {
            @Transactional
            public void updateOrder(Order order) {
                try {
                    // update order
                } catch {
                }
            }
        }
        ```
    7，异常类型错误
        ```java
        @Service
        public class OrderServiceImpl implements OrderService {
            @Transactional
            public void updateOrder(Order order) {
                try {
                    // update order
                } catch {
                    throw new Exception("更新错误");
                }
            }
        }
        ```
        这样事务也是不生效的，因为默认回滚的是：RuntimeException，如果你想触发其他异常的回滚，需要在注解上配置一下，如：@Transactional(rollbackFor = Exception.class)
事物的传播机制
    Required：当前存在事物，则加入这个事物，如果没有当前事物，自己创建一个事物
    Supports：当前存在事物，则加入这个事物，如果没有事物，就以非事物方式运行
    Mandatory：当前存在事物，则加入这个事物，如果没有当前事物，抛出异常

    Requires_New：创建一个新事物，如果当前存在事物，就刮起该事物
        简单理解就是方法A和方法B的事物互不干扰，各用各的事物。方法B事物的执行与方法A的事物无关

    Not_Supported：以非事物方式执行，如果当前存在事物，则刮起该事物
        方法A调用方法B，如果方法A有事物那就运行你的，方法B里面不实用事物
    Never：不使用事物，如果当前事物存在，则抛出异常

    Nested：如果当前事物存在，则嵌套在事物中运行，否则与Required操作一样（开启一个事物）
        方法A如果没有事物，方法B自己开启一个事物
        方法A如果有事物，方法B就嵌套在里面，
        方法A是父事物，方法B是子事物，父事物回滚时，子事物也会回滚
        子事物回滚，父事物不一定会回滚，因为子事物可以使用try catch捕获异常
事物的隔离级别
    Spring的事物隔离级别有4个：
        Read_Uncommitted：一个事物可以读取到另一个事物未提交的记录
            这是事物最弱的隔离级别。除了脏读的问题，还可能出现不可重复读和幻读的问题
        Read_Committed：一个事物只能读取到已经提交的记录，不能读取到未提交的记录
            这种情况下不会出现脏读的问题，但是可能会出现其他问题。
            比如不可重复读：
                事物A在两次读取的过程中间，事物B修改了那条记录并进行提交。因此事物A两次读取的记录不一致
                这个问题称为不可重复读（两次读取的记录不一致，重复读就会发现问题）
        Repeatable_Read：
            一个事物可以多次从数据库读取某条记录，而且多次读取的那条记录都是一致的，相同的
            这个隔离级别可以避免脏读和不可重复读的问题呢，但是可能发生幻读的问题
            比如：
                事物A两次从数据库读取一系列记录，期间事物B插入了某条记录并提交。
                事物A第二次读取时，会读取到事物B刚刚插入的那条记录。在事物期间，事物A两次读取的一系列记录不一致，这个问题称为幻读
        Serializable：最强的隔离级别
            事物开始执行时会对所有级别上锁，仿佛事物是以串行的方式进行的。会防止脏读、不可重复读和幻读
            但是性能会下降
        还有一个是数据库的默认隔离界别Default：
            mysql默认是：Repeatable_Read
            oracle默认是：Read_Committed
    几种异常：
        脏读：读取到了其他事物还没有提交的数据
        不可重复读：对数据进行读取，发现两次读取结果不同，也就是说没有读取到相同的内容。这是因为其他事物对这个数据同时进行了修改或删除
        幻读：事物A根据条件查询到了N条数据，但是此时事物B更改或增加了M条符合事物A查询条件的数据，这样事物A再次进行查询的时候会发现N+M条数据，产生幻读
    如果数据库的隔离级别是Read Commit，而Spring配置的隔离级别是Repeatable Read，那么隔离级别以哪一个为准呢
    以Spring配置的为准，如果Spring设置的隔离级别数据库不支持，才以数据库为准






        



# Spring AOP
什么是AOP？
    AOP就是面向切面编程，是一种开发概念，通过AOP我们可以把一些非业务逻辑的代码从业务中抽取出来
    以非入侵的方式与原方法进行协同。这样使得原方法更专注于业务逻辑，代码接口更加清晰易于维护。
AOP中几个比较重要的概念：
    切面(Aspect)：
        切面由切入点和通知组成，它既包含了横切逻辑的定义，也包括了切入点的定义
        可以简单地认为, 使用 @Aspect 注解的类就是切面
    目标对象(Target)
        目标对象指将要被增强的对象，即包含主业务逻辑的类对象。或者说是被一个或者多个切面所通知的对象。
    连接点(JoinPoint)：
        连接点，指程序运行过程中的一个点，例如方法调用、异常处理。在Spring AOP中，仅支持方法级别的连接点
    切入点(PointCut)
        切入点是对连接点进行拦截的条件定义。用于匹配连接点
    通知(Advice)：
        通知，即我们定义的一个切面中的横切逻辑，有"around"、"before"和"after"三种类型
        在很多AOP框架中，advice通常作为一个拦截器，也可以包含多个拦截器作为一条链路围绕着Join point进行处理
    织入(Weaving)：
        织入，在有了连接点、切点、通知以及切面，如何将他们应用到程序中呢？
        就是织入，在切点的引导下，将通知逻辑插入到目标方法，使得我们的通知逻辑在方法调用时得以执行
Spring AOP中的Introduction是什么？
    其目标是对于一个已有的类引入新的接口，简单的说，你可以把当前对象转型成另一个对象，那么很显然，你就可以调用另一个对象的方法了
    比如：
        假设已经有一个UserService类提供了保存User对象的服务，但是现在想增加对User进行验证的功能
        在不修改UserService类代码的前提下就可以通过Introduction来解决。
        1，首先定义一个Verifier接口，里面定义了进行验证的方法validate()，如下所示：
            ```java
            package com.jackfrued.aop;
            import com.jackfrued.models.User;
            public interface Verifier {
                public boolean validate(User user);
            }
            ```
        2，接下来给出该接口的一个实现类BasicVerifier，如下所示：
            ```java
            package com.jackfrued.aop;
            import com.jackfrued.models.User;
            public class BasicVerifier implements Verifier {
                @Override
                public boolean validate(User user) {
                    if(user.getUsername().equals("jack") && user.getPassword().equals("1234")) {
                        return true;
                    }
                    return false;
                }
            }
            ```
        3，如何才能为UserService类增加验证User的功能呢，如下所示定义Aspect：
            ```java
            package com.jackfrued.aop;
            import org.aspectj.lang.annotation.Aspect;
            import org.aspectj.lang.annotation.DeclareParents;
            import org.springframework.stereotype.Component;
            @Aspect
            @Component
            public class MyAspect {
                @DeclareParents(value="com.tsinghuait.services.UserService", defaultImpl=com.tsinghuait.aop.BasicVerifier.class)
                public Verifier verifer;
            }
            ```
        4，接下来就可以将UserService对象转型为Verifier对象并对用户进行验证了，如下所示：
            ```java
            package com.jackfrued.main;
            import org.springframework.context.ApplicationContext;
            import org.springframework.context.support.ClassPathXmlApplicationContext;
            import com.jackfrued.aop.Verifier;
            import com.jackfrued.models.User;
            import com.jackfrued.services.Service;
            class Test {
                public static void main(String[] args) {
                    User user1 = new User();
                    user1.setUsername("abc");
                    user1.setPassword("def");
                    ApplicationContext factory = new ClassPathXmlApplicationContext("config.xml");
                    Service s = (Service) factory.getBean("service");
                    Verifier v = (Verifier) s;
                    if(v.validate(user1) {
                        System.out.println("验证成功");
                        s.serve(user1);
                    }
                }
            }
            ```
Spring AOP中的Advisor是什么？
    Advisor是切面的另外一种实现，能够将通知以更为复杂的方式织入到目标对象中，是将通知包装为更复杂切面的装配器。
    Advisor就像是一个小的自包含的切面，这个切面只有一个通知。切面自身通过一个Bean表示，并且必须实现一个默认接口。
    ```java
    // AbstractPointcutAdvisor是默认接口
    public class LogAdvisor extends AbstractPointcutAdvisor {
        private Advice advice; // Advice
        private Pointcut pointcut; // 切入点
        @PostConstruct
        public void init() {
            // AnnotationMatchingPointcut是依据修饰类和方法的注解进行拦截的切入点。
            this.pointcut = new AnnotationMatchingPointcut((Class) null, Log.class);
            // 通知
            this.advice = new LogMethodInterceptor();
        }
    }
    ```
Spring AOP自动代理的实现
    当我们有了，连接点、切点、通知以及切面之后，我们如何将他们织入我们的应用呢？
    在Spring AOP中提供了自动代理的实现，底层借助JDK动态代理和CGLIB动态代理创建对象
    回顾Spring IoC加载bean的过程中，Bean的实例化前和实例化后等生命周期阶段都提供了扩展点
    会调用相应的BeanPostProcessor处理器对Bean进行处理，当我们开启了自动代理（例如通过@EnableAspectJAutoProxy注解）
    则会向IoC容器中注册一个AbstractAutoProxyCreator自动代理对象，该对象实现了BeanPostProcessor
    在每个bean初始化后被调用，解析出当前Spring上下文中所有的Advisor（会缓存）
    如果这个Bean需要进行处理，则会通过JDK动态代理或者CGLIB 动态代理创建一个代理对象并返回
    所以得到的Bean对象实际上是一个代理对象。这样一来，开发人员只需要配置好切面的相关信息，
    Spring将会自动代理，和Spring IoC完美的结合在一起
Spring Configuration Class CGLIB 提升？
    IoC容器初始化后，在Bean的后置处理时，会对@Configuration 标注的 BeanDefinition 进行处理，
    进行CGLIB提升，使得这个Bean天然就是一个CGLIB代理

Sping AOP 应用到哪些设计模式？
    创建型模式：抽象工厂模式、工厂方法模式、构建器模式、单例模式、原型模式
    结构型模式：适配器模式、组合模式、装饰器模式、享元模式、代理模式
    行为型模式：模板方法模式、责任链模式、观察者模式、策略模式、命令模式、状态模式
Spring AOP 在 Spring Framework 内部有哪些应用？
    Spring事件、Spring事务、Spring数据、Spring缓存抽象、Spring本地调度、Spring整合、Spring远程调用
AOP是基于动态代理实现的
主要有两种代理实现，分别是JDK动态代理和CGLIB 动态代理两种
静态代理：
    代理类实现被代理类的接口，同时持有被代理类的引用，新增处理逻辑，当我们调用代理类方法的时候，实际调用被代理类的引用
    静态代理是通过实现被代理类所实现的接口，内部保存了被代理对象，在实现方法中对处理逻辑进行增强
    实际方法的执行调用了被代理对象的方法。
    静态代理比较简洁直观，但是在复杂的场景下，需要为每个代理对象创建代理类，不易维护。
    所以就有了动态代理，
JDK动态代理：
    是基于接口的代理，通过反射机制生成一个实现代理接口的类，在调用具体方法时会调用InvokeHandler来处理。
    需要调用Proxy.newProxyInstance方法创建代理对象，方法传入的三个参数分别是：
        用于加载代理对象的Class类加载器
        代理对象需要实现的接口
        代理对象的处理器
    新生成的代理对象的Class对象会继承Proxy，且实现所有入参interface中的接口，在实现的方法中实际是调用入参InvocationHandler的invoke方法
    JDK 动态代理只能基于接口代理，不能基于类代理？
        因为JDK动态代理生成的代理对象需要继承proxy这个类，在Java中类是单继承关系，无法再继承一个代理类
    为什么InvocationHandler不能直接声明到代理对象里面，而是直接放入继承的Proxy父类中？
        代理类既然是 JDK 动态生成的，那么 JDK 就需要识别出哪些类是生成的代理类，哪些是非代理类，或者说 JDK 需要对代理类做统一的处理，
        这时如果没有一个统一的类 Proxy 来进行引用根本无法处理。
CGLIB动态代理
    JDK 动态代理主要通过重组字节码实现，首先获得被代理对象的引用和所有接口，生成新的类必须实现被代理类的所有接口
    动态生成Java 代码后编译新生成的 .class 文件并重新加载到 JVM 运行
    
    JDK 代理直接写 Class 字节码，CGLib 是采用 ASM 框架写字节码，生成代理类的效率低
    但是 CGLib 调用方法的效率高，因为 JDK 使用反射调用方法。
    CGLib 使用 FastClass 机制为代理类和被代理类各生成一个类，这个类会为代理类或被代理类的方法生成一个 index，这个 index 可以作为参数直接定位要调用的方法。



    JDK动态代理的目标对象必须是一个接口，在我们日常生活中，无法避免开发人员不写接口直接写类，或者根本不需要接口，直接用类进行表达。
    这个时候我们就需要通过字节码提升的手段来做这个事情，在运行时，非编译时，创建一个新的class对象，这种称为字节码提升。
    在 Spring 内部有两个字节码提升的框架，ASM（过于底层，直接操作字节码）和 CGLIB（相对于前者更加简便）
    CGLIB动态代理是基于类代理（字节码提升），通过 ASM（Java 字节码的操作和分析框架）将被代理类的 class 文件加载进来，修改其字节码生成一个子类。
    需要借助于 CGLIB 的 org.springframework.cglib.proxy.Enhancer 类来创建代理对象，设置以下几个属性：
        Class<?> superClass：被代理的类
        Callback callback：回调接口
    新生成的代理对象的 Class 对象会继承 superClass 被代理的类，在重写的方法中会调用 callback 回调接口（方法拦截器）进行处理。
cglib如何实现动态代理的，总结来说就是以下步骤
    通过生成字节码创建原始类的一个子类作为代理类，原来父类中所有方法的实现均托管给net.sf.cglib.proxy.MethodInterceptor对象








https://www.cnblogs.com/lifullmoon/p/14654774.html


# Spring推断构造方法
Bean实例化阶段需要进行构造方法的推导。
1，先检查是否指定了具体的构造方法和构造方法参数值，或者在BeanDefinition中是否缓存了具体的构造方法或构造方法参数值
如果存在那么直接使用该构造方法实例化
2，如果没有确定的构造方法，那么找出类中的所有的构造方法
3，如果只有一个无参的构造方，直接使用无参的构造方法实例化
4，如果有多个可用的构造方法或者当前bean需要自动通过构造方法注入
那么根据所指定的构造方法参数值，确定所需要的最少的构造方法参数值个数
5，对所有的构造方法进行排序，参数多的在前面，然后遍历每个构造方法
6，如果不是调用getBean方法时所指定的构造方法参数值，那么根据构造方法参数类型查找值
7，如果是调用getBean时所指定的构造方法参数值，直接利用这些值
8，如果当前构造方法找到了对应的构造方法参数值，那么这个构造方法就是可用的
但是这个构造方法并不一定是最佳的，所以这里就会涉及到是否有多个构造方法匹配了同样的值
这个时候会用值和构造方法类型进行匹配程度打分，找到一个最匹配的
总结：
没有加@Autowired注解，有多个构造方法，返回null。使用无参构造！
没有加@Autowired注解，只有一个有参构造，返回这个构造方法。使用这个构造方法!
没有加@Autowired注解，只有一个无参构造，返回null。使用无参构造！
加了@Autowired注解，只有一个required为true的构造方法，返回这个构造方法。使用这个构造方法!
加了@Autowired注解，有多个required为true的构造方法，抛异常！
加了@Autowired注解，有一个required为true和其他的required为false的构造方法，抛异常！
加了@Autowired注解，没有required为true的构造方法，返回所有required为false的构造方法以及无参构造方法！优先使用参数个数比较长的，公共的构造方法。
    


# 循环依赖
首先需要了解两点
- spring是通过递归的方式来获取目标bean以及其依赖的bean
- spring实例化bean的时候是分两步进行的，首先实例化bean，然后添加属性

解决循环依赖是通过spring的三级缓存来解决的
1，Spring尝试通过ApplicationContext.getBean()方法获取A对象的实例，由于Spring容器中还没有A对象实例，因而其会创建一个A对象
2，然后发现其依赖了B对象，因而会尝试递归的通过ApplicationContext.getBean()方法获取B对象的实例，但是Spring容器中此时也没有B对象的实例，因而其还是会先创建一个B对象的实例
3，此时A对象和B对象都已经创建了，并且保存在Spring容器中了，只不过A对象的属性b和B对象的属性a都还没有设置进去
4，在前面Spring创建B对象之后，Spring发现B对象依赖了属性A，因而此时还是会尝试递归的调用ApplicationContext.getBean()方法获取A对象的实例，因为Spring中已经有一个A对象的实例，
虽然只是半成品（其属性b还未初始化），但其也还是目标bean，因而会将该A对象的实例返回此时，B对象的属性a就设置进去了
5，然后还是ApplicationContext.getBean()方法递归的返回，也就是将B对象的实例返回，此时就会将该实例设置到A对象的属性b中。

# 3， FactoryBean和BeanFactory区别
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

# aop
AOP实现的关键在于AOP框架自动创建的AOP代理，

AOP代理主要分为静态代理和动态代理，静态代理的代表为AspectJ；而动态代理则以Spring AOP为代表
Spring AOP使用的动态代理，所谓的动态代理就是说AOP框架不会去修改原来原来的对象，而是在需要的时候重新生成一个代理对象，这个代理对象包含了目标对象的全部方法，并且在指定的切点做了了处理，而且最终还是回调原对象的方法。

动态代理主要有两种方式，JDK动态代理和CGLIB动态代理。JDK动态代理通过反射来接收被代理的类，并且要求被代理的类必须实现一个接口。JDK动态代理的核心是InvocationHandler接口和Proxy类。

如果目标类没有实现接口，那就要选择使用CGLIB来动态代理目标类。
CGLIB会让生成的代理类继承当前对象，并在代理类中对代理方法进行强化处理(前置处理、后置处理等)。
在CGLIB底层底层实现是通过ASM字节码处理框架来转换字节码并生成新的代理类

注意，CGLIB是通过继承的方式做的动态代理，因此如果某个类被标记为final，那么它是无法使用CGLIB做动态代理的。
# 7， 三级缓存

# 8， ApplicationContext和BeanFactory区别

# 9， 设计模式

# Spring自带的线程池ThreadPoolTaskExecutor
Spring默认也是自带了一个线程池方便我们开发，它就是ThreadPoolTaskExecutor
Spring默认线程池simpleAsyncTaskExecutor
Spring异步线程池的接口类是TaskExecutor，本质还是java.util.concurrent.Executor，没有配置的情况下，默认使用的是simpleAsyncTaskExecutor。
Spring默认的@Async用线程池名字为SimpleAsyncTaskExecutor，而且每次都会重新创建一个新的线程，所以可以看到TaskExecutor-后面带的数字会一直变大。
simpleAsyncTaskExecutor的特点是，每次执行任务时，它会重新启动一个新的线程，并允许开发者控制并发线程的最大数量（concurrencyLimit），
从而起到一定的资源节流作用。默认是concurrencyLimit取值为-1，即不启用资源节流。

## Spring的线程池ThreadPoolTaskExecutor
上面介绍了Spring默认的线程池simpleAsyncTaskExecutor，但是Spring更加推荐我们开发者使用ThreadPoolTaskExecutor类来创建线程池，
其本质是对java.util.concurrent.ThreadPoolExecutor的包装。
这个类则是spring包下的，是Spring为我们开发者提供的线程池类，这里重点讲解这个类的用法。
Spring提供了xml给我们配置ThreadPoolTaskExecutor线程池，但是现在普遍都在用SpringBoot开发项目，所以直接上yaml或者properties配置即可
或者也可以使用@Configuration配置也行，下面演示配置和使用。

## TaskDecorator线程的装饰（跨线程传递ThreadLocal的方案） 
源码分析：
源码：org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor#initializeExecutor
```java
@Override protected ExecutorService initializeExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
    BlockingQueue < Runnable > queue = createQueue(this.queueCapacity);
    ThreadPoolExecutor executor;
    //配置了线程装饰器
    if (this.taskDecorator != null) {
        executor = new ThreadPoolExecutor(this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds, TimeUnit.SECONDS, queue, threadFactory, rejectedExecutionHandler) {@Override public void execute(Runnable command) {
                //装饰线程类
                Runnable decorated = taskDecorator.decorate(command);
                if (decorated != command) {
                    decoratedTaskMap.put(decorated, command);
                }
                //执行原有逻辑
                super.execute(decorated);
            }
        };
    } else {
        executor = new ThreadPoolExecutor(this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds, TimeUnit.SECONDS, queue, threadFactory, rejectedExecutionHandler);
    }
    if (this.allowCoreThreadTimeOut) {
        executor.allowCoreThreadTimeOut(true);
    }
    this.threadPoolExecutor = executor;
    return executor;
}
```
实际使用
声明spring线程池时，需要指定TaskDecorator，在装饰代码中获取到ThreadLocal的值。
```java
@Configuration
@EnableAsync
public class ThreadPoolConfig {
    @Bean("asyncServiceExecutor")
    public ThreadPoolTaskExecutor asyncRabbitTimeoutServiceExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        //核心线程数
        threadPoolTaskExecutor.setCorePoolSize(5);
        //核心线程若处于闲置状态的话，超过一定的时间(KeepAliveTime)，就会销毁掉。
        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
        //最大线程数
        threadPoolTaskExecutor.setMaxPoolSize(10);
        //配置队列大小
        threadPoolTaskExecutor.setQueueCapacity(300);
        //加入装饰器
        threadPoolTaskExecutor.setTaskDecorator(new ContextCopyingDecorator());
        //配置线程池前缀
        threadPoolTaskExecutor.setThreadNamePrefix("test-log-");
        //拒绝策略:只要线程池未关闭，该策略直接在调用者线程中串行运行被丢弃的任务，显然这样不会真的丢弃任务，但是可能会造成调用者性能急剧下降
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
    @Slf4j
    static class ContextCopyingDecorator implements TaskDecorator {
        @Nonnull
        @Override
        public Runnable decorate(@Nonnull Runnable runnable) {
            //主流程
            String res = TestMappingController.local.get();
            log.info("装饰前：" + res);
            //子线程逻辑
            return () -> {
                try {
                    //将变量重新放入到run线程中。
                    TestMappingController.local.set(res);
                    log.info("打印日志-开始");
                    runnable.run();
                } finally {
                    log.info("打印日志-结束");
                }
            };
        }
    }
}
```
业务逻辑：
```
@PostMapping("test2") 
public void test() {
    //主线程执行
    local.set("主线程设置的ThreadLocal");
    //子线程执行
    executor.execute(() - >{
        String res = local.get();
        log.info("线程:" + res);
    });
}
```
执行结果：可以看到，在ContextCopyingDecorator中是主线程在调用，所以可以获取到主线程的ThreadLocal信息。




# BeanPostProcessor后置处理器
AutowiredAnnotationBeanPostProcessor：用来处理@Autowired、@Value
CommonAnnotationBeanPostProcessor：用来处理@Resource、@PostContruct、@PreDestory
ConfigurationPropertiesBindingPostProcessor.register(context.getDefaultListableBeanFactory)：用来解析@ConfigurationProperties


