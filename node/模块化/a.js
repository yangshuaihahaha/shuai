// require 方法有两个作用：
//     1,加载模块并执行里面的代码
//     2,拿到被加载文件模块导出的对象
//
//     在每个文件模块中都提供了一个对象：exports
//     exports默认是一个空对象
//     把所有的被外部访问的成员挂载到exports
var ret = require('./b')


console.log(ret)