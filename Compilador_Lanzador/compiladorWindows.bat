@echo off
cd ..
mkdir out
for /R src %%f in (*.java) do javac -d out -cp "libs/*;src" %%f
echo Compilación finalizada.
