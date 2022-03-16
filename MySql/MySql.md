# Mysql存储引擎
什么是存储引擎？
说白了就是如何存储数据，如何为存储数据建立索引，如何更新、查询数据等技术实现方法
例如，如果你在研究大量临时数据，你也许需要内存存储引擎，因为内存存储引擎能够在内存中存储所有的表格数据。
或者你需要一个支持事物处理的数据库，以确保事务处理不成功时数据的回退能力，就选择支持事物的存储引擎


## 1，InnoDB存储引擎介绍
- 支持事物，InnoDB存储引擎主要是为了在线事物处理（OLTP）应用设计的
- 行锁设计，支持外键，非锁定读
- 支持多版本并发控制来获得高并发性
- 提供了插入缓冲、二次写、自适应哈希索引、预读等高性能和高可用功能

适用场景：
    - 更新密集的表。InnoDB存储引擎特别适合处理多重并发的更新需求
    - 事物
    - 自动灾难恢复。与其他存储引擎不同，InnoDB表能够自动从灾难中恢复
    - 外键约束。
    - 支持自动增加列AUTO_INCREMENT属性


## 2，MyISAM存储引擎
- 不支持事物，他的设计目标是面向在线分析应用
- 支持全文索引
- 表锁设计
- 它的缓冲池只缓冲索引文件，不缓冲数据文件，所以MyISAM存储引擎表由MYD和MYI组成，前者存储数据文件，后者存储索引文件

MyISAM表是独立于操作系统的，这说明可以轻松的将其从Windows服务器移植到Linux服务器
每当我们建立一个MyISAM引擎的表时，会在本地磁盘上建立三个文件，文件名就是表名
例如，我们建立一个MyISAM引擎的tb_Demo表，那么会生成一下三个文件：
tb_Demo.frm，存储表定义
tb_Demo.MYD，存储数据
tb_Demo.MYI，存储索引

适用场景：
    1，选择密集型数据。MyISAM存储引擎在筛选大量数据时非常迅速
    2，插入密集型数据。MyISAM的并发插入特性允许同时选择和插入数据

