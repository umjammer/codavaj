/*
 *   Copyright 2005 Peter Klauser
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.codavaj.type;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * A reflection-like representation of java.lang.Class.
 */
public class Type extends Modifiable implements Commentable {

    private static final Logger logger = Logger.getLogger(Type.class.getName());

    private String superType = null;
    private List<String> implementsList = new ArrayList<>();
    private List<Method> methodList = new ArrayList<>();
    private List<Field> fieldList = new ArrayList<>();
    private List<EnumConst> enumConstList = new ArrayList<>();
    private List<Method> constructorList = new ArrayList<>();
    private List<String> comment = null;
    private List<Type> innerTypeList = new ArrayList<>();
    private String typeName;
    private String typeParameters; // generics
    private Package pckg;

    /**
     * Get the fully qualified type name - a.b.c.d.E for an interface or class
     * type. Inner classes are identified as having a '$' in thir name -
     * a.b.c.d.E$F .
     *
     * @return the fully qualified type name.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Set the fully qualified type name.
     *
     * @param name the fully qualified type name.
     */
    public void setTypeName(String name) {
        this.typeName = name;
    }

    /**
     * Return the type name without package information, - a.b.c.D becomes D.
     * Inner classes are named without their enclosing type names - a.b.c.D$E
     * gives E.
     *
     * @return the type name without package information
     */
    public String getShortName() {
        if ( getEnclosingType() != null ) {
            // inner classes - their short name is after the $ of the enclosing type
            // name a.b.c.E$F is a.b.c.E and F
            int idx = typeName.indexOf(getEnclosingType());
            if ( idx != -1 ) {
                return typeName.substring(idx + getEnclosingType().length() + 1);
            }
        } else {
            if (typeName != null) {
                if (typeName.lastIndexOf(".") != -1) {
                    return typeName.substring(typeName.lastIndexOf(".")
                        + 1);
                } else {
                    return typeName;
                }
            }
        }

        return null;
    }

    /**
     * Return the package name of a type - a.b.c.d.E$F would be a.b.c.d
     *
     * @return the package name of the type, "" if in default package
     */
    public String getPackageName() {
        if ((typeName != null) && (typeName.contains("."))) {
            return typeName.substring(0, typeName.lastIndexOf("."));
        }

        return "";
    }

    /** */
    public String getSourceFilename() {
        return getPackageName().replace(".", File.separator) + File.separator + getShortName() + ".java";
    }

