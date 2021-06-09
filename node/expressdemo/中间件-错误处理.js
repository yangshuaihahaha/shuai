const express = require('express');
const fs = require('fs')
const app = express();

app.get('/index',(req,res)=>{
    throw new Error('程序发生了未知错误')
})

//错误处理中间件
app.use((err, req, res, next) => {
    res.status(500).send(err.message);
})

app.listen(3000)
console.log('网站服务器启动成功')
