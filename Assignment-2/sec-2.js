// Closure example
function makeAdder(x) {
    return function (y) {
        return x + y;   // x is captured from outer scope
    };
}

const add10 = makeAdder(10);
console.log(add10(5));   // Output: 15

// Dynamic typing + implicit coercion
console.log("10" + 5);   // Output: "105"
console.log("10" - 5);   // Output: 5
