package com.example.shuai.StatelessSession;

//StatelessSession（无状态 session）接口:
//作为选择，Hibernate 提供了基于命令的 API，可以用 detached object 的形式把数据以流的方法加入到数据库，或从数据库输出。
//StatelessSession 没有持久化上下文，也不提供多少高层的生命周期语义。特别是，无状态 session 不实现第一级 cache，也不和第二级缓存，或者查询缓存交互。
//它不实现事务化写，也不实现脏数据检查。用 stateless session 进行的操作甚至不级联到关联实例。
//stateless session 忽略集合类（Collections）。通过 stateless session 进行的操作不触发 Hibernate 的事件模型和拦截器。
//无状态 session 对数据的混淆现象免疫，因为它没有第一级缓存。无状态 session 是低层的抽象，和低层 JDBC 相当接近

public class StatelessSession {

    public static void main(String[] args) {

    }
}
