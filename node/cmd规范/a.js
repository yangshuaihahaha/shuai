// example.js
var x = 5;
var addX = function (value) {
  return value + x;
};
exports.x = x;

// exports.hello = function() {
//     return 'hello';
// };
// module.exports = 'Hello world';

module.exports = function (){ return 'hello';};