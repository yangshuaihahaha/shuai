# 枚举
枚举是一种数据类型，事实上是一种受限制的类，并有自己的方法，创建枚举时编译器会自动为你生成一个相关的类，这个类继承自java.lang.Enum
除了不能继承，可以将enum看作一个常规的类

1,枚举中提供的基础方法：
    values(): 返回enum实例数组，而且数组中的元素严格保持在enum中声明时的顺序
    name(): 返回实例名
    ordinal(): 返回实例声明时的次序，从0开始
    getDeclaringClass(): 返回实例所属的enum类型
    equals(): 判断是否同一个对象
    可以通过 == 来比较enum实例

2,枚举可以添加普通方法、静态方法、抽象方法、构造方法
    ```
    public enum ErrorCode {
        OK(0) {
            public String getDescription() {
                return "成功";
            }
        },
        ERROR_A(100) {
            public String getDescription() {
                return "错误A";
            }
        },
        ERROR_B(200) {
            public String getDescription() {
                return "错误B";
            }
        };
        private int code;
        // 构造方法：enum的构造方法只能被声明为private权限或不声明权限
        private ErrorCode(int number) { // 构造方法
            this.code = number;
        }
        public int getCode() { // 普通方法
            return code;
        } // 普通方法
        public abstract String getDescription(); // 抽象方法
        public static void main(String args[]) { // 静态方法
            for (ErrorCode s : ErrorCode.values()) {
                System.out.println("code: " + s.getCode() + ", description: " + s.getDescription());
            }
        }
    }
    ```
3,枚举可以实现接口
    ```
    public interface INumberEnum {
        int getCode();
        String getDescription();
    }
    public enum ErrorCodeEn2 implements INumberEnum {
        OK(0, "成功"),
        ERROR_A(100, "错误A"),
        ERROR_B(200, "错误B");
        ErrorCodeEn2(int number, String description) {
            this.code = number;
            this.description = description;
        }
        private int code;
        private String description;
        @Override
        public int getCode() {
            return code;
        }
        @Override
        public String getDescription() {
            return description;
        }
    }
    ```
4,枚举不可以继承
    enum 不可以继承另外一个类，当然，也不能继承另一个 enum 。
5,EnumMap和EnumSet
    EnumMap
        EnumMap几乎和HashMap是一样的，区别在于EnumMap的key是一个Enum
        ```
        public enum Types {
            RED, GREEN, BLACK, YELLO
        }
        @Test
        public void useEnumMap(){
            EnumMap<Types, String> activityMap = new EnumMap<>(Types.class);
            activityMap.put(Types.BLACK,"black");
            activityMap.put(Types.GREEN,"green");
            activityMap.put(Types.RED,"red");
        }
        ```
        什么时候使用EnumMap？
            因为在EnumMap中，所有的key的可能值在创建的时候已经知道了，所以使用EnumMap和hashMap相比，可以提升效率。
            同时，因为key比较简单，所以EnumMap在实现中，也不需要像HashMap那样考虑一些复杂的情况
    EnumSet
        EnumSet 是枚举类型的高性能 Set 实现。它要求放入它的枚举常量必须属于同一枚举类型。
            ```
            public enum EnumColor
            {
                RED, GREEN, PINK, YELLOW, BLACK
            }
            //如果我们想获取EnumColor类的所有枚举实例，那么有2种方式：
            // 返回数组
            EnumColor[] values  = EnumColor.values();
            for(EnumColor each : values)
            {
                System.out.println(each);
            }
             
            // 返回EnumSet
            EnumSet<EnumColor> allSet = EnumSet.allOf(EnumColor.class);
            for(EnumColor each : allSet)
            {
                System.out.println(each);
            }
            
            //使用EnumSet可以获取某个范围的枚举实例：
            EnumSet<EnumColor> partialSet = EnumSet.range(EnumColor.PINK, EnumColor.BLACK);
            System.out.println(partialSet);// [PINK, YELLOW, BLACK]
            ```