# Mysql事物
    事物就是一个操作序列，这些操作要么都执行要么都不执行，是一个不可分割的单位。
    
    在缺省模式下，mysql是autocommit模式的，所有的数据库更新操作都会及时提交，在缺省情况下，mysql不支持事物。
    如果表类型使用InnoDB table的话，你的mysql可以使用事务处理，使用set autocommit=0用来设置非自动提交模式，这时你必须使用commit来提交你的更改，或者用rollback来回滚你的更改
    
    原子性
        原子性指的是整个数据可事物是一个不可分割的单位，每一个都是一个原子操作。如果一系列的操作中，有一个操作失败了，那么我们将这个事物中的所有操作恢复到执行事物之前的状态
    
    一致性
        事务的执行使得数据库从一种正确状态转换成另一种正确状态，是指事物中的方法要么同时成功，要么都不成功
    
    隔离性
        事务是可以开启很多的，Mysql数据库中可以同时启动很多事务，但是事务和事务之间是相互隔离的互不影响的
        
        事务的隔离级别：
            mysql的事务隔离级别分为四大等级：读未提交（read uncommitted）、读提交（read committed）、可重复读（可重复读）、串行化（serializable）
            
            读未提交会读到另一个事务未提交的数据，产生脏读数据，读提交则解决了脏读，出现了不可重复读，即在一个事务任意时刻读到的数据可能不一样，可能会受到其他事务对数据修改提交后的影响
            一般是对于update操作
            
            可重复读解决了之前不可重复读和脏读的问题，但是带来了幻读的问题，幻读一般是针对于insert操作。
            
            例如：第一个事务查询一个user表id=100发现不存在改行数据，这时第二个事务进来了，新增了一条id=100的数据并提交了事务
            
            这是第一个事务新增一条id=100的数据就会主键冲突，第一个事务再select一下发现id=100已经存在，这就是幻读
            
        四个隔离级别逐渐增强，但是性能越来愈差，这是为什么呢？
            这得从Mysql的锁说起。mysql的锁可以分为享锁/读锁（shared locks）、排他锁/写锁（Exclusive Locks）、间隙锁、行锁、表锁
            
            在四个隔离级别中加锁肯定是要消耗性能的，而读未提交是没有任何锁的，所以没有任何隔离效果，性能最好
            
            对于串行化是一把大锁，读的时候加共享锁，不能写，写的时候，加的是排它锁，阻塞其他事物的写入和读取
            若是其他的事物长时间不能写入就会超时，所以它的性能最差，对于它并没有什么并发可言
            
            对于读提交和可重复读，他俩是兼顾解决数据问题，然后又要一定的并发，所以在实现上锁机制会比串行化优化很多，提高并发性，性能更好
            
        几种锁的概念
            共享锁是针对同一份数据，多个读操作可以同时进行，简单来说即读加锁，不能写并且可并行读；
            排它锁针对写操作，假如当前写操作没有完成，那么它会阻断其他的写锁和读锁，即写加锁，其他读写都阻塞
            而行锁和表锁，是从锁粒度上划分的，行锁锁定当前数据行，锁的粒度小，加锁慢，发生锁冲突的概率小，并发度高，行锁也是MyISAM和InnoDB的区别之一，InnoDB支持行锁并且支持事务
            而表锁的粒度大，加锁快，开销小，但是锁冲突概率大，并发度低
            间隙锁分为两种：Gap Locks和Next-Key Locks。Gap Locks会锁住两个索引之间的区间，比如select * from user where id>3 and id<5 for update，就会在（3，5）之间加上Gap Locks
            Next-key Locks是Gap locks+Record Locks形成区间锁，select * from user where id>3 and id<5 for update会在【3，5】之间加上Next-key Locks
       
        那么mysql中什么时候会加锁呢
            在数据库增、删、改、查中，只有增、删、改才会用上排它锁，而只是查询并不会加锁，只能通过在select语句后显示加lock in share mode或者for update来加共享锁或者排它锁 
                
                
            
    持久性
        当成功插入一条数据库记录时，数据库必须保证有一条永久写入到数据库磁盘中
    
    事务的底层实现原理
        在实现MVCC时用到了一致性视图，用于支持读提交和可重复读的实现
        在实现可重复读的隔离级别，只需要在事物开始时创建一致性视图，也叫快照，之后的查询里都公用这个一致性视图，后续的事务对数据的更改对当前事务是不可见的，这样就实现了重复读
        而读提交，每一个语句执行前都会计算出一个新的视图，这个也是可重复读和读提交在MVCC实现层上面的区别
        
        快照在MVCC底层是怎么工作的？
            在InnoDB中每个事物都有自己的事务id，并且是唯一的，递增的
            对于mysql中每一个数据行都有可能存在多个版本，在每次事务更新数据的时候，都会生成一个新的数据版本，并且把自己的数据id赋值给当前版本的roe trx_id
            假如三个事务更新了同一行数据，那么就会有对应的三个数据版本。
            实际上版本1、版本2并非实际物理存在的，而图中的U1和U2实际就是undo log，这v1和v2版本是根据当前v3和undo log计算出来的。
    
        那对于一个快照来说，你直到它要遵循什么规则吗？
            对于一个事务视图来说除了对自己更新的总是可见，另外还有三种情况：版本未提交的，都是不可见的；版本已经提交，但是是在创建视图之后提交的也是不可见的；版本已经提交，若是在创建视图之前提交的是可见的。
        假如两个事务执行写操作，又怎么保证并发呢？
            假如事务1和事务2都要执行update操作，事务1先update数据行的时候，先回获取行锁，锁定数据，当事务2要进行update操作的时候，也会取获取该数据行的行锁，但是已经被事务1占有，事务2只能wait。
        在没有索引的情况下，没办法快速定位到数据行怎么加锁？
            没有索引的情况下，就获取所有行，都加上锁，然后mysql会再次过滤符合条件的行并释放锁，只有符合条件的才会继续持有锁，这样性能消耗会很大    
    
    读未提交：
        一个事物可以读到其他事务未提交的数据，会导致脏读的发生
    读已提交（不可重复读）：
        一个事物可以读取到已提交的数据。导致幻读的发生。oracle数据库默认隔离级别
    可重复读：
        一个事务开始到结束，都可以反复读取到事务开始时看到的数据，并不会发生变化。避免了脏读、不可重复读和幻读的发生（MVCC + Undo Log解决幻读），mysql的默认隔离级别
    串行化：
        读写会锁住整张表，保证数据不出错，在读数据的时候会加表级共享锁，每次写数据会加表级别排它锁。这个级别会导致InnoDB并发大幅下降。
       
    原子性是基于日志的Redo/Undo机制
        Redo log用来记录某数据块被修改后的值，可以用来恢复未写入data file的已成功事务更新的数据
            防止在发生故障的时间点，尚有脏页未写入磁盘，在重启MySQL服务的时候，根据Redo Log进行重做，从而达到事务的未入磁盘数据进行持久化这一特性。
            一旦事务成功提交且数据持久化落盘之后，此时Redo log中的对应事务数据记录就失去了意义，所以Redo log的写入是日志文件循环写入的
        Undo log是用来记录数据更新前的值，保证数据更新失败能够回滚
            假如数据在执行过程中不小心崩了，可以通过该日志的方式，回滚之前已经执行成功的操作，实现事物的一致性
            具体实现流程：
                假如某个数据库崩溃，崩溃之前有事务A和事务B在执行，事务A已经提交，事务B未提交。当数据库重启进行crash-recovery时，
                就会通过Redo log将已经提交的事务的更改写到数据文件，而没有提交的就通过Undo log进行roll back
            Undo log保证事物的原子性（在InnoDB引擎中，还是用Undo log实现MVCC） 
    
    Innodb在可重复读隔离级别下,是如何通过MVCC + Undo Log，解决幻读的？
        事务未提交之前，Undo保存了未提交之前的数据，Undo中的数据可作为旧数据快照供其他并发事务进行快照读
        案例：
            事务A(更新语句)在进行update之前，'备份旧数据 --> undo buffer ---> 落地undo log'
            事务B(查询语句)此时查询的是undo buffer中的内容（快照读）
            事务A(更新语句)若意外rollback，会从undo buffer中数据恢复（实现事务原子性）
    
    当前读和快照读
        快照读：
            sql读取的是快照版本，就是历史版本，普通的select就是快照读，数据的读取将有cache(原本数据) + undo(事务修改过的数据)两部分组成、
        当前读：
            sql读取的是最新版本。通过锁机制来保证读取的数据无法通过其他事务进行修改
            update、deldete、insert、 select...lock in share mode、 select...for update都是当前读  
   
    
    什么是MVCC？
    全称是多版本并发控制系统
    InnoDB的mvcc是通过在每行记录后面保存两个隐藏的列来实现，这两个列一个保存了行的创建时间，一个保存行的过期时间（删除时间）。当然存储的并不是真实的时间，而是系统的版本号
    每开始一个新的事务，系统版本号都会新增，事务开始时刻的系统版本号会作为事物的版本号，用来查询到每行记录版本号进行比较
        可重读隔离级别下mvcc是如何工作的
            select：InnoDB会根据一下条件检查每一项记录：
                第一，InnoDB只查找版本早于当前事务的版本的数据行，这样可以确保事务读取的行要么是在开始事务之前已经存在要么事务自身插入或者修改过的
                第二，行的删除版本号要么未定义，要么大于当前事务版本号，这样可以确保事务读取到的行在事务开始之前未被删除
            insert：InnoDB为新插入的每一行保存当前系统版本号作为行版本号
            delete：InnoDB为删除的每一行保存当前系统版本号作为删除标识
            update：InnoDB为插入的一行新纪录保存当前系统版本号作为行版本号，同时保存当前系统版本号到原来的行作为删除标识保存这两个版本号
                    使用大多数操作都不用加锁。它的不足是每行记录都需要额外的存储空间，需要做更多的检查工作和额外的维护工作    
        
    mysql事务实现原理
    InnoDB是通过多版本并发控制解决不可重复读问题，加上间隙锁解决幻读问题。因此InnoDB的RR隔离级别其实实现了串行化界别的效果。
    而且保留了比较好的并发性能。事物的隔离性是通过锁实现，而事物的原子性、一致性和持久性是通过事务日志实现
    
    mysql只读事务到底有什么作用？
        很多人认为MySQL只读事务没啥用，有点鸡肋，以前我也这么认为，自从认真研究了事务的隔离级别和事务的并发问题后，发现这个只读事务其实并不是大家想象的那么无用。下面举一个实际开发中可能会遇到的例子来说明只读事务的作用。
        展示用户列表的需求，后台从数据库查询用户列表返回给前端分页展示，分页通过数据库的分页功能实现，前端分页插件需要后台返回总共有多少条记录，以便渲染分页效果，后端返回的数据大概长这样：
        {
            total:18,//总共多少条记录
            list: [...]//当前页记录
        }
        所以后台需要查询两次数据库：查询总数和查询列表。（有人会说，就不能全部查询出来再在代码里通过sublist分页吗？我这个例子里只有18条数据，万一有180万条数据呢，全部加载到内存不怕内存溢出吗？）
        假设我第一次查询总数返回的是18条，结果在我第二次查询列表的时候数据表中插入了一条用户数据，然后我刚好查询的是第二页的数据，原本是有8条数据，结果现在查到了9条。然后就在页面看到了神奇的一幕：总共18条数据，按照每页显示10条来分页，结果第二页有9条数据，这不是有毛病么？
        是的，上述情景就是典型的不可重复读问题。我们都知道有个事务隔离级别叫“可重复读”，此隔离级别可以实现可重复读，但是别忘了事务的隔离级别前提是要有事务啊，两个查询语句都没有处于事务中，还谈啥事务的隔离级别啊？还谈啥可重复读啊？这个我还真做过实验，就算事务的隔离级别是可重复读级别，两个查询语句没有处于事务中时，还是会出现不可重复读问题。
        所以在可重复读隔离级别下需要结合事务才能实现可重复读，而只读事务比读写事务更高效，因为不需要写数据，所以做了一些优化。没错，只读事务就是这么来的。
        还有人说只读事务不能进行写操作，这确实没错，但我认为不能写是结果而不是目的。
        如果执行的是单条查询语句，是没有必要启用事务支持的，数据库默认执行sql执行期间的读一致性，如果是执行多条查询语句，例如统计查询，报表查询，在这种场景下，多条查询sql必须保证整体的读一致性
        否则，在前条sql查询之后，后条查询之前，数据被其他用户改变，则会出现不一致的状态
       
        


