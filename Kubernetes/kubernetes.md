# 什么是Kubernetes
Kubernetes是一个容器管理工具。可以提供容器的部署、扩容和负载均衡等。
真实的生产环境会包含多个容器，而这些容器还会跨越多个主机部署。
Kubernetes 提供这些容器所需的的编排与管理能力。


# 简述Kubernetes和Docker的关系
Docker提供容器的生命周期管理和Docker镜像构建运行时容器。它的主要作用是将应用程序运行时所需的依赖打包到一个容器中，从而实现了可移植性的优点。
但是Docker作为单一的容器技术，我们在生产环境中，往往会有大规模的容器化部署。
而Docker难以支撑大规模容器化部署的要求，所以就有了Kubernetes。
Kubernetes可以对大规模容器进行组织和管理。在Docker的基础上，为容器化的应用提供部署、动态伸缩和负载均衡等一系列完整功能，提高了大规模容器集群管理的便捷性



# 基础概念
Master（主节点）：
    控制 Kubernetes 节点的机器，也是创建作业任务的地方。
Node（节点）：
    这些机器在 Kubernetes 主节点的控制下执行被分配的任务。
Pod：
    由一个或多个容器构成的集合，作为一个整体被部署到一个单一节点。
    同一个 pod 中的容器共享 IP 地址、进程间通讯（IPC）、主机名以及其它资源。
    Pod 将底层容器的网络和存储抽象出来，使得集群内的容器迁移更为便捷。
Replication controller（复制控制器）： 
    控制一个 pod 在集群上运行的实例数量。
Service（服务）：
    Service提供了一个统一的服务访问入口以及服务代理和发现机制，关联多个相同Label的Pod。
    将服务内容与具体的 pod 分离。Kubernetes 服务代理负责自动将服务请求分发到正确的 pod 处，不管 pod 移动到集群中的什么位置，甚至可以被替换掉。
kubectl： 
    这是 Kubernetes 的命令行配置工具。
Label：
    Kubernetes中的Label实质是一系列的Key/Value键值对，其中key与value可自定义。
    Label可以附加到各种资源对象上，如Node、Pod、Service、RC等。
    一个资源对象可以定义任意数量的Label，同一个Label也可以被添加到任意数量的资源对象上去。
    Kubernetes通过Label Selector（标签选择器）查询和筛选资源对象。
Volume：
    Volume是Pod中能够被多个容器访问的共享目录，Kubernetes中的Volume是定义在Pod上，
    可以被一个或多个Pod中的容器挂载到某个目录下。
Namespace：
    Namespace用于实现多租户的资源隔离，可将集群内部的资源对象分配到不同的Namespace中
    形成逻辑上的不同项目、小组或用户组，便于不同的Namespace在共享使用整个集群的资源的同时还能被分别管理。




# Minikube、Kubectl、Kubelet分别是什么
Minikube是一种可以在本地轻松运行一个单节点Kubernetes群集的工具
Kubectl是一个命令行工具，可以使用该工具控制Kubernetes集群管理器，如检查群集资源，创建、删除和更新组件，查看应用程序。
Kubelet是一个代理服务，它在每个节点上运行，并使从服务器与主服务器通信。





# 数据服务分类：
有状态服务：DBMS（数据库管理系统）
    有状态的服务需要在本地存储持久化数据，典型的是分布式数据库的应用
    分布式节点实例之间有依赖的拓扑关系.比如,主从关系
    如果K8S停止分布式集群中任 一实例pod,就可能会导致数据丢失或者集群的crash.
无状态服务：LVS APACHE
    无状态服务不会在本地存储持久化数据.多个服务实例对于同一个用户请求的响应结果是完全一致的
    这种多服务实例之间是没有依赖关系,比如web应用,在k8s控制器 中动态启停无状态服务的pod并不会对其它的pod产生影响.




# Master节点架构
![img.png](image/Master主节点架构.png)
kube-apiserver：
    对外暴露 K8S 的 api 接口，是外界进行资源操作的唯一入口
    提供认证、授权、访问控制、API 注册和发现等机制
etcd：
    etcd 是兼具一致性和高可用性的键值数据库，可以作为保存 Kubernetes 所有集群数据的后台数据库。
    Kubernetes 集群的 etcd 数据库通常需要有个备份计划
kube-scheduler
    主节点上的组件，该组件监视那些新创建的未指定运行节点的 Pod，并选择节点让 Pod 在上面运行。
    所有对 k8s 的集群操作，都必须经过主节点进行调度
kube-controller-manager
    在主节点上运行控制器组件
    这些控制器包括:
        节点控制器(Node Controller): 负责在节点出现故障时进行通知和响应。
        副本控制器(Replication Controller): 负责为系统中的每个副本控制器对象维护正确数量的 Pod。
        端点控制器(Endpoints Controller): 填充端点(Endpoints)对象(即加入 Service与 Pod)。
        服务帐户和令牌控制器(Service Account & Token Controllers): 为新的命名空间创建默认帐户和 API 访问令牌


# Node节点架构
![img.png](image/Node节点架构.png)
kubelet：
    一个在集群中每个节点上运行的代理。它保证容器都运行在 Pod 中。
    负责维护容器的生命周期，同时也负责 Volume(CSI)和网络(CNI)的管理;
kube-proxy
    负责为 Service 提供 cluster 内部的服务发现和负载均衡;
容器运行环境(Container Runtime)
    容器运行环境是负责运行容器的软件。
    Kubernetes 支持多个容器运行环境: Docker、 containerd、cri-o、 rktlet 以及任何实现 Kubernetes CRI (容器运行环境接口)。
fluentd
    是一个守护进程，它有助于提供集群层面日志 集群层面的日志






# k8s集群安装
kubeadm 是官方社区推出的一个用于快速部署 kubernetes 集群的工具。 这个工具能通过两条指令完成一个 kubernetes 集群的部署:
1，创建一个Master节点
```kubeadm init```
2，将一个 Node 节点加入到当前集群中
```kubeadm join <Master 节点的 IP 和端口 >```

## 部署步骤
安装步骤总结：
    1，在所有节点上安装 Docker 和 kubeadm
    2，部署 Kubernetes Master
    3，部署容器网络插件
    4，部署 Kubernetes Node，将节点加入 Kubernetes 集群中
