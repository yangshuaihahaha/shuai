# nacos核心功能点
服务注册
    nacos client会通过发送rest请求的方式向nacos server注册自己的服务，提供自身的元数据，比如ip地址、端口信心。
    nacos server接收到注册请求后，就会把这些元数据信息存储在一个双层的内存map中
    Nacos服务端收到请求后，做以下三件事：
        1，构建一个Service对象保存到ConcurrentHashMap集合中
        2，使用定时任务对当前服务下的所有实例建立心跳检测机制
        3，基于数据一致性协议服务数据进行同步
    源码分析
        1，在spring-cloud-alibaba-nacos-discpvery的spring.factories中发现核心注册类NacosDiscoveryAutoConfiguration
        2，NacosDiscoveryAutoConfiguration中会实例化三个bean，其中一个主要用于服务注册的是NacosAutoServiceRegistration
        3，NacosAutoServiceRegistration继承AbstractAutoServiceRegistration，AbstractAutoServiceRegistration实现了ApplicationListener<WebServerInitializedEvent>
        4，看到listener我们就应该知道，Nacos是通过Spring的事件机制继承到SpringCloud中去的。
        5，AbstractAutoServiceRegistration实现了onApplicationEvent抽象方法,并且监听WebServerInitializedEvent事件(当Webserver初始化完成之后) , 调用this.bind ( event )方法。
        6，bind就是注册开始的入口，然后里面有一个refister()方法开始注册流程
        7，真正的注册方法是NamingService.registerInstance（主要分为两部分，分别是心跳检测和服务注册）
        8，首先是beatReactor.addBeatInfo创建心跳信息实现健康检测
            这里主要是客户端通过schedule定时向服务端发送一个数据包 ,然后启动-个线程不断检测服务端的回应,
            如果在设定时间内没有收到服务端的回应,则认为服务器出现了故障。Nacos服务端会根据客户端的心跳包不断更新服务的状态。
        9，服务注册serverProxy.registerService
            这里会调用server实例注册接口：nacos/v1/ns/instance
            实现代码咋nacos-naming模块下的InstanceController类中
            这里主要做了三件事：
                1，构建一个service对象保存到ConcurrentHashMap集合中
                2，使用定时任务对当前服务下的所有实例建立心跳检测机制
                3，基于数据一致性协议服务数据进行同步
            nacos注册表的结构：
                Map<namespace, Map<group::serviceName, Service>>
                NameSpace(命名空间) -> Group(分组) -> Service(微服务) -> Cluster(集群) -> Instance(具体实例)
            具体步骤
                1，从请求参数汇总获得serviceName（服务名）和namespaceId（命名空间Id）
                2，创建一个空服务（在Nacos控制台“服务列表”中展示的服务信息），实际上是初始化一个serviceMap，它是一个ConcurrentHashMap集合
                3，根据namespaceId、serviceName从缓存中获取Service实例，如果Service实例为空，通过putService()方法将服务缓存到内存。
                4，执行service.init()：建立健康检查机制
                    它主要通过定时任务不断检测当前服务下所有实例最后发送心跳包的时间，
                    如果超时,则设置healthy为false表示服务不健康,并且发送服务变更事件。
                5，执行addInstance()(这里指的是阿里自己实现的AP模式Distro协议)：
                    DistroConsistencyServiceImpl.put(key, value)
                        1，将注册实例更新到内存注册表
                            往阻塞队列tasks中放注册实例的数据
                            循环从阻塞队列tasks中拿去到实例数据进行处理
                            将注册实例信息更新到注册表内存结构中去（里使用CopyOnWrite思想，解决冲突，提高并发）
                        2，同步实例信息到nacos server集群其他节点
                            也是把注册的实例放到一个阻塞队列里面，然后线程启动的时候就一直循环的从队列中取数据 
                            如果注册实例达到一定数量就批量同步给nacos其他节点，或者距离上次节点同步达到一定时间也开始批量同步
                            如果同步不成功就重试         
                6，consistencyService.listen实现数据一致性监听
                    Nacos服务地址动态感知原理
                        可以通过subscribe方法来实现监听，其中serviceName表示服务名、EventListener表示监听到的事件：
                            void subscribe(String serviceName, EventListener listener) throws NacosException;
                        具体调用方式
                            NamingService naming = NamingFactory.createNamingService(System.getProperty("serveAddr"));
                            naming.subscribe("example", event -> {
                                if (event instanceof NamingEvent) {
                                    System.out.println(((NamingEvent)event).getServerName();
                                    System.out.println(((NamingEvent)event).getInstances();
                                }
                            })
                        或者调用selectInstance方法，如果将subacribe属性设置为true，会自动注册监听
                            public List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy, boolean subscribe))
                        客户端有一个hostReactor类，它的功能是实现服务的动态更新，基本原理是：
                            1，客户端发起时间订阅后，在HostReactor中有一个UpdateTask线程，每10s发送一次Pull请求，获得服务端最新的地址列表
                            2，对于服务端，它和服务提供者的实例之间维持了心跳检测，一旦服务提供者出现异常，则会发送一个Push消息给Nacos客户端，也就是服务端消费者
                            3，服务消费者收到请求之后，使用HostReactor中提供的processServiceJSON解析消息，并更新本地服务地址列表
服务心跳
    服务注册后，nacos client会维护一个定时心跳来持续通知nacos server，说明服务一直处于可用状态
服务同步
    nacos server集群之间会互相同步服务实例，保证服务信息的一致性
服务发现
    服务消费者（nacos client）在调用服务提供者的服务时，会发送一个rest请求给nacos server，获取上面注册的服务清单
    并缓存到nacos client本地，同时会在nacos client本地开启一个定时任务定时拉取服务端最新的注册表信息更新到本地缓存
服务健康检查
    nacos server会开启一个定时任务来检查注册服务实例的健康状况，对于超过15s没有收到客户端心跳的实例会将它的healthy属性设置为false
    如果某个实例超过30s没有收到心跳，直接剔除该实例（被剔除的实例如果恢复发送心跳则会重新注册）