const express = require('express');
const fs = require('fs')
const promisify = require('util').promisify
const readFile = promisify(fs.readFile)
const app = express();

app.get('/index', (req, res, next) => {
    try {
        readFile('../aaa.js')
    } catch (e) {
        next(e)
    }
})

//错误处理中间件
app.use((err, req, res, next) => {
    res.status(500).send(err.message);
})

app.listen(3000)
console.log('网站服务器启动成功')