# mysql的锁机制
    表级别锁
        开销小，加锁快；不会出现死锁；锁定粒度大，发生锁冲突的概率高，并发度低
    行级锁
        开销小，加锁慢；会出现死锁；锁定粒度小，发生锁冲突概率低，并发度高
    页面锁
        开销和加锁时间介于表锁和行锁之间；会出现死锁；锁定粒度介于表锁和行锁之间。并发度一般
    仅从锁角度，表级锁更适合查询为主，只有少量按索引条件更新数据的应用，如web应用；而行级锁则更适合有大量按索引条件并发更新少量不同数据，同时又有并发查询的应用，如一些在线事务处理系统    
    1，行锁
    行级锁是mysql锁粒度最细的一种锁，表示只针对当前操作的行进行加锁。行级锁能大大减少数据库操作的冲突。其加锁粒度最小，但加锁的开销也最大有可能出现死锁的情况
    行级锁按照使用方式分为共享锁和排它锁
        共享锁用法（S锁 读锁）：
            若事务T对数据对象A加上S锁，则事务T可以读A但不能更改A，其他事务只能再对A加S锁，而不能加X锁，直到T释放A上的S锁。
            这保证了其他事务可以读A，但在T释放A上的S锁之前不能对A进行任何修改
            共享锁就是允许多个线程同时获取一个锁，一个锁可以同时被多个线程拥有
        排它锁（X锁 写锁）
            若事务T对数据对象加上X锁，事务T可以读A也可以修改A，其他的事物不能再对A加任何锁，直到T释放A上的锁。
            这保证了其他事务在T释放A上的锁之前不能再读取和修改A   
    2，乐观锁
        乐观锁不是数据库自带的，需要我们自己去实现。乐观锁是指操作数据库时(更新操作)，想法很乐观，认为这次的操作不会导致冲突，在操作数据时，并不进行任何其他的特殊处理（也就是不加锁），而在进行更新后，再去判断是否有冲突了。
        通常实现是这样的：在表中的数据进行操作时(更新)，先给数据表加一个版本(version)字段，每操作一次，将那条记录的版本号加1。也就是先查询出那条记录，获取出version字段,如果要对那条记录进行操作(更新),则先判断此刻version的值是否与刚刚查询出来时的version的值相等，如果相等，则说明这段期间，没有其他程序对其进行操作，则可以执行更新，将version字段的值加1；如果更新时发现此刻的version值与刚刚获取出来的version的值不相等，则说明这段期间已经有其他程序对其进行操作了，则不进行更新操作。
    3，悲观锁      
        悲观锁就是在操作数据时，认为此操作会出现数据冲突，所以在进行每次操作时都要通过获取锁才能进行对相同数据的操作，这点跟java中的synchronized很相似，所以悲观锁需要耗费较多的时间。另外与乐观锁相对应的，悲观锁是由数据库自己实现了的，要用的时候，我们直接调用数据库的相关语句就可以了。
        说到这里，由悲观锁涉及到的另外两个锁概念就出来了，它们就是共享锁与排它锁。共享锁和排它锁是悲观锁的不同的实现，它俩都属于悲观锁的范畴。
    4，共享锁
        共享锁指的就是对于多个不同的事务，对同一个资源共享同一个锁。相当于对于同一把门，它拥有多个钥匙一样。
        对于悲观锁，一般数据库已经实现了，共享锁也属于悲观锁的一种，那么共享锁在mysql中是通过什么命令来调用呢。通过查询资料，了解到通过在执行语句后面加上lock in share mode就代表对某些资源加上共享锁了。
    5，排它锁
        排它锁与共享锁相对应，就是指对于多个不同的事务，对同一个资源只能有一把锁。 与共享锁类型，在需要执行的语句后面加上for update就可以了
        排他锁指的是一个事务在一行数据加上排他锁后，其他事务不能再在其上加其他的锁。mysql InnoDB引擎默认的修改数据语句：update,delete,insert都会自动给涉及到的数据加上排他锁，select语句默认不会加任何锁类型，如果加排他锁可以使用select …for update语句，加共享锁可以使用select … lock in share mode语句。所以加过排他锁的数据行在其他事务种是不能修改数据的，也不能通过for update和lock in share mode锁的方式查询数据，但可以直接通过select …from…查询数据，因为普通查询没有任何锁机制。
    6，意向锁
        为了允许行锁和表锁共存，实现多粒度锁机制，InnoDB还有两种内部使用的意向锁（Intention Locks），这两种意向锁都是表锁。
            1，意向共享锁（IS）：事务打算给数据行共享锁，事务在给一个数据行加共享锁前必须先取得该表的IS锁。
            2，意向排他锁（IX）：事务打算给数据行加排他锁，事务在给一个数据行加排他锁前必须先取得该表的IX锁。
    7，InnoDB行锁实现方式
        InnoDB行锁是通过给索引上的索引项加锁来实现的，这点mysql和oracle不同，后者是通过数据块中对相应数据行加锁来实现的，InnoDB这种行锁实现特点意味着：
        只有通过索引条件检索数据，InnoDB才使用行级锁，否则InnoDB使用表锁
        实际应用中要特别注意InnoDB这一特性，不然可能会导致大量锁冲突，从而影响并发性能
    8，间隙锁
        当我们用范围条件而不是相等条件检索数据，并请求共享或者排它锁时，InnoDB会给符合条件的已有数据记录的索引项加锁；
        对于键值在条件范围内但并不存在的记录，叫做"间隙（GAP）"，InnoDB也会对这个"间隙"加锁，这种锁机制就是间隙锁（Next-Key）。
        举例来说，假如emp表中只有101条记录，其empid的值分别是 1,2,…,100,101，下面的SQL：
            ```Select * from  emp where empid > 100 for update;```
            是一个范围条件的检索，InnoDB不仅会对符合条件的empid值为101的记录加锁，也会对empid大于101（这些记录并不存在）的“间隙”加锁。
            InnoDB使用间隙锁的目的，一方面是为了防止幻读，以满足相关隔离级别的要求，对于上面的例子，要是不使 用间隙锁，如果其他事务插入了empid大于100的任何记录，那么本事务如果再次执行上述语句，就会发生幻读；另外一方面，是为了满足其恢复和复制的需要。
            很显然，在使用范围条件检索并锁定记录时，InnoDB这种加锁机制会阻塞符合条件范围内的键值的并发插入，这会造成严重的锁等待。
            因此，在实际应用开发中，尤其是并发插入比较多的应用，我们要尽量优化业务逻辑，尽量使用相等条件来访问更新数据，避免使用范围条件
    如果一个事务请求的锁模式与当前锁兼容，InnoDB就请求的锁授予该事务。反之，如果两者不兼容，该事物就等待锁释放
    意向锁InnoDB自动加的，不需要用户干预。
    对于update、delete和insert语句，InnoDB会自动给涉及数据集加排它锁（X），对于 select，InnoDB不会加任何锁
    事务可以通过一下语句显示给记录集 

