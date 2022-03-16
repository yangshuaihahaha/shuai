/*
source-map: 一种提高源代码到构建代码映射技术（如果构建代码出错了，通过映射可以追踪源代码错误）
[inline-|hidden-|eval-][nosources-][cheap-[module-]]source-map
source-map:外部
    错误代码准确信息 和 源代码的错误位置
inline-source-map:内联
    只生成一个内联的source-map
    错误代码准确信息 和 源代码错误位置
hidden-source-map：外部
    错误代码错误原因，但没有错误位置
eval-source-map:内联
    每一个文件都生成对应的source-mao,都在eval错误代码准确信息 和 源代码错误位置
nosources-source-map:外部
    错误代码准确信息，但是没有任何源代码信息
cheap-source-map:外部
    错误代码准确信息 和 源代码错误位置
    只能精确到行
cheap-module-source-map:外部  
    错误代码准确信息 和 源代码错误位置

开发环境：速度快，调试友好
    速度快（eval>inline>cheap>...）
        eveal-cheap-source-map
        eveal-source-map
    调试更友好
        source-map
        cheap-module-source-map
        cheap-source-map
    一般选择 eval-source-map（调试更友好）
            eval-cheap-moudle-source-map(性能更好)

生产环境：源代码要不要隐藏？调试要不要友好
    内联会让代码体积变大，所以生产环境不用内联
    nosources-source-map
    hidden-source-map
    source-map()调试友好
*/

const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const OptimizeCssAssetsWebpackPlugin = require('optimize-css-assets-webpack-plugin');
const { resolve } = require('path');
//开发环境配置，能让代码运行起来

//定义nodejs环境变量，决定使用browserlist的那个环境
process.env.NODE_ENV = 'production';

const commonCssLoader = [
    //打包成单独css文件
    MiniCssExtractPlugin.loader,
    'css-loader',
    {
        //css兼容性处理，还需要在package.json中定义browserlist
        loader: 'postcss-loader',
        options: {
            ident: 'postcss',
            plugins: () => [
                require('postcss-preset-env')()
            ]
        }
    }
]

module.exports = {
    //入口起点
    //这里加上入口html，开启热更新
    entry: ['./src/js/index.js', './src/js/index.html'],
    //输出
    output: {
        filename: 'js/build.js',
        path: path.resolve(__dirname, 'build')
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: [...commonCssLoader]
            },
            {
                test: /\.less$/,
                use: [...commonCssLoader,'less-loader']
            },
            /*
            正常来讲一个文件只能被一个loader处理，当一个文件被多个loader处理，那么一定要指定loader执行的先后顺序：
            先执行eslint后执行babel
            */
            //js语法检查
            {
                //在package.json中配置eslintConfig --> aorbnb
                test: /\.js$/,
                exclude: '/node_modules/',
                //优先执行
                enforce: 'pre',
                loader: 'eslint-loader',
                options: {
                    fix: true
                }
            },
            //js兼容性处理
            {
                test: /\.js$/,
                exclude: '/node_modules/',
                loader: 'babel-loader',
                options: {
                    presets: [
                        [
                            '@babel/preset-env',
                            {
                                useBuiltIns: 'usage',
                                corejs: {version: 3},
                                targets: {
                                    chrome: '60',
                                    firefox: '50'
                                }
                            }
                        ]
                    ]
                }
            },
            {
                //匹配那些文件
                test: '/\.(jpg|png|gif)$/',
                //处理图片资源
                //使用一个loader
                //下载url-loader,file-loader
                loader: 'url-loader',
                options: {
                    //图片大小小于8kb，就会被base64处理
                    //优点：减少请求数量（减少服务器压力）
                    //缺点：图片体积会更大（文件请求速度更慢）
                    limit: 8*1024,
                    name: '[hash:10].[ext]',
                    outputPath: 'imgs',
                    esModule: false
                }
            },
            //html处理
            {
                test: '/\.html$/',
                loader: 'html-loader'
            }
            ,
            //其他文件
            {
                exclude: '/\.(js|css|less|html|jpg|png|gif)$/',
                loader: 'file-loader',
                options: {
                    outputPath: 'media'
                }
            }
        ]
    },
    //pluguns
    plugins: [
        new MiniCssExtractPlugin({
            filename: 'css/built.css'
        }),
        new HtmlWebpackPlugin({
            template: './src/index.html',
            //压缩html代码
            minify: {
                //移除空格
                collapseWhitespace: true,
                //移除注释
                removeComments: true
            }
        }),
        //压缩css
        new OptimizeCssAssetsWebpackPlugin()
    ],
    //只要将环境配置为生产环境就会自动压缩js
    mode: 'development',
    devServer: {
        contentBase: resolve(__dirname, 'build'),
        compress: true,
        port:3000,
        open: true,
        hot:true
    },
    devtool: 'source-map'
}