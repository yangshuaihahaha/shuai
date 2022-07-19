# Docker命令
结合功能和应用场景方面的考虑，把命令行划分为4个部分，方便我们快速概览 Docker 命令行的组成结构：
环境信息相关	
    1. info
        环境信息相关
        这个命令在开发者报告 Bug 时会非常有用，结合 docker vesion 一起，可以随时使用这个命令把本地的配置信息提供出来，
        方便 Docker 的开发者快速定位问题
    2. version
        显示 Docker 的版本号，API 版本号，Git commit，Docker 客户端和后台进程的 Go 版本号。
系统运维相关	
    1. attach
        使用方法：
            docker attach [OPTIONS] CONTAINER
        使用这个命令可以挂载正在后台运行的容器，在开发应用的过程中运用这个命令可以随时观察容器內进程的运行状况。
        开发者在开发应用的场景中，这个命令是一个非常有用的命令。
    2. build
        使用方法：
            docker build [OPTIONS] PATH | URL | -
            这个命令是从源码构建新 Image 的命令。
            因为 Image 是分层的，最关键的 Base Image 是如何构建的是用户比较关心的，Docker 官方文档给出了构建方法
    3. commit
        使用方法：
            docker commit [OPTIONS] CONTAINER [REPOSITORY[:TAG]]
        这个命令的用处在于把有修改的 container 提交成新的 Image，然后导出此 Imange 分发给其他场景中调试使用。
        Docker 官方的建议是，当你在调试完 Image 的问题后，应该写一个新的 Dockerfile 文件来维护此 Image。
        commit 命令仅是一个临时创建 Imange 的辅助命令。
    4. cp
        使用方法： 
            cp CONTAINER:PATH HOSTPATH
        使用 cp 可以把容器內的文件复制到 Host 主机上。这个命令在开发者开发应用的场景下，会需要把运行程序产生的结果复制出来的需求，在这个情况下就可以使用这个 cp 命令。
    5. diff
        使用方法：
            docker diff CONTAINER
        diff 会列出3种容器内文件状态变化（A - Add，D - Delete，C - Change）的列表清单。
        构建Image的过程中需要的调试指令。
    6. images
        使用方法：
            docker images [OPTIONS] [NAME]
        Docker Image 是多层结构的，默认只显示最顶层的 Image。
        不显示的中间层默认是为了增加可复用性、减少磁盘使用空间，加快 build 构建的速度的功能，一般用户不需要关心这个细节。
    7. export/ import / save / load
        使用方法：
            docker export red_panda > latest.tar
            docker import URL|- [REPOSITORY[:TAG]]
            docker save IMAGE
            docker load
        这一组命令是系统运维里非常关键的命令。加载（两种方法：import，load），导出（一种方法：save，export）容器系统文件。
    8. inspect
        使用方法：
            docker inspect CONTAINER|IMAGE [CONTAINER|IMAGE...]
        例子：
            $ sudo docker inspect --format='{{.NetworkSettings.IPAddress}}' $INSTANCE_ID
        查看容器运行时详细信息的命令。了解一个 Image 或者 Container 的完整构建信息就可以通过这个命令实现。
    9. kill
        使用方法：
            docker kill [OPTIONS] CONTAINER [CONTAINER...]
        杀掉容器的进程。
    10. port
        使用方法：
            docker port CONTAINER PRIVATE_PORT
        打印出 Host 主机端口与容器暴露出的端口的 NAT 映射关系
    11. pause / unpause
        使用方法：
            docker pause CONTAINER
        使用 cgroup 的 freezer 顺序暂停、恢复容器里的所有进程。详细 freezer 的特性，请参考官方文档。
    12. ps
        使用方法：
            docker ps [OPTIONS]
        docker ps 打印出正在运行的容器，docker ps -a 打印出所有运行过的容器。
    13. rm
        使用方法：
            docker rm [OPTIONS] CONTAINER [CONTAINER...]
        删除指定的容器。
    14. rmi
        使用方法：
            docker rmi IMAGE [IMAGE...]
        指定删除 Image 文件。
    15. run
        使用方法：
            docker run [OPTIONS] IMAGE [COMMAND] [ARG...]
        例子：
            $ sudo docker run --cidfile /tmp/docker_test.cid ubuntu echo "test"
        这个命令是核心命令，可以配置的子参数详细解释可以通过 docker run --help 列出。
    16. start / stop / restart
        使用方法：
            docker start CONTAINER [CONTAINER...]
        这组命令可以开启（两个：start，restart），停止（一个：stop）一个容器。
    17. tag
        使用方法：
            docker tag [OPTIONS] IMAGE[:TAG] [REGISTRYHOST/][USERNAME/]NAME[:TAG]
        组合使用用户名，Image 名字，标签名来组织管理Image。
    18. top
        使用方法：
            docker top CONTAINER [ps OPTIONS]
        显示容器內运行的进程。
    19. wait
        使用方法：
            docker wait CONTAINER [CONTAINER...]
        阻塞对指定容器的其他调用方法，直到容器停止后退出阻塞。
    20. rename
        使用方法：
            docker rename CONTAINER NEW_NAME
        重新命名一个容器。
    21. stats
        使用方法：
            docker stats [OPTIONS] [CONTAINER...]
        实时显示容器资源使用监控指标。
    22. update
        使用方法：
            docker update [OPTIONS] CONTAINER [CONTAINER...]
        更新一或多个容器实例的 IO、CPU、内存，启动策略参数。
    23. exec
        使用方法：
            docker exec [OPTIONS] CONTAINER COMMAND [ARG...]
        在运行中容器中运行命令。
    24. deploy
        使用方法：
            docker deploy [OPTIONS] STACK
        部署新的 stack 文件，两种格式 DAB 格式和 Compose 格式，当前使用趋势来看，Compose 成为默认标准。
    25. create
        使用方法：
            docker create [OPTIONS] IMAGE [COMMAND] [ARG...]
        这是一个重要的命令，可以创建容器但并不执行它。
