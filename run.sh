#!/bin/bash

JAR_LIB="$PWD/lib/*"
EXE_LIB="$PWD/lib/sigar-bin"

javac -classpath "$JAR_LIB" -d $PWD/classes `find src -name \*.java`
java -Djava.library.path=$EXE_LIB -cp "$PWD/classes:$JAR_LIB" io.aiven.oshi_sigar/Comparator

