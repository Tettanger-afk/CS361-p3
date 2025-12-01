# Project #3: Turing Machine Simulator

- **Author:** Alexander James Luis Huett, Caleb Wells
- **Class:** CS361
- **Semester:** Fall 2025

## Overview

This repository contains a Java Turing Machine (TM) simulator. The simulator
parses TM definition files (plain text) and executes the machine until it
reaches a halting state. Outputs include the visited tape contents, output
length, sum of symbols, and elapsed time.

## Reflection

I decided to try vibe coding for a change. I have to admit I was pleasantly/unpleasantly suprised to see that not only was it very efficient, but also really fast. As long as you know what you are doing i.e. version control and incremental requests with copilot. 

IIt basically filled out everything involing TM and modified my shellscripts completely. Though it took it quite a while to update the code to what I had wanted and introduce  methods, and when that was done I requested for cache memory for even faster computation from ~35 seconds to ~10.

At first it was running with relatively quickly because I had planned ahead and thought Hash maps would be rather quick. When I asked it for something even faster it provided trasition tables which decreased the time to computation by a tenth. Test5 was running from ~10 seconds to ~1 second.

Overall I think that you have to be very careful and use version control a lot. One of the requests I made was for even more efficiency was Ring Buffer, which is suppose to solve the issue with the tape where it has to expand(expand it both direction). When it finished modifying TM it said there was a corrupted file and had to restore to one of the commits I had made. Without that I would have lost all of my code. It can be really consuming to just keep making requests to Copilot, but you should definetly practice good version control so you don't end up burning all of that work. If you have something working, commit it to the main/branch and then start making more changes. 


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

The simplest way to run the `TMTest` unit tests is with Maven:

```bash
mvn test
```

If you don't have Maven, you can download the JUnit Platform Console standalone
JAR directly from Maven Central and run it manually. Example (uses `curl`):

```bash
curl -L -o junit-platform-console-standalone-1.9.3.jar \
   https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.9.3/junit-platform-console-standalone-1.9.3.jar
java -jar junit-platform-console-standalone-1.9.3.jar --class-path . --scan-classpath
```

Alternatively import the project into your IDE and run `TMTest` from there.

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

# Sources Used

- Copilot was used for everything
----------
