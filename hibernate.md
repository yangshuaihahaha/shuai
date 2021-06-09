hibernate自动保存入库的问题
https://www.cnblogs.com/dyh004/p/13879301.html

Hibernate session FlushMode的五种设置


1.对象状态
临时状态/瞬时态(transient)：
刚刚用new语句创建，没有被持久化，不处于session中。
特点:没有oid,不在session当中

持久化状态(persistent)：
已经被持久化，加入到session的缓存中。
特点:有oid,在session当中

脱管态/游离状态(detached)：
已经被持久化，但不处于session中。
特点:有oid,不在session当中

删除状态(removed)：
对象有关联的ID，并且在Session管理下，但是已经计划被删除。
特点:有oid,在session当中,最终的效果是被删除.
可以不考虑，没有什么意义。





ibernate的保存
hibernate对于对象的保存提供了太多的方法，他们之间有很多不同，这里细说一下，以便区别：
一、预备知识：
在所有之前，说明一下，对于hibernate，它的对象有三种状态，transient、persistent、detached
下边是常见的翻译办法：
transient：瞬态或者自由态
persistent：持久化状态
detached：脱管状态或者游离态

瞬时状态的实例可以通过调用save()、persist()或者saveOrUpdate()方法进行持久化。
持久化实例可以通过调用 delete()变成脱管状态。通过get()或load()方法得到的实例都是持久化状态的。
脱管状态的实例可以通过调用 update()、0saveOrUpdate()、lock()或者replicate()进行持久化。

save()和persist()将会引发SQL的INSERT，delete()会引发SQLDELETE，
而update()或merge()会引发SQLUPDATE。对持久化（persistent）实例的修改在刷新提交的时候会被检测到，
它也会引起SQLUPDATE。saveOrUpdate()或者replicate()会引发SQLINSERT或者UPDATE

二、save 和update区别
把这一对放在第一位的原因是因为这一对是最常用的。
save的作用是把一个新的对象保存
update是把一个脱管状态的对象保存

三,update 和saveOrUpdate区别
这个是比较好理解的，顾名思义，saveOrUpdate基本上就是合成了save和update
引用hibernate reference中的一段话来解释他们的使用场合和区别
通常下面的场景会使用update()或saveOrUpdate()：
程序在第一个session中加载对象
该对象被传递到表现层
对象发生了一些改动
该对象被返回到业务逻辑层
程序调用第二个session的update()方法持久这些改动

saveOrUpdate()做下面的事:
如果对象已经在本session中持久化了，不做任何事
如果另一个与本session关联的对象拥有相同的持久化标识(identifier)，抛出一个异常
如果对象没有持久化标识(identifier)属性，对其调用save()
如果对象的持久标识(identifier)表明其是一个新实例化的对象，对其调用save()
如果对象是附带版本信息的（通过<version>或<timestamp>）并且版本属性的值表明其是一个新实例化的对象，save()它。
否则update() 这个对象

四,persist和save区别
这个是最迷离的一对，表面上看起来使用哪个都行，在hibernate reference文档中也没有明确的区分他们.
这里给出一个明确的区分。（可以跟进src看一下，虽然实现步骤类似，但是还是有细微的差别）
这里参考http://opensource.atlassian.com/projects/hibernate/browse/HHH-1682中的一个说明：
---------------------------------------------------------------------------------
I found that a lot of people have the same doubt. To help to solve this issue
I'm quoting Christian Bauer:
"In case anybody finds this thread...

persist() is well defined. It makes a transient instance persistent. However,
it doesn't guarantee that the identifier value will be assigned to the persistent
instance immediately, the assignment might happen at flush time. The spec doesn't say
that, which is the problem I have with persist().

persist() also guarantees that it will not execute an INSERT statement if it is
called outside of transaction boundaries. This is useful in long-running conversations
with an extended Session/persistence context.A method like persist() is required.

save() does not guarantee the same, it returns an identifier, and if an INSERT
has to be executed to get the identifier (e.g. "identity" generator, not "sequence"),
this INSERT happens immediately, no matter if you are inside or outside of a transaction. This is not good in a long-running conversation with an extended Session/persistence context."

---------------------------------------------------------------------------------
简单翻译一下上边的句子的主要内容：
1，persist把一个瞬态的实例持久化，但是并"不保证"标识符被立刻填入到持久化实例中，标识符的填入可能被推迟
到flush的时间。

2，persist"保证"，当它在一个transaction外部被调用的时候并不触发一个Sql Insert，这个功能是很有用的，
当我们通过继承Session/persistence context来封装一个长会话流程的时候，一个persist这样的函数是需要的。

3，save"不保证"第2条,它要返回标识符，所以它会立即执行Sql insert，不管是不是在transaction内部还是外部


五,saveOrUpdateCopy,merge和update区别
首先说明merge是用来代替saveOrUpdateCopy的，这个详细见这里
http://www.blogjava.net/dreamstone/archive/2007/07/28/133053.html
然后比较update和merge
update的作用上边说了，这里说一下merge的
如果session中存在相同持久化标识(identifier)的实例，用用户给出的对象的状态覆盖旧有的持久实例
如果session没有相应的持久实例，则尝试从数据库中加载，或创建新的持久化实例,最后返回该持久实例
用户给出的这个对象没有被关联到session上，它依旧是脱管的
重点是最后一句：
当我们使用update的时候，执行完成后，我们提供的对象A的状态变成持久化状态
但当我们使用merge的时候，执行完成，我们提供的对象A还是脱管状态，hibernate或者new了一个B，或者检索到
一个持久对象B，并把我们提供的对象A的所有的值拷贝到这个B，执行完成后B是持久状态，而我们提供的A还是托管状态

