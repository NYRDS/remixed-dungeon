@echo off
setlocal
set DIR=%~dp0
set JAVA_HOME=%DIR%jdk
"%JAVA_HOME%\bin\java" --add-opens java.base/java.util=ALL-UNNAMED -Dassets.dir=assets -jar "%DIR%RemixedDungeon.jar"