#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export JAVA_HOME="$DIR/jdk/Contents/Home"
"$JAVA_HOME/bin/java" --add-opens java.base/java.util=ALL-UNNAMED -Dassets.dir=assets -jar "$DIR/RemixedDungeon.jar"