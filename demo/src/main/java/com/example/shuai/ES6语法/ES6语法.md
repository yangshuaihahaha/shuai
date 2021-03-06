https://www.jianshu.com/p/d23a506cdca2
# var ，let ， const 的区别
## 不存在变量提升
var 命令会发生变量提升现象，即变量可以在声明之前使用，值为undefined。
let 和 const 则没有变量声明提升的功能，必须要先声明才能使用
```js
console.log(a)
console.log(b) //这句会报错，let不存在变量提升
var a;
let b;
```
## 不允许重复声明
var命令能重复声明，后者覆盖前者
let 和 const不允许在相同作用域内，重复声明同一个变量
```js
var a = 1;
var a = 2;

let b = 'a';
let b = 'b';//报错，不允许重复声明
```
## 作用域
var 的作用域是以函数为界限
let 和 const 的作用域是块作用域，块级作用域指 { } 内的范围
var 可以定义全局变量和局部变量，let 和 const 只能定义局部变量
const 的声明的常量不能被修改，但对于引用类型来说，堆内存中的值是可以被改变的。
```js
const c = [1, 2];
c[0] = 3 //引用类型来说，堆内存中的值可以被改变
console.log(c);
```
## 变量作为全局属性
定义的变量会作为window对象的属性，let不会

# 暂时性死区
定义：块级作用域内存在let命令时，所声明的变量就“绑定”这个区域，不再受外部的影响。
```js
{
    //let a 之前的区域成为暂时性死区，调用a 会报错
    let a = "hello";
}
```
# for循环中的作用域问题
设置循环变量的部分是父级作用域，而循环体内部是一个单独的子作用域。
ES6 中引用变量采用就近原则
```js
for (let i = 0; i < 3; i++) {
    
}
console.log(i)
// 以上代码报错，说明设置循环变量的部分与外部作用域不是同一个块作用域
for (let i = 0; i < 3; i++) {
    let i = 'x'
    console.log(i)
}
//以上代码正常运行，在循环体中可以使用 let 重新声明 i，说明设置循环变量的部分和循环体内部处于两个不同的块作用域
```
结论：for循环中设置循环变量的部分为一个单独的块作用域
# ES6 变量解构用法
## 1、数组的解构赋值　
左边是变量,右边是值,左边无匹配值时为undefined】
```js
//完全解构【左右数据恰好匹配】
let [a,b,c] = [1,2,3];

//不完全解构【左右数据不同】
//a、左边数据多则只声明该变量,其值为undefined
//b、右边数据多余项则忽视不考虑
let [a, [b], d] = [1, [2, 3], 4];     //a = 1; b = 2; d = 4 

//集合解构【扩展运算符的使用...】
//...tail返回目前右边未匹配的所有值组成的数组
let [head, ...tail] = [1, 2, 3, 4]; //head = 1; tail = [2, 3, 4]

//默认值【当匹配值严格等于undefined,默认值生效】
let [x, y = 'b'] = ['a']; 　　// x='a', y='b’

//默认值为函数
//注：先判断是否匹配到值,若匹配值严格等于undefined,再进行默认值的赋值运算;否则,默认值赋值操作不会执行
function test() {
    console.log('test');
    return 2;
}
let [x = test()] = [];
console.log(x);        //test    2
```
## 2、对象的解构赋值
【右边不存在左边变量对应的属性名时,对象属性值为undefined;即对象中未声明的属性的值为undefined
```js
//对象原始结构赋值【变量重命名后,最终声明的变量是重命名的变量
let {name:myName,age:myAge} = {name:'nzc',age:18}
//上面代码类比于下面代码【左边name匹配右边对象中同名属性获取其属性值并赋值给name重命名的myName变量-->将let myName = 'nzc'】
console.log(myName, myAge) //nzc 18

//对象的属性没有次序,变量必须与属性同名才能取到正确的值【重命名相同可以简写
let {name:name,age:age} = {name:'nzc',age:18}
//简写如下
let {name,age} = {name:'nzc',age:18}
//类比于下面代码
let name = 'nzc';
let age = 18;

//对象嵌套解构
let person = { param: [ 'nzc', { age: 18 } ] };
let { param: [name, { age }] } = person; //name='nzc' age=18
//类比于下面   param变量被重命名为 [name, { age }],所以自身并未声明;即不存在param变量
let { param: [name, { age }] } = { param: [ 'nzc', { age: 18 } ] }

//默认值(默认值生效的条件是，对象的属性值严格等于undefined)
//name='nzchs'->name变量默认值;age:myAge=21->myAge默认值【age重命名为myAge再赋予默认值】
let {name='nzchs',age:myAge=21} = {name:'nzc',age:18}
let {name='nzchs',age:myAge=21} = {name:'nzc'}  
```