5,枚举应用场景
    组织常量：
        ```
        enum Color { RED, GREEN, BLUE }
        enum Color { RED, GREEN, BLUE, }
        enum Color { RED, GREEN, BLUE; }
        ```
    switch:
        ```
        enum Signal {RED, YELLOW, GREEN}
        public static String getTrafficInstruct(Signal signal) {
            String instruct = "信号灯故障";
            switch (signal) {
                case RED:
                    instruct = "红灯停";
                    break;
                case YELLOW:
                    instruct = "黄灯请注意";
                    break;
                case GREEN:
                    instruct = "绿灯行";
                    break;
                default:
                    break;
            }
            return instruct;
        }
        ```
    组织枚举:
        可以将类型相近的枚举通过接口或者类组织起来
        但是一般使用接口方式进行组织。原因是：java接口编译时自动为enum类型加上public static修饰符；而java类编译时只会加上static修饰符
        ```
        public interface Plant {
            enum Vegetable implements INumberEnum {
                POTATO(0, "土豆"),
                TOMATO(0, "西红柿");
                Vegetable(int number, String description) {
                    this.code = number;
                    this.description = description;
                }
                private int code;
                private String description;
                @Override
                public int getCode() {
                    return 0;
                }
                @Override
                public String getDescription() {
                    return null;
                }
            }
            enum Fruit implements INumberEnum {
                APPLE(0, "苹果"),
                ORANGE(0, "桔子"),
                BANANA(0, "香蕉");
                Fruit(int number, String description) {
                    this.code = number;
                    this.description = description;
                }
                private int code;
                private String description;
                @Override
                public int getCode() {
                    return 0;
                }
                @Override
                public String getDescription() {
                    return null;
                }
            }
        }
        ```
    错误码枚举类型定义
       ```
       public enum ErrorCodeEn {
           OK(0, "成功"),
           ERROR_A(100, "错误A"),
           ERROR_B(200, "错误B");
           ErrorCodeEn(int number, String description) {
               this.code = number;
               this.description = description;
           }
           private int code;
           private String description;
           public int getCode() {
               return code;
           }
           public String getDescription() {
               return description;
           }
       }
       ```
# 泛型
泛型是JDK1.5之后出来的新特性，目的是为了解决类型转换的安全问题，是一个类安全机制
Java的泛型是伪泛型，这是因为Java在编译期间会把所有的泛型信息擦除掉。
Java泛型基本上都是在编译器层次上实现的，在生成字节码中是不包含这些泛型中的类型信息的
泛型的好处：
    1，类型安全：
        在运行期间出现的问题，转移到编译时期。减少出错概率
    2，消除强制类型转换：
        没有泛型的返回，我们可以认为是一个Object，在使用时需要对区进行强制转换。这样代码更加复杂，而且容易出错
        ```
        private <T> T getListFisrt(List<T> data) {
            if (CollectionUtils.isEmpty(data)) {
                return null;
            }
            return data.get(0);
        }
        ```
        如果这里使用Object就会变得异常麻烦
        ```
        private Object getListFisrt(List<Object> data) {
            if (CollectionUtils.isEmpty(data)) {
                return null;
            }
            return data.get(0);
        }
        User user = (User)getListFisrt(UserList);
        Privilege privilege = (User)getListFisrt(PrivilegeList);
        Role role = (Role)getListFisrt(RoleList);
        ```
    3，提高性能
        在泛型的初始实现中，编译器将强制类型转换（没有泛型的话，程序员会执行这些强制类型转换）插入字节码中
        但是更多类型信息可用于编译器这一事实，为未来版本的JVM优化带来可能
        由于泛型的实现方式，支持泛型几乎不需要JVM或类文件修改
        所有工作都在编译器中完成，编译器生成类似于没有泛型时所写的代码，只是更能确保类型安全
类型擦除？
    使用泛型的时候加上类型参数，在编译器编译的时候去掉，这个过程称为类型擦除
泛型的使用：
    泛型主要有三种使用方式：
        泛型类：
            泛型类型用于类的定义中，称为泛型类。通过泛型可以完成对一组类的操作对外开放形同的接口
            用于类的定义中，最典型的就是各种集合框架容器类比如List、Map、Set
            ```
            public class GenericsClassDemo<T> {
                //t这个成员变量的类型为T,T的类型由外部指定
                private T t;
                //泛型构造方法形参t的类型也为T，T的类型由外部指定
                public GenericsClassDemo(T t) {
                    this.t = t;
                }
                //泛型方法getT的返回值类型为T，T的类型由外部指定
                public T getT() {
                    return t;
                }
            }
            ```
        泛型方法：
            是在调用方法的时候指明泛型的具体类型
            定义格式：修饰符 <代表泛型的变量> 返回值类型 方法名(参数){  }
            ```
            public <T> T getListFisrt(List<T> data) {
                if (CollectionUtils.isEmpty(data)) {
                    return null;
                }
                return data.get(0);
            }
            ```
            public 与 返回值中间<T>非常重要，可以理解为声明此方法为泛型方法。
            只有声明了<T>的方法才是泛型方法，泛型类中的使用了泛型的成员方法并不是泛型方法。
            <T>表明该方法将使用泛型类型T，此时才可以在方法中使用泛型类型T。
        泛型接口：
            泛型接口和泛型方法的定义基本相同。泛型接口常被用在各种类的生产器中
            定义格式：修饰符 interface接口名<代表泛型的变量> {  }
