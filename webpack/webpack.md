什么是css预处理器
    css预处理器是一门专门的编程语言，用来为css增加一些编程特性（css本身不是编程语言）
    不需要考虑浏览器兼容性问题，因为css预处理器最终编译和输出仍然是标准的css样式
    可以在css预处理器中：使用变量、简单逻辑判断、函数式编程技巧
css预处理器主要目的
    css语法不够强大（例如：css选择器无法进行嵌套，导致css中存在较多重复的选择器语句；css无法定义变量以及没有合理的样式复用机制，导致整体css样式难以维护）
    为了减少css代码冗余，为css提供样式复用机制，提高css代码可维护性
css预处理器工作流程
    各种预处理器语法不同，但是最终的工作流程是一样的，以sass为例
    - 以sass提供的语法规则编写样式代码
    - 经过编译器把sass编写的代码转换成标准的css代码
    - 浏览器加载解析转换后的css样式
webpack是什么
    是一种前端静态资源构建工具，是一种静态资源打包器，在webpack看来，前端所有的资源文件（js/json/css/img/less...）都会作为模块处理
    它将根据模块的依赖关系进行静态分析，打包生成对应的静态资源
webpack的五个核心概念
    1，entry：入口指示以那个文件为入口起点开始打包，分析构建内部依赖图
    2，output：输出指示webpack打包后的资源bundles输出到哪里去，以及如何命名
    3，loader：让webpack能够去处理那些非javascript文件（webpack自身只理解javascript）
    4，plugins：插件可以用于执行范围更广的任务，插件范围包括从打包优化和压缩，一直到重新定义环境中的变量等
    5，mode：模式分为两种：development、production。区别就是production模式会压缩代码

webpack能处理js/json资源，不能处理css/img等其他资源

webpack.config.js:
    weipack的配置属性，指示webpack干那些活（当你运行webpack时会加载里面的配置）。所有的构建工具都是基于node.js平台运行的，模块化采用common.js