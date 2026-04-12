// Package main implements a concurrent data processing pipeline using goroutines and channels.
package main

import (
	"fmt"
	"log"
	"math/rand"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"sync"
	"time"
)

// Task represents a unit of work.
type Task struct {
	ID      int
	Payload string
}

func (t Task) String() string {
	return fmt.Sprintf("Task{id=%d, payload=%q}", t.ID, t.Payload)
}

// resultWriter appends lines to a file; a mutex serializes concurrent writes from workers.
type resultWriter struct {
	mu   sync.Mutex
	path string
}

func newResultWriter(path string) *resultWriter {
	return &resultWriter{path: path}
}

func (a *resultWriter) appendLine(line string) error {
	a.mu.Lock()
	defer a.mu.Unlock()

	f, err := os.OpenFile(a.path, os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0o644)
	if err != nil {
		return fmt.Errorf("open result file: %w", err)
	}
	defer func() {
		if cerr := f.Close(); cerr != nil {
			log.Printf("warning: closing result file: %v", cerr)
		}
	}()

	if _, err := f.WriteString(line + "\n"); err != nil {
		return fmt.Errorf("write result line: %w", err)
	}
	return nil
}

func processTask(workerName string, t Task) error {
	ms := 50 + rand.Intn(120)
	time.Sleep(time.Duration(ms) * time.Millisecond)
	if strings.Contains(t.Payload, "FAIL") {
		return fmt.Errorf("simulated processing failure for payload")
	}
	return nil
}

func worker(id int, jobs <-chan Task, results *resultWriter, wg *sync.WaitGroup) {
	defer wg.Done()
	name := fmt.Sprintf("Worker-%d", id)
	log.Printf("goroutine started: %s", name)

	for t := range jobs {
		if err := processTask(name, t); err != nil {
			log.Printf("ERROR %s processing %s: %v", name, t, err)
			line := fmt.Sprintf("%s processed Task{id=%d} -> ERROR: %s", name, t.ID, err.Error())
			if werr := results.appendLine(line); werr != nil {
				log.Printf("ERROR %s writing result file: %v", name, werr)
			}
			continue
		}
		line := fmt.Sprintf("%s processed %s -> OK", name, t)
		if err := results.appendLine(line); err != nil {
			log.Printf("ERROR %s writing result file: %v", name, err)
		}
	}

	log.Printf("goroutine completed: %s", name)
}

func main() {
	log.SetFlags(log.LstdFlags | log.Lmicroseconds)

	workerCount := 4
	taskCount := 20
	outPath := filepath.Join("..", "output", "go_results.txt")

	if len(os.Args) > 1 {
		if v, err := strconv.Atoi(os.Args[1]); err == nil && v > 0 {
			workerCount = v
		}
	}
	if len(os.Args) > 2 {
		if v, err := strconv.Atoi(os.Args[2]); err == nil && v > 0 {
			taskCount = v
		}
	}
	if len(os.Args) > 3 {
		outPath = os.Args[3]
	}

	if err := os.MkdirAll(filepath.Dir(outPath), 0o755); err != nil {
		log.Fatalf("create output directory: %v", err)
	}

	// Fresh file for this run (truncate); per-line appends use O_APPEND inside appendLine.
	if f, err := os.Create(outPath); err != nil {
		log.Fatalf("create output file: %v", err)
	} else {
		if cerr := f.Close(); cerr != nil {
			log.Printf("warning: close new file: %v", cerr)
		}
	}

	log.Printf("Starting Data Processing System (Go): workers=%d tasks=%d output=%s", workerCount, taskCount, outPath)

	jobs := make(chan Task, workerCount*2)
	results := newResultWriter(outPath)

	var wg sync.WaitGroup
	for w := 0; w < workerCount; w++ {
		wg.Add(1)
		go worker(w, jobs, results, &wg)
	}

	// Producer: enqueue tasks; closing the channel signals workers to exit (no deadlock).
	go func() {
		defer func() {
			close(jobs)
		}()
		for i := 0; i < taskCount; i++ {
			payload := fmt.Sprintf("chunk-%d", i)
			if i%7 == 0 {
				payload = fmt.Sprintf("chunk-FAIL-%d", i)
			}
			select {
			case jobs <- Task{ID: i, Payload: payload}:
			case <-time.After(30 * time.Second):
				log.Printf("ERROR: timed out sending task %d (workers may be stuck)", i)
				return
			}
		}
	}()

	wg.Wait()
	log.Printf("All workers finished. Results file: %s", outPath)
}
