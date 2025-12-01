#!/bin/bash
set -euo pipefail
trap 'find . -type f -name "*.class" -exec rm -f {} + || true' EXIT

echo "Compiling..."
# Ensure previous outputs are removed before running
if [ -f "./CleanUp.sh" ]; then
    bash ./CleanUp.sh
fi
# compile sources but skip TMTest.java (JUnit not required for simulator run)
JAVA_PROJECT_FILES=$(ls *.java 2>/dev/null | grep -v TMTest.java || true)
javac -d . tm/*.java $JAVA_PROJECT_FILES || { echo "Compilation failed" >&2; exit 1; }

mkdir -p output

F="input/file2.txt"
if [ ! -f "$F" ]; then
    echo "Input file '$F' not found." >&2
    exit 1
fi

echo "Running simulator on $F"
B=$(basename "$F")
OUT="output/${B}.out"
java -cp . tm.TMSimulator "$F" > "$OUT.tmp" 2>&1 || true
# print timing line (so user sees elapsed time in terminal)
grep '^elapsed (s):' "$OUT.tmp" || true
# remove timing line so canonical comparison remains unchanged
grep -v '^elapsed (s):' "$OUT.tmp" > "$OUT" || true
rm -f "$OUT.tmp"

B=$(basename "$F")
EXPECTED="expected/${B}.out"
if [ ! -f "$EXPECTED" ]; then
    echo "Expected file '$EXPECTED' not found. Captured output at $OUT"
    echo "If this output is correct, promote it with: mv $OUT $EXPECTED"
    exit 2
fi

DIFF="output/${B}.diff"
if diff -u -B "$EXPECTED" "$OUT" > "$DIFF"; then
    echo "${F}: OK (matches expected)"
    rm -f "$DIFF"
    exit 0
else
    echo "${F}: MISMATCH - see $DIFF for details"
    exit 1
fi
