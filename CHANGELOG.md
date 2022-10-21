# change history

## version 1.4.x

 * update v6 parsing and rendering
 * enable to parse javadoc on www directly

## version 1.4.0

 * java8 support
 * java11 support
 * java12 support
 * java13 support
 * i18n support
 * add unit test

## version 1.3.0

 * java1.5 support for Enums and Generics
 * java1.6 support for Annotations

## version 1.2.0

 * java1.5 support (except enum and generics)

## version 1.1.0

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

## initial version 1.0.0
