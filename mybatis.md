# 什么是mybatis
1，mybatis是一个半ORM（对象关系映射）框架，它内部封装了jdbc。在开发时只需要关注sql本身。
直接编写原生的sql，可以严格的控制sql执行的性能，灵活度高。
2，mybatis通过xml文件或者注解的方式将要执行的各种statement配置起来，通过java对象和
statement中的sql动态参数进行映射生成最终的sql语句，最后由mybatis框架执行sql并将结果映射为java对象返回

# 适合的场景
1，mybatis专注于sql本身，是一个灵活的dao层解决方案
2，对性能的要求很高，或者需求变化较大的项目，比如互联网项目

# mybatis和hibernate有什么不同
## mybatis
优点
1，基于sql编程，相当灵活
2，sql写在xml文件里面，解除了sql和应用程序代码耦合，便于统一管理
3，提供映射标签，支持对象和数据库orm字段的关系映射
4，提供对象关系映射标签，支持对象关系组件维护
5，查询速度快
缺点
1，关联表多的时候，sql工作量很大
2，sql依赖数据库，导致数据库的可移植性差
3，由于xml的标签id必须唯一，导致dao方法不支持重载
4，dao层过于简单，对象组装工作量大
5，不支持级联更新，级联删除
6，mybatis的日志除了基本的记录功能外，其他的功能薄弱
7，编写动态sql时，不方便调试
8，提供的写动态sql标签功能简单，编写动态sql受限，可读性低
## hibernate
优点
1，hibernate的对象/关系映射能力强
2，有更好的二级缓存机制
3，有更好的数据库移植性
4，拥有完整的日志系统。包括sql记录，关系异常，优化警告，缓存提示，脏数据警告
缺点
1，学习门槛高，程序员如何设计O/R映射，在性能和对象模型之间取得平衡
2，hibernate的sql很多都是自动生成的，无法直接维护sql。虽然支持原生的sql，但是灵活程度上不如mybatis

# #{}和${}的区别是什么
{}是预编译处理，${}是字符串替换。使用#{}可以有效的防止 SQL 注入，提高系统安全性。

# 当实体类中的属性名和表中的字段名不一样 ，怎么办 
1，通过在查询的 sql 语句中定义字段名的别名， 让字段名的别名和实体类的属性名一致。
```
<select id=”selectorder”  parametertype=”int”  resultetype=” me.gacl.domain.order”>
    select order_id id, order_no orderno ,order_price price form orders where order_id=#{id};
</select>
```
2，通过来映射字段名和实体类属性名的一一对应的关系。
```
<select id="getOrder" parameterType="int" resultMap="orderresultmap">
select * from orders where order_id=#{id}
</select>
<resultMap type=”me.gacl.domain.order”  id=”orderresultmap”>
<!–用 id 属性来映射主键字段–>
<id property=”id”  column=”order_id”>
<!–用 result 属性来映射非主键字段，property 为实体类属性名，column 为数据表中的属性–>
<result property =  “orderno”  column =”order_no”/>
<result property=”price”  column=”order_price”  />
</reslutMap>
```
# 通常一个Xml 映射文件，都会写一个Dao 接口与之对应，请问，这个Dao 接口的工作原理是什么？Dao 接口里的方法， 参数不同时，方法能重载吗
Dao接口就是Mapper接口。接口的全限名就是映射文件中namespace值，接口中的方法名就是映射文件中mapper中statement的id值，
接口方法内的参数就是传递给sql的参数。

Mapper 接口是没有实现类的，当调用接口方法时，接口全限名+方法名拼接字符串作为 key 值， 可唯一定位一个 MapperStatement。在 Mybatis 中， 每一个
select、insert 、update、delete标签，   都会被解析为一个MapperStatement 对象.
举例： com.mybatis3.mappers.StudentDao.findStudentById， 可以唯一 找 到 namespace 为 com.mybatis3.mappers.StudentDao 下 面 id 为findStudentById 的 MapperStatement 。
Mapper 接口里的方法，是不能重载的，因为是使用 全限名+方法名
的保存和寻找策略。Mapper 接口的工作原理是 JDK 动态代理， Mybatis 运行时会使用 JDK 动态代理为 Mapper 接口生成代理对象 proxy， 代理对象会拦截接口方法， 转而执行 MapperStatement 所代表的 sql， 然后将 sql 执行结果返回。

