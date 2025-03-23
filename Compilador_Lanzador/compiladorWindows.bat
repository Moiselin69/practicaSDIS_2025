@echo off
cd ..
mkdir out
javac -d out -cp "libs/*" src/**/*.java
echo Compilación finalizada.
