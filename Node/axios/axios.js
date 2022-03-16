const axios = require('axios');

// // 发送一个请求 for a user with a given ID
// axios.get('http://localhost:3000/posts/1')
//     .then(function (response) {
//         // handle success
//         console.log(response.data);
//     })
//     .catch(function (error) {
//         // handle error
//         console.log(error);
//     })
//     .then(function () {
//         // always executed
//     });
// 上述请求也可以像下面这样写
axios.get('http://localhost:3000/posts', {
    params: {
        ID: 1
    }
})
    .then(function (response) {
        console.log(response.data);
    })
    .catch(function (error) {
        console.log(error);
    })
    .then(function () {
        // always executed
    });

// 想要使用async/await? 添加 `async` 关键字 to your outer function/method.
async function getUser() {
    try {
        const response = await axios.get('/user?ID=12345');
        console.log(response);
    } catch (error) {
        console.error(error);
    }
}

// 发送POST 请求
axios.post('/user', {
    firstName: 'Fred',
    lastName: 'Flintstone'
})
    .then(function (response) {
        console.log(response);
    })
    .catch(function (error) {
        console.log(error);
    });

// 发送多个并发请求
function getUserAccount() {
    return axios.get('/user/12345');
}

function getUserPermissions() {
    return axios.get('/user/12345/permissions');
}

axios.all([getUserAccount(), getUserPermissions()])
    .then(axios.spread(function (acct, perms) {
        // Both requests are now complete
    }));
// axios API
// 可以通过给axios传递相关config发送一个请求.
// 发送 POST request
axios({
    method: 'post',
    url: '/user/12345',
    data: {
        firstName: 'Fred',
        lastName: 'Flintstone'
    }
});
// GET request 远程图片
axios({
    method: 'get',
    url: 'http://bit.ly/2mTM3nY',
    responseType: 'stream'
}).then(function (response) {
    response.data.pipe(fs.createWriteStream('ada_lovelace.jpg'))
});
// axios(url[, config])
// 发送 GET 请求 (默认的 method)
axios('/user/12345');

// Request method 别名
// 为方便起见，已为所有支持的请求方法提供了别名。
axios.request(config)
axios.get(url[, config])
axios.delete(url[, config])
axios.head(url[, config])
axios.options(url[, config])
axios.post(url[, data[, config]])
axios.put(url[, data[, config]])
axios.patch(url[, data[, config]])

// 并发
// Helper函数用于处理并发请求。
axios.all(iterable)
axios.spread(callback)
// 创建一个实例
// 您可以使用自定义的config创建新的axios实例。
const instance = axios.create({
    baseURL: 'https://some-domain.com/api/',
    timeout: 1000,
    headers: {'X-Custom-Header': 'foobar'}
});


// 实例方法
// 下面列出了可用的实例方法。 指定的config将与实例config合并。
axios#request(config)

axios#get(url[, config])

axios#delete(url[, config])

axios#head(url[, config])

axios#options(url[, config])

axios#post(url[, data[, config]])

axios#put(url[, data[, config]])

axios#patch(url[, data[, config]])