日志信息相关	
    1. events
        使用方法：
            docker events [OPTIONS]
        打印容器实时的系统事件。
    2. history
        使用方法：
            docker history [OPTIONS] IMAGE
        打印指定 Image 中每一层 Image 命令行的历史记录。
    3. logs
        使用方法：
            docker logs CONTAINER
        批量打印出容器中进程的运行日志。
Docker Hub服务相关	
    1. login/ logout
        使用方法：
            docker login [OPTIONS] [SERVER]
            docker logout [SERVER]
        登录登出 Docker Hub 服务。
    2. pull / push
        使用方法：
            docker push NAME[:TAG]
        通过此命令分享 Image 到 Hub 服务或者自服务的 Registry 服务。
    3. search
        使用方法：
            docker search TERM
        通过关键字搜索分享的 Image。
# 容器卷
Docker的理念：
    - 将运用与运行环境打包形成容器运行，运行可以伴随着容器，但是我们对数据的要求希望是持久化的
    - 容器之间希望有可能共享数据
Docker容器产生的数据，如果不通过docker commit生成新的镜像，使得数据作为镜像的一部分保存下来
那么当容器删除之后，数据自然也就没有了

为了能保存数据在docker中我们使用卷
主要是**为了解决数据持久化和数据共享**的问题
添加容器卷
    1，直接命令添加
        docker run -it -v /宿主机绝对路径目录:/容器内目录 镜像名
    2，Dockerfile添加
        （1）file构建
            # volume test
            FROM centos #第一层镜像
            VOLUME ["/dataVolumeContainer1","/dataVolumeContainer2"] #第二层镜像
            CMD echo "finshed, --------success"
            CMD /bin/bash # 第三层镜像
        （2）build生成镜像
            docker build -f /mydocker/Dockerfile -t yangshuai/centos .
            **yangshuai/centos**指的是命名空间