详细步骤：
    1， 设置 linux 环境(三个节点都执行)
    ```
    ## 关闭防火墙:
        systemctl stop firewalld systemctl disable firewalld
    ## 关闭 selinux:
        sed -i 's/enforcing/disabled/' /etc/selinux/config setenforce 0
    ## 关闭 swap:
        swapoff -a 临时
        sed -ri 's/.*swap.*/#&/' /etc/fstab 永久 free -g 验证，swap 必须为 0;
    ## 添加主机名与 IP 对应关系 
        vi /etc/hosts
        10.0.2.15 k8s-node1
        10.0.2.24 k8s-node2
        10.0.2.25 k8s-node3
        hostnamectl set-hostname <newhostname>:指定新的 hostname su 切换过来
    ## 将桥接的 IPv4 流量传递到 iptables 的链: 
        cat > /etc/sysctl.d/k8s.conf << EOF 
        net.bridge.bridge-nf-call-ip6tables = 1 
        net.bridge.bridge-nf-call-iptables = 1
        EOF
        sysctl --system
    ## 疑难问题: 遇见提示是只读的文件系统，运行如下命令 
        mount -o remount rw /
        date 查看时间 (可选)
        yum install -y ntpdate
        ntpdate time.windows.com 同步最新时间
    ```
    2，所有节点安装 Docker、kubeadm、kubelet、kubectl
    （1）卸载系统之前的 docker
        ```
        sudo yum remove docker \ docker-client \
        docker-client-latest \ docker-common \ docker-latest \ docker-latest-logrotate \ docker-logrotate \ docker-engine
        ```
    （2）安装 Docker-CE
        ```
        ## 安装必须的依赖
            sudo yum install -y yum-utils \
            device-mapper-persistent-data \ lvm2
        ## 设置 docker repo 的 yum 位置 
            sudo yum-config-manager \
            --add-repo \
            https://download.docker.com/linux/centos/docker-ce.repo
        ## 安装 docker，以及 docker-cli
            sudo yum install -y docker-ce docker-ce-cli containerd.io
        ```
    （3）配置 docker 加速
        ```
        sudo mkdir -p /etc/docker
        sudo tee /etc/docker/daemon.json <<-'EOF' {
        "registry-mirrors": ["https://82m9ar63.mirror.aliyuncs.com"] }
        EOF
        sudo systemctl daemon-reload sudo systemctl restart docker
        ```
    （4）启动 docker & 设置 docker 开机自启
        ```
        systemctl enable docker
        ```
    （5）安装 kubeadm，kubelet 和 kubectl
        ```
        yum list|grep kube
        yum install -y kubelet-1.17.3 kubeadm-1.17.3 kubectl-1.17.3
        systemctl enable kubelet 
        systemctl start kubelet
        ```
    3，部署 k8s-master
    （1）master 节点初始化
        ```
        $ kubeadm init \
        --apiserver-advertise-address=10.0.2.15 \
        --image-repository registry.cn-hangzhou.aliyuncs.com/google_containers \ --kubernetes-version v1.17.3 \
        --service-cidr=10.96.0.0/16 \
        --pod-network-cidr=10.244.0.0/16
        ```
        由于默认拉取镜像地址 k8s.gcr.io 国内无法访问，这里指定阿里云镜像仓库地址。可以手动 按照我们的 images.sh 先拉取镜像，
    （2）测试 kubectl(主节点执行)
        ```
        mkdir -p $HOME/.kube
        sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config sudo chown $(id -u):$(id -g) $HOME/.kube/config
        ```
        $ kubectl get nodes 获取所有节点
        目前 master 状态为 notready。等待网络加入完成即可。
        kubeadm join 10.0.2.15:6443 --token 8mgmlh.cgtgsp3samkvpksn \ --discovery-token-ca-cert-hash
        sha256:3cf99aa2e6bfc114c5490a7c6dffcf200b670af21c5a662c299b6de606023f85
    4，安装 Pod 网络插件(CNI)
        ```$ kubectl apply -f \ https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml```
    5，加入 Kubernetes Node
        在 Node 节点执行。 向集群添加新节点，执行在 kubeadm init 输出的 kubeadm join 命令







# KubeSphere
默认的 dashboard 没啥用，我们用 kubesphere 可以打通全部的 devops 链路。 Kubesphere 集成了很多套件，集群要求较高
KubeSphere 是一款面向云原生设计的开源项目，在目前主流容器调度平台 Kubernetes 之 上构建的分布式多租户容器管理平台
提供简单易用的操作界面以及向导式操作方式，在降 低用户使用容器调度平台学习成本的同时，极大降低开发、测试、运维的日常工作的复杂度。

## 安装前提环境
1，安装 helm(master 节点执行)
2，安装 Tiller(master 执行)
3，安装 OpenEBS(master 执行)

## 最小化安装
KubeSphere 最小化安装:
```kubectl apply -f \ https://raw.githubusercontent.com/kubesphere/ks-installer/master/kubesphere-minimal.yaml```
查看安装日志，请耐心等待安装成功。

## 完整化安装
完整化安装主要是安装KubeSphere一些扩展模块，比如内置的DevOps系统、告警通知模块等
比如开启安装 DevOps 系统
    通过修改 ks-installer 的 configmap 可以选装组件，执行以下命令（kubectl 命令需要以 root 用户执行）。
    ```$ kubectl edit cm -n kubesphere-system ks-installer```
    参考如下修改 ConfigMap
    ```
    devops:
          enabled: True
          jenkinsMemoryLim: 2Gi
          jenkinsMemoryReq: 1500Mi
          jenkinsVolumeSize: 8Gi
          jenkinsJavaOpts_Xms: 512m
          jenkinsJavaOpts_Xmx: 512m
          jenkinsJavaOpts_MaxRAM: 2g
          sonarqube:
            enabled: True
    ```

