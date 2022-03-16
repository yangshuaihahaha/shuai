/**
 * 懒加载
 * webpackChunkName是重新命名
 * webpackPrefetch预加载
 * 等待其他资源加载完毕，浏览器空闲了，再偷偷加载资源
 */
 import(/*webpackChunkName: 'test', webpackPrefetch: true */'./print').then((result) => {
    console.log(result)
}).catch()