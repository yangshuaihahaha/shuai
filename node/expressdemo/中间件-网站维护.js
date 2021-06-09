const express = require('express');

const app = express();

app.use((req, res, next) => {
    res.send('当前网站正在维护')
})

app.use('/admin', (req, res, next) => {
    let login = true
    if (login) {
        next()
    } else {
        res.send('您还未登录')
    }
})

app.use('/admin', (req, res) => {
    res.send('您已登陆，可以访问主页')
})

app.use((req, res, next) => {
    res.status(404).send('当前页面不存在')
})

app.listen(3000)
console.log('网站服务器启动成功')
