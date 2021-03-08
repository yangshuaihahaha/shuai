# 1、node有哪些特征，与其他服务器端对比
https://www.jianshu.com/p/a841a5ebdd37?utm_campaign= maleskine&utm_content=note&utm_medium=seo_notes&utm_source=recommendation
## 1.1.单线程
在Java、PHP等服务器端语言中，会为每一个客户端连接创建一个新的线程。而每个线程需要耗费大约2MB内存。也就是说，理论上，一个8GB内存的服务器可以同时连接的最大用户数为4000个左右。要让Web应用程序支持更多的用户，就需要增加服务器的数量，而Web应用程序的硬件成本当然就上升了。

Node.js不为每个客户连接创建一个新的线程，而仅仅使用一个线程。当有用户连接了，就触发一个内部事件，通过非阻塞I/O、事件驱动机制，让Node.js程序宏观上也是并行的。使用Node.js，一个8GB内存的服务器，可以同时处理超过4万用户的连接。
另外，单线程带来的好处，操作系统完全不再有线程创建、销毁的时间开销。
坏处，就是一个用户造成了线程的崩溃，整个服务都崩溃了，其他人也崩溃了。
## 1.2.非阻塞I/O
例如，当在访问数据库取得数据的时候，需要一段时间。在传统的单线程处理机制中，在执行了访问数据库代码之后，整个线程都将暂停下来，等待数据库返回结果，才能执行后面的代码。也就是说，I/O阻塞了代码的执行，极大地降低了程序的执行效率。

由于Node.js中采用了非阻塞型I/O机制，因此在执行了访问数据库的代码之后，将立即转而执行其后面的代码，把数据库返回结果的处理代码放在回调函数中，从而提高了程序的执行效率。

当某个I/O执行完毕时，将以事件的形式通知执行I/O操作的线程，线程执行这个事件的回调函数。为了处理异步I/O，线程必须有事件循环，不断的检查有没有未处理的事件，依次予以处理。

阻塞模式下，一个线程只能处理一项任务，要想提高吞吐量必须通过多线程。而非阻塞模式下，一个线程永远在执行计算操作，这个线程的CPU核心利用率永远是100%。

## 1.3.事件驱动 event-driven
在Node中，客户端请求建立连接，提交数据等行为，会触发相应的事件。在Node中，在一个时刻，只能执行一个事件回调函数，但是在执行一个事件回调函数的中途，可以转而处理其他事件（比如，又有新用户连接了），然后返回继续执行原事件的回调函数，这种处理机制，称为“事件环”机制。

Node.js底层是C++（V8也是C++写的）。底层代码中，近半数都用于事件队列、回调函数队列的构建。用事件驱动来完成服务器的任务调度，这是鬼才才能想到的。
单线程，单线程的好处，减少了内存开销，操作系统的内存换页。
如果某一个事情，进入了，但是被I/O阻塞了，所以这个线程就阻塞了。
非阻塞I/O， 不会傻等I/O语句结束，而会执行后面的语句。
非阻塞就能解决问题了么？比如执行着小红的业务，执行过程中，小刚的I/O回调完成了，此时怎么办？？
事件机制，事件环，不管是新用户的请求，还是老用户的I/O完成，都将以事件方式加入事件环，等待调度。
说是三个特点，实际上是一个特点，离开谁都不行，都玩儿不转了。
Node.js中所有的I/O都是异步的，回调函数，套回调函数。


# 2、Node.js适合开发什么
Node.js适合用来开发什么样的应用程序呢？
善于I/O，不善于计算。因为Node.js最擅长的就是任务调度，如果你的业务有很多的CPU计算，实际上也相当于这个计算阻塞了这个单线程，就不适合Node开发。

当应用程序需要处理大量并发的I/O，而在向客户端发出响应之前，应用程序内部并不需要进行非常复杂的处理的时候，Node.js非常适合。Node.js也非常适合与web socket配合，开发长连接的实时交互应用程序。
比如：

用户表单收集
考试系统
聊天室
图文直播
提供JSON的API


# 3、Node.js与PHP、JSP的不同
Node.js不是一种独立的语言，与PHP、JSP、Python、Perl、Ruby的“既是语言，也是平台”不同，Node.js的使用JavaScript进行编程，运行在JavaScript引擎上（V8）。

与PHP、JSP等相比（PHP、JSP、.net都需要运行在服务器程序上，Apache、Naginx、Tomcat、IIS），Node.js跳过了Apache、Naginx、IIS等HTTP服务器，它自己不用建设在任何服务器软件之上。Node.js的许多设计理念与经典架构（LAMP = Linux + Apache + MySQL + PHP）有着很大的不同，可以提供强大的伸缩能力。Node.js没有web容器。

