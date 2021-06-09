// function yaoshaizi() {
//     // let sino = parseInt(Math.random() * 6 + 1)
//     // setTimeout(() => {
//     //     return sino;
//     // }, 3000)
//     return new Promise((resolve, reject) => {
//         let sino = parseInt(Math.random() * 6 + 1)
//         setTimeout(() => {
//             resolve(sino)
//             reject(sino)
//         }, 3000)
//     })
// }
//
// async function test() {
//     // console.log(yaoshaizi())
//     yaoshaizi().catch(reason => console.error("reason" + reason))
//     yaoshaizi().then(value => console.log("value" + value))
//     // let n = await yaoshaizi()
//     console.log('123456')
// }
// test();


// async function test() {
//     return "hello world!"
// }
// console.log(test())// Promise { 'hello world!' }
// async function main() {
//     test().then(value => console.log(value))
//
//     // var data = await test();
//     // console.log(data);// hello world!
// }
// main()

var promise = new Promise(function (resolve, reject) {
    resolve('ok');
    throw new Error('test');
});
promise
    .then(function (value) {
        console.log(value)
    })
    .catch(function (error) {
        console.log(error)
    });

