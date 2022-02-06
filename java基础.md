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
    T和？运用的地方有点不同,?是定义在引用变量上,T是类上或方法上
    “<T>"和"<?>"，首先要区分开两种不同的场景：
        类型参数“<T>”主要用于第一种，声明泛型类或泛型方法。
        无界通配符“<?>”主要用于第二种，使用泛型类或泛型方法
    ?号通配符表示可以匹配任意类型，任意的Java类都可以匹配。
    只能调对象与类型无关的方法，不能调用对象与类型有关的方法。
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


java运行机制
volatile能创建数组么
为什么要实现序列化

T是什么意思


Thread.yield();