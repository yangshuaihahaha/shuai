//获取模板引擎实例
var template = require('art-template');

//获取文件操作对象实例
var fs = require('fs');

//获取服务器实例
var http = require('http');

//创建服务器，并绑定8888端口
var server = http.createServer().listen(8888, function () {
    console.log('Server star ....');
})

//服务器监听方法
server.on('request', function (req, res) {

    //获取请求地址
    var url = req.url;

    if (url === '/') {

        //读取当前目录下的'tpl.html'文件
        fs.readFile('./tpl.html', function (err, data) {
            if (err) {
                //读取失败
                return console.log('读取文件失败了')
            }
            //读取成功后，使用模板引擎替换字段
            /**
             * 替换规则
             * 替换标记：{{}}
             * 例如：{{ name }}  替换为下面的  Node
             */
            var ret = template.render(data.toString(), {

                //对应{{ name }}
                name: 'Node',

                //对应{{ age }}
                age: 18,

                //对应{{ province }}
                province: '上海市',

                //对应{{each hobbies}} {{ $value }} {{/each}}
                //此为数组 each开始循环  /each结束循环
                hobbies: [
                    '写代码',
                    '打游戏',
                    '听音乐'
                ],

                //对应{{ title }}
                title: '个人信息'
            })
            res.end(ret);
        })
    } else {

        //其他请求地址
        res.end('404');
    }
})