## 3、字符串的解构赋值
解构时，字符串被转换成了一个类似数组的对象。
```js
//解构时，字符串被转换成了一个类似数组的对象。
let [a, b, c] = 'hello'; //a=h;b=e;c=l

//length属性解构
let {length : len} = 'hello'; //len = 5 【匹配右边字符串转换为的类数组对象的length属性并将其值赋值给重命名的len变量】
```

## 4、数值和布尔值解构赋值
解构时，如果等号右边是数值和布尔值，则会先转为相应的基本引用数据类型对象
```js
let {toString: str1} = 123; //函数 str1 === Number.prototype.toString     返回true
let {toString: str2} = true; //函数 str2 === Boolean.prototype.toString        返回true
```

## 5、函数参数的解构赋值
基本数组解构赋值传参
```js
function add([x, y]){ return x + y; }
add([1, 2]);   //函数add返回值为3 

//函数参数带有默认值
function test({x = 0, y = 0}) {
    return [x, y];
}
//函数调用
test({x: 3, y: 8}); // 返回值为[3, 8]
test({x: 3}); // 返回值为[3, 0]
test({}); // 返回值为[0, 0]
test(); //报错 Cannot destructure property `x` of 'undefined' or 'null'
```


## 6、解构常用用途
### 1、变量值的交换   
```js
let x = 1;
let y = 2;
[x,y] = [y,x];  
console.log(x,y); //2 1   
```
 