axios#getUri([config])
// Request Config
// 这些是用于发出请求的可用配置选项。 只需要url。 如果未指定method，请求将默认为GET。
{
    // `url` 是将被用于此请求的服务器URL。
    url: '/user',

    // `method` 是此请求使用的请求方法。
    method: 'get', // default

    // `baseURL` will be prepended to `url` unless `url` is absolute.
    // It can be convenient to set `baseURL` for an instance of axios to pass relative URLs
    // to methods of that instance.
    baseURL: 'https://some-domain.com/api/',

    // `transformRequest` allows changes to the request data before it is sent to the server
    // This is only applicable for request methods 'PUT', 'POST', and 'PATCH'
    // The last function in the array must return a string or an instance of Buffer, ArrayBuffer,
    // FormData or Stream
    // You may modify the headers object.
    transformRequest: [function (data, headers) {
    // Do whatever you want to transform the data

    return data;
}],

    // `transformResponse` allows changes to the response data to be made before
    // it is passed to then/catch
    transformResponse: [function (data) {
    // Do whatever you want to transform the data

    return data;
}],

    // `headers` are custom headers to be sent
    headers: {'X-Requested-With': 'XMLHttpRequest'},

    // `params` are the URL parameters to be sent with the request
    // Must be a plain object or a URLSearchParams object
    params: {
        ID: 12345
    },

    // `paramsSerializer` is an optional function in charge of serializing `params`
    // (e.g. https://www.npmjs.com/package/qs, http://api.jquery.com/jquery.param/)
    paramsSerializer: function (params) {
        return Qs.stringify(params, {arrayFormat: 'brackets'})
    },

    // `data` is the data to be sent as the request body
    // Only applicable for request methods 'PUT', 'POST', and 'PATCH'
    // When no `transformRequest` is set, must be of one of the following types:
    // - string, plain object, ArrayBuffer, ArrayBufferView, URLSearchParams
    // - Browser only: FormData, File, Blob
    // - Node only: Stream, Buffer
    data: {
        firstName: 'Fred'
    },

    // `timeout` specifies the number of milliseconds before the request times out.
    // If the request takes longer than `timeout`, the request will be aborted.
    timeout: 1000, // default is `0` (no timeout)

        // `withCredentials` indicates whether or not cross-site Access-Control requests
        // should be made using credentials
        withCredentials: false, // default

    // `adapter` allows custom handling of requests which makes testing easier.
    // Return a promise and supply a valid response (see lib/adapters/README.md).
    adapter: function (config) {
    /* ... */
},

    // `auth` indicates that HTTP Basic auth should be used, and supplies credentials.
    // This will set an `Authorization` header, overwriting any existing
    // `Authorization` custom headers you have set using `headers`.
    auth: {
        username: 'janedoe',
            password: 's00pers3cret'
    },

    // `responseType` indicates the type of data that the server will respond with
    // options are 'arraybuffer', 'blob', 'document', 'json', 'text', 'stream'
    responseType: 'json', // default

        // `responseEncoding` indicates encoding to use for decoding responses
        // Note: Ignored for `responseType` of 'stream' or client-side requests
        responseEncoding: 'utf8', // default

    // `xsrfCookieName` is the name of the cookie to use as a value for xsrf token
    xsrfCookieName: 'XSRF-TOKEN', // default

    // `xsrfHeaderName` is the name of the http header that carries the xsrf token value
    xsrfHeaderName: 'X-XSRF-TOKEN', // default

    // `onUploadProgress` allows handling of progress events for uploads
    onUploadProgress: function (progressEvent) {
    // Do whatever you want with the native progress event
},

    // `onDownloadProgress` allows handling of progress events for downloads
    onDownloadProgress: function (progressEvent) {
        // Do whatever you want with the native progress event
    },

    // `maxContentLength` defines the max size of the http response content in bytes allowed
    maxContentLength: 2000,

        // `validateStatus` defines whether to resolve or reject the promise for a given
        // HTTP response status code. If `validateStatus` returns `true` (or is set to `null`
        // or `undefined`), the promise will be resolved; otherwise, the promise will be
        // rejected.
        validateStatus: function (status) {
    return status >= 200 && status < 300; // default
},
    // `maxRedirects` defines the maximum number of redirects to follow in node.js.
    // If set to 0, no redirects will be followed.
    maxRedirects: 5, // default
        // `socketPath` defines a UNIX Socket to be used in node.js.
        // e.g. '/var/run/docker.sock' to send requests to the docker daemon.
        // Only either `socketPath` or `proxy` can be specified.
        // If both are specified, `socketPath` is used.
        socketPath: null, // default
    // `httpAgent` and `httpsAgent` define a custom agent to be used when performing http
    // and https requests, respectively, in node.js. This allows options to be added like
    // `keepAlive` that are not enabled by default.
    httpAgent: new http.Agent({ keepAlive: true }),
    httpsAgent: new https.Agent({ keepAlive: true }),
    // 'proxy' defines the hostname and port of the proxy server.
    // You can also define your proxy using the conventional `http_proxy` and
    // `https_proxy` environment variables. If you are using environment variables
    // for your proxy configuration, you can also define a `no_proxy` environment
    // variable as a comma-separated list of domains that should not be proxied.
    // Use `false` to disable proxies, ignoring environment variables.
    // `auth` indicates that HTTP Basic auth should be used to connect to the proxy, and
    // supplies credentials.
    // This will set an `Proxy-Authorization` header, overwriting any existing
    // `Proxy-Authorization` custom headers you have set using `headers`.
    proxy: {
    host: '127.0.0.1',
        port: 9000,
        auth: {
        username: 'mikeymike',
            password: 'rapunz3l'
    }
},
    // `cancelToken` specifies a cancel token that can be used to cancel the request
    // (see Cancellation section below for details)
    cancelToken: new CancelToken(function (cancel) {
    })
}

// Response Schema
// 一个请求的响应包含如下信息.
{
    // `data` is the response that was provided by the server
    data: {},
    // `status` is the HTTP status code from the server response
    status: 200,
    // `statusText` is the HTTP status message from the server response
    statusText: 'OK',
    // `headers` the headers that the server responded with
    // All header names are lower cased
    headers: {},
    // `config` is the config that was provided to `axios` for the request
    config: {},
    // `request` is the request that generated this response
    // It is the last ClientRequest instance in node.js (in redirects)
    // and an XMLHttpRequest instance the browser
    request: {}
}
// 当使用 then 时, 你会收到如下响应:
axios.get('/user/12345')
    .then(function (response) {
        console.log(response.data);
        console.log(response.status);
        console.log(response.statusText);
        console.log(response.headers);
        console.log(response.config);
    });