## 建立多租户系统
KubeSphere 多租户体系中，将资源划分为以下三个层级：
- 集群
- 企业空间
- 项目和 DevOps 工程
针对不同层级的资源都可以灵活地定制角色用以划分用户的权限范围，实现不同用户之间的资源隔离。
![img.png](image/kubesphere多租户系统.png)
## 权限管理模型
常见的权限管理模型有 ACL、DAC、MAC、RBAC、ABAC 这几类。
在 KubeSphere 中我们借助 RBAC 权限管理模型来做用户的权限控制，
用户并不直接和资源进行关联，而是经由角色定义来进行权限控制。


# 基础使用（部署WordPress）
## 创建 MySQL 密钥
（1）以项目普通用户 project-regular登录 KubeSphere，在当前项目下左侧菜单栏的 配置中心 选择 密钥，点击 创建。
    ![img.png](image/创建MySQL密钥.png)
（2）填写密钥的基本信息，完成后点击 下一步。
    - 名称：作为 MySQL 容器中环境变量的名称，可自定义，例如 mysql-secret
    - 别名：别名可以由任意字符组成，帮助您更好的区分资源，例如 MySQL 密钥
    - 描述信息：简单介绍该密钥，如 MySQL 初始密码
（3）密钥设置页，填写如下信息，完成后点击 创建。
    - 类型：选择 默认(Opaque)
    - Data：Data 键值对填写 MYSQL_ROOT_PASSWORD和 123456
    ![img.png](image/密钥设置.png)
## 创建 WordPress 密钥
同上，创建一个 WordPress 密钥，Data 键值对填写 WORDPRESS_DB_PASSWORD和 123456。此时两个密钥都创建完成。
![img.png](image/创建WordPress密钥.png)
## 创建存储卷
1，在当前项目下左侧菜单栏的 存储卷，点击 创建，基本信息如下。
    - 名称：wordpress-pvc
    - 别名：Wordpress 持久化存储卷
    - 描述信息：Wordpress PVC
2，完成后点击 下一步，存储类型默认 local，访问模式和存储卷容量也可以使用默认值，点击 下一步，直接创建即可。
    ![img.png](image/创建存储卷.png)
## 创建应用
### 添加 MySQL 组件
1，在左侧菜单栏选择 应用负载 → 应用，然后点击 部署新应用。
    ![img.png](image/部署新应用.png)
2，基本信息中，参考如下填写，完成后在右侧点击 添加组件。
    - 应用名称：必填，起一个简洁明了的名称，便于用户浏览和搜索，例如填写 wordpress
    - 描述信息：简单介绍该工作负载，方便用户进一步了解
3，Mysql组件信息
    - 名称： mysql
    - 组件版本：v1
    - 别名：MySQL 数据库
    - 负载类型：选择 有状态服务
    ![img.png](image/MySQL组件信息.png)
4，点击 添加容器镜像，镜像填写 mysql:5.6（应指定镜像版本号)，然后按回车键或点击 DockerHub，点击 使用默认端口。
    ![img.png](image/添加容器镜像.png)
5，下滑至环境变量，在此勾选 环境变量，然后选择 引用配置文件或密钥，名称填写为 MYSQL_ROOT_PASSWORD，下拉框中选择密钥为 mysql-secret和 MYSQL_ROOT_PASSWORD。
    ![img.png](image/勾选环境变量.png)
6，点击 添加存储卷模板，为 MySQL 创建一个 PVC 实现数据持久化。
    ![img.png](image/添加存储卷.png)
    ![img.png](image/添加存储卷2.png)
    存储卷名称：必填，起一个简洁明了的名称，便于用户浏览和搜索，此处填写 mysql-pvc
    存储类型：选择集群已有的存储类型，如 Local
    容量和访问模式：容量默认 10 Gi，访问模式默认 ReadWriteOnce (单个节点读写)
    挂载路径：存储卷在容器内的挂载路径，选择 读写，路径填写 /var/lib/mysql
## 添加WordPress组件
1，参考如下提示完成 WordPress 组件信息：
    - 名称： wordpress
    - 组件版本：v1
    - 别名：Wordpress前端
    - 负载类型：默认 无状态服务
    ![img.png](image/添加WordPress组件.png)
2，点击 添加容器镜像，镜像填写 wordpress:4.8-apache（应指定镜像版本号)，然后按回车键或点击 DockerHub，点击 使用默认端口。
    ![img.png](image/添加wordpress容器镜像.png)
3，下滑至环境变量，在此勾选 环境变量，这里需要添加两个环境变量：
    - 点击 引用配置文件或密钥，名称填写 WORDPRESS_DB_PASSWORD，选择在第一步创建的配置 (Secret) wordpress-secret和 WORDPRESS_DB_PASSWORD。
    - 点击 添加环境变量，名称填写 WORDPRESS_DB_HOST，值填写 mysql，对应的是上一步创建 MySQL 服务的名称，否则无法连接 MySQL 数据库。
    ![img.png](image/添加WordPress环境变量.png)
4，点击 添加存储卷，选择已有存储卷 wordpress-pvc，访问模式改为 读写，容器挂载路径 /var/www/html。完成后点击 √。
    ![img.png](image/添加WordPress存储卷.png)
5，检查 WordPress 组件信息无误后，再次点击 √，此时 MySQL 和 WordPress 组件信息都已添加完成，点击 创建。








# DevOps
1，项目开发需要考虑的维度
    Dev:怎么开发? 
    Ops:怎么运维? 
    高并发:怎么承担高并发 
    高可用:怎么做到高可用
2，什么是 DevOps
    ![img.png](image/DevOps.png)
    微服务，服务自治。
    DevOps: Development 和 Operations 的组合
    - DevOps 看作开发(软件工程)、技术运营和质量保障(QA)三者的交集。
    - 突出重视软件开发人员和运维人员的沟通合作，通过自动化流程来使得软件构建、测试、 发布更加快捷、频繁和可靠。
    - DevOps 希望做到的是软件产品交付过程中 IT 工具链的打通，使得各个团队减少时间损 耗，更加高效地协同工作。专家们总结出了下面这个 DevOps 能力图，良好的闭环可以大大 增加整体的产出。
