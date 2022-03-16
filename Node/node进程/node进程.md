# node进程
Node是单线程模式运行的，但是是用的是事件驱动来处理并发的，这样有助于我们在多核cpu上创建子进程从而提高性能

Node提供了child_process模块来创建子进程
- exec - child_process.exec 使用子进程执行命令，缓存子进程的输出，并将子进程的输出以回调函数参数的形式返回。

- spawn - child_process.spawn 使用指定的命令行参数创建新进程。

- fork - child_process.fork 是 spawn()的特殊形式，用于在子进程中运行的模块，如 fork('./son.js') 相当于 spawn('node', ['./son.js']) 。与spawn方法不同的是，fork会在父进程与子进程之间，建立一个通信管道，用于进程之间的通信。
## exec()方法
```
child_process.exec(command[, options], callback)
```
**command**： 字符串， 将要运行的命令，参数使用空格隔开
**options** ：对象，可以是：
- cwd ，字符串，子进程的当前工作目录
- env，对象 环境变量键值对
- encoding ，字符串，字符编码（默认： 'utf8'）
- shell ，字符串，将要执行命令的 Shell（默认: 在 UNIX 中为/bin/sh， 在 Windows 中为cmd.exe， Shell 应当能识别 -c开关在 UNIX 中，或 /s /c 在 Windows 中。 在Windows 中，命令行解析应当能兼容cmd.exe）
- timeout，数字，超时时间（默认： 0）
- maxBuffer，数字， 在 stdout 或 stderr 中允许存在的最大缓冲（二进制），如果超出那么子进程将会被杀死 （默认: 200*1024）
- killSignal ，字符串，结束信号（默认：'SIGTERM'）
- uid，数字，设置用户进程的 ID
- gid，数字，设置进程组的 ID
**callback** ：回调函数，包含三个参数error, stdout 和 stderr。

## spawn() 方法
```
child_process.spawn(command[, args][, options])
```
**command**： 将要运行的命令
**args**： Array 字符串参数数组
**options Object**
- cwd String 子进程的当前工作目录
- env Object 环境变量键值对
- stdio Array|String 子进程的 stdio 配置
- detached Boolean 这个子进程将会变成进程组的领导
- uid Number 设置用户进程的 ID
- gid Number 设置进程组的 ID
spawn() 方法返回流 (stdout & stderr)，在进程返回大量数据时使用。进程一旦开始执行时 spawn() 就开始接收响应。


## fork() 方法
```
child_process.fork(modulePath[, args][, options])
```
**modulePath**： String，将要在子进程中运行的模块
**args**： Array 字符串参数数组
**options**：Object
- cwd String 子进程的当前工作目录
- env Object 环境变量键值对
- execPath String 创建子进程的可执行文件
- execArgv Array 子进程的可执行文件的字符串参数数组（默认： process.execArgv）
- silent Boolean 如果为true，子进程的stdin，stdout和stderr将会被关联至父进程，否则，它们将会从父进程中继承。（默认为：false）
- uid Number 设置用户进程的 ID
- gid Number 设置进程组的 ID
返回的对象除了拥有ChildProcess实例的所有方法，还有一个内建的通信信道。