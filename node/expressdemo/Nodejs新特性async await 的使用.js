// 以前异步处理方式，通过回调函数来获取异步方法数据。
function getData(callback) {
    setTimeout(function () {
        var name = "小红"
        callback(name)
    }, 1000)
}

getData(function (name) {
    console.log(name);//小红
})

// ES6出来可以通过Promise来进行异步处理
function getData(resolve, reject) {
    setTimeout(function () {
        var name = "Tom"
        resolve(name);
    }, 1000)
}

var p = new Promise(getData)

p.then((data) => {
    console.log(data); //Tom
})

// async 是异步简写 ， 而await可以认为是async wait的简写，所以应该很好理解async用于申明一个异步的function，而await用于等待一个异步方法执行完成。
// 简单理解：async 是让方法变成异步，await是等待异步方法执行完毕。
async function test() {
    return "hello world!"
}

console.log(test())// Promise { 'hello world!' }
async function main() {
    var data = await test();
    console.log(data);// hello world!
}

main()

// 示例2
async function test() {
    return new Promise((resolve, reject) => {
        setTimeout(function () {
            var name = "Lucy";
            resolve(name);
        }, 1000)
    })
}

console.log(test())

async function main() {
    var data = await test();
    console.log(data);//Lucy
}

main()


// 语法很简单，就是在函数前面加上async 关键字，来表示它是异步的，那怎么调用呢？async 函数也是函数，平时我们怎么使用函数就怎么使用它，直接加括号调用就可以了，为了表示它没有阻塞它后面代码的执行，我们在async 函数调用之后加一句console.log;
async function timeout() {
    return 'hello world'
}

timeout();
console.log('虽然在后面，但是我先执行');
// async 函数 timeout  调用了，但是没有任何输出，它不是应该返回 'hello world',  先不要着急， 看一看timeout()执行返回了什么？ 把上面的 timeout() 语句改为console.log(timeout())
// 原来async 函数返回的是一个promise 对象，如果要获取到promise 返回值，我们应该用then 方法， 继续修改代码
async function timeout() {
    return 'hello world'
}

timeout().then(result => {
    console.log(result);
})
console.log('虽然在后面，但是我先执行');
// 我们获取到了"hello world',  同时timeout 的执行也没有阻塞后面代码的执行，和 我们刚才说的一致。
// 这时，你可能注意到控制台中的Promise 有一个resolved，这是async 函数内部的实现原理。如果async 函数中有返回一个值 ,当调用该函数时，内部会调用Promise.solve() 方法把它转化成一个promise 对象作为返回，但如果timeout 函数内部抛出错误呢？ 那么就会调用Promise.reject() 返回一个promise 对象， 这时修改一下timeout 函数
async function timeout(flag) {
    if (flag) {
        return 'hello world'
    } else {
        throw 'my god, failure'
    }
}

console.log(timeout(true))  // 调用Promise.resolve() 返回promise 对象。
console.log(timeout(false)); // 调用Promise.reject() 返回promise 对象。
// 如果函数内部抛出错误， promise 对象有一个catch 方法进行捕获。
timeout(false).catch(err => {
    console.log(err)
})
// async 关键字差不多了，我们再来考虑await 关键字，await是等待的意思，那么它等待什么呢，它后面跟着什么呢？其实它后面可以放任何表达式，
// 不过我们更多的是放一个返回promise 对象的表达式！！！。注意await 关键字只能放到async 函数里面
function doubleAfter2seconds(num) {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            resolve(2 * num)
        }, 2000);
    })
}

async function testResult() {
    let result = await doubleAfter2seconds(30);
    console.log(result);
}

