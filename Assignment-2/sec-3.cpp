#include <iostream>
using namespace std;

int main() {
    int* data = new int[1000000];  // Allocate on heap

    long long sum = 0;
    for (int i = 0; i < 1000000; i++) {
        sum += data[i];
    }

    delete[] data;  // Free memory

    cout << "done" << endl;
    return 0;
}
