#!/bin/bash
set -euo pipefail
trap 'find . -type f -name "*.class" -exec rm -f {} + || true' EXIT

JAR=lib/junit-platform-console-standalone-1.9.3.jar
if [ ! -f "$JAR" ]; then
  echo "JUnit standalone jar not found at $JAR. Please download it into lib/." >&2
  exit 2
fi

echo "Compiling (including tests)..."
javac -d . -cp "$JAR" tm/*.java *.java

echo "Running JUnit tests (TMTest)..."
java -jar "$JAR" --class-path . --select-class TMTest
