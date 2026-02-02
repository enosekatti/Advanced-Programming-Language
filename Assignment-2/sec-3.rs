fn main() {
    let mut v: Vec<i32> = Vec::new();  // Heap allocation managed by Vec

    for i in 0..1_000_000 {
        v.push(i);
    }

    // Borrow v immutably to compute sum without taking ownership
    let sum: i64 = v.iter().map(|x| *x as i64).sum();
    println!("sum = {}", sum);

    // v is automatically freed when it goes out of scope
}