六,flush和update区别
这两个的区别好理解
update操作的是在脱管状态的对象
而flush是操作的在持久状态的对象。
默认情况下，一个持久状态的对象是不需要update的，只要你更改了对象的值，等待hibernate flush就自动
保存到数据库了。hibernate flush发生再几种情况下：
1，调用某些查询的时候
2，transaction commit的时候
3，手动调用flush的时候

七,lock和update区别
update是把一个已经更改过的脱管状态的对象变成持久状态
lock是把一个没有更改过的脱管状态的对象变成持久状态
对应更改一个记录的内容，两个的操作不同：
update的操作步骤是：
（1）更改脱管的对象->调用update
lock的操作步骤是：
(2)调用lock把对象从脱管状态变成持久状态-->更改持久状态的对象的内容-->等待flush或者手动flush



session flush在commit之前默认都会执行, 也可以手动执行，他主要做了两件事： 
1） 清理缓存。 
2） 执行SQL。 

flush: Session 按照缓存中对象属性变化来同步更新数据库。

默认情况下，Session 会在以下情况下调用 flush:

1. 直接调用 session.flush。

2. 当应用调用Transaction.commit() 时, 会先调用 flush， 然后再向数据路提交。

3. 在做查询时(HQL, Criteria)，如果缓存中持久化对象的属性发生变化，会先 flush 缓存，以保证查询结果是最新的数据。

flush 缓存的例外情况：如果对象使用native 生成器生成 ID 时，在当调用session.save() 去保存对象时， 会直接向数据库插入该实体的 insert 语句。

flush 的五种 FlushModel ：

1、NEVEL：已经废弃了，被MANUAL取代了
2 MANUAL：
如果FlushMode是MANUAL或NEVEL,在操作过程中Hibernate会将事务设置为readonly，所以在增加、删除或修改操作过程中会出现如下错误
org.springframework.dao.InvalidDataAccessApiUsageException: Write operations are not allowed in read-only mode (FlushMode.NEVER) - turn your Session into FlushMode.AUTO or remove 'readOnly' marker from transaction definition；
解决办法：配置事务， Spring会读取事务中的各种配置来覆盖hibernate的session中的FlushMode；
3 AUTO
设置成auto之后，当程序进行查询、提交事务或者调用session.flush()的时候，都会使缓存和数据库进行同步，也就是刷新数据库
4 COMMIT
提交事务或者session.flush()时，刷新数据库；查询不刷新
5 ALWAYS：
每次进行查询、提交事务、session.flush()的时候都会刷数据库


ALWAYS和AUTO的区别：当hibernate缓存中的对象被改动之后，会被标记为脏数据（即与数据库不同步了）。当 session设置为FlushMode.AUTO时，hibernate在进行查询的时候会判断缓存中的数据是否为脏数据，是则刷数据库，不是则不刷，而always是直接刷新，不进行任何判断。很显然auto比always要高效得多。




1、flush()方法进行清理缓存的操作,执行一系列的SQL语句,但不会提交事务;
commit()方法会先调用flush()方法,然后提交事务. 提交事务意味着对数据库所做的更新会永久保持下来   所谓清理,是指Hibernate 按照持久化象的状态来同步更新数据库   


2、Flush()后只是将Hibernate缓存中的数据提交到数据库,如果这时数据库处在一个事物当中,则数据库将这些SQL语句缓存起来,当Hibernate进行commit时,会告诉数据库,你可以真正提交了,这时数据才会永久保存下来,也就是被持久化了.    

3、commit针对事物的， flush针对缓存的， 数据同 步到数据库中后只要没有commit还是可以rollback的。



rollbackFor，isolation，propagation，timeout

# 什么是hibernate的延迟加载
hibernate主要通过代理来实现延迟加载。具体过程是：hibernate从数据库获取一个对象时，或获取对象的集合属性时，或获取某一个对象所关联的对象时。由于没有使用到该对象的数据，hibernate并不从数据库加载真正的数据
而是为该对象创建一个代理来代表这个对象，这个对象上的所有属性都是默认值；只有真正需要使用该对象的数据时才创建这个真实的对象，真正从数据库加载它的数据，提高查询效率

注意：
    1、不能判断User = null；代理对象不可能为空
        代理对象的限制：和代理关联的session对象，如果session关闭后访问代理则抛异常。session关闭之前访问数据库
    2、getId()方法不行因为参数为ID，getClass()方法不用访问数据库就可以得到的数据
    
Hibernate中默认采用延迟加载的情况主要有以下几种
    1、当调用session上的load()加载一个实体时，会采用延迟加载。
    2、当session加载某个实体时，会对这个实体中的集合属性值采用延迟加载。
    3、当session加载某个实体时，会对这个实体所有单端关联的另一个实体对象采用延迟加载。