// Config Defaults
// 全局 axios defaults
axios.defaults.baseURL = 'https://api.example.com';
axios.defaults.headers.common['Authorization'] = AUTH_TOKEN;
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';


// Interceptors（拦截器）
// 可以在被then or catch处理之前拦截请求或响应.
// 添加一个 request interceptor
axios.interceptors.request.use(function (config) {
    // Do something before request is sent
    return config;
}, function (error) {
    // Do something with request error
    return Promise.reject(error);
});

// 添加一个 response interceptor
axios.interceptors.response.use(function (response) {
    // Do something with response data
    return response;
}, function (error) {
    // Do something with response error
    return Promise.reject(error);
});
// 删除拦截器.
const myInterceptor = axios.interceptors.request.use(function () {/*...*/});
axios.interceptors.request.eject(myInterceptor);
// 给自定义的axios实例添加拦截器.
const instance = axios.create();
instance.interceptors.request.use(function () {/*...*/});

// 错误处理
axios.get('/user/12345')
    .catch(function (error) {
        if (error.response) {
            // The request was made and the server responded with a status code
            // that falls out of the range of 2xx
            console.log(error.response.data);
            console.log(error.response.status);
            console.log(error.response.headers);
        } else if (error.request) {
            // The request was made but no response was received
            // `error.request` is an instance of XMLHttpRequest in the browser and an instance of
            // http.ClientRequest in node.js
            console.log(error.request);
        } else {
            // Something happened in setting up the request that triggered an Error
            console.log('Error', error.message);
        }
        console.log(error.config);
    });


// axios原理
// createInstance底层根据默认设置 新建一个Axios对象， axios中所有的请求[axios, axios.get, axios.post等...]内部调用的都是Axios.prototype.request,
// 将Axios.prototype.request的内部this绑定到新建的Axios对象上,从而形成一个axios实例。新建一个Axios对象时，会有两个拦截器，request拦截器，response拦截器。

// 1，请求拦截器
// 请求拦截器的作用是在请求发送前进行一些操作，例如在每个请求体里加上token，统一做了处理如果以后要改也非常容易。
axios.interceptors.request.use(function (config) {
    // 在发送请求之前做些什么，例如加入token
    return config;
}, function (error) {
    // 对请求错误做些什么
    return Promise.reject(error);
});
// 2，响应拦截器
// 响应拦截器的作用是在接收到响应后进行一些操作，例如在服务器返回登录状态失效，需要重新登录的时候，跳转到登录页。
axios.interceptors.response.use(function (response) {
    // 在接收响应做些什么，例如跳转到登录页
    return response;
}, function (error) {
    // 对响应错误做点什么
    return Promise.reject(error);
});
//axios的特点有哪些？
// 1，Axios 是一个基于 promise 的 HTTP 库，支持promise所有的API
// 2，它可以拦截请求和响应
// 3，它可以转换请求数据和响应数据，并对响应回来的内容自动转换成 JSON类型的数据
// 4，安全性更高，客户端支持防御 XSRF
//axios相关配置属性？
// url是用于请求的服务器URL
// method是创建请求时使用的方法,默认是get
// baseURL将自动加在url前面，除非url是一个绝对URL。它可以通过设置一个baseURL便于为axios实例的方法传递相对URL
// transformRequest允许在向服务器发送前，修改请求数据，只能用在'PUT','POST'和'PATCH'这几个请求方法
// headers是即将被发送的自定义请求头
// headers:{'X-Requested-With':'XMLHttpRequest'},
// params是即将与请求一起发送的URL参数，必须是一个无格式对象(plainobject)或URLSearchParams对象
// params:{
//     ID:12345
// },
// auth表示应该使用HTTP基础验证，并提供凭据这将设置一个Authorization头，覆写掉现有的任意使用headers设置的自定义Authorization头
// auth:{
//     username:'janedoe',
//     password:'s00pers3cret'
// },
// 'proxy'定义代理服务器的主机名称和端口
// auth表示HTTP基础验证应当用于连接代理，并提供凭据
// 这将会设置一个Proxy-Authorization头，覆写掉已有的通过使用header设置的自定义Proxy-Authorization头。
// proxy:{
//     host:'127.0.0.1',
//         port:9000,
//         auth::{
//             username:'mikeymike',
//             password:'rapunz3l'
//         }
// },