# mybatis是如何进行分页的，分页插件原理是什么
1，如果使用RowBounds进行分页的话，它是针对ResultSet结果集进行的内存分页，并非物理分页。
2，可以在 sql 内直接书写带有物理分页的参数来完成物理分页功能， 也可以使用分页插件来完成物理分页。
3，分页插件的基本原理是在插件的拦截方法内拦截待执行的 sql，然后重写 sql，根据 dialect 方言，添加对应的物理分页语句和物理分页参数。

# Mybatis 是如何将sql 执行结果封装为目标对象并返回的？ 都有哪些映射形式？
1，第一种是使用标签， 逐一定义数据库列名和对象属性名之间的映射关系。
2，第二种是使用 sql 列的别名功能， 将列的别名书写为对象属性名。
有了列名与属性名的映射关系后， Mybatis 通过反射创建对象， 同时使用反射给对象的属性逐一赋值并返回， 那些找不到映射关系的属性， 是无法完成赋值的。

# 如何获取自动生成的(主)键值?
insert 方法总是返回一个 int 值 ， 这个值代表的是插入的行数。
如果采用自增长策略，自动生成的键值在 insert 方法执行完后可以被设置到传入的参数对象中。
```
<insert id=”insertname”  usegeneratedkeys=”true”  keyproperty=” id”>

insert into names (name) values (#{name})

</insert>

name name = new name(); name.setname(“fred”);
int rows = mapper.insertname(name);
// 完成后,id 已经被设置到对象中system.out.println(“rows inserted =  ”  + rows);
system.out.println(“generated key value =  ”  + name.getid());
```
# 在 mapper 中如何传递多个参数?
1、第一种： DAO 层的函数
public UserselectUser(String name,String area);
对应的 xml,#{0}代表接收的是 dao 层中的第一个参数，#{1}代表 dao 层中第二参数，更多参数一致往后加即可。
```
<select id="selectUser" resultMap="BaseResultMap"> 
    select * fromuser_user_t whereuser_name = #{0} anduser_area=#{1}
</select>
```
2、第二种： 使用 @param 注解:
```
public interface usermapper {
    user selectuser(@param(“username”) string username,@param(“hashedpassword”) string hashedpassword);
}
```
然后,就可以在 xml 像下面这样使用(推荐封装为一个 map,作为单个参数传递给mapper):
```
<select id=”selectuser”  resulttype=”user”> 
    select id, username, hashedpassword from some_table where username = #{username} and hashedpassword = #{hashedpassword}
</select>
```
3、第三种： 多个参数封装成 map
```
try {
    //映射文件的命名空间.SQL 片段的 ID，就可以调用对应的映射文件中的SQL
    //由于我们的参数超过了两个，而方法中只有一个 Object 参数收集，因此我们使用 Map 集合来装载我们的参数
    Map < String, Object > map = new HashMap(); 
    map.put("start", start);
    map.put("end", end);
    return sqlSession.selectList("StudentID.pagination", map);
} catch (Exception e) { e.printStackTrace(); sqlSession.rollback(); throw e;

} finally {
    MybatisUtil.closeSqlSession();
}
```
# Mybatis 动态sql 有什么用？执行原理？有哪些动态sql？
Mybatis 动态 sql 可以在 Xml 映射文件内，以标签的形式编写动态 sql
执行原理是根据表达式的值 完成逻辑判断并动态拼接 sql 的功能。

Mybatis 提供了 9 种动态 sql 标签：trim | where | set | foreach | if | choose | when | otherwise | bind 。

# MyBatis 实现一对一有几种方式?具体怎么操作的？
1，联合查询，是几个表联合查询,只查询一次, 通过在resultMap 里面配置 association 节点配置一对一的类就可以完成；
2，嵌套查询，是先查一个表，根据这个表里面的结果的 外键 id，去再另外一个表里面查询数据,也是通过 association 配置，但另外一个表的查询通过 select 属性配置。

# MyBatis 实现一对多有几种方式,怎么操作的
1，联合查询，是几个表联合查询,只查询一次,通过在resultMap 里面的 collection 节点配置一对多的类就可以完成； 
2，嵌套查询，是先查一个表,根据这个表里面的 结果的外键 id,去再另外一个表里面查询数据,也是通过配置 collection,但另外一个表的查询通过 select 节点配置。