//Promise 是什么？
// 抽象表达：
//        Promise 是JS中进行异步编程的新的解决方案（旧的是谁？=> 纯回调的形式）
// 具体表达：
//      从语法上来说：Promise 是一个构造函数
//      从功能上来说：Promise 对象用来封装一个异步操作并可以获取其结果
//
//
// Promise 的状态改变
//
// pending 变为 resolved
// pending 变为rejected
// 说明：只有这2种，且一个 Promise 对象只能改变一次，无论变成成功还是失败，都会有一个结果数据，成功的结果数据一般称为 value，失败的结果数据一般称为 reason。
// 基本使用
const p = new Promise((resolve, reject) => { // 执行器函数，同步执行
    // 2. 执行异步操作任务
    setTimeout(() => {
        const time = Date.now() // 如果当前时间是偶数就代表成功，否则代表失败
        // 3.1 如果成功了，调用resolve(value)
        if (time % 2 === 0) {
            resolve('成功的数据，value = ' + time)
        } else {
            // 3.2 如果失败了，调用reject(reason)
            reject('失败的数据，reason = ' + time)
        }

    }, 1000);
})
p.then(value => {
    // 接受得到成功的value数据，专业术语：onResolved
    console.log('成功的回调', value)
}, reason => {
    // 接受得到失败的reason数据，专业术语：onRejected
    console.log('失败的回调', reason)
})

// 什么是嵌套回掉函数？
// 例如我们需要返回一个data数据，而这个data数据由一个回调函数提供
function receive() {
    let myData;
    setTimeout(function (data) {
        myData = data;
    }, 0, "data");
    return myData;
}

console.log(receive());  //undefined
// 由于异步机制, 我们始终无法得到返回的myData。因为return是本轮事件中执行，而setTimeout则会等到下一轮
// 但是如果改用嵌套回调函数
function receive(callback) {
    let myData;
    setTimeout(function (data) {
        callback(data);
        console.log(data);
        myData = data;
    }, 0, "data");
    return myData;
}

receive(function (data) {
    //对返回的data进行相关操作
    console.log(data);  //data
})
// 此时我们便能正确的的到data并对data进行相应的操作!
// 为什么有了嵌套回调函数还需要Promise?
// e.g
function getId(url, callback) {
    //doSomething
    let id = url + "-->id";
    callback(id);
}

function getNameById(id, callback) {
    //doSomething
    let name = id + "-->name";
    callback(name);
}

function getCourseByName(name, callback) {
    //doSomething
    let course = name + "-->course";
    callback(name);
}

getId("url", function (id) {
    //getId在调用回调函数前已经得到了id
    //现在是回调函数对id进行相关操作
    console.log(id);
    getNameById(id, function (name) {
        //回调函数对name进行相关操作
        console.log(name);
        getCourseByName(name, function (course) {
            //回调函数对course进行相关操作
            console.log(course);
        })
    })
})
// 可以看出来, 嵌套回调函数十分繁琐 !
// 但promise能让我们写出干净的代码且避免 call-back hell 回调地狱
// 用promise改写后的代码
function getId(url) {
    return new Promise(function (resolve) {
        let id = url + "-->id";
        console.log(id)
        resolve(id);
    })
}

function getNameById(id) {
    //doSomething
    let name = id + "-->name";
    console.log(name);
    return name;

}

function getCourseByName(name) {
    //doSomething
    let course = name + "-->course";
    console.log(course);
    return course;
}

//then返回的promise会把返回的name作为resolve参数传递下去
getId("url").then(function (id) {
    return getNameById(id);
}).then(function (name) {
    let course = getCourseByName(name);
    //对course进行相关操作
})


// 为什么要用 Promise？
// 1，指定回调函数的方式更加灵活
// 旧的
function successCallback(result) {
    console.log('处理成功:' + result)
}

function failureCallback(error) {
    console.log('处理失败:' + error)
}

createAudioFileSync(audioSettings, successCallback, failureCallback)
// 使用 Promise
const promise = createAudioFileSync(audioSettings)
setTimeout(() => {
    promise.then(successCallback, failureCallback)
}, 3000);
//2，支持链式调用，可以解决回调地狱问题