# mysql主从复制和读写分离
    作用：
        在实际的生产环境中，对数据库的读和写都在同一个数据库服务器中，是不能满足实际需求的，通过主从复制的方式来同步数据，再通过读写分离来提升数据库的并发负载能力
    mysql复制类型
        1，基于语句的复制。在服务器上执行sql语句，在从服务器上执行同样的语句，mysql默认采用基于语句的复制执行效率高
        2，基于行的复制。把改变的内容复制过去，而不是把命令在从服务器上执行一遍
        3，混合类型。默认采用基于语句的复制，一旦发现基于语句无法精确复制时，就会采用基于行的复制
    复制过程：
        在每个事务更新数据完成之前，master在二进制日志记录这些改变。写入二进制日志完成后，master通知存储引擎提交事务
        1，slave将master的binary log复制到其中继日志。首先slave开始一个工作线程（I/O），I/O线程在master上打开一个普通连接，然后开始binlog dump process
        binlog dump process从master的二进制日志中读取事件，如果已经跟上master，它会睡眠等待master产生新的事件，I/O线程将这些事件写入中继日志。
        2，sql从线程处理该过程的最后一步，sql线程将从中继日志读取事件，并重新放其中的事件而更新slave日志。使其与master中的数据一致，
        只要该线程与I/O线程保持一致，中继日志通常会位于os缓存中，所以中继日志的开销很小
    读写分离原理
        读写分离就是在主服务器上修改，数据会同步到从服务器，从服务器只能提供读数据，不能写入，实现备份的同时也实现了数据库的优化，提升了服务器的安全
        常见的mysql读写分离：
            1，基于程序代码内部实现
                在代码中根据select、insert进行路由分类，这类方法也是目前生产环境下应用最广泛的。优点是性能较好，
                因程序在代码中实现不需要增加额外的硬件开支，缺点是需要开发人员来实现，运维人员无从下手
            2，基于中间代理层实现
                代理层一般介于应用服务器和数据服务器之间，代理数据库服务器收到应用服务器的请求后根据判断后转发到后端数据库，有以下代表性的程序
    
    主从同步的配置：
        1，登入主服务器的mysql，创建用于从服务器同步数据使用的账号
            GRANT REPLICATION SLAVE ON *.* TO 'slave'@'%' identified by 'slave';
            FLUSH PRIVILEGES;
        2，获取主服务器的二进制信息
            SHOW MASTER STATUS;
            File为使用的日志文件名字，Position为使用的文件位置，配置从服务器的时候会用到
        3，设置从服务器的my.ini
        4，进入从服务器的mysql，设置连接到master主服务器
            change master to master_host='192.168.204.129', master_user='slave', master_password='slave',master_log_file='mysql-bin.000001', master_log_pos=590;
            master_host：主服务器的ip地址；
            master_log_file：前面查询到的主服务器日志文件名
            master_log_pos：前面查询到的主服务器日志文件位置
        5，开启同步
            start slave 
    读写分离配置（amoeba）：
        1，安装并配置Amoeba软件
            mkdir /usr/local/amoeba
            tar zxf amoeba-mysql-binary-2.2.0.tar.gz -C /usr/local/amoeba/
            chmod -R 755 /usr/local/amoeba/
            /usr/local/amoeba/bin/amoeba
            出现以下内容说明安装成功了
                [root@centos1 ]#  /usr/local/amoeba/bin/amoeba
                amoeba start|stop
                .
        2，配置amoeba读写分离，两个从节点读负载均衡
            在主从服务器上开放权限给amoeba（三台服务器上都做相同设置，这里以一台为例）
            mysql> grant all on *.* to liang@'192.168.1.%' identified by '123456';
        3，修改amoeba.xml文件
            [root@centos1 ]#  cd /usr/local/amoeba/
            [root@centos1 ]#  vim conf/amoeba.xml 
            ```
            <bean class="com.meidusa.amoeba.mysql.server.MysqlClientAuthenticator">
                <property name="user">amoeba</property>//这里的帐户名和密码在后面链接amoeba使用
                <property name="password">123456</property>
                <property name="filter">
                    <property name="LRUMapSize">1500</property>
                    <property name="defaultPool">master</property>//修改为master
                    //注意：这里原有注释，需要删除
                    <property name="writePool">master</property>//修改为master
                    <property name="readPool">slaves</property>//修改为slaves
                    <property name="needParse">true</property>
            ```
            [root@centos1 ]#  vim conf/dbServers.xml 
            ```
            <property name="user">liang</property>//之前设置开放权限的用户名和密码
            //注意删掉此位置的注释
            <property name="password">123456</property>
            //注意删掉此位置的注释
            </factoryConfig>
            .............................................................................
            <dbServer name="master"  parent="abstractServer">   //修改为master
                <factoryConfig>
                    <!-- mysql ip -->
                    <property name="ipAddress">192.168.1.30</property>  //修改IP
                </factoryConfig>
            </dbServer>
            <dbServer name="slave1"  parent="abstractServer">   //修改为slave1
                <factoryConfig>
                    <!-- mysql ip -->
                    <property name="ipAddress">192.168.1.40</property>  //修改IP
                </factoryConfig>    
            </dbServer>
            <dbServer name="slave2"  parent="abstractServer">   //复制一份，修改为slave2
                <factoryConfig>
                    <!-- mysql ip -->
                    <property name="ipAddress">192.168.1.50</property>  //修改IP
                </factoryConfig>
            </dbServer>
            <dbServer name="slaves" virtual="true">         //修改为slaves
                <poolConfig class="com.meidusa.amoeba.server.MultipleServerPool">
                    <!-- Load balancing strategy: 1=ROUNDROBIN , 2=WEIGHTBASED , 3=HA-->
                    <property name="loadbalance">1</property>
                    <!-- Separated by commas,such as: server1,server2,server1 -->
                    <property name="poolNames">slave1,slave2</property> //修改为slave1，slave2
                </poolConfig>
            </dbServer>
            </amoeba:dbServers>
            ```
            4，启动amoeba软件
                [root@centos1 ]#   bin/amoeba start&
                [root@centos1 ]#  netstat -anpt | grep java
        。
