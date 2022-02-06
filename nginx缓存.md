# 分发层+应用层，双层nginx架构提升缓存命中率方案
问题
    现要获取一个商品id=1的商品详情页数据，由于nginx负载均衡，请求会均匀的到各个nginx服务器上去，
    这时每个nginx都会发送一次请求到redis上去获取缓存数据，放到本地，缓存命中率很低，导致redis压力暴增
解决
    分发层+应用层，双层nginx
    分发层nginx，负责流量分发的逻辑和策略，这个里面可以根据你自定义的一些规则，比如根据productId去进行hash
    然后对后端nginx数量取模，将某一个商品的访问请求，固定路由到一个nginx后端服务器取，保证nginx缓存命中率
# nginx 中写 lua 脚本把缓存放到 nginx 本地缓存中
脚本思路
1，应用nginx的lua脚本接受请求
2，获取请求参数中的商品id，以及商品店铺id
3，根据商品id和商品店铺id，在nginx本地缓存中尝试获取数据
4，如果nginx在本地缓存中没有获取到数据，那么就到redis分布式缓存中获取数据，如果获取到了数据，还要设置到nginx本地缓存中
    但是这里有个问题，不要直接使用nginx+lua直接获取redia数据
    因为openresty没有太好的redis cluster的支持包，所以建议是发送http请求到缓存数据生产服务，由该服务提供一个http接口
    缓存数据生产服务可以基于redis cluster api从redis中获取数据，并返回给nginx
5，如果缓存数据生产服务没有在redis分布式缓存中获取到数据，那么就在自己本地ehcache中获取数据，返回给nginx，也要设置到nginx本地缓存中
6，如果ehcache本地缓存中都没有数据，那么就需要去原始的服务中拉取数据，该服务会从mysql中查询，拉取到数据之后返回给nginx，并重新设置到ehcache和redis
7，nginx最终获取到数据