# 4、Node.js没有Web容器
在使用Apache服务器时，我们经常可以看到在htdocs目录中有各种子文件夹，我们要访问指定页面，只需要在浏览器地址栏中输入127.0.0.1:80/app/index.html类似这样的结构
但是，Node.js由于没有Web容器，所以在url 地址后面在输入/xx.xx时并不能正常显示
有这么一个文件目录结构：

fang.html里面是一个红色的、正方形的div，yuan.html里面是一个 绿色的、圆形的div
现在新建一个noWebContainer.js，看能否在url中输入fang.html打开页面
运行127.0.0.1:4000，并在url后面加上/fang.html，发现完全没用。

现在初步对“Node.js没有web容器”这句话有了一点印象了，那想要打开fang.html，怎么办呢？
必须通过res.end(data);来返回页面

# 5, CommonJS中require/exports和ES6中import/export区别
CommonJS模块的重要特性是加载时执行，及脚本代码在require的时候，就会全部执行。一旦出现某个模块被“循环加载”就只输出已经执行的部分，还没有执行的部分是不输出的
ES6模块是动态引用，如果使用import从一个模块加载变量，那些变量不会缓存，而是成为一个指向被加载模块的引用,impor/export最终都是编译为require/exports来执行的

# 6, AMD CMD规范的区别
1, CMD基于require.js, AMD基于sea.js    
2, AMD采用异步加载模块，模块的加载不影响后面语句的执行。所有依赖这个模块的语句，都定义在一个回掉函数中等到加载完这个函数才会执行
AMD也采用require()语句加载模块，但是不同于CommonJS，它要求两个参数：
```
require([module], callback);
```
3, AMD推崇依赖前置，CMD推崇就近依赖
4, CMD是延迟执行的，而AMD是提前执行的
# 7，简述同步和异步的区别，如何避免回调地狱
同步方法调用一旦开始，调用者必须等到方法调用返回后，才能继续后续的行为

异步方法调用一旦开始，方法调用就会立即返回，调用者就可以继续后续的操作。而异步方法通常会在另外一个线程中，整个过程，不会阻碍调用者的工作
避免回调地狱：



## 7.1, Promise
promise模式在任何时刻都处于以下三种状态之一：未完成（unfulfilled）、已完成（resolved）和拒绝（rejected）。
以下为一个有几级嵌套的函数，看起来比较令人恶心。（如果换成缩进四个字符可想而知）
```js
'use strict';
const md = require('markdown-it')();
const fs = require('fs');
fs.watchFile('nodejs.md', (curr, prev) => {
  let mdStr = fs.readFile('./nodejs.md', 'utf-8', (err, data) => {
    let mdData = md.render(data);
    let htmlTemplate = fs.readFile('./index.html', 'utf-8', (err, data) => {
      let html = data.replace('{{content}}', mdData);
      console.log(mdData);
      fs.writeFile('./nodejs.html', html, 'utf-8', (err, data) => {
        if (err) {
          throw err;
        } else {
          console.log('OK');
        }
      });
    });
  });
});

```
以下用promise的方式实现同样的效果，首先把异步函数封装一下，然后下面可以直接调用。可能看起来代码比之前的版本更多，但是封装的异步函数是可以复用的。等任务多了就不显得代码多了。（但看最后调用函数的部分是不是优雅了不少）
```js
'use strict';
const fs = require('fs');
const md = require('markdown-it')();
var Q = require('q');

function fs_readFile(file, encoding) {

  var deferred = Q.defer();
  fs.readFile(file, encoding, function(err, data) {
    if (err) deferred.reject(err); // rejects the promise with `er` as the reason
    else
      deferred.resolve(data) // fulfills the promise with `data` as the value
  });
  return deferred.promise; // the promise is returned
}

function fs_writeFile(file, data, encoding) {
  var deferred = Q.defer();
  fs.writeFile(file, data, encoding, function(err, data) {
    if (err) deferred.reject(err); // rejects the promise with `er` as the reason
    else deferred.resolve(data); // fulfills the promise with `data` as the value
  });
  return deferred.promise ;// the promise is returned
    //return 1; // the promise is returned
}

function fs_watchFile(file, curr, prev) {
  var deferred = Q.defer();
  fs.watchFile(file, function(curr, prev) {
    if (!prev) deferred.reject(err); // rejects the promise with `er` as the reason
    else deferred.resolve(curr); // fulfills the promise with `data` as the value
  });
  return deferred.promise // the promise is returned
}

function markdowm_convert(file, encoding, mdData) {

  var convertData = md.render(mdData);
  console.log(convertData);
  var deferred = Q.defer();
  fs.readFile(file, encoding, function(err, data) {
    if (err) deferred.reject(err); // rejects the promise with `er` as the reason
    else {
      data = data.replace('{{content}}', convertData);
      deferred.resolve(data); // fulfills the promise with `data` as the value
    }
  })
  return deferred.promise; // the promise is returned
}




// ===============promise实现  =====================
fs_watchFile('nodejs.md')
  .then(function() {
    return fs_readFile('./nodejs.md', 'utf-8');
  })
  .then(function(mdData) {
    return markdowm_convert('./index.html', 'utf-8', mdData);
  })
  .then(function(data) {
    fs_writeFile('./nodejs.html', data, 'utf-8');
  });

```