### 2、函数参数的赋值：
```js
//[a=0,b=1] = [1]   a=1,b=1
function test([a=0,b=1]){
    return a+b;
}
test([1]);  //返回值为2
```
### 3、提取对象中的数据
```js
let obj= { id: 42, status: "OK", data: [867, 5309] };
let { id, status, data: number } = obj;  //定义对应的变量 
```
### 4、输入模块的指定方法
```js
const { SourceMapConsumer, SourceNode } = require("source-map");
```
### 5、遍历map结构
```js
var map = new Map();
map.set('name', 'nzc');
map.set('age', 18);
for (let [key, value] of map) {
    console.log(key + " is " + value);    // name is nzc   age is 18
}
```
# ES6 rest参数
rest 参数： 接收不定参
1. rest 参数是一种方式(形参)，rest参数可以重命名为其他参数 ...a
2. rest 参数只能作为最后一个参数
## 1, rest 参数搭配的变量是一个数组，该变量将多余的参数放入数组中。
```js
function add(...values) {
  let sum = 0;
 
  for (var val of values) {
    sum += val;
  }
 
  return sum;
}
 
add(2, 5, 3) // 10
//上面代码的add函数是一个求和函数，利用 rest 参数，可以向该函数传入任意数目的参数。
```
## 2, rest 参数代替arguments变量的例子。
```js
// arguments变量的写法
function sortNumbers() {
  return Array.prototype.slice.call(arguments).sort();
}
 
// rest参数的写法
const sortNumbers = (...numbers) => numbers.sort();
```
arguments对象不是数组，而是一个类似数组的对象。所以为了使用数组的方法，必须使用Array.prototype.slice.call先将其转为数组。rest 参数就不存在这个问题，它就是一个真正的数组
## 3, rest 参数之后不能再有其他参数，否则会报错。
```js
// 报错
function f(a, ...b, c) {
  // ...
}
```
## 4, 函数的length属性，不包括 rest 参数。
```js
(function(a) {}).length  // 1
(function(...a) {}).length  // 0
(function(a, ...b) {}).length  // 1
```
# ES6 中箭头函数
1，ES6 中函数式声明方式被箭头函数 => 取代   
2，箭头函数：使用 => 定义函数     
3，当函数没有参数时，（）不能省略  
4，当函数只有一个参数，且函数体是一句代码，且是返回语句，参数的（）可省略、函数体 {} 可省略、return 可省略，中间使用 => 连接
5，若函数体只有一句，且不是return 语句， 不能省略 {}   
6，若函数体有多条语句，不能省略 {}   
7，若函数有多个参数，不能省略()   
8，若函数的返回值为对象，此时不能省略return   
## 1, 使用箭头函数注意
1，箭头函数不适用于声明函数
2，箭头函数不适用于DOM事件
3，箭头函数不能作为构造函数（迭代器）
4，箭头函数内不能使用arguments
5，不能使用yield命令
## 2, 箭头函数this指向
1，箭头函数没有this，this是父级的  
2，定义时候绑定，就是this是继承自父执行上下文！！中的this  
3，ES5中，this指调用者，ES6中，this指定义时候绑定  
箭头函数的this永远指向其父作用域，任何方法都改变不了，包括call，apply，bind。
普通函数的this指向调用它的那个对象。
```js
let person = {
    name:'jike',
    init:function(){
        //为body添加一个点击事件，看看这个点击后的this属性有什么不同
        document.body.onclick = ()=>{
            alert(this.name);//?? this在浏览器默认是调用时的对象,可变的？                  
        }
    }
}
person.init();
//上例中，init是function，以person.init调用，其内部this就是person本身，而onclick回调是箭头函数，
//其内部的this，就是父作用域的this，就是person，能得到name。

let person = {
    name:'jike',
    init:()=>{
        //为body添加一个点击事件，看看这个点击后的this属性有什么不同
        document.body.onclick = ()=>{
            alert(this.name);//?? this在浏览器默认是调用时的对象,可变的？                  
        }
    }
}
person.init();
//上例中，init为箭头函数，其内部的this为全局window，onclick的this也就是init函数的this，也是window，
//得到的this.name就为undefined。
```
## 3, 箭头函数不能作为构造函数，不能使用new
```js
//构造函数如下：
function Person(p){
    this.name = p.name;
}
//如果用箭头函数作为构造函数，则如下
var Person = (p) => {
    this.name = p.name;
}
```
由于this必须是对象实例，而箭头函数是没有实例的，此处的this指向别处，不能产生person实例，自相矛盾。
## 4, 箭头函数没有arguments，caller，callee
箭头函数本身没有arguments，如果箭头函数在一个function内部，它会将外部函数的arguments拿过来使用。
箭头函数中要想接收不定参数，应该使用rest参数...解决。
```js
let B = (b)=>{
  console.log(arguments);
}
B(2,92,32,32);   // Uncaught ReferenceError: arguments is not defined

let C = (...c) => {
  console.log(c);
}
C(3,82,32,11323);  // [3, 82, 32, 11323]
```
## 5, 箭头函数通过call和apply调用，不会改变this指向，只会传入参数
```js
let obj2 = {
    a: 10,
    b: function(n) {
        let f = (n) => n + this.a;
        return f(n);
    },
    c: function(n) {
        let f = (n) => n + this.a;
        let m = {
            a: 20
        };
        return f.call(m,n);
    }
};
console.log(obj2.b(1));  // 11
console.log(obj2.c(1)); // 11
```
## 6, 箭头函数没有原型属性
```js

var a = ()=>{
  return 1;
}

function b(){
  return 2;
}

console.log(a.prototype);  // undefined
console.log(b.prototype);   // {constructor: ƒ}
```
## 7, 箭头函数不能作为Generator函数，不能使用yield关键字
## 8, 箭头函数返回对象时，要加一个小括号
```js
var func = () => ({ foo: 1 }); //正确
var func = () => { foo: 1 };   //错误
```
## 9, 箭头函数在ES6 class中声明的方法为实例方法，不是原型方法
```js
//deom1
class Super{
    sayName(){
        //do some thing here
    }
}
//通过Super.prototype可以访问到sayName方法，这种形式定义的方法，都是定义在prototype上
var a = new Super()
var b = new Super()
a.sayName === b.sayName //true
//所有实例化之后的对象共享prototypy上的sayName方法


//demo2
class Super{
    sayName =()=>{
        //do some thing here
    }
}
//通过Super.prototype访问不到sayName方法，该方法没有定义在prototype上
var a = new Super()
var b = new Super()
a.sayName === b.sayName //false
//实例化之后的对象各自拥有自己的sayName方法，比demo1需要更多的内存空间
```
## 10, 多重箭头函数就是一个高阶函数，相当于内嵌函数
```js
const add = x => y => y + x;
//相当于
function add(x){
  return function(y){
    return y + x;
  };
}
```
## 11, 箭头函数常见错误
```js
let a = {
  foo: 1,
  bar: () => console.log(this.foo)
}

a.bar()  //undefined
```
bar函数中的this指向父作用域，而a对象没有作用域，因此this不是a，打印结果为undefined
# 字符串遍历
```js
let str = "hello";
//1.for遍历
for (let i = 0; i < str.length; i++) {
    console.log(i, str[i]);     //i 索引    数值类型
}

//2.数组->for->for in
let arr = [1, 2, 3];
for (let i in arr) {
    console.log(i, arr[i]);     //i 索引    字符串类型
}

//3.for... of
for(let i of str){
    console.log(i);     //数据
}

//4.解构
let [a, b, c, d ,e] = str;
console.log(a, b, c, d ,e);
```
# ES6 新增字符串方法
```js
//字符串新增方法：
方法                返回值          作用
includes('str')     boolean         判断字符串中包含子串
endWith('str')      boolean         判断字符串以"str"结尾
startWith('str')    boolean         判断字符串以"str"开头
repeat(n)           重复拼接自身     重复n次输出字符串 repeat + repeat

//不全方法: 补全字符串长度
padStart(length, s);        字符串开头补全
endStart(length, s);        字符串末尾补全
```
# ES6模板字符串
模板字符串 反引号 ``
1.支持换行 //空白会打印出来
2.模板中传变量 ${变量}
```js
let obj = {
    name: 'jack',
    age: 20
};
console.log(`名称：${obj.name},年龄：${obj.age}`);  //名称：jack,年龄：20
```
# ES6 对于对象的扩展
```js
//1.对象的属性简写: 当对象的属性名和属性值（变量）名称一致，可省略赋值
//2.对象的方法简写：
let name = "jack";
let age = 22;

let obj = {
    name,
    age,
    walk: function () {
        console.log(this.name + " is walk");
    },
    say() {
        console.log(this.name + ` say`);
    }
}
```
# ES6的Set结构
## set结构：此结构中元素是唯一的，不能重复
```js
//方法：返回值是set可以连缀
add(val)           //加元素
delete(val)        //删元素
has(val)           //判断是否包含元素     boolean
clear()            //删除所有数据

//属性：
    size    //元素个数

//使用new Set() 实例化
```
## set结构的遍历
for of 遍历set   
for of 遍历keys()   
for of 遍历values()   
for of 遍历对象实体 entries   
forEach遍历 set   
使用扩展运算符 和 解构 将set结构转为数组   
```js
// for of 遍历数据
for (let i of set) {
    console.log(i);     //1  2  5  3
}

// 遍历 keys    等于遍历set
for (let i of set.keys()) {
    console.log(i);
}

//遍历values
for (let i of set.values()) {
    console.log(i);
}

//遍历对象实体 entries
for (let i of set.entries()) {
    console.log(i[0]);
}

//解构的方式遍历对象实体
for (let [k, v] of set.entries()) {
    console.log(k, v);
}

// for  each遍历 set
set.forEach(i => {
    console.log(i);
})
```
# ES6的Map结构
## Map 结构：由于对象的属性只能接受字符串类型，所有产生了Map结构，优化对象结构
方法：返回值是Map实例可以连缀   
    set(key,val)        加元素   
    get(key)            取元素   
    has(key)            判断是否包含元素     boolean   
    clear()             删除所有数据   
