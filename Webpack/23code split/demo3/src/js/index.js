/**
 * 通过js代码，让某个文件单独打包成一个chunk
 * 这样就可以将print单独打包成一个文件
 */
import('./print').then((result) => {
    console.log(result)
}).catch()