# mysql索引
    索引其实是一种数据结构，能够帮助我们快速的检索数据库中的数据
    索引是帮助mysql高效获取数据的排好序的数据结构 
        
    二叉树索引优缺点：
        优点：
            可以解决大量数据一次性无法加载进内存的问题，二叉树可以批量加载进内存
        缺点：
            1，如果数据本身是有序的，极端情况下会变成链表，性能急剧降低
            2，数据量大的情况下，树高度很高，检索次数事件很久    
    hash索引的优缺点：
        优点：
            hash索引底层是哈希表，是一种key-value存储数据的结构，所以多个数据在存储关系上完全没有任何顺序关系，所以对于区间查询是无法直接通过索引查询的，
            需要全表扫描，所以哈希索引只适合等值查询场景。
        缺点：
            1，利用hash存储的话需要将所有数据文件添加到内存，比较耗费内存空间
            2，如果所有的查询都是等值查询，那么hash确实快，但是在企业或者实际工作环境中范围查找的数据更多
               而不是等值查询，因此hash就不合适了
            3，会有哈希碰撞的问题，效率不一定比B树高
    Btree索引优缺点：
        优点：
            1，B树是一种多路搜索树（返回整个表时需要往上回查），每个子节点可以拥有多于2个子节点，M路的B树最多可拥有M个子节点。设计成多路，其目的是为了降低树的高度，降低查询次数，提高查询效率。
            2，它的节点是天然有序的（左子节点小于父节点，父节点小于右子节点），所以对于查询的时候不需要全表扫描
            3，降低了树的高度，减少了io次数
                
           
    为什么采用B+ 树作为索引
        使用二叉树时，索引存放的是具体数据以及存放在磁盘上的地址，类似key-value的形式，但是在极端的情况下会变成链表，造成深度过多，IO次数增加
        使用红黑树时，深度也会过多，造成io次数过多
        使用hash表在存储索引的时候，会把索引值做一个哈希运算生成的散列值当作索引值，搜索的时候把值做一个hash运算
        然后就能快速找到具体的值，哈希散列和值是一一对应的关系，但是在范围搜索的时候并不适合
        
        如果想要深度降低，那就需要横向增加，让每个节点上存储更多的元素，这就有了B-tree：
            叶子节点具有相同的深度，叶子结点指针为空
            所有索引元素不重复
            节点中的数据索引从左至右递增排列
            
        升级后就有了B+tree
            几千万的数据只需要进行3次io
            1，把所有的索引元素在叶子节点有一份全量的索引元素，把处于中间位置的一些元素提取出来放到非叶子节点作为冗余元素
            
            2，非叶子结点存储的是索引键值（就是主键）和子节点的内存地址，是成对出现的！！！，不存储具体的数据，这样的话节省很多空间
            一个叶子节点的大小是16kb，那么一个节点就可以存放大概1170个元素，那么高度为3的B+树，就可以存放一千万的数据
            
            3，每个叶子结点包含的元素是指针、键值（主键）、索引数据，叶子结点存放在磁盘节省内存空间，叶子结点之间会用指针链接，提高区间访问性能（在进行范围查找的时候）
            
            4，索引存放在磁盘中，同时可以分批加入内存，提高查询效率
            
            
            索引的元素按理说是存储在磁盘上面的，为什么不能将全量的元素都放在叶子结点呢？这样直接加载到内存速度也很快，这样的话浪费时间，而且浪费内存
            非叶子节点不存储data，只存储索引（会有冗余），可以放更多的索引，叶子节点包含所有索引字段，叶子结点用指针连接，提高区间访问性能  
            
            叶子节点之间有链表，非常适合范围查找，本来叶子之间就是有序的，在范围查找的时候
            比如查找大于20的数据，就可以直接取叶子几点20之后的数据，如果叶子结点之间没有链表的话，就需要向上查找，非常耗时
            
            
    聚集索引和非聚集索引
        区别就是所以和数据是否分开存储        
        InnoDB索引实现（聚集）
            叶子结点的 存储的是索引值和具体的数据 
        MySAM索引文件个数据文件是分离的（非聚集）
            叶子结点存储的是索引值和磁盘文件地址，找到索引后会根据磁盘文件地址在myd文件找到具体的索引
            
     
    为什么InnoDB表必须包含主键，并且使用Int类型的自增主键
        首先InnoDB存储引擎，如果自己不设置主键，存储引擎也会自动给加一个主键
        使用int类型作为主键是因为，在查找的时候相互比效率比较高，而且int类型节约空间
        要自增，如果主键不自增的话，在插入一条数据的时候，要维护到索引里面查找的时候需要分裂是非常耗费性能的，如果是递增的那就是一直往后面加元素
     
    
    为什么非主键索引结构叶子节点存储的是主键值？（一致性和节省存储空间）
        1，保持一致性
            当数据库进行dml操作时，同一行记录的页地址会发生改变，因非主键索引保存的是主键值，无需进行更改
        2，节省存储空间
            innodb数据本身就已经汇聚到主键索引所在的B+树了，如果普通索引还继续再保存一份数据，就会导致有多少索引就要存多少份数据
        商城系统订单表会存储一个用户ID作为关联外键，而不推荐完整的用户信息，因为当我们用户表中的信息（真实名称，手机号，收货地址...）修改后，
        不需要再次维护订单表的用户数据，同时也节省了存储空间
    
    
    联合索引底层结构是什么样的
        由数据库a字段和b字段组成一个联合索引
        本质上来说，联合索引也是一个B+树，和单值索引不同的是，联合索引的键值对不是1个，而是大于1个
        a,b排序分析：
        a顺序：1，1，2，2，3，3
        b顺序：1，2，1，4，1，2
        大家可以发现a字段是有序排列的，b字段是无序排列的（因为B+树只能选一个字段构建有序的树）
        又不小心发现，在a相等的情况下，b字段是有序的。
        
        使用联合索引时，索引列的定义顺序将会影响到最终索引的使用情况。例如联合索引（a,b,c），mysql会从最左边的列优先匹配，如果最左边的没起到作用，在未使用覆盖索引的情况下就只能全表扫描
        从联合索引的底层结构思考，mysql会优先以联合索引第一列匹配，才会匹配下一列，如果不指定第一列匹配值，就无法得知下一步查询哪个节点
        另一种情况，如果遇到范围查询，那B+树也无法对下一列进行等值匹配了
        
    索引失效
        1，分析最佳左前缀原理
            遵循最佳左前缀法则的例子：select * from testTable where a=1 and b=2
                首先a字段在B+树上是有序的，所以我们可以通过二分查找法来定位到a=1的位置。
                其次在a确定的情况下，b是相对有序的，因为有序，所以同样可以通过二分查找法找到b=2的位置。
            不遵循最佳左前缀的例子：select * from testTable where b=2
                我们来回想一下b有顺序的前提：在a确定的情况下。
                现在你的a都飞了，那b肯定是不能确定顺序的，在一个无序的B+树上是无法用二分查找来定位到b字段的。
                所以这个时候，是用不上索引的。
        2，范围查询右边失效原理
            select * from testTable where a>1 and b=2
            首先a字段在B+树上是有序的，所以可以用二分查找法定位到1，然后将所有大于1的数据取出来，a可以用到索引。 
            b有序的前提是a是确定的值，那么现在a的值是取大于1的，可能有10个大于1的a，也可能有一百个a。 
            大于1的a那部分的B+树里，b字段是无序的（开局一张图），所以b不能在无序的B+树里用二分查找来查询，b用不到索引。
        3，like索引失效原理
            字符串的排序方式：先按照第一个字母排序，如果第一个字母相同，就按照第二个字母排序。。。以此类推
            %号放右边（前缀）：由于B+树的索引顺序，是按照首字母的大小进行排序，前缀匹配又是匹配首字母。所以可以在B+树上进行有序的查找，查找首字母符合要求的数据。所以有些时候可以用到索引
            %%叫做（中缀）：这个是查询任意位置的字母满足条件即可，只有首字母是进行索引排序的，其他位置的字母都是相对无序的，所以查找任意位置的字母是用不上索引的。
            %放在左边（后缀）：尾部的字母是没有顺序的，所以不能按照索引顺序查询，就用不到索引
            
    
    mysql默认的存储引擎innodb只显式支持B树索引，对于频繁访问的表，innodb会透明建立自适应hash索引
    即在B树索引基础上建立hash索引，可以显著提高查询效率，对于客户端是透明的，不可控制的，隐式的
    