3，什么是 CI&CD
    ![img.png](image/CI&CD.png)
    （1）持续集成(Continuous Integration)
        - 持续集成是指软件个人研发的部分向软件整体部分交付，频繁进行集成以便更快地发现 其中的错误。“持续集成”源自于极限编程(XP)，是 XP 最初的 12 种实践之一。
        - CI 需要具备这些:
            - 全面的自动化测试。这是实践持续集成&持续部署的基础，同时，选择合适的自动化测试工具也极其重要;
            - 灵活的基础设施。容器，虚拟机的存在让开发人员和 QA 人员不必再大费周折;
            - 版本控制工具。如 Git，CVS，SVN 等;
            - 自动化的构建和软件发布流程的工具，如 Jenkins，flow.ci;
            - 反馈机制。如构建/测试的失败，可以快速地反馈到相关负责人，以尽快解决达到一个更稳定的版本。
    （2）持续交付(Continuous Delivery)
        持续交付在持续集成的基础上，将集成后的代码部署到更贴近真实运行环境的「类生产环境」 (production-like environments)中。持续交付优先于整个产品生命周期的软件部署，建立 在高水平自动化持续集成之上。
        持续交付和持续集成的优点非常相似:
            - 快速发布。能够应对业务需求，并更快地实现软件价值。
            - 编码->测试->上线->交付的频繁迭代周期缩短，同时获得迅速反馈;
            - 高质量的软件发布标准。整个交付过程标准化、可重复、可靠，
            - 整个交付过程进度可视化，方便团队人员了解项目成熟度; 
            - 更先进的团队协作方式。从需求分析、产品的用户体验到交互 设计、开发、测试、运维等角色密切协作，相比于传统的瀑布式软件团队，更少浪费。
    （3）持续部署(Continuous Deployment)
        持续部署是指当交付的代码通过评审之后，自动部署到生产环境中。持续部署是持续交付的 最高阶段。这意味着，所有通过了一系列的自动化测试的改动都将自动部署到生产环境。它 也可以被称为“Continuous Release”。





# Kubesphere中的Devops功能
![img.png](image/简单说明流水线完整工作过程.png)
阶段一. 从Github检出我们的代码
阶段二. Unit test: 单元测试
阶段三. SonarQube 进行代码质量检测
阶段四. 把我们的代码构建成Docker镜像
阶段五. 将镜像推送至Dockerhub或者阿里云的镜像仓库
阶段六. Deploy to dev: 将 master 分支部署到 Dev 环境，此阶段需要审核。
阶段七. Push with tag: 生成 tag 并 release 到 GitHub，并推送到 DockerHub。
阶段八. Deploy to production: 将发布的 tag 部署到 Production 环境。






# 使用Kubesphere创建Mysql集群
1，创建一个有状态的服务mysql-master，容器副本数量为1。
2，添加容器镜像，版本是5.7
3，设置环境变量，主要是设置用户名密码
4，设置配置文件和挂载

然后同样的步骤创建两个slaver：mysql-master1，mysql-master2

这样我们就得到了3个mysql服务器。
然后就可以进行主从同步的配置了。

同理我们可以这样进行redis集群的搭建，以及sentinel、nacos、ElasticSearch、集群搭RocketMQ建等等





# k8s部署应用的流程
1，将我们项目中的每一个微服务都打包成一个镜像，然后上传到Dockerhub
    这就需要在每一个微服务项目下面准备一个Dockerfile，Docker按照这个Dockerfile将项目打包成镜像
2，然后将打包成的微服务镜像上传到Dockerhub
    因为在k8s中创建服务，必须是从Dockerhub中拉去的镜像
3，还需要为每个微服务项目生成一个k8s部署描述文件
    Jenkins自动创建服务Pod，就是根据这个描述文件来的
4，我们还可以指定每个微服务的副本数，然后再将这些副本封装成一个service对外提供服务
这里的所有过程都是通过Jenkins持续集成来实现的


# 生产环境配置抽取
之前的配置中我们都使用的是ip地址的方式配置依赖服务的ip和端口号，比如：
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
spring.redis.host=192.168.56.10

这样的ip地址访问是难记住的。所以这里我们改成域名的方式。
以nacos为例，我们再创建一个nacos-service，指定它的应用负载到原来的nacos服务。
然后指定访问类型是以"集群内部通过服务后端Endpoint IP直接访问服务Headless"也就是以域名的方式访问
这样我们就可以域名的方式访问nacos，比如：spring.cloud.nacos.discovery.server-addr=nacos-service.gulimall:8848



# 创建微服务的Dockerfile
每个微服务下面的dockerfile基本上都这样
docker就可以按照dockerfile打包成镜像
```
FROM java:8
EXPOSE 8080
VOLUME /temp
ADD target/hello.jar /app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

# 创建k8s部署描述文件
该描述文件放会在每个微服务下面创建一个deploy文件夹。
文件内容示例devops-sample.yaml
```yml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: kubesphere
    component: ks-sample
    tier: backend
  name: ks-sample
  namespace: kubesphere-sample-prod
