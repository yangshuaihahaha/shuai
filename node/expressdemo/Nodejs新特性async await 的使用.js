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
async function test () {
    return "hello world!"
}
console.log(test())// Promise { 'hello world!' }
async function main () {
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