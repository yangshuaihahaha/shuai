const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
//开发环境配置，能让代码运行起来

module.exports = {
    //入口起点
    entry: './src/js/index.js',
    //输出
    output: {
        filename: 'js/build.js',
        path: path.resolve(__dirname, 'build')
    },
    //pluguns
    plugins: [
        new HtmlWebpackPlugin({
            template: './src/index.html'
        })
    ],
    //只要将环境配置为生产环境就会自动压缩js
    mode: 'production'
}