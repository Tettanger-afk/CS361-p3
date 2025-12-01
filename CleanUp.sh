#!/bin/bash
set -euo pipefail


# CleanUp.sh
# Removes all files and subdirectories inside the `output/` directory
# without removing the `output/` directory itself.

DIR="output"

if [ ! -d "$DIR" ]; then
	echo "Directory '$DIR' not found; nothing to clean." >&2
	exit 0
fi

# ensure directory exists
if [ ! -d "$DIR" ]; then
	echo "Directory '$DIR' not found; nothing to clean." >&2
	exit 0
fi

echo "Cleaning contents of '$DIR'..."
# Remove all entries directly under tmp (files and directories)
find "$DIR" -mindepth 1 -maxdepth 1 -exec rm -rf {} +

echo "Clean complete: contents of '$DIR' removed."

exit 0
