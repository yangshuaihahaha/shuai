const add = (x, y) => {
    return x + y;
}
console.log(add(2, 5))

new Promise(reslove => {
    console.log('hahaha');
    reslove();
})