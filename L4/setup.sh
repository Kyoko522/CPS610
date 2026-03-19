#!/bin/bash
# setup.sh — Mac/Linux
# Downloads the SQLite JDBC driver, compiles, and runs the project.
# Run with: bash setup.sh

JAR="lib/sqlite-jdbc-3.49.1.0.jar"
URL="https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.49.1.0/sqlite-jdbc-3.49.1.0.jar"

# 1. Create lib/ folder if it doesn't exist
mkdir -p lib

# 2. Download the JAR only if it isn't already there
if [ ! -f "$JAR" ]; then
    echo "Downloading SQLite JDBC driver..."
    curl -L -o "$JAR" "$URL"
    echo "Download complete."
else
    echo "SQLite JDBC driver already present, skipping download."
fi

# 3. Compile all Java files
echo "Compiling..."
javac -cp ".:$JAR" *.java

# 4. Run
echo "Running..."
java -cp ".:$JAR" Main
