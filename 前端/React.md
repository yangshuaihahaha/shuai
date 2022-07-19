# PureComponent介绍
PureComponent 也就是纯组件，取代其前身 PureRenderMixin。
PureComponent 是优化 React 应用程序最重要的方法之一，易于实施，只要把继承类从 Component 换成 PureComponent 即可
可以减少不必要的 render 操作的次数，从而提高性能，而且可以少写 shouldComponentUpdate 函数
# connect原理
首先connect之所以会成功是因为Provider组件在原应用组件上包裹一层使原来整个应用成为Provider的子组件接收Redux的store作为props通过context对象传递给子孙组件上的connectconnect做了些什么。
它真正连接 Redux和 React它包在我们的容器组件的外一层它接收上面 Provider 提供的 store 里面的state 和 dispatch传给一个构造函数返回一个对象以属性形式传给我们的容器组件

connect是一个高阶函数首先传入mapStateToProps、mapDispatchToProps然后返回一个生产Component的函数(wrapWithConnect)然后再将真正的Component作为参数传入wrapWithConnect这样就生产出一个经过包裹的Connect组件
该组件具有如下特点：
    通过props.store获取祖先Component的store props包括stateProps、dispatchProps、parentProps,合并在一起得到nextState作为props传给真正的Component 
    componentDidMount时添加事件this.store.subscribe(this.handleChange)实现页面交互shouldComponentUpdate时判断是否有避免进行渲染提升页面性能并得到nextState 
    componentWillUnmount时移除注册的事件this.handleChange

# react hooks它带来了那些便利
1、 代码逻辑聚合逻辑复用
2、 HOC嵌套地狱
3、 代替 class
4、 React 中通常使用 类定义 或者 函数定义 创建组件:
在类定义中我们可以使用到许多 React 特性例如 state、 各种组件生命周期钩子等但是在函数定义中我们却无能为力因此 React 16.8 版本推出了一个新功能 (React Hooks)通过它可以更好的在函数定义组件中使用 React 特性。
好处:
    1、 跨组件复用: 其实 render props / HOC 也是为了复用相比于它们Hooks 作为官方的底层 API最为轻量而且改造成本小不会影响原来的* 组件层次结构和传说中的嵌套地狱
    2、 类定义更为复杂
    3、 不同的生命周期会使逻辑变得分散且混乱不易维护和管理
    4、 时刻需要关注 this的指向问题
    5、 代码复用代价高高阶组件的使用经常会使整个组件树变得臃肿
    6、 状态与UI隔离: 正是由于 Hooks 的特性状态逻辑会变成更小的粒度并且极容易被抽象成一个自定义 Hooks组件中的状态和 UI 变得更为清晰和隔离。
# setState到底是异步还是同步?
有时表现出异步,有时表现出同步
1，setState只在合成事件和钩子函数中是“异步”的在原生事件和setTimeout 中都是同步的
2，setState 的“异步”并不是说内部由异步代码实现其实本身执行的过程和代码都是同步的
只是合成事件和钩子函数的调用顺序在更新之前导致在合成事件和钩子函数中没法立马拿到更新后的值
形成了所谓的“异步”当然可以通过第二个参数setState(partialState, callback)中的callback拿到更新后的结果
3，setState 的批量更新优化也是建立在“异步”合成事件、钩子函数之上的
在原生事件和setTimeout 中不会批量更新
在“异步”中如果对同一个值进行多次setState的批量更新策略会对其进行覆盖取最后一次的执行如果是同时setState多个不同的值在更新时会对其进行合并批量更新