泛型通配符
    1，无边界通配符
        无边界通配符主要作用是让泛型能够接收未知类型的数据
        使用方式：采用<?>的形式，比如List<?>，
    2，固定上边界通配符
        使用固定上边界通配符，就能够接收指定类及其子类型的数据
        使用方式：采用 <? extends E> 的形式，这里的 E 就是该泛型的上边界
    3，固定下边界通配符
        使用固定下边界通配符，能够指定类及其父类型的数据。
        使用方式：<? super E>
通配符使用原则
    只从方法的形参集合获取值，那么使用"? extends T"上界通配符
    只从方法的形参集合写入值，那么使用"? super T"下界通配符
    如果既要存又要取，那么就不要使用通配符了
泛型的各种表示方式
    ? 表示不确定的Java类型    
    E — Element，常用在java Collection里，如：List<E>,Iterator<E>,Set<E>
    K,V — Key，Value，代表Map的键值对
    N — Number，数字
    T — Type，类型，如String，Integer等等
    这里的E,K,V,N,T可以是任何字母，只不过写成这样是为了更有代表意义
    这个<T> T 表示返回值T的类型是泛型，T是一个占位符，用来告诉编译器，这个东西是先给我留着， 等我编译的时候再告诉你是什么类型。
    private <T> T getListFisrt(List<T> data) {
        if (CollectionUtils.isEmpty(data)) {
          return null;
        }
        return data.get(0);
    }
T和？
    1，T表示一种类型，?是通配符，泛指所有的类型
    2，T和？运用的地方有点不同，?是定义在引用变量上，T是类或接口或方法上
Java中泛型T和Class<T>以及Class<?>
    单独的T代表一个类型，而Class<T>和Class<?>代表这个类型所对应的类
    Class<T>在实例化的时候，T要替换成具体类
    Class<?>它是个通配泛型，?可以代表任何类型   
    <? extends T>受限统配，表示T的一个未知子类。
    <? super T>下限统配，表示T的一个未知父类。
public T find(Class<T> clazz, int id);
    根据类来反射生成一个实例，而单独用T没法做到。
<T> T 用法
    这个<T> T 表示的是返回值T是泛型，T是一个占位符，用来告诉编译器，这个东西先给我留着，等我编译的时候，告诉你。
    public class Demo {
        public static void main(String[] args) {
            Demo demo = new Demo();
            //获取string类型
            List<String> array = new ArrayList<String>();
            array.add("test");
            array.add("doub");
            String str = demo.getListFisrt(array);
            System.out.println(str);
            //获取nums类型
            List<Integer> nums = new ArrayList<Integer>();
            nums.add(12);
            nums.add(13);
            Integer num = demo.getListFisrt(nums);
            System.out.println(num);
        }
        /**
         * 这个<T> T 可以传入任何类型的List
         * 参数T
         *     第一个 表示是泛型
         *     第二个 表示返回的是T类型的数据
         *     第三个 限制参数类型为T
         * @param data
         * @return
         */
        private <T> T getListFisrt(List<T> data) {
            if (data == null || data.size() == 0) {
                return null;
            }
            return data.get(0);
        }
    
    }
T 用法
    返回值，直接写T表示限制参数的类型，这种方法一般多用于共同操作一个类对象，然后获取里面的集合信息啥的。
    public class Demo2<T> {
        public static void main(String[] args) {
            //限制T 为String 类型
            Demo2<String> demo = new Demo2<String>();
            //获取string类型
            List<String> array = new ArrayList<String>();
            array.add("test");
            array.add("doub");
            String str = demo.getListFisrt(array);
            System.out.println(str);
            //获取Integer类型 T 为Integer类型
            Demo2<Integer> demo2 = new Demo2<Integer>();
            List<Integer> nums = new ArrayList<Integer>();
            nums.add(12);
            nums.add(13);
            Integer num = demo2.getListFisrt(nums);
            System.out.println(num);
        }
        /**
         * 这个只能传递T类型的数据
         * 返回值 就是Demo<T> 实例化传递的对象类型
         * @param data
         * @return
         */
        private T getListFisrt(List<T> data) {
            if (data == null || data.size() == 0) {
                return null;
            }
            return data.get(0);
        }
    }


# volatile能创建数组么？
可以创建volatile数组，但是只是一个指向数组的引用，而不是整个数组，如果改变了引用指向的数组，将会受到volatile的保护
但是如果多个线程同时改变数组元素，volatile就起不到保护作用了

# 为什么要实现序列化
什么是序列化？
    序列化就是将对象转换为可传输的字**节流的过程
为什么要序列化？
    序列化的最终目的是为了对象可以跨平台存储，进行网络传输
序列化的实现
    将需要被序列化的类实现Serializable接口，该接口没有需要实现的方法，实现该接口主要是为了标注对象是可以被序列化的
    然后就可以通过ObjectOutputStream进行对象的传输。
    serialVersionUID是为了在反序列化的过程中判断版本是否一致，如果发送者和接收者的serialVersionUID一致，可以进行反序列化，否则抛出异常

