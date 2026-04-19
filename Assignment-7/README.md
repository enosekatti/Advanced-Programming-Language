# Assignment 7: Multi-Paradigm Problem Solving

This project implements the same statistics problem in three languages using three paradigms:

- **C**: procedural
- **OCaml**: functional
- **Python**: object-oriented

## Problem

Given a list of integers, calculate:

- Mean
- Median
- Mode (one or more values when tied)

## Project Structure

- `c/` - C implementation
- `ocaml/` - OCaml implementation
- `python/` - Python implementation
- `tests/` - shared test dataset and test runner
- `outputs/` - captured runtime outputs
- `docs/` - report and submission documentation

## Quick Start

From `Assignment-7`, run:

```powershell
powershell -ExecutionPolicy Bypass -File .\tests\run_all_tests.ps1
```

This script attempts to build and execute all three implementations and saves outputs to `outputs/`.

## Notes

- If OCaml is not installed, the OCaml section of the test script will be skipped with a clear message.
- The dataset used for all three implementations is `tests/sample_input.txt`.
