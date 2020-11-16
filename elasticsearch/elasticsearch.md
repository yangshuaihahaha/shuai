# 什么是ElasticSearch
ElasticSearch是一个基于Lucene的搜索引擎，它是一个分布式多用户能力的全文搜索引擎，基于RESTful接口

# elasticsearch 的倒排索引是什么
传统的我们检索文章就是通过逐个遍历，找到关键词的位置，而倒排索引是通过分词策略，形成词典和文章的关系表，这种词典+映射表即为倒排索引

# elasticsearch 索引(写入)文档的规程
1，ES的任意一个节点都可以作为协调节点接受请求
2，协调节点接收到请求后，ES会根据传入的__routing参数计算出文档要分配到的节点编号
3，节点执行写操作，成功后会转发到其他的副节点，都写入成功后接受请求的协调节点向客户端发送请求报告写入成功

# Elasticsearch 更新和删除文档的过程。
1，删除和更新也都是写操作，但是Elasticsearch中的文档是不可变的，因此不能被删除或者改动以展示其改变
2，磁盘上每个段都有一个相应的.del文件，当删除请求发送后，文档并没有真的被删除，而是在.del文件中标记为删除，该文档依然能被匹配查询
但是会在结果中被过滤掉，当段合并时，在.del文件中被标记为删除的段不再写入新的段
3，在新的文档被创建时，Elasticsearch会为该文档指定一个版本号，当执行更新时，旧的文档会在.del文件中被标记为删除，新的文档会被索引到
一个新的段，旧的文档依然能够匹配查询到，但是会在结果中被过滤掉

# Elasticsearch 搜索的过程。
1，搜索分为两个过程，Query Then Fetch
2，在查询阶段，查询会广播到索引的每一个分片，每个分片都会在本地执行搜索并构建一个匹配的文档
3，每个分片返回各自优先队列中所有的文档ID和排序值给协调节点，它合并这些值到自己的优先队列中来产生一个全局排序过后的结果列表
4，接下来就是取回阶段，协调节点辨别出那些文档需要被取回，并向相关的分片提交多个GET请求，每个切片返回给协调节点然后返回结果给客户端

# Elasticsearch 什么是索引
类似于关系型数据库中的表，存放一堆具有相似文档的数据，比如说一个客户索引，订单索引，

# Elasticsearch 什么是文档
类似于关系型数据库中的行，存储数据的载体，包含一个或多个存有数据的字段

# Elasticsearch如何选择索引或类型来存储数据
一个索引存储在一个或多个分片中，Lucene索引在磁盘空间、内存使用和文件描述符等方面有一定的固定开销
因此，一个大的索引比几个小的索引更有效率

# 什么是Elasticsearch中的mapping
mapping是用来定义Document中的每个字段的一些属性，比如说类型，是否存储，是否分词等

# 什么是Elasticsearch的translog
translog是elasticsearch的事务日志文件，它记录了所有对索引分片的事务操作（add/update/delete），每个分片对应一个translog文件。
translog是用来恢复数据的。Es用“后写”的套路来加快写入速度 — 写入的索引并没有实时落盘到索引文件，而是先双写到内存和translog文件，

# Elasticsearch的优化
## 1，设计阶段的优化
1）Mapping 阶段充分结合各个字段的属性，是否需要检索、是否需要存储等
2）针对需要分词的字段，合理设置分词器
3）基于日期模版创建索引
4）使用别名进行索引管理
## 2，写入优化
1）写入前副本数设置为0，关闭 refresh_interval，写入后恢复副本数和刷新间隔；
2）写入过程中：采取 bulk 批量写入
3）尽量使用自动生成的id
## 3，查询优化
1）充分利用倒排索引机制能keyword尽量keyword
2）数据量大的时候，可以基于时间敲定索引再检索
3）禁用wildcard
4）禁用批量 terms

# Elasticsearch中match、match_phrase、query_string和term的区别
text字段会被分词，keyword字段不会被分词

## 1，term查询keyword字段。
term不会分词。而keyword字段也不分词。需要完全匹配才可。
## 2，term查询text字段。
因为text字段会分词，而term不分词，所以term查询的条件必须是text字段分词后的某一个。

## 3，match查询keyword字段
match会被分词，而keyword不会被分词，match的需要跟keyword的完全匹配可以。
## 4，match查询text字段
match分词，text也分词，只要match的分词结果和text的分词结果有相同的就匹配。

## 5，match_phrase匹配keyword字段。
match_phrase会被分词，而keyword不会被分词，match_phrase的需要跟keyword的完全匹配才可以。
## 6，match_phrase匹配text字段。
match_phrase是分词的，text也是分词的。match_phrase的分词结果必须在text字段分词中都包含，而且顺序必须相同，而且必须都是连续的。

slop参数告诉match_phrase查询词条能够相隔多远时仍然将文档视为匹配

## 7，query_string查询key类型的字段，无法查询。
## 8，query_string查询text类型的字段。
和match_phrase区别的是，query_string查询text类型字段，不需要连续，顺序还可以调换。




