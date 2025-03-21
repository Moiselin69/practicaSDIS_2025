#!/bin/bash
mkdir -p out
# Compilar el proyecto
javac -d out -cp "libs/*" $(find src -name "*.java")
