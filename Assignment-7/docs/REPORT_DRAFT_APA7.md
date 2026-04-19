# Multi-Paradigm Problem Solving: Mean, Median, and Mode

Student Name  
Course Name  
Instructor Name  
Date

## Introduction

This assignment compares how three programming paradigms solve the same problem: computing mean, median, and mode for a list of integers. The implementations were completed in C (procedural), OCaml (functional), and Python (object-oriented). The objective was to evaluate differences in program structure, state management, and expressiveness while producing equivalent output across languages.

## Implementation Overview

The same input dataset was used in all implementations (`tests/sample_input.txt`) to ensure fair comparison. Each solution reads integers from a file and reports the same three statistics.

In the C version, the solution was written with procedural decomposition using functions (`calculate_mean`, `calculate_median`, and `calculate_mode`). Array memory was managed manually with `malloc`/`realloc`/`free`, and sorting was implemented through `qsort`. This style provided close control over data representation and runtime behavior but required additional code for safety and allocation checks.

In the OCaml version, the design emphasized immutable data and higher-order list processing. Aggregate logic used `List.fold_left`, and mode detection was expressed as transformations over sorted lists and frequency tuples. The functional approach reduced side effects and made data flow explicit, although list indexing and recursive run-count logic required careful pattern design.

In the Python version, an object-oriented design was used through a `StatisticsCalculator` class. Each statistic is encapsulated as a method (`mean`, `median`, `mode`), with input validation in the constructor. Built-in structures (`list`, `Counter`) enabled concise implementation. This approach offered strong readability and straightforward extensibility, especially for adding new statistics later.

## Comparative Analysis

The procedural C implementation highlighted control and performance-oriented design but introduced the highest complexity in memory handling and edge-case management. OCaml encouraged declarative thinking and immutability, which improved predictability but required comfort with recursive and pipeline-based style. Python provided the shortest and most maintainable implementation for this assignment due to expressive standard library support and direct OOP modeling.

A key difference across paradigms was how state was represented and transformed. C used mutable arrays and explicit loops; OCaml emphasized pure transformations and recursion; Python encapsulated behavior in a reusable object with minimal boilerplate. These differences directly affected development speed, readability, and debugging effort.

## Challenges Encountered

The primary challenge in C was dynamic memory management and ensuring no leaks on early returns. In OCaml, constructing mode logic without mutable counters required additional recursive structure. In Python, the technical challenge was minimal, but careful method design was still needed to keep behavior consistent with the C and OCaml versions.

## Conclusion

All three paradigms successfully solved the required statistics problem and produced matching results. The assignment demonstrated that paradigm choice significantly influences implementation strategy, code volume, and maintainability. For rapid development and clarity, Python OOP was most efficient; for formal functional reasoning, OCaml was strongest; for low-level control, C remained the most explicit but most verbose.

## Reference

American Psychological Association. (2020). *Publication manual of the American Psychological Association* (7th ed.).

## GitHub Repository Link

Replace with your repo URL before submission:

`https://github.com/<your-username>/<your-repo>`

## Appendix Placeholder (Screenshots)

Append screenshot pages in the final DOCX/PDF for:

- C code + C output
- OCaml code + OCaml output
- Python code + Python output