// Promise 的API说明
// 1）Promise 构造函数 Promise (excutor) {}，excutor 会在 Promise 内部立即同步回调,异步操作在执行器中执行
//
// excutor 函数：执行器 (resolve, reject) => {}
// resolve 函数：内部定义成功时我们调用的函数 value => {}
// reject 函数：内部定义失败时我们调用的函数 reason => {}
// 2）Promise.prototype.then 方法：(onResolved, onRejected) => {}，指定用于得到成功 value 的成功回调和用于得到失败 reason 的失败回调返回一个新的 promise 对象
//
// onResolved 函数：成功的回调函数 (value) => {}
// onRejected 函数：失败的回调函数 (reason) => {}
// 3）Promise.prototype.catch 方法：(onRejected) => {}，onRejected 函数：失败的回调函数 (reason) => {}，then() 的语法糖, 相当于： then(undefined, onRejected)。
//
// 4）Promise.resolve 方法：(value) => {}，value：成功的数据或 promise 对象，返回一个成功/失败的 promise 对象。
//
// 5）Promise.reject 方法：(reason) => {}，reason：失败的原因，返回一个失败的 promise 对象
//
// 6）Promise.all 方法： (promises) => {}，promises：包含 n 个 promise 的数组，返回一个新的 promise, 只有所有的 promise 都成功才成功, 只要有一个失败了就直接失败。
//
// 7）Promise.race 方法：(promises) => {}，promises: 包含 n 个 promise 的数组，返回一个新的 promise, 第一个完成的 promise 的结果状态就是最终的结果状态。
// 产生一个成功值为 1 的 Promise 对象
const p1 = new Promise((resolve, reject) => {
    resolve(1)
})
// 产生一个成功值为 2 的 Promise 对象
const p2 = Promise.resolve(2)
// 产生一个失败值为 3 的 Promise 对象
const p3 = Promise.reject(3)

p1.then(value => console.log(value))
p2.then(value => console.log(value))
p3.catch(reason => console.error(reason))

// const pAll = Promise.all([p1, p2])
const pAll = Promise.all([p1, p2, p3])
pAll.then(values => {
    console.log('all onResolved()', values) // all onResolved() [ 1, 2 ]
}, reason => {
    console.log('all onRejected()', reason) // all onRejected() 3
})

const race = Promise.race([p1, p2, p3])
race.then(value => {
    console.log('all onResolved()', value)
}, reason => {
    console.log('all onRejected()', reason)
})

// Promise resolve()的用法
// 一直无法理解什么为什么会在异步之后使用 resolve() 这个resolve是什么意思，直到看到了这个回答
// Promise 对象代表一个异步操作，有三种状态：Pending（进行中）、Resolved（已完成，又称 Fulfilled）和 Rejected（已失败）。
// 通过回调里的resolve(data)将这个promise标记为resolverd，然后进行
// 下一步then((data)=>{//do something})，resolve里的参数就是你要传入then的数据
return new Promise((omg, reject) => {
    console.log('this is esri.js');
    axios.get(configFilePath).then(function (configResponse) {
        console.log(configResponse);
        GLOABLE = configResponse.data;
        const options = {
            url: GLOABLE.esriApiUrl + "init.js"
        };
        // _this.attributeSubmitModel.uploadUrl = GLOABLE.fileServerUrl + "error_report/medias";
        loadCss(GLOABLE.esriApiUrl + "esri/css/main.css")
        loadModules([
            "esri/Map",
            "esri/WebMap",
            "esri/Basemap",
            'esri/views/MapView',
            "esri/layers/TileLayer",
            "esri/layers/MapImageLayer",
        ], options)
            .then(([
                       Map,
                       WebMap,
                       Basemap,
                       MapView,
                       TileLayer,
                       MapImageLayer
                   ]) => {
                _this.Map = Map;
                _this.WebMap = WebMap;
                _this.Basemap = Basemap;
                _this.MapView = MapView;
                _this.TileLayer = TileLayer;
                _this.MapImageLayer = MapImageLayer;
                omg()
            })
    })
// 可以这样理解，在新建 promise 的时候就传入了两个参数
// 这两个参数用来标记 promise的状态的，这两个参数是两个方法，并且这两个参数可以随意命名，我这里的使用的是omg  也不影响使用
// 用于表示 promise 的状态
// 到执行到 resolve()这个方法的时候，就改变promise的状态为
// fullfiled ，当状态为 fuulfiled的时候就可以执行.then()
// 当执行到 reject()  这个方法的时候，就改变 promise 的状态为
// reject，当 promise 为reject 就可以.catch() 这个promise了
// 然后这两个方法可以带上参数，用于.then() 或者 .catch() 中使用。
// 所以这两个方法不是替代，或者是执行什么，他们的作用就是 用于改变
// promise 的状态。
// 然后，因为状态改变了，所以才可以执行相应的 .then() 和 .catch()操作。
