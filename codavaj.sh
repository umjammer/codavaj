#!/bin/bash
CODAVAJ_HOME=$(pwd)
CP=$(/usr/local/bin/classpath $CODAVAJ_HOME/codavaj-*.jar $CODAVAJ_HOME/lib/*.jar)
java -classpath $CP org.codavaj.Main $@
