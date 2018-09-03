#!/bin/sh
rm -rf fifo
mkdir fifo
max=10
if [ -n "$1" ]; then 
	max=$1
fi
max=$((max * 2))
for (( i = 0; i < $max; i++)); do mkfifo fifo/$i; done
