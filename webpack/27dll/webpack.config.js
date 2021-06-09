const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const AddAssetHtmlWebpackPlugin = require('add-asset-html-webpack-plugin');
const { webpack } = require('webpack');
const { resolve } = require('path');

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
        //告诉webpack那些文件不需要打包，同时使用时的名称也得变
        new webpack.DllReferencePlugin({
            manifest: resolve(__dirname, 'dll/manifest.json')
        }),
        //将某个文件打包输出去，并在html中自动引入该资源
        new AddAssetHtmlWebpackPlugin({ 
            filepath: resolve(__dirname, 'dll/jquery.js')
        })
    ],
    mode: 'development',
    enternals: {
        //这里忽略jquery打包，然后通过cnd的方式引入到html
        //忽略 库名：npm包名
        jquery: 'jQuery'
    } 
}