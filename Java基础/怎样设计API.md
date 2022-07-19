"语言首先是写给人看的，只是恰巧（incidentally）能够运行" ——《计算机程序的构造和解释》。

好的API应对客户端友好，换言之就是能够直接通过其方法签名而理解它做的事情，而不用深入去阅读方法的实现，甚至深入阅读API所在的整个类。

单纯的介绍如何设计好API似乎如"海市蜃楼"般的虚无缥缈，因此本文从设计&实现的角度出发，针对我们在设计并实现API的过程中提出一些小意见。

首先回顾一下API方法的组成模块：

API注释

访问修饰符

返回值

方法名称

参数列表

异常列表

方法主体

针对API方法的组成模块，将提出几点小意见；可简单归纳为："一个原则，三点建议，两个思考，三要五不要"。





# 一原则


最小知识原则（Least Knowledge Principle）
最小知识原则，或称迪米特法则；是一种面向对象程序设计的指导原则，它描述了一种保持代码松耦合的策略。

它描述的是一个软件实体应尽可能少地与其他实体发生相互作用；这里的软件实体是一个广义的概念，可指代系统、类、模块、对象、函数、变量等。

用更加通俗的语言来描述就是：“不应该有直接依赖关系的类之间，不要有依赖；有依赖关系的类之间，尽量只依赖必要的接口”。（“软件实体”替换成“类”）

用一个例子来描述，DatabaseConfig 类为数据库实体类，用以描述数据源信息；JdbcUtils 类用以封装一些基础的JDBC操作。

/**
 * 数据库实体对象
 * @date 2020/9/6
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseConfig {

    private long id;

    private int clusterId;

    private String host;

    private String port;

    private String dbName;

    private String dbType;

    private String jdbcUrl;

    private String username;

    private String password;

    private String dbOwner;

    private String createUser;

    private String updateUser;

    private String createTime;

    private String updateTime;

}
```
/**
 * jdbc底层操作
 * 执行SQL 包装数据等
 * @date 2020/9/6
 **/