spec:
  progressDeadlineSeconds: 600
  replicas: 2
  selector:
    matchLabels:
      app: kubesphere
      component: ks-sample
      tier: backend
  strategy:
    rollingUpdate:
      maxSurge: 100%
      maxUnavailable: 100%
    type: RollingUpdate
  template:
    metadata:
      labels:
        app: kubesphere
        component: ks-sample
        tier: backend
    spec:
      containers:
        - env:
            - name: CACHE_IGNORE
              value: js|html
            - name: CACHE_PUBLIC_EXPIRATION
              value: 3d
          image: $REGISTRY/$HARBOR_NAMESPACE/$APP_NAME:$TAG_NAME
          readinessProbe:
            httpGet:
              path: /
              port: 8080
            timeoutSeconds: 10
            failureThreshold: 30
            periodSeconds: 5
          imagePullPolicy: Always
          name: ks
          ports:
            - containerPort: 8080
              protocol: TCP
          resources:
            limits:
              cpu: 300m
              memory: 600Mi
            requests:
              cpu: 100m
              memory: 100Mi
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
      dnsPolicy: ClusterFirst
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
```



# 编写Jenkinsfile文件，部署每一个微服务
```yml
pipeline {
  agent {
    node {
      label 'maven'
    }
  }
参数化构建，这里可以接受外面传进来的参数
    parameters {
        string(name:'TAG_NAME',defaultValue: '',description:'')
    }
环境变量
    environment {
        DOCKER_CREDENTIAL_ID = 'dockerhub-id'
        GITHUB_CREDENTIAL_ID = 'github-id'
        KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
        REGISTRY = 'docker.io'
        DOCKERHUB_NAMESPACE = 'docker_username'
        GITHUB_ACCOUNT = 'kubesphere'
        APP_NAME = 'devops-java-sample'
    }

    stages {
        checkout代码
        stage ('checkout scm') {
            steps {
                checkout(scm)
            }
        }
单元测试
        stage ('unit test') {
            steps {
                container ('maven') {
                    sh 'mvn clean  -gs `pwd`/configuration/settings.xml test'
                }
            }
        }
 构建镜像，推送到Dockerhub
        stage ('build & push') {
            steps {
                container ('maven') {
        打包镜像
                    sh 'mvn  -Dmaven.test.skip=true -gs `pwd`/configuration/settings.xml clean package'
                    sh 'docker build -f Dockerfile-online -t $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
                    withCredentials([usernamePassword(passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID" ,)]) {
                        sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
                        sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER'
                    }
                }
            }
        }
push最新版本
        stage('push latest'){
           when{
             branch 'master'
           }
           steps{
                container ('maven') {
                  sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest '
                  sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest '
                }
           }
        }
发布到dev环境
        stage('deploy to dev') {
          when{
            branch 'master'
          }
          steps {
            input(id: 'deploy-to-dev', message: 'deploy to dev?')
            kubernetesDeploy(configs: 'deploy/dev-ol/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
          }
        }
        stage('push with tag'){
          when{
            expression{
              return params.TAG_NAME =~ /v.*/
            }
          }
          steps {
              container ('maven') {
                input(id: 'release-image-with-tag', message: 'release image with tag?')
                  withCredentials([usernamePassword(credentialsId: "$GITHUB_CREDENTIAL_ID", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                    sh 'git config --global user.email "kubesphere@yunify.com" '
                    sh 'git config --global user.name "kubesphere" '
                    sh 'git tag -a $TAG_NAME -m "$TAG_NAME" '
                    sh 'git push http://$GIT_USERNAME:$GIT_PASSWORD@github.com/$GITHUB_ACCOUNT/devops-java-sample.git --tags --ipv4'
                  }
                sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:$TAG_NAME '
                sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:$TAG_NAME '
          }
          }
        }
        stage('deploy to production') {
          when{
            expression{
              return params.TAG_NAME =~ /v.*/
            }
          }
          steps {
            input(id: 'deploy-to-production', message: 'deploy to production?')
            kubernetesDeploy(configs: 'deploy/prod-ol/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
          }
        }
    }
}
```
这里构建完Jenkinsfile之后我们就可以使用这个流水线了，我们需要运行多次来启动每一个微服务，如下图所示
在运行的时候可以输入Project Name，那么就可以构建某一个微服务了
![img.png](image/使用devops流水线构建微服务.png)





# 前置nginx的部署
因为我们的nginx里面有动静分离的所以需要我们自己打包上传到dockerhub
然后使用Kubesphere部署到k8s集群。
![img.png](image/前置nginx的部署.png)
如图所示，我们需要有一个ingress-controller前置网关域名路由。
将我们的请求访问都转发给动静分离的nginx，然后nginx再将请求准发给我们的网关。

在Kubesphere中前置网关路由是非常简单的
![img.png](image/Kubesphere前置网关路由.png)
如图所示，只要我们访问了gulimall.com就转发给gulimall-nginx这个服务。






创建3个pod分别是mysql-master、mysql-slave1、mysql-slave2








# Pod控制器
类型：
    自主式pod：Pod退出了，此类型的Pod不会被创建
    控制器管理的pod：在控制器的生命周期里，始终要维持Pod的副本数
Pod控制器类型
    ReplicationController & ReplicaSet
        1，ReplicationController用来确保容器应用的副本数始终保持在用户定义的副本数，即如果有容器异常退出，会自动创建新的Pod来替代
        2，而如果异常多出来的容器也会自动回收。在新的版本Kubernetes中建议使用ReplicaSet来取代ReplicationController；
        3，ReplicaSet与ReplicationController没有本质的不同，只是名字不一样，并且ReplicaSet支持集合式的selector（通过标签来进行匹配）；
    Deployment
        1，Deployment为Pod和ReplicaSet提供了一个声明式定义（declarative）方法，用来替代以前的ReplicationController来方便管理应用。典型的应用场景：
            - 定义Deployment来创建Pod和ReplicaSet
            - 滚动升级和回滚应用
                虽然ReplicaSet可以独立使用，但一般还是建议使用Deployment管理ReplicaSet，这样就无需担心其他机制的不兼容问题（比如ReplicaSet不支持rolling-update但Deployment支持）；
                ![](./image/Deployment滚动更新原理.png)：会重新启动一个ReplicaSet，然后逐个将pod移到新的RS下面。但是原来的ReplicaSet并不会删除，而是停用，如果要恢复，那么将pod逐个迁移回去，并启动。
            - 扩容和缩容
            - 暂停和继续Deployment
    HPA（Horizontal Pod Autoscaling）
        仅适用于Deployment和ReplicaSet，在V1版本中仅支持根据Pod的CPU利用率扩容，在vlalpha版本中，支持根据内存和用户自定义的metric扩容
        ![](./image/HPA原理.png)：当CPU大于80%的时候会进行pod扩容，最大pod数不超过10个。直到CPU小于80%。
    StatefulSet是为了解决有状态服务的问题（对应Deployments和ReplicaSets是为无状态服务而设计），其应用场景包括：
        * 稳定的持久化存储，即Pod重新调度后还是能访问到相同的持久化数据，基于PVC来实现
        * 稳定的网络标志，即Pod重新调度后其PodName和HostName不变，基于Headless Service（即没有Cluster IP的Service）来实现
        * 有序部署，有序扩展，即Pod是有序的，在部署或者扩展的时候要依据定义的顺序依次进行（即从0到N-1，在下一个Pod运行之前所有的Pod必须是Running和Ready状态），基于init containers
        * 有序收缩，有序删除（即从N-1到0）
    DaemonSet确保全部（或者一些）Node上运行一个Pod副本。当有Node加入集群式，也会为他们新增一个Pod。当有Node从集群移除时，这些Pod也会被回收。删除DaemonSet将会删除它创建的所有Pod
        使用DaemonSet的一些典型用法：
            * 运行集群存储daemon，例如在每个Node上运行glusterd、ceph
            * 在每个Node上运行日志收集daemon，例如fluentd、logstash
            * 在每个Node上运行监控daemon。例如Proetheus Node Exporter
    Job：
        负责批处理任务，即仅执行一次的任务，它保证批处理任务的一个或多个Pod成功结束
    Cron Job
        管理基于时间的Job，即：
            * 在给定时间点只运行一次
            * 周期性的在给定时间点运行
总结：
    一般部署到RS&Deployment
    需要以node为节点的部署到DaemonSet
    有状态的服务部署到StatefulSet
    批处理的部署到Job&Cron Job
    自动扩展部署到HPA
使用：
    Deployment：
        ![](./image/RS、RC和Deployment关联.png)
        ![](./image/Deployment部署nginx应用.png)
        ![](./image/Deployment扩容、自动扩容、更新镜像、回滚.png)
    DaemonSet：
        ![](./image/DaemonSet部署应用.png)：会确保集群节点运行一个pod副本
    Job：    
        ![](./image/Job部署应用.png)
    Cron Job：
        ![](./image/CronJob.png)
        ![](./image/CronJob Spec.png)
        ![](./image/CronJob Spec2.png)
        ![](./image/CronJob部署应用.png)




# Service
1，Kubernetes Service定义了这样一种抽象：一个Pod的逻辑分组，一种可以访问它们的策略 -- 通常为微服务
这一组Pod能够被Service访问到，通常是通过Label Selector；
2，客户端想去访问一组pod。如果这一组pod是不相关的，是不能通过service去代理的。service选择一组pod是通过标签去选择的。
被service代理后，service拥有自己的ip和端口，client就可以通过service间接的访问到具体的pod（通过Round Balance算法轮训访问）；
3，Service能够提供负载均衡的能力，但是在使用上有以下限制：
- 只提供4层负载均衡能力，而没有7层功能，但有时我们需要更多的规则来匹配转发请求，这点上4层负载均衡是不支持的
![](./image/服务发现.png)
![](./image/服务发现1.png)

Service类型：
    ClusterIp：默认类型，自动分配一个仅Cluster内部可以访问的虚拟IP
    NodePort：在ClusterIP基础上为Service在每台机器绑定一个端口，这样可以通过<NodeIP>:NodePort来访问该服务
    LoadBalancer：在NodePort的基础上，借助cloud provider创建一个外部负载均衡器，并请求转发到<NodeIP>:NodePort
    ExternalName：把集群外部的服务引入到集群内部来，在集群内部直接使用。没有任何类型代理被创建，这只有Kubernetes1.7或者更高版本的K8s才支持
    ![](./image/svc技术原理图.png)
代理模式分类：
    VIP和Service代理：
        在Kubernetes集群中，每个Node运行一个kube-proxy进程。kube-proxy负责为service实现了一种VIP（虚拟IP）的形式，而不是ExternalName的形式。
        在Kubernetes v1.0版本，代理完全在userspace。在Kubernetes v1.1版本，新增了iptables代理，但是并不是默认的运行模式
        从Kubernetes v1.2起，默认就是iptables代理。在Kubernetes v1.14版本开始默认使用ipvs代理
        在Kubernetes v1.0版本，Service是4层（TCP/UDP over IP）概念。在Kubernetes v1.1版本新增了Ingress API（beta版），开始表示"7层"（HTTP）服务
    为什么不实用round-robin DNS？进行负载均衡？
        因为DNS有缓存
    1，userspace代理模式
        需要kube-proxy进行一层代理
        ![](./image/userspace代理模式.png)
    2，iptables代理模式
        所有的代理直接由防火墙iptables进行代理，而不需要kube-proxy
        ![](./image/iptables代理模式.png)
    3，ipvs代理模式
        这种模式，kube-proxy会监视Kubernetes service对象和Endpoints，调用netlink接口以相应的创建ipvs规则并定期与kubernetes service对象和endpoints对象同步ipvs规则
        以确保ipvs状态与期望一致。访问服务时，流量将被重定向到其中一个后段pod；
        与iptables类似，ipvs于netfilter的hook功能，但使用哈希表座位底层数据结构并在内核空间中工作。这意味着ipvs可以更快的重定向流量，并在同步代理规则时具有更好的性能。此外，ipvs为负载均衡提供了更多选项，例如：
            rr：轮训调度
            lc：最小连接数
            dh：目标哈希
            sh：源哈希
            sed：最短希望延迟
            nq：不排队调度
        ![](./image/ipvs代理模式.png)
    ClusterIP Service的创建
        clusterIP主要在每个node节点使用iptables，将发向clusterIP对应端口的数据，转发到kube-proxy中。
        然后kube-proxy自己内部实现有负载均衡的方法，并可以查到这个service下对应pod的地址和端口，进而把数据转发给对应的pod地址和端口
        为了实现上述功能，需要几个组件协调工作：
            ![](./image/几个组件协调工作.png)
        ![](./image/创建Cluster类型的Service.png)
    Headless Service的创建
        有时不需要或者不想要负载均衡，以及单独的Service IP。遇到这种情况，可以通过指定CLuster IP（spec.clusterIP）的值为"Node"来创建Headless Service。
        这类的service并不会分配Cluster IP，kube-proxy不会处理它们，而且平台也不会为它们进行负载均衡和路由
        ![](./image/创建Headless类型的Service.png)
    NodePort Service的创建
        nodeport的原理在于在node上开了一个端口，将向该端口的流量导入到kube-proxy，然后由kube-proxy进一步到给对应的pod
        ![](./image/创建NodePort类型的Service.png)
    NodePort Service的创建（付费）
        ![](./image/LoadBalancer类型的Service.png)
    ExternalName Service的创建
        ![](./image/创建ExternalName类型的Service.png)

    

    
        




# 网络通讯方式
Kubernetes的网络模型假定了所有pod都在一个可以直接连通的扁平的网络空间中，这在CGE（Google Compute Engine）里面是现成的网络模型
Kubernetes假定这个网络已经存在。而在私有云里面搭建Kubernetes集群，就不能假定这个网络存在了。我们需要自己实现这个网络假设，将不同节点上的Docker容器之间互相访问先打通
然后运行Kubernetes

通讯模式：
    同一个pod内的多个容器之间：io（共用同一个pause的网络站，走的就是这个网络站的io）
    各pod之间的通讯：Overlay Network
    Pod和service之间的通讯：各节点的Iptables
    ![](./image/不同情况下的网络通信方式.png)
Kubernetes + Flannel
    ![](./image/kubernetes+flannel.png)
    Flannel是CoreOS团队针对Kubernetes设计的一个网络规划服务，简单来说，它的功能是让集群中的不同节点主机创建的Docker容器都具有全集群唯一的虚拟IP地址。
    而且它还能在这些IP地址之间建立一个覆盖网络（Overlay Network），通过这个覆盖网络，将数据包原封不动的传递到目标容器内
    etcd之flannel提供说明：
        存储管理flannel可分配的ip地址段资源
        监控etcd中每个pod的实际地址，并在内存中建立维护pod节点路由表






# 资源清单
什么是资源：
    K8s中所有内容都抽象为资源，资源实例化之后，叫做对象
集群资源分类：
    名称空间级别：比如kubeadm安装k8s集群的时候会把所有的系统组建放在kube-system这个名称空间下面，当我们通过命令kubectl get pod -n default获取的时候是获取不到的
    集群级别：比如role，不管在什么名称空间下面定义，在其他名称空间下面都能拿得到（准确来说在定义的时候就没有指定名称空间）
    元数据级别：比如HPA
名称空间级别的资源类型
    工作负载型资源（workload）：Pod、ReplicaSet、Deployment、StatefulSet、DaemonSet、Job、CronJob（ReplicationController在v1.11版本中被废弃）
    服务发现及负载均衡型资源（ServiceDiscovery LoadBalance）：Service、Ingress...
    配置存储型资源：Volume（存储卷，给Pod提供持久化的能力）、CSI（容器存储接口，可以扩展各种何样的第三方存储券，只要pod符合csi的规范那么k8s集群就可以调用到pod的存储）
    特殊类型的存储卷：COnfigMap（当配置中心来使用的资源类型）、Secret（保存敏感数据）、DownwardAPI（把外部环境中的信息输出给容器）
集群资源：
    Namespace、Node、Role、ClusterRole、RoleBinding、ClusterRoleBinding
元数据型资源：
    HPA、PodTemplate、LimitRange
资源清单含义：
    在k8s中，一般使用yaml格式的文件来创建符合我们预期的pod。这样的yaml文件我们一般称为资源清单
资源清单属性：
    ![](./image/资源清单必要属性.png)
    ![](./image/资源清单主要属性.png)
    ![](./image/资源清单主要属性2.png)
    ![](./image/资源清单额外属性.png)
容器的生命周期：
![](./image/容器生命周期.png)
启动步骤：
    1，首先kubectl想kubeapi接口发送指令
    2，kubeapi会调度到kubelet（etcd完成的存储）
    3，kubelet去操作对应的CRI，CRI完成容器的初始化
    4，CRI会先启动一个pause的基础容器（负责同一个pod中网络以及存储卷的共享）
    5，进行1个或多个init C的初始化（也可以没有init C、init C初始化是逐个线形初始化的）
    6，init C正常初始化后进行main C的初始化
    7，main C初始化前允许执行一条start脚本的执行
    8，main C初始化前允许执行一条stop脚本的执行
    9，在main C初始化的过程中会有readiness和Liveness进行参与（readiness）
init容器：
    pod能够具有多个容器，应用运行在容器里面，但是它也可能有一个或多个先于应用容器启动的init容器
    init容器与普通容器非常像，除了如下两点：
        - init容器总是运行到成功完成为止
        - 每个init容器都必须在下个init容器启动之前完成
        如果pod的init容器失败，Kubernetes会不断的重启pod，直到init容器成功为止。然而，如果pod对应的restartPolocy为never，他不会重新启动
    init容器作用：
        ![](./image/init容器的作用.png)
    init容器使用：
        ![](./image/pod模版.png)
        ![](./image/svc模版.png)
    init特殊说明：
        ![](./image/init特殊说明.png)
        ![](./image/init特殊说明2.png)


容器探针：
    readiness：
        是就绪检测。一般在主容器main C运行后进行。根据命令、tcp连接、htpp协议获取状态判断容器是否可用。如果可用的话将pod容器状态改为running
    Liveness：
        是生存检测。会伴随整个pod的生命周期。当Liveness探测到main C主容器里面的进程出现损坏的时候，那么就会执行对应的重启命令或者删除命令等等
    探针是由kubelet对容器执行的定期诊断。要执行诊断，kubelet调用由容器实现的Handler。有三种类型的处理程序：
        ExecAction：在容器内执行指定命令。如果命令退出时返回码为0则认为诊断成功
        TCPSocketAction：对指定端口上的容器IP地址进行TCP检查。如果端口打开，则诊断被认为成功
        HTTPGetAction：对指定的端口和路径上的容器的IP地址执行HTTP Get请求。如果响应的状态码大于等于200小于400，则被认为诊断成功
    每次探测都将获得一下三种结果之一：
        成功：容器通过了诊断
        失败：容器未通过诊断
        未知：诊断失败，因此不会采用任何行动
    探测方式：
        livenessProbe：
            指示容器是否正在运行。如果探测失败，则kubelet会杀死容器，并且容器将受到其重启策略的影响。如果容器不提供存活探针，则默认状态为success
        readlinessProbe：
            指示容器是否准备好服务请求。如果就绪探测失败，端点控制器将从与Pod匹配的所有Service的端点中删除该Pod的IP地址
            初识延迟之前的就绪状态默认为Faliure。如果容器不提供就绪探针，则默认为success
    探针示例：
        ![](./image/liveness-exec.png)
        ![](./image/liveness-httpget.png)
        ![](./image/liveness-tcp.png)
        就绪检测存活检测一样，只需要将livenessProbe改为readlinessProbe即可

start、stop、相位
    ![](./image/启动、退出动作.png)

Pod phase可能存在的可能性：
    也就是Pod的status
    ![](./image/Pod phase可能存在的值.png)





# ingress
![](./image/Nginx-Ingress.png)
入口网络是充当Kubernetes集群入口点的规则的集合。这允许入站连接，可以将其配置为通过可访问的URL，负载平衡流量或通过提供基于名称的虚拟主机在外部提供服务。
因此，Ingress是一个API对象，通常通过HTTP管理群集中对服务的外部访问，这是公开服务的最强大方法
1，Ingress HTTP代理访问
![](./image/Ingress HTTP代理访问-deployment.yaml.png)
![](./image/Ingress HTTP代理访问-service.yaml.png)
![](./image/Ingress HTTP代理访问-ingress.yaml.png)
2，Ingress HTTPS代理访问
![](./image/Ingress HTTPS代理访问-证书以及cert存储方式.png)
![](./image/Ingress HTTPS代理访问-ingress.yaml.png)






# 存储
## 1，Config Map
ConfigMap功能是在Kubernetes1.2版本中引入，许多应用程序会从配置文件、命令行参数或者环境变量中读取配置信息
ConfigMap API给我们提供了向容器中注入配置信息的机制，ConfigMap可以被用来保存单个属性，也可以用来保存整个配置文件或者JSON二进制大对象
ConfigMap的创建：
    1，使用目录创建：
        ![](./image/使用目录创建ConfigMap.png)
        -from-file指定在目录下的所有文件都会被用在ConfigMap里面创建一个键值对，键的名字就是文件名，值就是文件内容
    2，使用文件创建
        ![](./image/使用文件创建ConfigMap.png)
        -from-file这个参数可以使用多次，你可以使用两次分别指定上个实例中的那两个配置文件，效果就跟指定整个目录是一样的
    3，使用字面值创建
        ![](./image/使用字面值创建ConfigMap.png)
        使用字面值创建，利用-from-literal参数传递配置信息，该参数可以使用多次
在Pod中使用ConfigMap：
    ![img.png](./image/创建special-config以及env-config.png)
    ![img.png](./image/pod中使用ConfigMap.png)
用ConfigMap设置命令行参数
    ![img.png](./image/用ConfigMap设置命令行参数.png)
在数据卷里面使用ConfigMap
    最基本的就是将文件填入数据卷，在这个文件中，键就是文件名，键值就是文件内容
    ![img.png](./image/在数据卷里面使用ConfigMap.png)
ConfigMap的热更新
    ![img.png](./image/log-config.png)
    ![img.png](./image/ConfigMap热更新.png)
    修改ConfigMap以热更新：
        kubectl edit configmap log-config
    修改log_level的值为DEBUG等待大概10秒钟时间，再次查看环境变量的值
        打印查看：kubectl exec my-nginx-7b55868ff4-hzq22 -it --car /etc/log_level
    注意：ConfigMap如果以env的方式挂载到容器，修改ConfigMap并不会实现热更新
## 2，Secret
Secret解决了密码、token、密钥等敏感数据的配置问题，而不需要吧这些敏感数据暴露到镜像或者Pod Spec中。Secret可以以volume或者环境变量的方式使用。
Secret有三种类型：
    Service Account：用来访问Kubernetes API，由Kubernetes自动创建，并且会自动挂载到Pod的/run/secret/kubernetes.io/serviceaccount目录中
    Opaque：base64编码格式的secret，用来存储密码、密钥等
    Kubernetes.io/dockerconfigjson：用来存储私有docker registry的认证信息
## Service Account
Service Account用来访问Kubernetes API，由Kubernetes自动创建，并且会自动挂载到Pod的/run/secret/kubernetes.io/serviceaccount目录中
![img.png](./image/Service Account使用.png)
## Opaque Secret
1，创建说明
Opaque类型的数据是一个map类型，要求value是base64编码格式：
![img.png](./image/base64编码格式.png)
![img.png](./image/secrets.yml.png)
2，使用方式
![img.png](./image/将Secret挂载到Volume.png)
![img.png](./image/将Secret导出到环境变量.png)
3，kubernetes.io/dockerconfigjson
使用Kuberctl创建docker registry认证的secret
![img.png](./image/使用kuberctl创建docker registry认证的secret.png)
![img.png](./image/通过imagePullSecres来引用创建myregistrykey.png)
## 3，Volume
容器磁盘上的文件的生命周期是短暂的，这就使得在容器中运行重要应用时会出现一些问题。首先，当容器崩溃时，kubelet会重启它，但是容器中的文件将丢失---容器以干净的状态（镜像最初的状态）重新启动。
其次，在pod中同时运行多个容器，这些容器之间通常需要共享文件。Kubernetes中的vloume抽象就很好的解决了这些问题
背景：
Kubernetes中的卷有明确的寿命---与封装它的pod相同。所以，卷的生命比Pod中的所有容器都长，当这个容器重启时数据仍然得以保存。
当然，当Pod不再存在时，卷也将不复存在。也许更重要的是，Kubernetes支持多种类型的卷，Pod可以同时使用任意数量的卷
卷的类型：
    ![img.png](./image/Kubernetes支持以下类型的卷.png)
    emptyDir：
        当Pod被分配给节点时，首先创建emptyDir卷，并且只要该Pod在该节点上运行，该卷就会存在。
        正如卷的名字所述，它最初是空的。Pod中的容器可以读取或写入emptyDir卷中的相同文件，尽管该卷可以挂载到每个容器中的相同或不同路径上。
        当出于任何原因从节点中删除Pod时，emptyDir中的数据永久删除
        emptyDir的用法有：
            - 暂存空间，例如用于基于磁盘的合并排序
            - 用作长时间计算崩溃时的检查点
            - Web服务器容器提供数据时，保存内存管理器容器提取的文件








https://mp.weixin.qq.com/s/2HwYG43pDP_5lSGPN825ZA
        



    



    

    

        