# Mybatis 是否支持延迟加载？如果支持，它的实现原理是什么？
Mybatis 仅支持 association 关联对象和 collection 关联集合对象的延迟加载， association 指的就是一对一， collection 指的就是一对多查询。
在 Mybatis 配置文件中， 可以配置是否启用延迟加载  lazyLoadingEnabled=true|false。

## 基本原理
使用 CGLIB 创建目标对象的代理对象， 当调用目标方法时， 进入拦截器方法， 比如调用 a.getB().getName()， 
拦截器 invoke()方法发现 a.getB()是null 值， 那么就会单独发送事先保存好的查询关联 B 对象的 sql， 
把 B 查询上来， 然后调用 a.setB(b)，于是 a 的对象 b 属性就有值了，接着完成 a.getB().getName()方法的调用。
这就是延迟加载的基本原理。

# Mybatis 的一级、二级缓存

## 一级缓存
Mybatis对缓存提供支持，但是在没有配置的默认情况下，它只开启一级缓存，一级缓存只是相对于同一个SqlSession而言。
所以在参数和SQL完全一样的情况下，我们使用同一个SqlSession对象调用一个Mapper方法，往往只执行一次SQL，因为使用SelSession第一次查询后，
MyBatis会将其放在缓存中，以后再查询的时候，如果没有声明需要刷新，并且缓存没有超时的情况下，SqlSession都会取出当前缓存的数据，而不会再次发送SQL到数据库。
### 1，一级缓存的生命周期有多长？
1，MyBatis在开启一个数据库会话时，会 创建一个新的SqlSession对象，SqlSession对象中会有一个新的Executor对象。
Executor对象中持有一个新的PerpetualCache对象；当会话结束时，SqlSession对象及其内部的Executor对象还有PerpetualCache对象也一并释放掉。
2，如果SqlSession调用了close()方法，会释放掉一级缓存PerpetualCache对象，一级缓存将不可用。
3，如果SqlSession调用了clearCache()，会清空PerpetualCache对象中的数据，但是该对象仍可使用。
4，SqlSession中执行了任何一个update操作(update()、delete()、insert()) ，都会清空PerpetualCache对象的数据，但是该对象可以继续使用
### 2、怎么判断某两次查询是完1，传入的statementId
全相同的查询？
2，查询时要求的结果集中的结果范围
3，这次查询所产生的最终要传递给JDBC java.sql.Preparedstatement的Sql语句字符串（boundSql.getSql() ）
4，传递给java.sql.Statement要设置的参数值

## 二级缓存
当二级缓存开启后，同一个命名空间(namespace) 所有的操作语句，都影响着一个共同的 cache，也就是二级缓存被多个 SqlSession 共享，是一个全局的变量。
当开启缓存后，数据的查询执行的流程就是 二级缓存 -> 一级缓存 -> 数据库。
 
二级缓存默认是不开启的，需要手动开启二级缓存，实现二级缓存的时候，MyBatis要求返回的POJO必须是可序列化的。
cache 标签有多个属性，一起来看一些这些属性分别代表什么意义
##cache 标签的属性
- eviction: 缓存回收策略，有这几种回收策略
- LRU - 最近最少回收，移除最长时间不被使用的对象
- FIFO - 先进先出，按照缓存进入的顺序来移除它们
- SOFT - 软引用，移除基于垃圾回收器状态和软引用规则的对象
- WEAK - 弱引用，更积极的移除基于垃圾收集器和弱引用规则的对象
默认是 LRU 最近最少回收策略
 
- flushinterval 缓存刷新间隔，缓存多长时间刷新一次，默认不清空，设置一个毫秒值
- readOnly: 是否只读；true 只读，MyBatis 认为所有从缓存中获取数据的操作都是只读操作，不会修改数据。MyBatis 为了加快获取数据，直接就会将数据在缓存中的引用交给用户。不安全，速度快。读写(默认)：MyBatis 觉得数据可能会被修改
- size : 缓存存放多少个元素
- type: 指定自定义缓存的全类名(实现Cache 接口即可)
- blocking： 若缓存中找不到对应的key，是否会一直blocking，直到有对应的数据进入缓存。

## 二级缓存的注意事项
1，缓存是以namespace为单位的，不同namespace下的操作互不影响。
2，insert,update,delete操作会清空所在namespace下的全部缓存。
3，通常使用MyBatis Generator生成的代码中，都是各个表独立的，每个表都有自己的namespace。
4，多表操作一定不要使用二级缓存，因为多表操作进行更新操作，一定会产生脏数据。


