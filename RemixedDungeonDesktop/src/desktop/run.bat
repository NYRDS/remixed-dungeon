@echo off
setlocal
set DIR=%~dp0
set JAVA_HOME=%DIR%jdk
rem default heap on big-RAM boxes plateaus the desktop build at ~630MB RSS
"%JAVA_HOME%\bin\java" -Xms64m -Xmx512m -XX:+UseSerialGC --add-opens java.base/java.util=ALL-UNNAMED -Dassets.dir=assets -jar "%DIR%RemixedDungeon.jar"
