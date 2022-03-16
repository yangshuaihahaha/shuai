/**
 * 使用dll技术，对某些库（第三方库：jquery，react，vue）进行单独打包
 * 当你运行webpack的时候，默认查找的是webpack.config.js这个文件
 * 指定运行的配置文件：webpack --config webpack.dll.js
 */
const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    entry: {
        //最终打包生成的[name] --> jquery
        //['jquery'] --> 要打包的库是jquery
        jquery: ['jQuery']
    },
    output: {
        filename: '[name].js',
        path: path.resolve(__dirname, 'dll'),
        library: '[name]_[hash]',//打包的库里面向外暴露出的内容叫什么名字
    },
    plugins: [
        //作用是打包生成一个mainfest.json，提供jquery的映射
        new webpack.DllPlugin({
            name: '[name]_[hash]',//映射库的暴露的内容名称
            path: path.resolve(__dirname, 'dll/mainfest.json'),//输出文件路径
        }),
    ],
    mode: 'production',
    enternals: {
        //这里忽略jquery打包，然后通过cnd的方式引入到html
        //忽略 库名：npm包名
        jquery: 'jQuery'
    } 
}