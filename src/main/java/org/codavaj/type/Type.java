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

import java.util.ArrayList;
import java.util.List;


/**
 * A reflection-like representation of java.lang.Class. 
 */
public class Type extends Modifiable {
    private String superType = null;
    private List<String> implementsList = new ArrayList<>();
    private List<Method> methodList = new ArrayList<>();
    private List<Field> fieldList = new ArrayList<>();
    private List<EnumConst> enumConstList = new ArrayList<>();
    private List<Method> constructorList = new ArrayList<>();
    private List<?> comment = null;
    private List<Type> innerTypeList = new ArrayList<>();
    private String typeName;
    private String typeParameters; // generics
    private Package pckg;
    
    /**
     * Creates a new Type object.
     */
    public Type() {
    }

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
            int idx = getTypeName().indexOf(getEnclosingType());
            if ( idx != -1 ) {
                return getTypeName().substring(idx + getEnclosingType().length() + 1);
            }
        } else {
            if (getTypeName() != null) {
                if (getTypeName().lastIndexOf(".") != -1) {
                    return getTypeName().substring(getTypeName().lastIndexOf(".")
                        + 1, getTypeName().length());
                } else {
                    return getTypeName();
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
    String getPackageName() {
        if ((getTypeName() != null) && (getTypeName().indexOf(".") != -1)) {
            return getTypeName().substring(0, getTypeName().lastIndexOf("."));
        }

        return "";
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
        if (getTypeName().indexOf("$") != -1) {
            // it is an inner class
            return getTypeName().substring(0, getTypeName().lastIndexOf("$"));
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
            // interface methods are automatically abstract - and javadoc doesnt
            // have this info
            method.setAbstract(true);
        }

        methodList.add(method);

        return method;
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
    public List<?> getComment() {
        return comment;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeComment DOCUMENT ME!
     */
    public void setComment(List<?> typeComment) {
        this.comment = typeComment;
    }

    /**
     * Add the name of an inner type to the instance.
     *
     * @param type the inner type
     */
    public void addInnerType(Type type) {
        getInnerTypeList().add(type);
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Field lookupFieldByName(String name) {
        for (int i = 0;
                (getFieldList() != null) && (i < getFieldList().size()); i++) {
            Field f = getFieldList().get(i);

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
        for (int i = 0;
                (getEnumConstList() != null) && (i < getEnumConstList().size()); i++) {
            EnumConst ec = getEnumConstList().get(i);

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
    public Method lookupConstructor(List<?> params) {
        for (int i = 0;
                (getConstructorList() != null)
                && (i < getConstructorList().size()); i++) {
            Method m = getConstructorList().get(i);

            if (m.getParameterList().size() != params.size()) {
                continue;
            }

            boolean matchedAll = true;

            for (int j = 0; j < params.size(); j++) {
                Parameter p1 = m.getParameterList().get(j);
                Parameter p2 = (Parameter) params.get(j);

                if (!p1.getType().equals(p2.getType())) {
                    matchedAll = false;

                    break;
                }
            }

            if (matchedAll) {
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
    public Method lookupMethodByName(String name, List<?> params) {
        for (int i = 0;
                (getMethodList() != null) && (i < getMethodList().size());
                i++) {
            Method m = getMethodList().get(i);

            if (name.equals(m.getName())) {
                if (m.getParameterList().size() != params.size()) {
                    continue;
                }

                boolean matchedAll = true;

                for (int j = 0; j < params.size(); j++) {
                    Parameter p1 = m.getParameterList().get(j);
                    Parameter p2 = (Parameter) params.get(j);

                    if (!p1.getType().equals(p2.getType())) {
                        matchedAll = false;

                        break;
                    }
                }

                if (matchedAll) {
                    return m;
                }
            }
        }

        return null;
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
	 * @return the typeParameters
	 */
	public String getTypeParameters() {
		return typeParameters;
	}

	/**
	 * @param typeParameters the typeParameters to set
	 */
	public void setTypeParameters(String typeParameters) {
		this.typeParameters = typeParameters;
	}
}
