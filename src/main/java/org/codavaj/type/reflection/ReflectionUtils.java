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

package org.codavaj.type.reflection;

import org.codavaj.type.EnumConst;
import org.codavaj.type.Field;
import org.codavaj.type.Method;
import org.codavaj.type.Modifiable;
import org.codavaj.type.Parameter;
import org.codavaj.type.Type;

/**
 * DOCUMENT ME!
 */
public class ReflectionUtils {

    /**
     * DOCUMENT ME!
     *
     * @param clazz DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Type getType(Class<?> clazz) {
        Type t = new Type();
        t.setTypeName(getTypeName(clazz.getName()));
        setModifiers(t, clazz.getModifiers());

        if ( clazz.isAnnotation() ) {
            t.setInterface(false);
            t.setAnnotation(true);
        }

        if (clazz.getSuperclass() != null) {
            t.setSuperType(getTypeName(clazz.getSuperclass().getName()));

            if ( "java.lang.Enum".equals(t.getSuperType())) {
                t.setEnum(true);
            }
        }

        Class<?>[] implementsList = clazz.getInterfaces();

        for (int i = 0; (implementsList != null) && (i < implementsList.length); i++) {
            t.addImplementsType(getTypeName(implementsList[i].getName()));
        }

        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();

        for (int i = 0; (fields != null) && (i < fields.length); i++) {
            if ( t.isEnum() && t.getTypeName().equals(fields[i].getType().getName())){
                getEnum(t, fields[i]);
            } else {
                getField(t, fields[i]);
            }
        }

        java.lang.reflect.Method[] methods = clazz.getDeclaredMethods();

        for (int i = 0; (methods != null) && (i < methods.length); i++) {
            getMethod(t, methods[i]);
        }

        java.lang.reflect.Constructor<?>[] constructors = clazz
            .getDeclaredConstructors();

        for (int i = 0; (constructors != null) && (i < constructors.length);
                i++) {
            getConstructor(t, constructors[i]);
        }

        Class<?>[] innerclasses = clazz.getDeclaredClasses();

        for (int i = 0; (innerclasses != null) && (i < innerclasses.length);
                i++) {
            t.addInnerType(getType(innerclasses[i]));
        }

        return t;
    }

    private static void getEnum(Type t, java.lang.reflect.Field reflectField) {
        EnumConst f = t.createEnumConst();

        f.setName(reflectField.getName());
    }

    private static void getField(Type t, java.lang.reflect.Field reflectField) {
        Field f = t.createField();

        f.setName(reflectField.getName());
        setModifiers(f, reflectField.getModifiers());

        Class<?> returnType = reflectField.getType();

        if (isArray(returnType.getName())) {
            f.setArray(true);
            f.setDegree(arrayDegreeFromClassname(returnType.getName()));
        }

        f.setType(getTypeName(returnType.getName()));

        if (f.isStatic() && f.isPublic() && f.isFinal()) {
            try {
                f.setValue(reflectField.get(null));
            } catch (IllegalAccessException iax) {
            }
        }
    }

    private static void getConstructor(Type t,
        java.lang.reflect.Constructor<?> reflectConstructor) {
        Method m = t.createConstructor();

        m.setName(reflectConstructor.getName());
        setModifiers(m, reflectConstructor.getModifiers());
        setMethodParameters(reflectConstructor.getParameterTypes(), m);
        setThrowsList(reflectConstructor.getExceptionTypes(), m);
    }

    private static void getMethod(Type t, java.lang.reflect.Method reflectMethod) {
        Method m = t.createMethod();

        m.setName(reflectMethod.getName());
        setModifiers(m, reflectMethod.getModifiers());
        setMethodParameters(reflectMethod.getParameterTypes(), m);
        setThrowsList(reflectMethod.getExceptionTypes(), m);
        m.setReturnParameter(getParameter(reflectMethod.getReturnType(), "return"));
    }

    /**
     * Return the Parameter represented by the reflection class.
     *
     * @param parameter the reflection parameter
     * @param name the name of the parameter
     *
     * @return the Parameter representing the reflection parameter.
     */
    private static Parameter getParameter(Class<?> parameter, String name) {
        Parameter p = new Parameter();

        if (isArray(parameter.getName())) {
            p.setArray(true);
            p.setDegree(arrayDegreeFromClassname(parameter.getName()));
        }

        p.setType(getTypeName(parameter.getName()));

        // reflection doesn't have the parameter name
        p.setName(name);

        return p;
    }

    /**
     * Sets the throws list of a method according to the reflection throws
     * classes.
     *
     * @param throwslist the reflection throws classes.
     * @param m the method to add the throws classes to.
     */
    private static void setThrowsList(Class<?>[] throwslist, Method m) {
        for (int i = 0; (throwslist != null) && (i < throwslist.length); i++) {
            Class<?> throwsclass = throwslist[i];
            m.addThrows(throwsclass.getName());
        }
    }

    /**
     * Set the method's parameters accorging to the reflection parameters.
     *
     * @param parameters the reflection parameters
     * @param m the method to add the parameters to.
     */
    private static void setMethodParameters(Class<?>[] parameters, Method m) {
        for (int i = 0; (parameters != null) && (i < parameters.length); i++) {
            m.addParameter(getParameter(parameters[i], "p" + i));
        }
    }

    /**
     * Set the Modifier properties according to reflection settings.
     *
     * @param m Modifiable to change
     * @param reflectionModifiers reflection settings
     */
    private static void setModifiers(Modifiable m, int reflectionModifiers) {
        m.setPrivate(java.lang.reflect.Modifier.isPrivate(reflectionModifiers));
        m.setProtected(java.lang.reflect.Modifier.isProtected(reflectionModifiers));
        m.setPublic(java.lang.reflect.Modifier.isPublic(reflectionModifiers));
        m.setAbstract(java.lang.reflect.Modifier.isAbstract(reflectionModifiers));
        m.setInterface(java.lang.reflect.Modifier.isInterface(reflectionModifiers));
        m.setFinal(java.lang.reflect.Modifier.isFinal(reflectionModifiers));
        m.setStatic(java.lang.reflect.Modifier.isStatic(reflectionModifiers));
        m.setStrictFp(java.lang.reflect.Modifier.isStrict(reflectionModifiers));
    }

    /**
     * Return true if the classname represents an array. For instance [[[[[I is
     * true.
     *
     * @param classname Class.getName
     *
     * @return true if the classname represents an array.
     */
    private static boolean isArray(String classname) {
        return classname.contains("[");
    }

    /**
     * Determine the array degree from the classname. For instance
     *
     * @param classname
     *
     * @return
     */
    private static int arrayDegreeFromClassname(String classname) {
        int count = 0;

        for (int i = 0; i < classname.length(); i++) {
            if (classname.charAt(i) == '[') {
                count++;
            }
        }

        return count;
    }

    /**
     * Return the underlying type name from a Class.getName(). Removes array
     * information from the typename.
     *
     * @param classname Class.getName()
     *
     * @return Type.getName()
     */
    private static String getTypeName(String classname) {
        if (!isArray(classname)) {
            return classname;
        }

        String strippedClassname = classname.substring(classname.lastIndexOf("[") + 1);

        switch (strippedClassname.charAt(0)) {
        case 'L':
            return strippedClassname.substring(1, strippedClassname.length() - 1); // cut ; off end
        case 'Z':
            return "boolean";
        case 'B':
            return "byte";
        case 'C':
            return "char";
        case 'D':
            return "double";
        case 'F':
            return "float";
        case 'I':
            return "int";
        case 'J':
            return "long";
        case 'S':
            return "short";
        }

        return classname; // should never happen!
    }
}