public class JdbcUtils {
    // 获取jdbc Connection
    public static Connection getConnection(@NonNull DatabaseConfig databaseConfig) throws ClassNotFoundException, SQLException {
        DbType dbType = DbType.valueOf(databaseConfig.getDbType());
        Class.forName(dbType.getDriver());
        return DriverManager.getConnection(databaseConfig.getJdbcUrl(), databaseConfig.getUsername(), databaseConfig.getPassword());
    }
}
```
这段代码虽然能满足业务需求，但有些地方可以做到更好。JdbcUtils作为一个底层的基础服务类，希望做到尽可能的通用，而不只是支持DatabaseConfig数据源；其次从另外一个角度来看，DatabaseConfig实体中有太多的属性字段，getConnection API到底依赖哪个字段难以确认；所以getConnectionAPI的设计一定程度上违背了 最小知识原则，依赖了不该有的直接依赖关系的DatasourceConfig类。

我们可对JdbcUtils的getConnection方法作以改造，使其满足最小知识原则。我们应该只提供getConnection需要的信息。
```
public static Connection getConnection(@NonNull String driver, @NonNull String jdbcUrl, @NonNull String username, @NonNull String password) throws ClassNotFoundException, SQLException {
    Class.forName(driver);
    return DriverManager.getConnection(jdbcUrl, username, password);
}
```
最小知识原则希望减少类之间的耦合，让类越独立越好。每个类都应该少了解系统中其他部分，这样一旦其他部分发生变化，自身就不会受到影响，避免了“城门失火，殃及池鱼”的发生。





三建议


1. 建议优先使用接口而不是具体实现类
优先使用接口类型作为API的返回值类型或参数类型，有利于提升API的可扩展性。这同时也是设计原则——依赖倒置原则所提倡的。

例如，上面例子中的DataSourceConfig数据源需要加密，应该使用Encryptor加密器接口，而不是具体的KeyCenterEncryptor加密器类；因为随着业务的发展，很有可能出现新的加密类型，使用KeyCenterEncryptor加密器类对会使得难以扩展新的加密类型。

/**
 * 加密器
 * @date 2020/9/6
 **/
public interface Encryptor {


    /**
     * 加密
     * @param str 待加密字符串
     * @return String 加密后的字符串
     */
    public String encrypt(String str);

}

/**
 * keyCenter加密器
 * @date 2020/9/6
 **/
public class KeyCenterEncryptor implements Encryptor {


    @Override
    public String encrypt(String str) {
        // ...

        // 执行keyCenter加密 & return
    }
}
2. 善于利用枚举类型
实际的开发场景中，需求在不断的变更迭代，善于利用枚举类型有利于"留有余地"的处理多样化的需求。并且枚举类型有着简洁、易读、可扩展的优点。

例如，上面所提到的加密类型，可以提供一个枚举类型用于描述。

/**
 * 加密类型枚举类
 * @date 2020/9/6
 */
@Getter
@AllArgsConstructor
public enum EncryptTypeEnum {


    /**
     * 加密类型
     */
    KEY_CENTER(0, "keyCenter加密"),

    MD5(1, "MD5加密")

    // 后续扩展新的加密类型....
    ;


    private final int code;

    private final String desc;

}
3. 统一API命名规则
API的命名应该遵循标准的命名规则，应该选择易于理解，并与同一个包中其他命名风格一致的名称，避免使用长的方法名称；具体可参考《阿里巴巴开发手册》的命名规范。例如：对于具有查询含义的API，可以以queryXXX来命名。





两思考


1. 是否使用Optional？
Optional容器是JAVA8提出的用以解决 "臭名昭著" 的空指针异常（NullPointerException）的一次尝试，当我们在编写某些无返回值的API方法时，Optional的出现提供了一种新的选择方案。但这并不意味着无返回值的API方法都应该使用Optional返回。

在遇到无返回值的时候，通常我们有3种选择方案：1.抛异常 2.返回null 3.返回Optional；实际开发中我们应该选择哪种方案合适呢？或许可以从以下几点做出判断：

Optional的思想和受检异常（Checked Exception）类似，它强迫客户端面对可能没有返回值的事实，而抛出未受检异常（UnChecked Exception）或者返回null没有显式的指明这一点，客户端可能会因忽略这一可能性而产生一些意想不到的后果。

如果API方法可能无法返回结果，并且在无返回结果的时候，客户端还必须执行特殊处理的情况下，应尝试返回Optional。

Optional是必须分配空间和初始化对象的，从Optional中读取值（value）也需要额外的处理；因此如果API方法有很明显的性能上的要求，不建议使用Optional。

下面可以看一个例子：
```
/**
 * 用accessToken换取用户信息
 * @param accessToken
 * @return UserInfo
 */
public Optional<UserInfo> getProfile(String accessToken) {
    if (StringUtils.isEmpty(accessToken)) {
        log.warn("token为空，无法根据token获取userProfile");
        return Optional.empty();
    }
    String url = OauthConstant.CAS_URL + "/oauth2.0/profile?access_token=" + accessToken;
    try {
        // 发送http请求获取用户profile
        // ...省略部分代码
    } catch (Exception e) {
        log.error("获取用户信息发生异常:", e);
        return Optional.empty();
    }
}

/**
 * 模拟API的调用方
 */