## 7.2, async/await
node的async包有多的数不清的方法我暂时只实验了一个waterfall

waterfall瀑布流的意思和async中另一个函数series差不多都是按照顺序执行，不同之处是waterfall每执行完一个函数都会产生一个值，然后把这个值给下一个函数用。

以下是嵌套了两级的读写文件程序
```js
fs.readFile('01.txt','utf-8',function(err,date){
  fs.writeFile('02.txt',date,'utf-8',function(err,date){
    console.log('复制完了');
  });
})
```
用async.waterfall 后代码如下
```js
async.waterfall([
  function(cb){
    fs.readFile('01.txt','utf-8',function(err,result){
      cb(err,result);
    });

  },function(result,cb){
    fs.writeFile('02.txt',result,'utf-8',function(err,result){
      cb(err,result);
    });
  }
  ],function(err,result){
 console.log('复制完了');
})
```

# 8，几种常见模块化规范的简介
- CommonJS规范主要用于服务端编程，加载模块是同步的，这并不适合在浏览器环境，因为同步意味着阻塞加载，浏览器资源是异步加载的

- AMD规范在浏览器环境中异步加载模块，而且可以并行加载多个模块。不过，AMD规范开发成本高，代码的阅读和书写比较困难

- ES6 在语言标准的层面上，实现了模块功能，而且实现得相当简单，完全可以取代 CommonJS 和 AMD 规范，成为浏览器和服务器通用的模块解决方案

# 9，使用NPM有哪些好处？
- 通过NPM，你可以安装和管理项目的依赖，并且能够指明依赖项的具体版本号
- 对于Node应用开发而言，你可以通过package.json文件来管理项目信息，配置脚本， 以及指明项目依赖的具体版本。

# 10，Node中each和foreach的区别
1，如果forEach里面执行的方法是有异步的，不会等他执行完。有可能出现整个函数都执行完了，foreach里面的函数还没有执行完
而普通的循环不回出现这种问题
2，for循环是可以使用break和continue去终止循环的，但是forEach不行
3，for多数时候都可以使用，当然一般需要知道循环次数，而forEach更适合集合对象的遍历和操作
注意：
forEach(function),这个function里面抛出的error或者reject的promise是不会被外层的try catch捕获的.
```js
async function test(){
    const arr = [1,2,3]
    try{
        arr.forEach((t)=>{
            return Promise.reject(`error${t}`);
        })
    } catch(err){
        console.log(err);
    }
}
test();
```
forEach本身是同步的，假如里面的函数式同步的，抛出的error是会被外面的try捕捉到的，假如是异步的，抛出的error就捕捉不到
运行之后会出现未捕获的reject promise
如果把return Promise.reject（error${t}）换成抛出error的话就会直接崩掉
因为这个function已经算是另一个上下文了

# 11，require的模块加载机制
1、先计算模块路径
2、如果模块在缓存里面，取出缓存
3、生成模块实例，存入缓存
4、加载模块
5、输出模块的exports属性

# 12，我们知道node导出模块有两种方式，一种是exports.xxx=xxx和Module.exports={}有什么区别吗
如果要输出一个键值对象{}，可以利用exports这个已存在的空对象{}，并继续在上面添加新的键值；

如果要输出一个函数或数组，必须直接对module.exports对象赋值。
 
#13，请介绍一下Node事件循环的流程
Node.js 是单进程单线程应用程序，但是因为 V8 引擎提供的异步执行回调接口，通过这些接口可以处理大量的并发，所以性能非常高。

Node.js 几乎每一个 API 都是支持回调函数的。

Node.js 基本上所有的事件机制都是用设计模式中观察者模式实现。

