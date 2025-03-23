@echo off
cd ..
mkdir out
for /R src %%f in (*.java) do javac -d out -cp "libs/*" %%f
echo Compilación finalizada.
