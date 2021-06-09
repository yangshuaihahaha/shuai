const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const OptimizeCssAssetsWebpackPlugin = require('optimize-css-assets-webpack-plugin');
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
    entry: './src/js/index.js',
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
    mode: 'production'
}