#include <iostream>
#include <functional>
using namespace std;

// Lambda closure example
function<int(int)> make_adder(int x) {
    return [x](int y) {   // x captured by value
        return x + y;
    };
}

int main() {
    auto add10 = make_adder(10);
    cout << add10(5) << endl;   // Output: 15
    return 0;
}
