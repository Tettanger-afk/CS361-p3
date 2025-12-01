# Project #3: Turing Machine Simulator

- **Author:** Alexander James Luis Huett, Caleb Wells
- **Class:** CS361
- **Semester:** Fall 2025

## Overview

This repository contains a Java Turing Machine (TM) simulator. The simulator
parses TM definition files (plain text) and executes the machine until it
reaches a halting state. Outputs include the visited tape contents, output
length, sum of symbols, and elapsed time.

## Quick start

Compile and run the simulator (manual):

```bash
javac -d . tm/*.java *.java
java -cp . tm.TMSimulator input/file0.txt
```

Run the provided test scripts (they compile, run the simulator and compare
against expected outputs). Example:

```bash
./run-test0.sh
./run-test2.sh
./run-test5.sh
./run-all-tests.sh
```

Clean previous outputs before running (scripts call `CleanUp.sh` automatically).

## Running JUnit tests

Two options are provided to run the `TMTest` unit tests:

- With Maven (recommended if you have Maven installed):

```bash
mvn test
```

- Without Maven: download the JUnit standalone console and run it (helper provided):

```bash
./get-junit.sh
java -jar lib/junit-platform-console-standalone-1.9.3.jar --class-path . --scan-classpath
```

See `RUNNING_TESTS.md` for more detail.

## Input file format

1. Line 1: number of states `n` (state `0` is start, state `n-1` is halt)
2. Line 2: number of input symbols `m` (tape alphabet is `{0,1,...,m}`)
3. Next `(m+1)*(n-1)` lines: transitions for states `0..n-2`, each line
   in the form: `next_state,write_symbol,direction` (direction is `L` or `R`)
4. Optional final line: input string (digits). If missing, unary default is used
   for unary machines (when `m == 1`), otherwise empty input is assumed.

## Helpful scripts

- `run-test0.sh`, `run-test2.sh`, `run-test5.sh` — run individual example tests.
- `run-all-tests.sh` — runs all three example tests and reports summary.
- `CleanUp.sh` — clears the `output/` directory (scripts call this automatically).
- `get-junit.sh` — downloads the JUnit console jar into `lib/` for running tests
  without Maven.

## Performance notes (discussion)

The current implementation uses a sparse `Map`-backed tape and per-state maps
for transitions. For faster execution consider:

- building a packed transition table (`int[]`) for O(1) state/symbol lookup;
- replacing the tape `Map` with an array-backed tape (or ring buffer) for
  lower-latency tape accesses when tape use is dense;
- using primitive collections or tuning initial capacities to reduce GC/boxing.

If you want, I can implement the packed transition table and incremental
metadata (sum/min/max) as a low-risk, high-impact speedup.
