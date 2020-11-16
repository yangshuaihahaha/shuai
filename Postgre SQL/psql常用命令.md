# 库操作
```
#创建一个 runoobdb 的数据库
postgres=# CREATE DATABASE runoobdb;

#切换到runoobdb数据库
\c runoobdb;

#列出所有的数据库
runoobdb=# \l

#创建表名字为posts，字段为title， content
runoobdb-# create table posts (title varchar(255), content text);

#更改表名字
alter table posts rename to shuaiposts;

#列出表的详细信息
\d posts

#退出
\q

#倒入sql文件
\i db.sql

#删除数据库
drop table shuaiposts
```

# 字段类型
数值类型
 integer(int)
 real 也就是浮点类型
 serial 用于自增id
文字类型
 char
 varchar
 text
布尔型
 boolean
日期类型
 date
 time
 timestamp
特色类型
 Array
 网络地址类型(inet)
 json类型
 xml类型
 
 # 表约束
约束条件
not null：不为空
unique：在所有数据中值必须要唯一
check：字段设置条件
default：字段默认值
primary key(not null, unique)：主键，不能为空，且不可重复
```
CREATE TABLE posts(
	id serial PRIMARY KEY ,
	title VARCHAR(255) NOT NULL ,
	content text CHECK(length(content) > 3) ,
	is_draft boolean DEFAULT TRUE ,
	is_del boolean DEFAULT FALSE ,
	created_date TIMESTAMP DEFAULT 'now'
);
```