# 数据库设计三大范式
1，第一范式（确保每列原子性）
    是最基本的范式。如果数据库表中所有字段都是不可拆分的原子值，就说明该数据库满足了第一范式
    第一范式的合理遵循需要根据系统的实际需求来定。比如某些数据库系统中需要用到“地址”这个属性，本来直接将“地址”属性设计成一个数据库表的字段就行。但是如果系统经常会访问“地址”属性中的“城市”部分，那么就非要将“地址”这个属性重新拆分为省份、城市、详细地址等多个部分进行存储，这样在对地址中某一部分操作的时候将非常方便。这样设计才算满足了数据库的第一范式，如下表所示。
2，第二范式（确保表中每列都和主键有关）
    在一个数据库表中，一个表中只能保存一种数据，不可以把多种数据保存在同一张数据库表中。
    第二范式需要确保数据库表中的每一列都和主键相关，而不能只与主键的某一部分相关（主要针对联合主键而言）。
    比如要设计一个订单信息表，因为订单中可能会有多种商品，所以要将订单编号和商品编号作为数据库表的联合主键。
    这样就产生一个问题：这个表中是以订单编号和商品编号作为联合主键。这样在该表中商品名称、单位、商品价格等信息不与该表的主键相关，而仅仅是与商品编号相关。所以在这里违反了第二范式的设计原则。
    而如果把这个订单信息表进行拆分，把商品信息分离到另一个表中，把订单项目表也分离到另一个表中，就非常完美了。
    这样设计，在很大程度上减小了数据库的冗余。如果要获取订单的商品信息，使用商品编号到商品信息表中查询即可。