    /**
     * The type name of the class from which this one is extending. null for
     * interfaces,  or the java.lang.Object type.
     *
     * @return the type name of the class from which this one is extending.
     */
    public String getSuperType() {
        return superType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param extendedType DOCUMENT ME!
     */
    public void setSuperType(String extendedType) {
        this.superType = extendedType;
    }

    /**
     * Inner Types have a '$' character in their names, return the Type name
     * before the '$'.
     *
     * @return the enclosing typename - a.b.D$E returns a.b.D
     */
    public String getEnclosingType() {
        if (typeName.contains("$")) {
            // it is an inner class
            return typeName.substring(0, typeName.lastIndexOf("$"));
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<String> getImplementsList() {
        return implementsList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName DOCUMENT ME!
     */
    public void addImplementsType(String typeName) {
        implementsList.add(typeName);
    }

    /**
     * @param methodSignature without package name, parent class or interface name
     * @return includes a constructor
     */
    public Optional<Method> getMethod(String methodSignature) {
//System.err.println("S: " + methodSignature);
        Optional<Method> result = methodList.stream().filter(f -> f.getSignatureString().equals(methodSignature)).findAny();
        if (result.isPresent()) {
            return result;
        } else {
            return constructorList.stream().filter(f -> f.getSignatureString().equals(methodSignature)).findAny();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<Method> getMethodList() {
        return methodList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Method createMethod() {
        Method method = new Method();

        if (isInterface() || isAnnotation()) {
            // interface methods are automatically abstract - and javadoc doesn't
            // have this info
            method.setAbstract(true);
        }

        methodList.add(method);

        return method;
    }

    /** @return includes an enum constant */
    public Optional<? extends Commentable> getField(String name) {
        Optional<? extends Commentable> result = fieldList.stream().filter(f -> f.getName().equals(name)).findAny();
        if (result.isPresent()) {
            return result;
        } else {
            return enumConstList.stream().filter(f -> f.getName().equals(name)).findAny();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<Field> getFieldList() {
        return fieldList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Field createField() {
        Field field = new Field();
        fieldList.add(field);

        return field;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<EnumConst> getEnumConstList() {
        return enumConstList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public EnumConst createEnumConst() {
        EnumConst enumConst = new EnumConst();
        enumConstList.add(enumConst);

        return enumConst;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<Method> getConstructorList() {
        return constructorList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Method createConstructor() {
        Method method = new Method();
        constructorList.add(method);

        return method;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<String> getComment() {
        return comment;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeComment DOCUMENT ME!
     */
    public void setComment(List<String> typeComment) {
        this.comment = typeComment;
    }

    /**
     * Add the name of an inner type to the instance.
     *
     * @param type the inner type
     * @throws IllegalArgumentException the type already defined
     */
    public void addInnerType(Type type) {
        if (getType(type.typeName).isPresent()) {
            throw new IllegalArgumentException("already defined");
        } else {
            innerTypeList.add(type);
        }
    }

    /**
     * @return unfilled type
     */
    public Type createInnerType() {
        Type innerType = new Type();
        innerTypeList.add(innerType);

        return innerType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Field lookupFieldByName(String name) {
        for (Field f : fieldList) {

            if (name.equals(f.getName())) {
                return f;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public EnumConst lookupEnumConstByName(String name) {
        for (EnumConst ec : enumConstList) {

            if (name.equals(ec.getName())) {
                return ec;
            }
        }

        return null;
    }

    /**
     * Lookup a method by name and matching types of parameters in order.
     *
     * @param params the ordered list of Parameters ( only the Type matters ).
     *
     * @return a method if found, otherwise null.
     */
    public Method lookupConstructor(List<Parameter> params) {
        for (Method m : constructorList) {

            if (m.matchesParams(params)) {
                return m;
            }
        }

        return null;
    }

    /**
     * Lookup a method by name and matching types of parameters in order.
     *
     * @param name the name of the method
     * @param params the ordered list of Parameters ( only the Type matters ).
     *
     * @return a method if found, otherwise null.
     */
    public Method lookupMethodByName(String name, List<Parameter> params) {
logger.finer(methodList.toString());
        for (Method m : methodList) {

            if (name.equals(m.getName())) {

                if (m.matchesParams(params)) {
                    return m;
                }
            }
        }

        return null;
    }

    /**
     * @param name short name
     * @return includes self
     */
    public Optional<Type> getType(String name) {
        if (name.equals(getShortName())) {
            return Optional.of(this);
        } else {
            return innerTypeList.stream().filter(t -> name.equals(t.getShortName())).findAny();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<Type> getInnerTypeList() {
        return innerTypeList;
    }

    public Package getPackage() {
        return pckg;
    }

    void setPackage(Package pckg) {
        this.pckg = pckg;
    }

    /**
     * @return the typeParameters ("<>" included)
     */
    public String getTypeParameters() {
        return typeParameters;
    }

    /** */
    public boolean isTypeParameter(String typeParameter) {
        return typeParameters == null ? false : Arrays.stream(typeParameters.split("[\\s,<>]")).anyMatch(typeParameter::equals);
    }

    /**
     * @param typeParameters the typeParameters to set
     */
    public void setTypeParameters(String typeParameters) {
        this.typeParameters = typeParameters;
    }

    /** class */
    public static final String LABEL_CLASS = "class";
    /** interface */
    public static final String LABEL_INTERFACE = "interface";
    /** enum */
    public static final String LABEL_ENUM = "enum";

    /**
     * @return java key word of "class", "interface", "enum" or "annotation".
     */
    public String getLabelString() {
        if (isEnum()) {
            return LABEL_ENUM;
        } else if (isAnnotation()) {
            return "@" + LABEL_INTERFACE;
        } else if (isInterface()) {
            return LABEL_INTERFACE;
        } else {
            return LABEL_CLASS;
        }
    }

    /**
     * TODO location
     * @param type with []
     */
    public static String getSignatureString(String type) {
        int degree = 0;
        while (type.indexOf("[]") > 0) {
            degree ++;
            type = type.substring(0, type.indexOf("[]"));
        }

        return getSignatureString(type, degree);
    }

    /** TODO location */
    public static String getSignatureString(String type, int arrayDegree) {
        String result;
        switch (type) {
        case "boolean":
            result = "Z"; break;
        case "byte":
            result = "B"; break;
        case "char":
            result = "C"; break;
        case "double":
            result = "D"; break;
        case "float":
            result = "F"; break;
        case "int":
            result = "I"; break;
        case "long":
            result = "J"; break;
        case "short":
            result = "S"; break;
        case "void":
            result = "V"; break;
        default:
            return getPrefix(arrayDegree, "L") + type.replace(".", "/").replace("$", "/") + ";";
        }
        return getPrefix(arrayDegree, "") + result;
    }

    /** */
    private static String getPrefix(int degree, String prefix) {
        if (degree > 0) {
            return prefix + "[".repeat(degree);
        } else {
            return prefix;
        }
    }
}
