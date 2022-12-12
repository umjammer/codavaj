[![Releases](https://jitpack.io/v/umjammer/codavaj.svg)](https://jitpack.io/#umjammer/codavaj)
[![Actions Status](https://github.com/umjammer/codavaj/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/codavaj/actions)
[![CodeQL](https://github.com/umjammer/codavaj/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/codavaj/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-8-b07219)

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

## Cooperate with Java Parser

| **parser** | **set javadoc to decompiled source** | **rename argument names as javadoc documented** | **code** |
|:-----------|:------------------------------------:|:-----------------------------------------------:|----------|
| [JavaParser](https://github.com/javaparser/javaparser) | ✅ | 🚫 | [📄](https://github.com/umjammer/codavaj/blob/master/src/test/java/Test02.java) |
| [rewrite](https://github.com/Netflix-Skunkworks/rewrite) | ✅ | 🚧 | [📄](https://github.com/umjammer/codavaj/blob/master/src/test/java/Test03.java) |
| [JDT](https://www.eclipse.org/jdt/) | ✅ | 🚫 | [📄](https://github.com/umjammer/codavaj/blob/master/src/test/java/Test04.java) |
| [spoon](https://github.com/INRIA/spoon) | ✅ | 🚫 | [📄](https://github.com/umjammer/codavaj/blob/master/src/test/java/Test05.java) |

## known issues

* codavaj does not introduce default constructor's if they weren't found
in the javadoc. This leads to compile problems if there are subclasses
which use the class's default constructor through the implicit super(). 

* nekohtml ~1.19.22
   * https://mvnrepository.com/artifact/net.sourceforge.nekohtml/nekohtml/1.9.22
      * https://mvnrepository.com/artifact/xerces/xercesImpl/2.11.0
   * but 1.19.22 doesn't work with this project currently
   * so i excluded xerces from dependencies, and add xerces 2.12.2 individually. idk how codeql detect those.
   * https://sourceforge.net/p/nekohtml/bugs/167/#fdcc

## TODO

 * ~~javadoc 1.8~~
 * javadoc html5
 * ~~javadoc 11~~
 * ~~en test case~~
 * https://github.com/HtmlUnit/htmlunit-neko
   * https://github.com/HtmlUnit/htmlunit-neko/security/advisories/GHSA-6jmm-mp6w-4rrg