3，第三范式(确保每列都和主键列直接相关,而不是间接相关)
    比如在设计一个订单数据表的时候，可以将客户编号作为一个外键和订单表建立相应的关系。而不可以在订单表中添加关于客户其它信息（比如姓名、所属公司等）的字段。如下面这两个表所示的设计就是一个满足第三范式的数据库表。

# ShardingSphere-Proxy
ShardingSphere-Proxy就是代理，类似网关，对外暴露一个入口，请求过来时，将请求转发到配置的真实的mysql实例。
转发之前做了一些处理工作，如果请求（也就是sql语句）带了分片键，则根据分片算法计算出真实的mysql实例以及表名，查询效率高
1，首先要编写的是server.yaml，配置全局信息
    ##治理中心
    # orchestration:
    #   name: orchestration_ds
    #   overwrite: true
    #   registry:
    #     type: zookeeper
    #     serverLists: localhost:2181
    #     namespace: orchestration
    #权限配置
    authentication:
      users:
        root:               #用户名
          password: root	#密码
        sharding:
          password: sharding 
          authorizedSchemas: sharding_db	#只能访问的逻辑数据库
    #Proxy属性
    props:
      max.connections.size.per.query: 1
      acceptor.size: 16  #用于设置接收客户端请求的工作线程个数，默认为CPU核数*2
      executor.size: 16  # Infinite by default.
      proxy.frontend.flush.threshold: 128  # The default value is 128.
        # LOCAL: Proxy will run with LOCAL transaction.
        # XA: Proxy will run with XA transaction.
        # BASE: Proxy will run with B.A.S.E transaction.
      proxy.transaction.type: LOCAL 	#默认为LOCAL事务
      proxy.opentracing.enabled: false     #是否开启链路追踪功能，默认为不开启。
      query.with.cipher.column: true
      sql.show: true				#SQL打印
      check.table.metadata.enabled: true			#是否在启动时检查分表元数据一致性，默认值: false
    # proxy.frontend.flush.threshold: 				# 对于单个大查询,每多少个网络包返回一次
