@echo off
REM setup.bat — Windows
REM Downloads the SQLite JDBC driver, compiles, and runs the project.
REM Double-click this file or run it from Command Prompt.

set JAR=lib\sqlite-jdbc-3.49.1.0.jar
set URL=https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.49.1.0/sqlite-jdbc-3.49.1.0.jar

REM 1. Create lib\ folder if it doesn't exist
if not exist lib mkdir lib

REM 2. Download the JAR only if it isn't already there
if not exist %JAR% (
    echo Downloading SQLite JDBC driver...
    curl -L -o %JAR% %URL%
    echo Download complete.
) else (
    echo SQLite JDBC driver already present, skipping download.
)

REM 3. Compile all Java files
echo Compiling...
javac -cp ".;%JAR%" *.java

REM 4. Run
echo Running...
java -cp ".;%JAR%" Main

pause
