#!/bin/bash
JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
mv pom.xml pom.xml.orig
sed -e 's@java8.home.*java8.home@java8.home>'$JAVA_HOME'</java8.home@' pom.xml.orig > pom.xml || mv pom.xml.orig pom.xml
diff pom.xml.orig pom.xml
if [ $? -eq 0 ]; then # no diff
 rm pom.xml.orig
fi
echo "java8.home=$JAVA_HOME" > java8.properties