2，分库分表（编辑config-sharding.yaml）
    //对外数据库名称
    schemaName: sharding_db
    //数据库链接公共参数
    dataSourceCommon:
     username: root
     password: 123456
     connectionTimeoutMilliseconds: 30000
     idleTimeoutMilliseconds: 60000
     maxLifetimeMilliseconds: 1800000
     maxPoolSize: 50
     minPoolSize: 1
     maintenanceIntervalMilliseconds: 30000
    //数据库地址，这里配置了两个库
    dataSources:
     ds_0:
       url: jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false
     ds_1:
       url: jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&useSSL=false
    //分片规则
    rules:
    - !SHARDING
     tables:
       //虚拟表名称，最后登陆 proxy之后，sharding_db库下只有一张表 test（我这里只分了一个表，用于测试）
       test:
         //ds_${0..1} 分库规则，库索引从0到1，一共两个，前缀为：ds_， test，分成3张表，索引从0到2，前缀为：test_
         actualDataNodes: ds_${0..1}.test_${0..2}
         tableStrategy:
           standard:
             //分片键
             shardingColumn: id
             shardingAlgorithmName: test_inline
         keyGenerateStrategy:
           column: id
           keyGeneratorName: snowflake  #主键生成策略 -- 雪花算法
    //默认数据库分片规则
     defaultDatabaseStrategy:
       standard:
         //依据 id 进行分片
         shardingColumn: id
         shardingAlgorithmName: database_inline
     defaultTableStrategy:
       none:
     shardingAlgorithms:
       database_inline:
         type: INLINE
         props:
           //数据库分片规则， id取模2，结果有 0 和 1，路由到 0 和 1这两个数据库
           algorithm-expression: ds_${id % 2}
       test_inline:
         type: INLINE
         props:
           //数据库表分表规则：id 模 3，结果有：0、1、2, 得到 test_0、test_1、test_2这三张表
           algorithm-expression: test_${id % 3}
     keyGenerators:
       snowflake:
         type: SNOWFLAKE
         props:
           worker-id: 123
3，配置主从同步（编写config-master_slave.yaml配置文件）
    schemaName: master_slave_db
    dataSources:
      master:
        url: jdbc:mysql://127.0.0.1:3307/test?characterEncoding=utf-8&serverTimezone=UTC&useSSL=false
        username: root
        password: root
        connectionTimeoutMilliseconds: 30000
        idleTimeoutMilliseconds: 60000
        maxLifetimeMilliseconds: 1800000
        maxPoolSize: 50
      slave0:
        url: jdbc:mysql://127.0.0.1:3308/test?characterEncoding=utf-8&serverTimezone=UTC&useSSL=false
        username: root
        password: root
        connectionTimeoutMilliseconds: 30000
        idleTimeoutMilliseconds: 60000
        maxLifetimeMilliseconds: 1800000
        maxPoolSize: 50
      slave1:
        url: jdbc:mysql://127.0.0.1:3309/test?characterEncoding=utf-8&serverTimezone=UTC&useSSL=false
        username: root
        password: root
        connectionTimeoutMilliseconds: 30000
        idleTimeoutMilliseconds: 60000
        maxLifetimeMilliseconds: 1800000
        maxPoolSize: 50
      slave2:
        url: jdbc:mysql://127.0.0.1:3310/test?characterEncoding=utf-8&serverTimezone=UTC&useSSL=false
        username: root
        password: root
        connectionTimeoutMilliseconds: 30000
        idleTimeoutMilliseconds: 60000
        maxLifetimeMilliseconds: 1800000
        maxPoolSize: 50
    masterSlaveRule:
      name: ms_ds
      masterDataSourceName: master
      slaveDataSourceNames:
        - slave0
        - slave1
        - slave2


弱一致性、强一致性、最终一致性

索引：https://juejin.im/post/5e6509fd518825490d1267eb


