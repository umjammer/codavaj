[![](https://jitpack.io/v/umjammer/codavaj.svg)](https://jitpack.io/#umjammer/codavaj)

# CODAVAJ - ( javadoc in reverse ) README

## Convert Javadoc to Java Source

to convert javadoc tree into java source (external references to Sun's 
standard javadocs are automatically resolved 
i.e. `http://java.sun.com/j2se/1.5.0/docs/api/`)

```
codavaj.cmd codavaj <javadoc-dir> <javasource-dir> {<external-link-url>}*
```

i.e.

```
codavaj.cmd codavaj tmp/jumpi/javadoc tmp/jumpi/src
```

or

```
codavaj.cmd codavaj tmp/jumpi/javadoc tmp/jumpi/src http://external.link.com/api
```

or

```
codavaj.cmd codavaj http://jumpi.sourceforge.net/javadoc/j2se tmp/jumpi/src
```

## Cooperate with [Javaparser](https://github.com/javaparser/javaparser)

 * set javadoc to decompiled source.


## Download (obsoleted, use convert)

to download an entire javadoc tree for further processing use:

```
codavaj.cmd wget <URL> <destination-dir>
```

i.e.

```
codavaj.cmd wget http://jumpi.sourceforge.net/javadoc/j2se tmp/jumpi/javadoc
```

## change history

version 1.4.x

 * update v6 parsing and rendering
 * enable to parse javadoc on www directly

version 1.4.0

 * java8 support
 * java11 support
 * java12 support
 * java13 support
 * i18n support
 * add unit test

version 1.3.0

 * java1.5 support for Enums and Generics
 * java1.6 support for Annotations

version 1.2.0

 * java1.5 support (except enum and generics)

version 1.1.0

 * resolve type names to externally linked javadocs. Links to Sun's
   reference apis are resolved automatically 
   ( i.e. http://java.sun.com/j2se/X/docs/api/ ). 
   Any other links will need to be given as extra parameters for the 
   codavaj command.
 * significant extentions to the reflection-like API to represent Packages
   and link Types to their respective Packages and back, and also represent
   package heirarchy.
 * fix wrong determination of a class as Interface if "Interface" part of
   classname.

initial version 1.0.0

## known issues

codavaj does not introduce default constructor's if they weren't found
in the javadoc. This leads to compile problems if there are subclasses
which use the class's default constructor through the implicit super(). 

## TODO

 * ~~javadoc 1.8~~
 * javadoc html5
 * ~~javadoc 11~~
 * ~~en test case~~