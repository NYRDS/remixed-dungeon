#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export JAVA_HOME="$DIR/jdk/Contents/Home"
# default heap on big-RAM boxes plateaus the desktop build at ~630MB RSS
"$JAVA_HOME/bin/java" -Xms64m -Xmx512m --add-opens java.base/java.util=ALL-UNNAMED -Dassets.dir=assets -jar "$DIR/RemixedDungeon.jar"
