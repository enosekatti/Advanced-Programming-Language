from collections import Counter


class StatisticsCalculator:
    def __init__(self, numbers):
        if not numbers:
            raise ValueError("numbers list cannot be empty")
        self.numbers = list(numbers)

    def mean(self):
        return sum(self.numbers) / len(self.numbers)

    def median(self):
        sorted_numbers = sorted(self.numbers)
        n = len(sorted_numbers)
        mid = n // 2
        if n % 2 == 1:
            return float(sorted_numbers[mid])
        return (sorted_numbers[mid - 1] + sorted_numbers[mid]) / 2

    def mode(self):
        counts = Counter(self.numbers)
        max_count = max(counts.values())
        if max_count == 1:
            return [], 1
        modes = sorted([num for num, count in counts.items() if count == max_count])
        return modes, max_count


def read_ints_from_file(path):
    with open(path, "r", encoding="utf-8") as f:
        tokens = f.read().split()
    return [int(token) for token in tokens]


def main():
    values = read_ints_from_file("../tests/sample_input.txt")
    calc = StatisticsCalculator(values)
    modes, freq = calc.mode()

    print("Python OOP Statistics")
    print(f"Input count: {len(values)}")
    print(f"Mean: {calc.mean():.2f}")
    print(f"Median: {calc.median():.2f}")
    if not modes:
        print("Mode: no mode (all values unique)")
    else:
        joined = ", ".join(str(m) for m in modes)
        print(f"Mode: {joined} (frequency {freq})")


if __name__ == "__main__":
    main()
