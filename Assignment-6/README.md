# Data Processing System (Java + Go)

Multi-worker pipeline: tasks flow through a shared queue (Java: lock/condition queue; Go: buffered channel), workers simulate compute delay, and results are appended to a text file with synchronized I/O.

## Java

From the `java` folder:

```bash
javac -d out dps/*.java
java -cp out dps.DataProcessingApp [workers] [tasks] [output-file]
```

Defaults: 4 workers, 20 tasks, `output/java_results.txt`.

## Go

From the `go` folder:

```bash
go run . [workers] [tasks] [output-file]
```

Defaults: 4 workers, 20 tasks, `../output/go_results.txt` (relative to the `go` directory).

## Assignment report

See `REPORT.md` for the APA-style write-up. Export it to PDF or Word, insert your GitHub repository URL, and append screenshots of the terminal output and key source files as required by your instructor.