# 注解
什么是注解？
    注解也叫元数据，是代码级别的说明，与类、接口枚举在同一层次
    可以声明在包、类、字段、方法、局部变量、方法参数等前面，用来对这些元素进行说明、注释
    简单来说就是代码中的特殊标记，这些标记可以在编译、类加载、运行时被读取，并执行相应的处理
注解分为三类：
    1，Java自带的注解：
        包括@Override（标明重写某个方法）、@Deprecated（标明某个类或方法过时）和@SuppressWarnings（标明要忽略的警告）
        使用这些注解后编译器会进行检查
    2，元注解：
        元注解是用于定义注解的注解
        @Retention - 标识这个注解怎么保存，是只在代码中，还是编入class文件中，或者是在运行时可以通过反射访问。
        @Documented - 用于指定被修饰的注解类将被javadoc工具提取成文档。
        @Target - 标记这个注解应该是作用于类，方法，字段
        @Inherited - 标记这个注解是继承于哪个注解类(默认 注解并没有继承于任何子类)
    3，自定义的注解
        根据自己的需求定义的注解
怎样自定义一个注解
    使用@interface
    注解中可以自定义成员变量，用于信息描述，和接口方法中的定义类似
        ```
        public @interface EnableAuth {
            String name();
        }
        ```
    还可以添加默认值：
        ```
        public @interface EnableAuth {
            String name() default "帅";
        }
        ```
    加上元注解信息
        ```
            @Target(ElementType.METHOD)
            @Retention(RetentionPolicy.RUNTIME)
            @Documented
            public @interface EnableAuth { }
        ```
如何获取注解中的值
    可以通过反射来判断类，方法，字段上是否有某个注解以及获取注解中的值, 获取某个类中方法上的注解代码示例如下：
        ```java
        Class<?> clz = bean.getClass();
        Method[] methods = clz.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(EnableAuth.class)) {
                String name = method.getAnnotation(EnableAuth.class).name();
            }
        }
        ```
注解原理：
    解析一个类或者方法的注解往往有两种形式，一种是编译期直接扫描，一种是运行期反射
    编译期扫描
        指的是在编译期对java代码检测，某个类或者方法被注解修饰，这时就会对这些注解进行处理
        最典型的就是@Override，一旦编译器监测到某个方法被@Override修饰了，就会检查当前方法是否重写了父类方法
    运行期反射：
        这里的注解一定是结合反射来运行的。
        注解相当于一个标识，不做具体的操作，具体操作是由反射来完成的。
        我们通过反射获取注解时，返回的是通过动态代理机制生成一个实现我们注解接口的代理类

# 领域驱动设计：
以往的系统中需求分析和系统设计都是分离的，这样割裂的设计导致很多软件在开发完成甚至投入使用后发现系统设计不符合需求
领域驱动核心应用就是解决复杂业务的设计问题，统一了分析和设计编程，使得软件能够更加灵活快速跟随需求变化
![img.png](images/领域驱动设计.png)
1，用户接口层（user interface）：
负责展示前端界面，即用户可见、可操作的前端展示，主要采用 MVC的模式（MVC（ model - view - controller） 是一种架构型模式，由模型（ model） 、视图（ view） 、控制器（ controller） 组成）。
2，应用层（application）：
负责响应用户界面层的各种请求，且不包含任何业务逻辑，仅根据相应控制逻辑调用领域层中涉及的不同服务、接口或数据。这一层关系到后续系统能做些什幺，它的主要任务就是将工作委托给领域对象进行实操。
3，领域层（domain）
负责规定整体的业务逻辑，是整体领域设计架构的关键内容。也可以命名为业务领域，是重中之重，也是必要环节。
个基本的域模型会包含 Entity、Value Object、Service、Factory、Repository
4，基础设施层（infrastructure）
是整个框架的底层，为其他层提供通用的技术实现能力，比如数据访问、网络通信、邮件发生等。有效的信息可以使得整个系统在运行时更加牢固，快递实现技术需求。


T是什么意思


# 怎么设计接口






# 接口安全 
1，使用MD5实现对接口加签，目的是为了防止篡改数据。
2. 基于网关实现黑明单与白名单拦截
3. 可以使用rsa非对称加密 公钥和私钥互换
4. 如果是开放接口的话，可以采用oath2.0协议
5. 使用Https协议加密传输,但是传输速度慢
6. 对一些特殊字符实现过滤 防止xss、sql注入的攻击
7. 定期使用第三方安全扫描插件
8. 接口采用dto、do实现参数转化 ，达到敏感信息脱敏效果
9. 使用token+图形验证码方法实现防止模拟请求
10. 使用对ip访问实现接口的限流,对短时间内同一个请求(ip)一直访问接口 进行限制。

Thread.yield();