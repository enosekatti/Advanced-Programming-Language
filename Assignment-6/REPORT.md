# Data Processing System: Concurrency and Error Handling in Java and Go

**Student Name**  
*University / Course*  
*Date*

> **Note for submission:** Replace the bracketed placeholders, add your **GitHub repository URL** below, convert this document to PDF or DOCX per course instructions, and **append screenshots** of sample runs and representative code windows.

**Repository:** [Paste your GitHub URL here, e.g., `https://github.com/yourusername/your-repo`]

---

## Abstract

This report describes a small data-processing system implemented in Java and Go in which multiple workers consume tasks from a shared queue, simulate computational work, and persist results to a shared file. The discussion focuses on synchronization choices, shutdown behavior, and exception or error handling in each language.

---

## Java: Concurrency and Exceptions

The Java implementation (`dps` package) uses a **producer–consumer** design. A **`ReentrantLock`** with **`Condition`** backs `SharedTaskQueue`, exposing **`addTask`** and **`getTask`** so that the deque is never modified concurrently without holding the lock, and consumers **await** when the queue is empty instead of busy-waiting (avoiding races and reducing deadlock risk from ad hoc polling).

Workers are **`ExecutorService`** tasks (`WorkerTask` implements `Callable<Void>`). Each worker loops: **`getTask`**, distinguishes normal **`Task`** instances from **`ShutdownSignal`** sentinels, runs **`processTask`** ( **`Thread.sleep`** simulates work), and records outcomes in **`ResultsCollector`**, which guards an in-memory list with another **`ReentrantLock`**. File lines are appended with **`try-with-resources`** and **`BufferedWriter`**; **`IOException`** is caught, logged with **`java.util.logging`**, and wrapped where appropriate so callers can continue other work.

**`InterruptedException`** is handled where threads block: queue wait and sleep set the interrupt flag again and log a warning so shutdown remains observable. After enqueueing all tasks and shutdown signals, the main thread calls **`executor.shutdown`** and **`awaitTermination`**, with **`shutdownNow`** if the pool does not finish in time, so threads terminate without hanging indefinitely.

---

## Go: Concurrency and Errors

The Go implementation models the shared queue as a **buffered channel** of **`Task`**. The producer goroutine sends tasks and **`close`s** the channel when finished; workers **`range`** over the channel, which is idiomatic Go for **clean termination** without extra sentinels. **`sync.WaitGroup`** counts workers so **`main`** waits until all goroutines exit.

Shared file output uses a small type with **`sync.Mutex`** and **`defer`** to **`Unlock`** after each append; **`os.OpenFile`** / **`WriteString`** errors are returned and logged at the call site rather than thrown. Processing errors are **explicit return values** from **`processTask`**, logged, and written as error lines to the results file. This matches Go’s convention of **`if err != nil`** after operations that can fail, with **`defer`** used for predictable cleanup (e.g., **`Close`**).

---

## Comparison of Concurrency Models

Java’s model is **thread-based**: preemptively scheduled OS threads share mutable state protected by locks and executor frameworks. Coordination uses **locks, conditions, and blocking queues**; failure often appears as **checked exceptions** (e.g., **`InterruptedException`**, **`IOException`**) handled in **`try-catch`** blocks.

Go emphasizes **goroutines** and **channels** as the primary means of structuring concurrency: communication synchronizes memory, and **closing a channel** can broadcast completion to many receivers. Errors are ordinary **return values**, encouraging explicit propagation and logging at each layer. Both designs avoid redundant consumption of tasks (each task is taken exactly once) and use structured shutdown so workers do not block forever after work is complete.

---

## References

Oracle America, Inc. (n.d.). *Java Platform, Standard Edition documentation*. https://docs.oracle.com/en/java/

The Go Authors. (n.d.). *The Go programming language specification*. https://go.dev/ref/spec

---

## Appendix (screenshots)

*[Insert screenshots here: (1) Java compile/run terminal output, (2) Go run output, (3) snippet of `SharedTaskQueue.java`, (4) snippet of `main.go` worker/channel setup.]*
