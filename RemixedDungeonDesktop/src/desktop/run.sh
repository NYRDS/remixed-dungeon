#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export JAVA_HOME="$DIR/jdk"
# default heap on big-RAM boxes plateaus the desktop build at ~630MB RSS (G1
# churn + glibc arenas); 512m covers the measured 196MB decode-storm peak
export MALLOC_ARENA_MAX=2 MALLOC_TRIM_THRESHOLD_=262144 MALLOC_MMAP_THRESHOLD_=65536
"$JAVA_HOME/bin/java" -Xms64m -Xmx512m -XX:+UseSerialGC --add-opens java.base/java.util=ALL-UNNAMED -Dassets.dir=assets -jar "$DIR/RemixedDungeon.jar"
