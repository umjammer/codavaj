#!/bin/bash
CODAVAJ_HOME=$HOME/src/000/codavaj-1.3.0
CP=$(/usr/local/bin/classpath $CODAVAJ_HOME/codavaj-*.jar $CODAVAJ_HOME/lib/*.jar)
java -classpath $CP org.codavaj.Main $@