public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
            throws Exception {
    // 省略从request获取accessToken过程
    Optional<UserInfo> userOpt = getProfile(token);
    return userOpt.map(userInfo -> {
        // 认证成功的流程 更新用户信息...
        updateUser(userInfo);
        UserHandler.set(userInfo);
        return true;

    }).orElseGet(() -> {
        // 无法获取到用户，重走登录流程（即API使用方在对无返回值的情况作的一些特殊处理）
        boolean login = originalProcess(request, response);
        if (!login) {
            return sendRedirect(request, response);
        }
        return true;
    });
}
```
从例子中我们可以知道，使用accessToken换取userProfile的过程中是有可能换取不到用户信息的（诸如：token为空，或者换取过程发生异常），当返回类型声明为Optional时，客户端（使用方）能很清楚的了解到这一事实（userProfile可能不存在），并且客户端在userProfile不存在的时候，需要重走登录流程（特殊流程）。
总而言之，如果发现API不能总是返回值，并且API的使用方在每次调用时考虑无返回值的情况很重要（可能会对无返回值的情况做一些特殊处理），并且该方法对性能上没有极致的要求，那么可以尝试使用Optional作返回。

2. 是否需要进行保护性拷贝？
《Effective Java3》曾指出，只要能不创建对象，就不要创建多余的对象。只要需要创建对象，就不要吝啬地创建它。前者的目的是尽可能减少不必要的资源消耗，提高运行效率；而后者是出于安全性考虑。

保护性拷贝就是后一半句话的体现，为了保护系统的安全性而选择牺牲部分性能。保护性拷贝可简单的理解为：API方法在返回的时候不希望客户端对返回的数据进行修改而破坏自身的（数据）结构，选择拷贝一份副本数据作为返回。

我们也能从Java API中寻得保护性拷贝的踪迹，LocalDateTime是Java8提出的线程安全的时间类。因为线程安全的需求性，它一旦创建完毕就不允许被修改，所有的修改操作都会生成新的对象返回。

LocalDateTime.class

public static LocalDate ofEpochDay(long epochDay) {
    long zeroDay = epochDay + DAYS_0000_TO_1970;
    // find the march-based year
    zeroDay -= 60;  // adjust to 0000-03-01 so leap day is at end of four year cycle
    // 中间省略部分代码...
    // check year now we are certain it is correct
    int year = YEAR.checkValidIntValue(yearEst);
    return new LocalDate(year, month, dom);
}
保护性拷贝同时也可发生在客户端，当API方法返回的是一个不可变的对象时，而客户端希望进行修改，那么就可以进行保护性拷贝，生产一份副本数据用以满足自身的需要。
总而言之，在获取API方法返回的对象引用时，可以思考一下：这个对象是否可变？自己的后续操作是否希望这个对象可变，来决定是否有必要进行保护性拷贝。





三要


1. 参数有效性检查
大多数的API方法对于传递接收的参数都有限制，例如索引值不能为负数、对象引用不能为null等；因此在注释文档中标注这些限制并且在方法体的开始进行参数有效性的检查是非常有必要的，这能够让我们在发生错误之后尽快检测到错误的来源，避免错误向下扩散。

对于公有的方法，要用javadoc的@throw标签在文档中说明违反参数值限制时会抛出的异常。这样的异常通常为 IllegalArgumentException，IndexOutOfBoundsException 或 NullPointerException。

/**
 * id查询用户
 * @param id 自增主键id
 * @return User
 * @throws IllegalArgumentException if id is less than or equal 0
 */
public User queryById(Integer id) {
    if (id <= 0) {
        throw new IllegalArgumentException("id <= 0 :" + id);
    }
    // do something & return...
}
Tips：很多时候我们的参数对象引用会要求不能为空，如果在每个方法javadoc注释上都单独标记这一约束会显得十分的冗余；这个时候我们可以使用类级注释，类级注释适用于类的所有公共方法中的所有参数。
如果没有做参数有效性检查，有可能会发生以下这两种情况：

方法在处理的过程中失败，产生了令人难以理解的异常（参数向下延申扩展），客户端不得不根据完整异常栈信息进行逐步排查。

方法计算异常但是正常返回，返回了计算出错的结果（脏数据的来源之一）。

无论是哪种情况，都会对客户端造成不必要的困扰，这并不是我们所希望看到的。

这是否意味着对于所有的参数都需要进行有效性检查呢？答案是否定的。有些情况下的参数检查的成本是十分昂贵且不切实际。比如：考虑一个为对象列表排序的方法：Collections.sort(list)；列表中的所有对象都必须是可以相互比较的。这个时候如果我们提前对集合list做每个元素是否可比较的检查，其实没有什么实际意义；因为sort方法会进行相关的检查。

这种由计算行为进行的检查称为隐式有效性检查，如果检查不成功会抛出错误的异常（有可能和我们javadoc标注的异常类型不一致）；这时我们应该对异常进行兜底转换，转换成我们申明的异常类型。

总而言之，在编写API方法的时候，我们需要考虑参数有哪些限制，在文档中声明这些限制并且在方法体的开始处，显式的检验这些限制。

2. 返回长度为零的数组或集合，而非null
当API方法的返回值类型为数组或者集合的时候，遇到无法返回的情况，我们应该返回对应的空数组或空集合，而不是返回null。

对于返回null的API，客户端在使用的时候每次都需要做与业务逻辑无关非空判断以增强自身代码的健壮性，对于"不那么严谨"的程序员来说，可能会因为忘记做非空判断来处理null返回值，以至于在未来的某一天因此发生一些"匪夷所思"的错误。

常规通用版——返回长度为零的空集合。
```
/**
 * 获取拥有这张表权限的所有人
 * @param dataId
 * @return List<String>
 */