容器间数据共享
    
# Dockerfile
Dockerfile是什么？
    是用来构建Docker镜像的构建文件，由一系列命令和参数脚本构成，类似于shell命令
构建3步骤：
    1，编写Dockerfile文件
    2，docker build
    3，docker run
Dockerfile内容基础知识
    1，每条保留指令都必须为大写字母且后面要跟随至少一个参数
    2，指令按照从上到下，顺序执行
    3，#表示注释
    4，每条指令都会创建一个新的镜像层，并对镜像进行提交
Dockerfile大致流程
    1，docker从基础镜像运行一个容器
    2，执行一条指令并对容器作出修改
    3，执行类似docker commit的操作提交一个新的镜像
    4，docker再基于刚提交的镜像运行一个新容器
    5，执行dockerfile中的下一条指令直到所有指令都执行完成
保留字指令
    FROM 基础镜像，当前镜像是基于哪个镜像的
    MAINTAINER 镜像维护者的姓名和邮箱地址
    RUN 容器构建的时候需要运行的命令
    EXPOSE 当前容器对外暴露出的端口
    WORKERDIR 指定在创建容器后，终端默认登陆的进来工作目录，一个落脚点
    ENV 用来在构建镜像过程中设置环境变量
    ADD 将宿主机目录下的文件拷贝进镜像且ADD命令会自动处理URL和解压tar压缩包
    COPY 类似ADD，拷贝文件和目录到镜像
    VOLUME  容器数据卷，用于持久化数据
    CMD 指定一个容器启动时要运行的命令，Dockerfile中可以有多个CMD命令，但只有最后一个生效，CMD会被docker run之后参数替换
    ENTRYPOINT  指定一个容器启动时要运行的命令，ENTRYPOINT和CMD一样，都是在指定容器启动程序及参数
    ONBULLD 是一个特殊的指令，它后面跟的是其它指令，比如 RUN, COPY 等，而这些指令，在当前镜像构建时并不会被执行。只有当以当前镜像为基础镜像，去构建下一级镜像的时候才会被执行

# Docker 与虚拟机有何区别
虚拟机也是一种虚拟化技术，它与 Docker 最大的区别在于它是通过模拟硬件，并在硬件上安装操作系统来实现。
1、启动速度
    启动虚拟机需要先启动虚拟机的操作系统，再启动应用，这个过程非常慢;
    而启动 Docker 相当于启动宿主操作系统上的一个进程。
2、占用资源
    虚拟机是一个完整的操作系统，需要占用大量的磁盘、内存和 CPU 资源，一台机器只能开启几十个的虚拟机。
    而 Docker 只是一个进程，只需要将应用以及相关的组件打包，在运行时占用很少的资源，一台机器可以开启成千上万个 Docker。
3、隔离性
    与虚拟机相比，docker隔离性更弱，docker属于进程之间的隔离，虚拟机可实现系统级别隔离。
4、安全性
    docker的安全性也更弱。Docker的租户root和宿主机root等同，一旦容器内的用户从普通用户权限提升为root权限，它就直接具备了宿主机的root权限，进而可进行无限制的操作。虚拟机租户root权限和宿主机的root虚拟机权限是分离的，并且虚拟机利用如Intel的VT-d和VT-x的ring-1硬件隔离技术，这种隔离技术可以防止虚拟机突破和彼此交互，而容器至今还没有任何形式的硬件隔离，这使得容器容易受到攻击

# 什么是Docker
Docker是一个容器化平台，它以容器的形式将你的应用程序及所有的依赖项打包在一起，以确保你的应
用程序在任何环境中无缝运行。
# 什么是Docker镜像?
Docker镜像是Docker容器的源代码，Docker镜像用于闯将容器，使用Build命令创建镜像。
# 什么是Docker容器?
Docker容器包括应用程序及所有的依赖项，作为操作系统的独立进程运行。
