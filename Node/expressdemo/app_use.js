const express = require('express');

const app = express();

app.use((req, res, next) => {
    console.log('请求走了app.use中间件')
    next()
})

app.use('/request', (req, res, next) => {
    console.log('请求走了app.use/request中间件')
    next()
})

app.use('/list', (req, res, next) => {
    res.send('list列表')
})

app.use('/request', (req, res, next) => {
    req.name = '张三'
    next()
})

app.use('/request', (req, res) => {
    res.send(req.name)
})

app.listen(3000)
console.log('网站服务器启动成功')