具体实现
    分发层
        /usr/hello/hello.conf
        ```
        server {
            listen       80;
            server_name  _;
            location /lua {
              default_type 'text/html';
              # 防止响应中文乱码
              charset utf-8;
              content_by_lua_file /usr/hello/lua/hello.lua;
            }
            # 分发逻辑脚本映射
            location /product {
              default_type 'text/html';
              # 防止响应中文乱码
              charset utf-8;
              content_by_lua_file /usr/hello/lua/distribute.lua;
            }
        }
        ```
        /usr/hello/lua/distribute.lua
        ```
        local uri_args = ngx.req.get_uri_args()
        -- 获取参数
        local productId = uri_args["productId"]
        local shopId = uri_args["shopId"]
        -- 定义后端应用 ip
        local host = {"192.168.99.170", "192.168.99.171"}
        -- 对商品 id 取模并计算 hash 值
        local hash = ngx.crc32_long(productId)
        hash = (hash % 2) + 1
        -- 拼接 http 前缀
        backend = "http://"..host[hash]
        -- 获取到参数中的路径，比如你要访问 /hello，这个例子中是需要传递访问路径的
        local method = uri_args["method"]
        -- 拼接具体的访问地址不带 host，如：/hello?productId=1
        local requestBody = "/"..method.."?productId="..productId.."&shopId="..shopId
        -- 获取 http 包
        local http = require("resty.http")
        local httpc = http.new()
        -- 访问，这里有疑问：万一有 cooke 这些脚本支持吗？会很麻烦吗？
        local resp, err = httpc:request_uri(backend, {
            method = "GET",
            path = requestBody,
            keepalive=false
        })
        -- 如果没有响应则输出一个 err 信息
        if not resp then
            ngx.say("request error :", err)
            return
        end
        -- 有响应测输出响应信息
        ngx.say(resp.body)  
        -- 关闭 http 客户端实例
        httpc:close()
        ```
    应用层
        安装依赖
            # 需要再后端服务获取信息，安装 http 依赖
            cd /usr/hello/lualib/resty/
            wget https://raw.githubusercontent.com/pintsized/lua-resty-http/master/lib/resty/http_headers.lua  
            wget https://raw.githubusercontent.com/pintsized/lua-resty-http/master/lib/resty/http.lua
            # 拿到数据之后需要进行模板渲染，添加 template 依赖
            # 这里渲染也是使用 lua 来完成
            cd /usr/hello/lualib/resty/
            wget https://raw.githubusercontent.com/bungle/lua-resty-template/master/lib/resty/template.lua
            mkdir /usr/hello/lualib/resty/html
            cd /usr/hello/lualib/resty/html
            wget https://raw.githubusercontent.com/bungle/lua-resty-template/master/lib/resty/template/html.lua
        /usr/hello/hello.conf
            # 配置 lua 的一个缓存实例，my_cache 是我们自定义的一块缓存名称
            # 要配置在 http 中，server 外，否则会报错
            # nginx: [emerg] "lua_shared_dict" directive is not allowed here in /usr/hello/hello.conf:11
            lua_shared_dict my_cache 128m;
            server {  
                listen       80;  
                server_name  _;
                # 配置模板路径
                set $template_location "/templates";  
                # 当然这个路径需要存在，因为后续需要用来存放 html
                set $template_root "/usr/hello/templates";
                location /lua {  
                  default_type 'text/html';  
                  # 防止响应中文乱码
                  charset utf-8;
                  content_by_lua_file /usr/hello/lua/hello.lua;
                }
                # 配置一个脚本映射，访问 product 的时候
                # 就执行 product.lua 脚本来完成 获取缓存渲染 html 并返回 html 的功能
                location /product {
                  default_type 'text/html';
                  # 防止响应中文乱码
                  charset utf-8;
                  content_by_lua_file /usr/hello/lua/product.lua;
                }    
            }
        /usr/hello/lua/product.lua
            local uri_args = ngx.req.get_uri_args()
            local productId = uri_args["productId"]
            local shopId = uri_args["shopId"]
            -- 获取到之前配置中分配的缓存对象
            local cache_ngx = ngx.shared.my_cache
            -- 拼接两个缓存 key
            local productCacheKey = "product_info_"..productId
            local shopCacheKey = "shop_info_"..shopId
            -- 通过缓存对象获取缓存中的 value
            local productCache = cache_ngx:get(productCacheKey)
            local shopCache = cache_ngx:get(shopCacheKey)
            -- 如果缓存中不存在对于的 value
            -- 就走后端缓存服务获取数据（缓存服务先走 redis ，不存在再走 ehcache，再走数据库）
            if productCache == "" or productCache == nil then
            	local http = require("resty.http")
            	local httpc = http.new()
              -- 这里地址是开发机器 ip，因为我们在 windows 上开发的，
              -- 这里直接访问开发环境比较方便
            	local resp, err = httpc:request_uri("http://192.168.99.111:6002",{
              		method = "GET",
              		path = "/getProductInfo?productId="..productId,
                  keepalive=false
            	})
            	productCache = resp.body
              -- 获取到之后，再设置到缓存中
            	cache_ngx:set(productCacheKey, productCache, 10 * 60)
            end
            if shopCache == "" or shopCache == nil then
            	local http = require("resty.http")
            	local httpc = http.new()
            	local resp, err = httpc:request_uri("http://192.168.99.111:6002",{
              		method = "GET",
              		path = "/getShopInfo?shopId="..shopId,
                  keepalive=false
            	})
            	shopCache = resp.body
            	cache_ngx:set(shopCacheKey, shopCache, 10 * 60)
            end
            -- 因为存到缓存中是一个字符串
            -- 所以使用 cjson 库把字符串转成 json 对象
            local cjson = require("cjson")
            local productCacheJSON = cjson.decode(productCache)
            local shopCacheJSON = cjson.decode(shopCache)
            -- 把商品信息和店铺信息拼接到一个大 json 对象中
            -- 这样做的原因是：template 渲染需要这样做
            local context = {
            	productId = productCacheJSON.id,
            	productName = productCacheJSON.name,
            	productPrice = productCacheJSON.price,
            	productPictureList = productCacheJSON.pictureList,
            	productSpecification = productCacheJSON.specification,
            	productService = productCacheJSON.service,
            	productColor = productCacheJSON.color,
            	productSize = productCacheJSON.size,
            	shopId = shopCacheJSON.id,
            	shopName = shopCacheJSON.name,
            	shopLevel = shopCacheJSON.level,
            	shopGoodCommentRate = shopCacheJSON.goodCommentRate
            }
            -- 使用 template 渲染 product.html 模板
            local template = require("resty.template")
            template.render("product.html", context)
        product.html 内容，就是很简单的插值占位
            product id: {* productId *}<br/>
            product name: {* productName *}<br/>
            product picture list: {* productPictureList *}<br/>
            product specification: {* productSpecification *}<br/>
            product service: {* productService *}<br/>
            product color: {* productColor *}<br/>
            product size: {* productSize *}<br/>
            shop id: {* shopId *}<br/>
            shop name: {* shopName *}<br/>
            shop level: {* shopLevel *}<br/>
            shop good cooment rate: {* shopGoodCommentRate *}<br/>




        

    