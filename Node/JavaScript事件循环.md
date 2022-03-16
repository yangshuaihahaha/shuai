# 宏任务
浏览器为了能够使js的内部task与DOM任务有序的执行，会在前一个task执行完毕结束后，在下一个task执行开始前，对页面进行重新渲染（render），这里说的task就是指宏任务。
```
task -> rander -> task
```
浏览器中宏任务一般包括:
## setTimeout, setInterval
定时器大家都知道他的作用和用法
## MessageChannel
消息通道, 兼容性不太好，实例如下。
```javascript
const channel = new MessageChannel();
// 在端口号上添加消息, 发送消息
channel.port1.postMessage('我爱你');
// 注册接收事件, 后绑定的接收函数，还是可以接收的到，所以可以看出是异步执行
channel.post2.onmessage = function(e) {
    console.log(e.data);
};
console.log('hello'); // 先走的hello，后出现我爱你.
```
## postMessage
消息通信机制
## setImmediate
立即执行定时器，不可以设置时间, 只在IE浏览器中实现了。
```javascript
setImmediate(function() {
    console.log('立即执行定时器，不可以设置时间')
})
```


# 微任务
微任务通常来说就是在当前task执行结束后立即执行的任务，比如对一系列动作做出反馈，或者是需要异步的执行任务但是又不需要分配一个新的task,这样便可以减小一点性能的开销。

只要执行栈中没有其他JS代码正在执行或者每个宏任务执行完，微任务队列会立即执行。

如果在微任务执行期间微任务队列中加入了新的微任务，就会把这个新的微任务加入到队列的尾部，之后也会被执行。

微任务包括:
## promise.then  
Promise的then方法就是一个微任务。
## async await
async函数的await之后的内容也是以微任务的形式来执行。
## MutationObserver
MutationObserver的作用是监控dom变化，dom变化了就会执行, 时间节点是等待所有代码都执行完，才执行该监控
```javascript
const observer = new MutationObserver(() => {
    console.log('节点已经更新');
    console.log(document.getElementById('app').children.length);
});
observer.observe(document.getElementById('app'), {
    'childList': true,
});
for (let i = 0; i < 20; i++) {
    document.getElementById('app').appendChild(document.createElement('p'));
}
for (let i = 0; i < 20; i++) {
    document.getElementById('app').appendChild(document.createElement('span'));
}
```
# 事件循环
```javascript
setTimeout(() => {
    console.log('timeout');
}, 0);

Promise.resolve().then(data => {
    console.log('then');
});

console.log('start');
```
首先我们知道js代码是自上而下开始执行，首先遇到setTimeout，setTimeout会立即被执行，但他的执行结果会产生一个异步宏任务，放入到宏任务队列中，等待一定的时间后执行，这里设置的0秒，但是0秒也不会立即执行，因为任务队列是一定要等到当前执行栈执行完毕才会考虑执行的。

接着代码执行到Promise.resolve().then这里，这句代码并不是任务代码所以会立即被执行，不过Promise.then会产生一个微任务放入到微任务队列当中等待主执行栈执行完毕执行。

代码继续向下执行console.log('start')，打印出start，执行栈执行完毕。

这时我们知道宏任务队列中存在console.log('timeout');因为定时器时间为0所以已经到了执行的时机，微任务队列中console.log('then');也到了执行时机，那他们谁先被执行呢?

JavaScript执行机制很简单，主栈执行完成之后，会执行微任务队列，先进入的微任务先执行，所有微任务执行完毕后，也就是微任务队列被清空之后再开始检查宏任务队列。将需要执行的宏任务执行掉。

所以这里会先打印出then，再打印出timeout。

1）所有同步任务都在主线程上执行，形成一个执行栈

2）当主线程中的执行栈为空时，检查事件队列是否为空，如果为空，则继续检查；如不为空，则执行3

3）取出任务队列的首部，加入执行栈

4）执行任务

5）检查执行栈，如果执行栈为空，则跳回第 2 步；如不为空，则继续检查



总结一句话就是: 先执行同步代码，再执行微任务，再检查宏任务是否到达时间，到达时间再执行。

