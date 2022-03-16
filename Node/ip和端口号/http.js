var http = require('ip和端口号/http')

var server = http.createServer()

server.on('request', function (request, response) {
    // response对象有一个方法：write可以用来给客户端发送响应数据
    // write可以使用多次，但是最后一定要用end来结束响应，否则客户端会一直等待
    console.log('收到客户端的请求')
    response.write('hello')
    response.write('nodejs')

    // 告诉客户端，我说完了，你可以呈递给客户了
    response.end()

    // 希望不同的路径响应不同的结果，可以通过request的url来判断

    // 在服务端默认发送的数据，其实是utf8编码的内容
    // 但是浏览器不知道你是utf8编码内容
    // 浏览器在不知道服务器响应内容的编码的情况下会按照当前系统默认的编码去解析
    // 解决方法就是正确的告诉浏览器我给你发送的内容是什么编码
    // 在http协议中，contact-type就是用来告知对方发送的数据内容是什么类型
    // res.setHeader('Content-Type', 'text/plain', 'charset=utf-8')
    // res.end('hello世界')
    if (url == '/plain') {
        // text/plain 就是普通的文本
        res.setHeader('Content-Type', 'text/plain', 'charset=utf-8')
        res.end('hello世界')
    } else if (url == '/html') {
        // 如果你发送的是html格式的字符串，则要告诉浏览器发送的是text/html
        res.setHeader('Content-Type', 'text/html', 'charset=utf-8')
        res.end('<p>hello世界</p>')
    } else if (url == '/xiaoming') {
        // data默认是二进制数据，可以通过转为能识别的字符串
        // res.end()支持两种数据类型，一种是二进制，一种是字符串
        fs.readFile('./resource/a.jpg', function (err, data) {
            res.setHeader('Content-Type', 'image/jpeg')
            res.end(data)
        })
        res.end('<p>hello世界</p>>')
    }
})

server.listen(3000, function () {
    console.log('服务器启动成功了，可以通过http://127.0.0.1:3000/来进行访问')
})