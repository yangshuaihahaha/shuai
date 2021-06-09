var express = require('express')
// 创建网站服务器
const app = express()
app.get('/', (req, res) => {
    res.send('Hello Express')
})
app.get('/list', (req, res) => {
    res.send({
        name: '张三',
        age: 10
    })
})
app.listen(3000)
console.log('网站服务器启动成功')