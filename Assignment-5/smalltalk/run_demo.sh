#!/usr/bin/env bash
# Run the Ride Sharing demo with GNU Smalltalk.
set -e
cd "$(dirname "$0")"
if ! command -v gst >/dev/null 2>&1; then
  echo "GNU Smalltalk (gst) not found. Install e.g.: brew install gnu-smalltalk"
  exit 1
fi
gst -f RideSharing.st
