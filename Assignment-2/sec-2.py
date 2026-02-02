# Closure example
def make_adder(x):
    def add(y):
        return x + y   # x is captured from outer scope
    return add

add10 = make_adder(10)
print(add10(5))   # Output: 15

# Dynamic typing example
x = "10"
print(x + "5")    # Output: 105