使用new Map()实例化
```js

// 1.添加数据 set()
map.set('name', 'jack').set('age', 22).set(0,100);
console.log(map);

// 2.获取数据 get()
console.log(map.get(0));        //100
console.log(map.get('name'))    //jack

// 3.判断存在数据 has()
console.log(map.has(0));        //true

// 4.删除数据 delete()
map.delete(0);
console.log(map);       //Map(2) {"name" => "jack", "age" => 22}

// 5.清空数据 clear()
map.clear();
console.log(map);       //Map(0) {}
```
## Map结构规则
```js
//1. map 支持数组作为构造函数的参数,但必须是二维数组
// let arr = [1, 2, 3, 4, 5];  //Iterator value 1 is not an entry object
let arr = [['name', 'jack'], ['age', 23]];
let map = new Map(arr);
console.log(map);   //Map(4) {"name" => "jack", "age" => 23}


// 2.key 不能重复，val可以重复, key如果重复会将原来的值覆盖
map.set('name','tom');
map.set('hob','sing');
console.log(map);   //Map(5) {"name" => "tom", "age" => 23, "hob" => "sing"}
```
## Map的遍历
```js
// 1.let of 遍历map
for (let i of map) {
    console.log(i[0], i[1]);
}

// 2.foreach 遍历 map
map.forEach((v, k) => {
    console.log(k, v);
})

// 3.let of 遍历map.keys
for (let k of map.keys()) {
    console.log(k, map.get(k));
}

// 4.let of 遍历map.values
for (let v of map.values()) {
    console.log(v);
}

// 5.let of 遍历map.entries
for (let i of map.entries()) {
    console.log(i[0], i[1]);
}

// 6.let of 遍历 map.entries + 解构
for (let [k, v] of map.entries()) {
    console.log(k, v);
}
```
# ES6的set结构、map结构类型间转换
```js
Set 和 数组
    1.set -> 数组
        1.Array.from();
        2.遍历set然后push
        3.扩展运算符

    2.数组 -> set
        1.new Set(arr);
    
Map 和 对象 和 string
    1.map -> 对象 -> String
        1.forEach遍历Map -> 对象 -> JSON.stringify
    2.String -> 对象 -> map
        1.JSON.parse -> 对象 -> for...in 遍历对象 -> Map.add()
```
## Set 和 数组之间
```js
let set = new Set();
set.add(1).add(2).add(3).add(2);

// 1.Array.from();
var arr = Array.from(set);
console.log(arr);

// 2.遍历set然后push
var arr  = [];
set.forEach(i=>{
    arr.push(i);
});
console.log(arr);

// 3.扩展运算符
var arr = [...set];
console.log(arr);
```
## map、对象和字符串之间
```js
let map = new Map();
map.set('name', 'jack').set('age', 22).set('tel', 151);

// 1.map -> 对象 -> String
var obj = {};
map.forEach((v,k)=>{
    obj[k] = v;
})
console.log(obj);

var str = JSON.stringify(obj);
console.log(str);

// 2.String -> 对象 -> map
var obj1 = JSON.parse(str);
var map1 = new Map();
for(let i in obj1){
    map1.set(i,obj1[i]);
}
console.log(map1);
```
# rest 和 扩展运算符区别
## rest参数，出现在函数参数位置
作用： 离散数据 -> 数组
```js
function fn(...rest) {
    console.log(rest);
}
fn(100, 110, 120);    //(3) [100, 110, 120]
```
## 扩展运算符出现在非参数位置
数组 -> 离散的数据
拆分伪数组： NodeList HTMLCollection arguments...
```js
let arr = [1, 2, 3, 4, 5];
console.log(...arr);    //1 2 3 4 5    

function fn1() {
    console.log(arguments);     //Arguments(3) [2, 3, 4]
    console.log(...arguments);  //2 3 4
}
fn1(2,3,4);
```
# ES6 面向对象
## class 类、模板
构造器：当类被实例化时，自动执行构造器   
每个类必须至少一个构造器，若没有，系统自动添加一个无参构造器   
构造函数，不能主动调用   
## set 和 get 设置和获取属性
get 不能传递参数
只有当有set方法时才可以写get方法，同时出现
## 静态方法
类自身的方法，不用实例化即可调用
不会被实例继承,直接通过类来调用
## 静态属性
类名.属性名 = 值;
## extends 实现继承
继承是单向的   
被继承的类属于父类，基类，也称超类   
静态方法可以被子类继承   
继承属性super()必须放在构造器第一句   
一个父类可以有多个子类，一个子类只有一个父类   
## 注意
父类可调用自己的成员方法   
父类可以调自己的静态方法   
父类不能调子类的成员方法   
子类的实例可以调父类成员方法   
子类的实例不能调父类静态方法   
子类可以调父类静态方法   
```js
class Person {
    constructor(uname, uage) {
        this.uname = uname;
        this.uage = uage;
    }
    //成员方法
    walk() {
        console.log(`${this.uname}正在奔跑！`);
    }

    // 静态方法:类自身的方法
    static cry() {
        console.log('人生下来就会哭！');
    }
}
// 静态属性
Person.living = 'earth';


//学生类继承Person类
class Student extends Person {
    constructor(sname, sage, sno) {
        super(sname, sage);  //此句必须在构造器第一句
        this.sno = sno;
    }

    // 成员方法
    study() {
        console.log(`${this.sname}在学习呢！`);
    }
    
    //set 访问器 控制属性的设置
    set sage(age) {
       
    }

    // get 访问器，获取属性
    get sage() {
        
    }
}

//创建类的实例   类的实例化
var stu = new Student('王飒', 23, '001');
```


https://www.cnblogs.com/paddix/p/5405678.html