## 二级缓存什么时候开启
一级缓存因为只能在同一个SqlSession中共享，所以会存在一个问题，在分布式或者多线程的环境下，不同会话之间对于相同的数据可能会产生不同的结果，
因为跨会话修改了数据是不能互相感知的，所以就有可能存在脏数据的问题，正因为一级缓存存在这种不足，所以我们需要一种作用域更大的缓存，这就是二级缓存。

1，因为所有的update操作(insert,delete,uptede)都会触发缓存的刷新，从而导致二级缓存失效，所以二级缓存适合在读多写少的场景中开启。
2，因为二级缓存针对的是同一个namespace，所以建议是在单表操作的Mapper中使用，或者是在相关表的Mapper文件中共享同一个缓存。多表操作会有脏数据

# 什么是 MyBatis 的接口绑定？有哪些实现方式？
1，接口的方法上面加上@Select、@Update 等注解， 里面包含 Sql 语句来绑定；
2，通过 xml 里面写 SQL 来绑定, 在这种情况下,要指定 xml 映射文件里面的 namespace 必须为接口的全路径名。

# Mapper 编写有哪几种方式？

## 第一种
接口实现类继承 SqlSessionDaoSupport： 使用此种方法需要编写mapper 接口， mapper 接口实现类、mapper.xml 文件。

1，在 sqlMapConfig.xml 中配置 mapper.xml 的位置
```
<mappers>
<mapper resource="mapper.xml 文件的地址" />
<mapper resource="mapper.xml 文件的地址" />
</mappers>
```
2，定义 mapper 接口
3，实现类集成 SqlSessionDaoSupport
mapper 方法中可以  this.getSqlSession()进行数据增删改查。
4，spring 配置
```
<bean id=" " class="mapper 接口的实现">
    <property name="sqlSessionFactory" ref="sqlSessionFactory"></property>
</bean>
```
## 第二种
使用 org.mybatis.spring.mapper.MapperFactoryBean：
1、在 sqlMapConfig.xml 中配置 mapper.xml 的位置， 如果 mapper.xml 和mappre 接口的名称相同且在同一个目录， 这里可以不用配置
```
<mappers>
<mapper resource="mapper.xml 文件的地址" />
<mapper resource="mapper.xml 文件的地址" />
</mappers>
```
2、定义 mapper 接口：
mapper.xml 中的 namespace 为 mapper 接口的地址
mapper 接口中的方法名和 mapper.xml 中的定义的 statement 的 id 保持一致
3、Spring 中定义
```
<bean id="" class="org.mybatis.spring.mapper.MapperFactoryBean">
    <property name="mapperInterface" value="mapper 接口地址" />
    <property name="sqlSessionFactory" ref="sqlSessionFactory" />
</bean>
```
## 第三种： 
使用 mapper 扫描器：
1，mapper.xml 文件编写：
mapper.xml 中的 namespace 为 mapper 接口的地址；
mapper 接口中的方法名和 mapper.xml 中的定义的 statement 的 id 保持一致；
如果将 mapper.xml 和 mapper 接口的名称保持一致则不用在 sqlMapConfig.xml 中进行配置。
2，定义 mapper 接口：
注意 mapper.xml 的文件名和 mapper 的接口名称保持一致， 且放在同一个目录
3，配置 mapper 扫描器：
```
<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
    <property name="basePackage" value="mapper 接口包地址"></property>
    <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
</bean>
```
使用扫描器后从 spring 容器中获取 mapper 的实现对象。

# 简述 Mybatis 的插件运行原理，以及如何编写一个插件。
Mybatis 仅可以编写针对 ParameterHandler、ResultSetHandler、
StatementHandler、Executor 这 4 种接口的插件， Mybatis 使用 JDK 的动态代理， 为需要拦截的接口生成代理对象以实现接口方法拦截功能， 每当执行这 4 种接口对象的方法时，就会进入拦截方法，具体就是 InvocationHandler 的 invoke() 方法， 当然， 只会拦截那些你指定需要拦截的方法。
编写插件： 实现 Mybatis 的 Interceptor 接口并复写 intercept()方法， 然后在给插件编写注解， 指定要拦截哪一个接口的哪些方法即可， 记住， 别忘了在配置文件中配置你编写的插件。




    

















