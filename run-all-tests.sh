#!/bin/bash
# Run file0, file2, and file5 test scripts and summarize results.
set -uo pipefail

SCRIPTS=("./run-test0.sh" "./run-test2.sh" "./run-test5.sh")
FAILED=0

for s in "${SCRIPTS[@]}"; do
  if [ ! -x "$s" ]; then
    echo "Warning: $s not found or not executable; attempting to run anyway"
  fi
  echo "============================================================"
  echo "Running: $s"
  # run the script; allow it to fail but continue
  if "$s"; then
    echo "Result: PASS"
  else
    rc=$?
    echo "Result: FAIL (exit code $rc)"
    FAILED=$((FAILED+1))
  fi
  echo
done

echo "============================================================"
if [ "$FAILED" -eq 0 ]; then
  echo "All tests passed"
  exit 0
else
  echo "$FAILED test(s) failed"
  exit 1
fi
