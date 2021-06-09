const add = (x, y) => {
    return x + y;
}
console.log(add(2, 5))

new Promise(reslove => {
    console.log('hahaha');
    reslove();
})
if (module.hot) {
    //一旦module.hot为true，说明开启了hmr功能，让hmr功能代码生效
    module.hot.accept('./print.js', function() {
        //方法会监听 print.js 的变化，一旦发生变化，其他模块不会重新打包构建
        //会执行后面的回掉函数
        print()
    })
}