const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    entry: './src/js/index.js',
    output: {
        filename: 'js/build.js',
        path: path.resolve(__dirname, 'build')
    },
    plugins: [
        new HtmlWebpackPlugin({
            template: './src/index.html',
        }),
    ],
    mode: 'development',
    enternals: {
        //这里忽略jquery打包，然后通过cnd的方式引入到html
        //忽略 库名：npm包名
        jquery: 'jQuery'
    } 
}