
# King

设计该项目的源头，来源于之前开发的那个分布式配置中心框架（[Tina](https://www.yuque.com/simonalong/xiangmu/pxg5a8)），里面有一个场景是有一个配置是定期失效，但是对业务方而言，每次调用都能保证最新，因此引入groovy和Quartz的任务调度模块。其中对于分布式调度框架的任务划分问题，自己引入了一个非常好的自研解决方案，`zk的通知和一致性哈希方式`进行机器的自动化扩容。后来Tina重写，其中任务调度部分跟配置中心已经不属于同一个维度，然后将该部分独立出去，并重新开发作为分布式的任务调度框架。<br />在原项目基础上又增加了根据名称作为调度的纯restful方式的调度策略。最后实现如下，其中（*）部分是该项目区别于其他项目的主要特点之一。
<a name="QAfei"></a>

### 特性：
1.`简单`：界面简单，提供任务的CURD<br />2.`动态化`：界面提供了对任务的运行和终止状态，也提供了用于手动触发的功能<br />3.`cron表达式`：调度采用quartz的cron表达式作为触发规则<br />4.`注册中心`：采用zk作为注册中心，利用zk的数据保存和节点监控回调保证服务端的一些特殊功能<br />5.`调度中心高可用`：通过集群化部署调度中心服务，可以保证任务的高可用<br />6.`任务的拆分`（*）：通过引入一致性哈希算法，保证不同机器管理不同的任务区域，保证一个任务只能归属于一台机器<br />7.`弹性扩容缩容`（*）：通过zk回调和引入一致性哈希算法，可以保证在新增和减少机器的时候，自动拆分某台机器任务和自动接手挂掉机器的任务<br />8.`脚本化调度`：所有的任务执行部分全部采用groovy的脚本化执行，想怎么做全部直接在界面上编写即可<br />9.`纯restful调度`：对于触发其他进程调度，这里采用命名空间方式实现纯restful的url方式，可以进行集群化的调度<br />10.`任务监控预警`：对任务的监控预警，在任务运行如果超过某个时间还没有运行完毕，则调用外部接口进行告警
<a name="ULKyV"></a>
# 目录：

* 一、[原理](#原理)
    * [技术栈](#技术栈)
    * 1.[整体流程](#整体流程)
        * 1.[站在业务方来看](#站在业务方来看)
        * 2.[站在任务调度方来看](#站在任务调度方来看)
    * 2.[一致性哈希算法](#一致性哈希算法)
    * 3.[zk节点](#zk节点)
        * 1.[服务端的对一致性哈希的划分记录](#服务端的对一致性哈希的划分记录)
        * 2.[客户端的对微服务的命名空间记录](#客户端的对微服务的命名空间记录)
        * 3.[服务端的进程启动回调通知](#服务端的进程启动回调通知)
        * 4.[客户端的进程启动回调通知](#客户端的进程启动回调通知)
     * 4.[命名空间](#命名空间)
* ​二、[界面化](#界面化)
    * 1.[界面化配置](#界面化配置)
    * 2.[脚本化介绍](#脚本化介绍)
    * 3.[脚本测试](#脚本测试)
* 三、[用法和功能](#用法和功能)
    * 1.[服务方配置](#服务方配置)
    * 2.[业务方使用](#业务方使用)
    * 3.[脚本化编写](#脚本化编写)
        * 1.[DB对应的db](#DB对应的db)
        * 3.[http对应的属性http](#http对应的属性http)
        * 3.[日志框架的log](#日志框架的log)
   
<h1 id="获取不同的列值类型">一、原理</h1>
项目的粗略图如下<br />![image.png](https://cdn.nlark.com/yuque/0/2019/png/126182/1558419453204-e765d3b1-4afb-4d0b-94c0-6d3ec40b696a.png#align=left&display=inline&height=952&name=image.png&originHeight=952&originWidth=1026&size=153436&status=done&width=1026)
<a name="eSnJm"></a>
## 技术栈：

1. Orm框架：是自己开发的一个Orm框架，详情见[这里](https://simonalong.github.io/Neo/)
1. 服务发现框架：这里选择的是zk，后面也会接入consul，etcd，eureka
1. 消息队列：采用的是apache管理的[rocketmq](https://rocketmq.apache.org/docs/simple-example/)
1. 调度引擎：quartz，quartz是所有调度框架的基础，包括spring的调度器底层也是由quartz实现
1. 脚本引擎：groovy
1. 网络框架：okhttp
1. 分布式缓存：redis
1. 单机缓存：guava
1. 项目框架：springBoot版本：v2.0.4
1. 前端：[ant-design-pro](https://pro.ant.design/index-cn)
1. 代码编辑器：采用基于[codeMirror](https://codemirror.net/)的[react-codemirror2](https://github.com/scniro/react-codemirror2)
<a name="70yXX"></a>

<h2 id="整体流程">1.整体流程</h2>
<a name="ed7I9"></a>

<h3 id="站在业务方来看">1.站在业务方来看</h3>
1.业务方通过引入king-spring-boot-starter模块，并配置一些相关配置，启动时候会将ip和端口号上报到zk中对应的命名空间<br />2.任务调度群的所有业务就会收到最新的名称和Ip端口号的映射<br />3.控制台管理任务的暂停和启动，对应的消息会发送到任务调度群，任务调度群，启动和移除对应的任务执行器<br />4.任务调度群执行响应的任务脚本，脚本中包含对业务方的调度，则会向业务方发起调度
<a name="TqatJ"></a>
<h3 id="站在任务调度方来看">2.站在任务调度方来看</h3>
1.启动新的任务调度进程加入集群，会触发所有的进程进行一致性哈希计算，算出哪个进程对应的任务被拆分给这新的进程，被拆分的进程会禁用掉那些被执行的任务，新的进程会启用对拆分下来的区域对应的任务。实现动态化的扩容，无需所有的机器重新哈希。<br />2.有服务启动消息发送，所有进程接收判断是否是自己管理，是自己管理，则会启动任务调度<br />3.任务调度，会解析groovy脚本并执行，脚本中我们内置了一些对象，其中包括http模块，可以用于纯restful化的对外部服务的调度，其中restful化是通过其中的namespace查找对应的ip和端口，通过负载均衡找到一个可用的ip，然后向这台ip的这个端口发送对应的请求
<a name="ngn4l"></a>
<h2 id="一致性哈希算法">2.一致性哈希算法（*）</h2>
一致性哈希算法是开发该项目的关键，一致性哈希算法的官方解释可以见[这里](https://zh.wikipedia.org/wiki/%E4%B8%80%E8%87%B4%E5%93%88%E5%B8%8C)，我们这里是利用的它的其中一个特性，就是利用通过对一个2^n个区域，进行少量的m个划分，可以变成m个区间。只要我们可以保证m<2^n，那么我们就可以这样说，相当于我们实现了m个数据对2^n的数据进行了管理。如果2^n对应的上千万上亿或者更多的数据，我们都不怕，即使这个数据量比2^n更大，也没问题，只要与上2^n肯定还是位于2^n之内。而m这个值如果对应一些服务，那么最后是不是就可以转换为，少量的机器，通过一致性哈希算法，进行管理无限量的数据了。<br />![image.png](https://cdn.nlark.com/yuque/0/2019/png/126182/1558421530071-e3790c42-ea8a-497b-80d3-d74ef4830855.png#align=left&display=inline&height=308&name=image.png&originHeight=308&originWidth=743&size=25698&status=done&width=743)<br />该项目就是利用上述的原理是想少量机器管理海量的任务，其中在该项目中，我这里设置了n为10，也就是说我们这里任务调度群服务的上限也就是m的最大值只能是1024，这个值，如果不满足实现项目中的，要求，可以去代码中进行修改。<br />对于进程的新增和进程挂掉，在利用一致性哈希解决方面可以说是非常的方便，在任务调度服务什么都没有的时候，第一个新增进程接管如上图中的整个圆环，如果后面又来一个，我们这里采用这样的划分算法：`区域最大和若都一样情况下按照起点最小确定区域，确定后对该区域进行对半划分`，则第一个接手的进程进行保留前一部分，后面一部分进行移除掉，而新增的这个接手后面的一部分，后来再来一个进程，则根据划分算法确定还是第一个区域。若后面再来一个，则这个时候确定区域划分则就是第二个进来的了。我们做一个接管图如下<br />![image.png](https://cdn.nlark.com/yuque/0/2019/png/126182/1558423919579-fc6a8911-1dd6-4c60-9b74-286cabdb2682.png#align=left&display=inline&height=228&name=image.png&originHeight=876&originWidth=927&size=63410&status=done&width=241) ![image.png](https://cdn.nlark.com/yuque/0/2019/png/126182/1558423866514-cae7d84d-7ebe-4a9c-9b6a-b4e9202a2c1e.png#align=left&display=inline&height=235&name=image.png&originHeight=919&originWidth=871&size=64878&status=done&width=223) ![image.png](https://cdn.nlark.com/yuque/0/2019/png/126182/1558424022803-00340920-eb4c-4723-9978-bdc46bed3037.png#align=left&display=inline&height=226&name=image.png&originHeight=895&originWidth=935&size=68043&status=done&width=236)<br />![image.png](https://cdn.nlark.com/yuque/0/2019/png/126182/1558424125073-35378024-9e12-4b2c-91fa-ad615bb93047.png#align=left&display=inline&height=229&name=image.png&originHeight=889&originWidth=946&size=70303&status=done&width=244)  ![image.png](https://cdn.nlark.com/yuque/0/2019/png/126182/1558424170129-80b63f51-707d-4a84-ad33-3c005ee0abd5.png#align=left&display=inline&height=212&name=image.png&originHeight=870&originWidth=945&size=74722&status=done&width=230)

上面是进程：1，2，3，4，5，依次启动并接管不同的范围。而对于进程的崩溃，则这里是通过合并算法：`确定自己的左侧的节点进行接手自己，如果自己是位于起始点即圆环的起始点0，则向右融合（我们这里为了防止循环融合这里通过起止点进行划分）` ，根据合并算法，我们假设上面的进程3挂掉，这个时候是进程5接手进程3的任务，最后划分图会变成这种<br />![image.png](https://cdn.nlark.com/yuque/0/2019/png/126182/1558424505688-3fc73e1f-1244-44fe-85a1-2d9c15ec2555.png#align=left&display=inline&height=227&name=image.png&originHeight=864&originWidth=945&size=72085&status=done&width=248) ![image.png](https://cdn.nlark.com/yuque/0/2019/png/126182/1558424830447-f12ecf49-aa97-4dd7-8670-673d281db652.png#align=left&display=inline&height=229&name=image.png&originHeight=1021&originWidth=1103&size=93172&status=done&width=247)

而如果这个时候进程3在进程启动的话，会怎么样呢，肯定是按照拆分算法进行拆分啦，这个时候就属5最大，对5进行而分拆分了，如上右图所述。
<a name="OQMBK"></a>
<h2 id="zk节点">3.zk节点</h2>
其中在zk中记录的节点是这样的：

```
king
|
|_server
|    |
|    |_s_00001,s_00002,s_00003...
|
|_client
|    |
|    |_namespace1
|    |    |
|    |    |_c_00001,c_00002...
|    |		
|    |_namespace2
|    |    |
|    |    |_c_00001,c_00002...
...
```

zk节点在整个项目里面提供了这样的两个功能数据记录和数据变更的通知：
<a name="5imCF"></a>
<h4 id="服务端的对一致性哈希的划分记录">1.服务端的对一致性哈希的划分记录</h4>
也就是上面的`/king/server` 其中king和server都是永久节点，内部的都是每一个服务端启动后生成的临时有序节点，内部放置的是每个节点划分和相邻节点的数据
<a name="CSasQ"></a>
<h4 id="客户端的对微服务的命名空间记录">2.客户端的对微服务的命名空间记录</h4>
也就是`/king/client`，其中client也是永久节点，内部是先有一层`namespace`，这个算是微服务名，这个是永久节点，在微服务里面都没有节点时候会被服务删除（暂时没有删除逻辑后面添加）。里面是各个微服务中对应的进程，都是临时有序节点，里面放置的都是进程对应ip和端口号
<a name="LAhOO"></a>
<h4 id="服务端的进程启动回调通知">3.服务端的进程启动回调通知</h4>
用于在有新的调度服务进程启动时候回调到所有已有的服务中，用于区域的划分用，以及有对应的服务进程崩掉后用于通知已有的服务，用于区域融合
<a name="BNGiS"></a>
<h4 id="客户端的进程启动回调通知">4.客户端的进程启动回调通知</h4>
用于在业务方使用分布式调度中心的`king-spring-boot-starter`包的时候，启动时候会向zk中注册，这个时候服务端会收到回调通知，用于更新本地存储的这个命名空间中的ip和端口

<a name="V7FcG"></a>

<h2 id="命名空间">4.命名空间</h2>

这个命名空间跟微服务的应用名一样的，就是用于表示同样的应用名中有多个进程，整个命名空间的启动方面上面已经有说明，我们这里主要介绍下，使用方面。我们知道启动后在server这里已经保存好最新的ip和端口了，在任务触发的脚本中，如果使用http方面的restful调用，则内部会将对应的名字进行转换，比如

```groovy
def http = dataMap.http
print http.get("namespace3/test/get/haode").send();
```

其中 get中的url，中我们可以使用http://xxxx/xxx 这种，也可以使用上面这种，上面这种就是，默认第一个字符为命名空间，后台会存储这个跟诸多ip和port的转换，通过负载均衡策略选择一个ip，拼接成一个url

<a name="ULH94"></a>
<h1 id="界面化">二、界面化</h1>

界面这里采用的是蚂蚁金服的ant-design-pro进行开发
<a name="ETfXC"></a>

<h1 id="界面化配置">1.界面化配置</h1>

在这里，对任务的界面化，分了两个维度，一个是任务的组，一个是任务详情，比较简单就上一张图，不在介绍了<br />![image.png](https://cdn.nlark.com/yuque/0/2019/png/126182/1558428055108-2390f4b9-e574-4bc6-9eff-092d144d83a6.png#align=left&display=inline&height=1157&name=image.png&originHeight=1157&originWidth=2558&size=151018&status=done&width=2558)
<a name="Z1M3b"></a>

<h2 id="脚本化介绍">2.脚本化介绍</h2>

脚本化编写，这里采用的是groovy的方式，groovy是基于jvm开发的另外一类语言，跟java很相似，能跟java进行无缝结合，具备java的常见的所有功能，写法上面完全可以根据java的方式来书写，不过作为脚本，我们这里内嵌了几个常见的变量，用于书写，所有的内嵌变量都是位于`dataMap`这个变量下面的，内嵌的变量有这么几个：<br />1.DB框架的db<br />2.http框架的http<br />3.日志框架的log

比如：

```groovy
def http = dataMap.http
def log = dataMap.log
log.info("dddaaa")
print http.get("namespace3/test/get/haode").send();
return "3122"
```

其中对应框架的一些api介绍，在后面的脚本化编写那一节进行介绍
<a name="FFQBO"></a>

<h2 id="脚本测试">3.脚本测试</h2>

这里还提供了脚本的测试模块，用于在不确定脚本编写的是否正确的情况下，可以在这里进行测试<br />![image2.png](https://cdn.nlark.com/yuque/0/2019/png/126182/1558430482819-82c1843b-ab8c-4912-b863-6c99a059dbfb.png#align=left&display=inline&height=1209&name=image2.png&originHeight=1209&originWidth=1845&size=105056&status=done&width=1845)

<a name="vcQHx"></a>

<h1 id="用法和功能">三、用法和功能</h1>

<a name="YJhtB"></a>

<h2 id="服务方配置">1.服务方配置</h2>

<a name="7ovtG"></a>

<h4 id="启动配置">1.启动配置</h4>

服务方这里启动有两个模块，一个是控制台，一个是server，服务方这里只需要配置一个zk地址和消息队列地址即可
<a name="Ntzsa"></a>

<h4 id="任务监控告警配置">2.任务监控告警配置</h4>

目前对于任务的监听告警配置这里需要单独接入对应的预警系统，目前预警是对于一个任务从执行到结束，如果中间超过默认时间（5s），则会向告警模块发送，而告警模块的具体展示源需要单独设置，这里不知道怎么设计好，所以暂时先这样，需要配置的需要单独进行在类 `AlertService` 里面添加对应的代码
<a name="4zYau"></a>

<h2 id="业务方使用">2.业务方使用</h2>

业务方使用时候，很简单，引入maven 和添加zk配置和命名空间，maven这里引入，其中该maven，可以在maven中心仓库是没有的，自己代码拉下来将其中一个模块发布到自己的私库即可使用。

```xml
<dependency>
  <groupId>com.simon.cloud</groupId>
  <artifactId>king-spring-boot-starter</artifactId>
</dependency>
```

配置添加如下

```yaml
king:
  zk:
    address: zookeeper://127.0.0.1:2181
  namespace: namespace3
```

其中zk配置读取支持多种方式，若有dubbo，则也会默认读取dubbo中的zk地址，也可以自己进行配置，两者均可。

```yaml
spring.dubbo.registry.address=127.0.0.1:2181
```

```yaml
dubbo.registry.address=127.0.0.1:2181
```

<a name="VIVdh"></a>

<h2 id="脚本化编写">3.脚本化编写</h2>

上面说了脚本化编写这里支持这么三种变量<br />1.DB框架的db<br />2.http框架的http<br />3.日志框架的log<br />下面依次介绍这三种变量能够做的东西
<a name="TIwkL"></a>

<h3 id="脚本化编写">1.DB对应的db</h3>

db这个变量对应的类其实就是[Neo](https://github.com/SimonAlong/Neo)框架中的Neo对象，也就是具备Neo对象中的所有api，对于这个使用，功能太多了，详情可去[Neo框架介绍](https://github.com/SimonAlong/Neo)中查看，我这里列举下常见的获取，首先一个Neo对应的是一个DB对象（后续Neo框架继续开发会支持多DB）。这个需要在代码内部进行配置。
<a name="aASl3"></a>

<h3 id="http对应的属性http">2.http对应的属性http</h3>

该http对应是内部的一个对象，拥有的api有如下这么些

```java
// http的一些方法
public HttpService get(String url){}
public HttpService post(String url){}
public HttpService head(String url){}
public HttpService put(String url){}
public HttpService patch(String url){}
public HttpService delete(String url){}

// 填充的数据头和数据体
public HttpService headers(NeoMap headMap) {}
public HttpService body(NeoMap bodyMap) {}

// 同步发送
public String send() {}
```

其中类NeoMap是框架Neo中的一个类，是Map<String, Object>的继承类，但是比它拥有更多的使用方式，详细介绍可以见[这里](https://github.com/SimonAlong/Neo#NeoMap%E7%B1%BB)。<br />我们举个例子，如下
```groovy
def http = dataMap.http
def log = dataMap.log
log.info("dddaaa")
print http.get("namespace3/test/get/haode").send();
print http.post("namespace3/test/post").body(NeoMap.of("a", 1, "c", 2)).send();
return "3122"
```
<a name="oFCED"></a>

<h3 id="日志框架的log">3.日志框架的log</h3>

这个应该就不用过多介绍了，使用的是门面模式的原生api的log