@Override
public List<String> getOdsTableUsers(Integer dataId) {
    if (dataId == null) {
        return new ArrayList<>(0);
    }
    try {
        // 执行查询 获取拥有该dataId对应数据表 有权限用户集userlist
        return new ArrayList<>(userlist);
    } catch (Exception e) {
        log.error("获取拥有数据表Id:{} 权限用户集失败", dataId, e);
        return new ArrayList<>(0);
    }
}
```
优化慎用版——返回共有的不可变空集合，以避免分配空间。
```
public List<String> getOdsTableUsers(Integer dataId) {
    if (dataId == null) {
        return Collections.emptyList();
    }
    // do something & return ....
}
```
Collections.emptyList() 方法返回的是 Collections 类定义的常量 public static final List EMPTY_LIST = new EmptyList<>()；
这么做能避免多次分配空间，理论上在性能上有一定的优化。但实际开发中不建议这么使用，假设有这么一个场景，客户端调用该方法后发现返回的为空集合，转而有其他的操作，那很可能会发生意想不到的错误。

/**
 * 模拟API接口 返回公有的不可变空集合
 * @param dataId 数据表id
 * @return List<String>拥有数据表访问权限的用户集合
 */
public static List<String> queryUserList(Integer dataId) {
    // 测试样例直接返回公有的不可变空集合
    return Collections.emptyList();
}

/**
 * 模拟客户端
 * @param dataId 数据表id
 */
public static void doFunnyThing(Integer dataId) {
    List<String> userList = queryUserList(dataId);
    if (userList.isEmpty()) {
        // 当发现userList为空的时候，希望做一些其他的操作 这时候产生意想不到的异常。
        String user = "i am user";
        userList.add(user);
    }
    // do some funny thing
}

public static void main(String[] args) {
    Integer dataId = 1;
    doFunnyThing(dataId);
}

// 运行结果：
Exception in thread "main" java.lang.UnsupportedOperationException
        at java.util.AbstractList.add(AbstractList.java:148)
        at java.util.AbstractList.add(AbstractList.java:108)
        at com.kylin.mhr.controller.TemporaryController.doFunnyThing(TemporaryController.java:24)
        at com.kylin.mhr.controller.TemporaryController.main(TemporaryController.java:30)
总之，永远不要返回null来代替返回空集合或空数据，这会让API更加的难以使用，容易出错，且没有性能上的优势。
3. 规范文档注释
如果要想一个API真正可用，就必须为其编写文档。API的文档注释应该简洁的描述它和客户端之间的约定，这个约定是指：做了什么，而非怎么做的。

API文档注释应包含：

所有的前提条件——客户端调用它的必要条件（例如：客户端传递的参数）

后置条件——API调用成功后发生的事情（例如：返回数据）

异常描述——前提条件是由@throw 标签针对未受检异常的隐含描述，每个未受检异常都对应一个违背前提条件的例子。

为了完整地描述方法的约定，文档注释应该为每个参数都使用一个@Param标记，方法使用@return标记返回类型(除非方法的返回类型是 void)，以及对于该方法抛出的每个异常，无论是受检的还是未受检的，都有一个@throws标签。

随意截取了Java API中LocalDateTime.class中的注释，供大家瞅瞅。

/**
 * Returns a copy of this {@code LocalDateTime} with the specified number of hours added.
 * <p>
 * This instance is immutable and unaffected by this method call.
 *
 * @param hours  the hours to add, may be negative
 * @return a {@code LocalDateTime} based on this date-time with the hours added, not null
 * @throws DateTimeException if the result exceeds the supported date range
 */
public LocalDateTime plusHours(long hours) {
    return plusWithOverflow(date, hours, 0, 0, 0, 1);
}
总而言之，规范文档注释十分的有必要。在实际的开发中，我们可以使用类似SonarLint等插件用以检查自己编写的API文档是否完备。




五不要


1. 避免过长的参数列表
避免过长的参数列表，参数不应该超过4个；参数过多会导致API不利于使用，调用方需要不断的阅读文档来理解。更应该避免相同类型的长参数，相同类型的长参数非常容易引发"不可预知的风险"——调用方弄错了参数的顺序，但是程序还能正常的编译运行，导致与预期不符的错误结果。

在实际的开发中，很有可能会出现需要的参数超过4个的情况，这个时候我们可以采取一些方法用以缩短参数列表。

方法拆解


将参数列表过长的API方法进行细化拆解，每个方法的参数列表只需要原有参数的子集。这样一定程度上会导致方法过多，但可通过方法间的正交性；去除部分方法。

创建参数辅助类


创建辅助类用以保存参数的分组。比如实时同步的注册topic操作，随着同步服务的升级，使用方在注册的时候新增org相关参数。这时候可以将所有的注册参数抽离封装成一个注册参数实体RegisterParamEntity。

总而言之，简短的参数列表对客户端更加的友好。

2. 避免可变参数
可变参数可接受零个或多个指定类型的参数，可变参数机制通过先创建一个数组，数组的大小等于调用时所传递的参数数量，然后将参数值传递到数组中，最后将数组传递给方法。

小声BB：在我有限的工作时间内，我倒是没见过含有可变参数的API，所以还是不用这东西吧~~

3. 避免相同参数数量的重载方法
重载方法（overloaded method）的调用是在编译时所决定的，是静态的；重写方法(overridden method)的调用是在运行时决定，是动态的。可能会因为记忆或理解上的偏差，而产生错误的使用方式。因此，安全而保守的策略是：避免导出两个相同参数数量的重载方法，因为我们始终可以给方法起不同的名，而非使用重载机制。

我们可以从JAVA的API中看出这一思想，ObjectOutputStream.class 中的write方法，并没有选择相同参数数量的重载机制，而是选择命名上做区分。

public void writeInt(int val)  throws IOException {
    bout.writeInt(val);
}

public void writeLong(long val)  throws IOException {
    bout.writeLong(val);
}

public void writeFloat(float val) throws IOException {
    bout.writeFloat(val);
}
4. 避免过度追求提供便利的方法
每个API方法都应该尽其所能。方法太多会使类难以学习、使用、文档化、测试和维护，应当尽量避免一些临时性质的API方法。

对于接口而言，方法太多会使接口实现者和接口使用者的工作变得复杂起来。对于类和接口所支持的每个动作，都提供一个功能齐全的方法。只有当一项操作被经常用到的时候，才考虑为它提供快捷方式（shorthand）。如果不能确定，还是不要提供快捷方式为好。

5. 避免过度或错误的使用Optional
Optional可以用来表示无返回的情况，但这并不意味着所有无返回的情况都应该用Optional。过度或错误的使用Optional 可能会在性能上或者理解上造成不必要的困扰。

对于对象的属性有可能为空的情况，是否有必要使用Optional封装？我认为这是一种过度使用的表现，我们应该关注的是业务本身，而Optional的功能仅应该用于API方法的返回值。

Optional本质上是一个最多可容纳一个元素的不可变集合，因此对于容器类型的返回值，是不能使用Optional来封装的，诸如：Collections、Map、Set、Stream、Array、Optional....另外，我们也不应该对返回自动装箱的基本类型使用Optional（诸如：Optional<Interger>），这样会导致2个级别的装箱操作，成本非常的高；应该使用诸如OptionalInt的存在。