Node.js 单线程类似进入一个while(true)的事件循环，直到没有事件观察者退出，每个异步事件都生成一个事件观察者，如果有事件发生就调用该回调函数.
## 13.1, 事件驱动程序
Node.js 使用事件驱动模型，当web server接收到请求，就把它关闭然后进行处理，然后去服务下一个web请求。
当这个请求完成，它被放回处理队列，当到达队列开头，这个结果被返回给用户。
这个模型非常高效可扩展性非常强，因为 webserver 一直接受请求而不等待任何读写操作。（这也称之为非阻塞式IO或者事件驱动IO）
在事件驱动模型中，会生成一个主循环来监听事件，当检测到事件时触发回调函数。

# 14，V8的垃圾回收机制

## 14.1, 查看V8的内存使用情况
使用process.memoryUsage(),返回如下
```
{
 rss: 4935680, //驻留集大小, 是给这个进程分配了多少物理内存(占总分配内存的一部分) 这些物理内存中包含堆，栈，和代码段。
 heapTotal: 1826816, //内存大小
 heapUsed: 650472, //已使用的内存大小
 external: 49879 //代表V8管理的，绑定到Javascript的C++对象的内存使用情况
}
```
## 14.2, V8的内存限制是多少，为什么V8这样设计
64位系统下是1.4GB， 32位系统下是0.7GB。
因为1.5GB的垃圾回收堆内存，V8需要花费50毫秒以上，做一次非增量式的垃圾回收甚至要1秒以上。这是垃圾回收中引起Javascript线程暂停执行的事件，在这样的花销下，应用的性能和影响力都会直线下降。

## 14.3, V8的内存分代和回收算法请简单讲一讲
在V8中，主要将内存分为新生代和老生代两代。新生代中的对象存活时间较短的对象，老生代中的对象存活时间较长，或常驻内存的对象。
### 14.3.1, 新生代
新生代中的对象主要通过Scavenge算法进行垃圾回收。这是一种采用复制的方式实现的垃圾回收算法。它将堆内存一份为二，每一部分空间成为semispace。在这两个semispace空间中，只有一个处于使用中，另一个处于闲置状态。处于使用状态的semispace空间称为From空间，处于闲置状态的空间称为To空间。
- 当开始垃圾回收的时候，会检查From空间中的存活对象，这些存活对象将被复制到To空间中，而非存活对象占用的空间将会被释放。完成复制后，From空间和To空间发生角色对换。
- 应为新生代中对象的生命周期比较短，就比较适合这个算法。
- 当一个对象经过多次复制依然存活，它将会被认为是生命周期较长的对象。这种新生代中生命周期较长的对象随后会被移到老生代中。
### 14.3.2, 老生代
老生代主要采取的是标记清除的垃圾回收算法。与Scavenge复制活着的对象不同，标记清除算法在标记阶段遍历堆中的所有对象，并标记活着的对象，只清理死亡对象。活对象在新生代中只占叫小部分，死对象在老生代中只占较小部分，这是为什么采用标记清除算法的原因。

## 14.4, 什么情况会造成内存无法回收或内存泄漏
一、全局变量
```
a = 10; 
//未声明对象。 
global.b = 11; 
//全局变量引用 
这种比较简单的原因，全局变量直接挂在 root 对象上，不会被清除掉。
```
二、闭包
```
function out() { 
 const bigData = new Buffer(100); 
 inner = function () { 
 } 
}
```
闭包会引用到父级函数中的变量，如果闭包未释放，就会导致内存泄漏。上面例子是 inner 直接挂在了 root 上，那么每次执行 out 函数所产生的 bigData 都不会释放，从而导致内存泄漏。

需要注意的是，这里举得例子只是简单的将引用挂在全局对象上，实际的业务情况可能是挂在某个可以从 root 追溯到的对象上导致的。

三、事件监听
Node.js 的事件监听也可能出现的内存泄漏。例如对同一个事件重复监听，忘记移除(removeListener)，将造成内存泄漏。这种情况很容易在复用对象上添加事件时出现，所以事件重复监听可能收到如下警告：
```
emitter.setMaxListeners() to increase limit
```
例如，Node.js 中 Agent 的 keepAlive 为 true 时，可能造成的内存泄漏。当 Agent keepAlive 为 true 的时候，将会复用之前使用过的 socket，如果在 socket 上添加事件监听，忘记清除的话，因为 socket 的复用，将导致事件重复监听从而产生内存泄漏。

原理上与前一个添加事件监听的时候忘了清除是一样的。在使用 Node.js 的 http 模块时，不通过 keepAlive 复用是没有问题的，复用了以后就会可能产生内存泄漏。所以，你需要了解添加事件监听的对象的生命周期，并注意自行移除。

  


