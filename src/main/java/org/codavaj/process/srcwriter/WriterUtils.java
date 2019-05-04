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

package org.codavaj.process.srcwriter;

import org.codavaj.type.Field;
import org.codavaj.type.EnumConst;
import org.codavaj.type.Method;
import org.codavaj.type.Modifiable;
import org.codavaj.type.Parameter;
import org.codavaj.type.Type;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import java.util.List;

/**
 * DOCUMENT ME!
 */
public class WriterUtils {

    public static final String LABEL_CLASS = "class";
    public static final String LABEL_INTERFACE = "interface";
    public static final String LABEL_ENUM = "enum";

    private static String LINEFEED = System.getProperty("line.separator");

    /**
     * Formatted representation of the Type.
     *
     * @param t the Type.
     * @return the formatted printed representation of the Type.
     */
    public static String print(Type t) {
        if (t == null) {
            return null;
        }

        StringWriter sw = new StringWriter();

        try {
            print(t, sw);
        } catch (IOException e) {
            sw.write("codavaj - exception printing type " + t.getTypeName());
            e.printStackTrace(new PrintWriter(sw));
        }

        return sw.toString();
    }

    /**
     * Formatted print the Type.
     *
     * @param t the Type.
     * @param w the Writer
     *
     * @throws IOException any IO problem.
     */
    public static void print(Type t, Writer w) throws IOException {
        print(t, w, 0);
    }

    protected static void printModifiers(Modifiable t, Writer w)
        throws IOException {
        if (t.isPublic()) {
            w.write(Modifiable.MODIFIER_PUBLIC);
            w.write(" ");
        } else if (t.isProtected()) {
            w.write(Modifiable.MODIFIER_PROTECTED);
            w.write(" ");
        } else if (t.isPrivate()) {
            w.write(Modifiable.MODIFIER_PRIVATE);
            w.write(" ");
        }

        if (t.isStatic()) {
            w.write(Modifiable.MODIFIER_STATIC);
            w.write(" ");
        }

        if (t.isAbstract()) {
            w.write(Modifiable.MODIFIER_ABSTRACT);
            w.write(" ");
        }

        if (t.isFinal()) {
            w.write(Modifiable.MOFIFIER_FINAL);
            w.write(" ");
        }

        if (t.isStrictFp()) {
            w.write(Modifiable.MODIFIER_STRICTFP);
            w.write(" ");
        }
    }

    protected static void printIndentation(int indentation, Writer w)
        throws IOException {
        for (int i = 0; i < indentation; i++) {
            w.write(" ");
        }
    }

    protected static void printLineFeed(Writer w) throws IOException {
        w.write(LINEFEED);
    }

    protected static void printComment(List<?> commentText, Writer w,
        int indentation) throws IOException {
        if ((commentText == null) || (commentText.size() == 0)) {
            return;
        }

        printIndentation(indentation, w);
        w.write("/**");
        printLineFeed(w);

        for (int i = 0; i < commentText.size(); i++) {
            printIndentation(indentation, w);
            w.write(" * ");
            w.write((String) commentText.get(i));
            printLineFeed(w);
        }

        printIndentation(indentation, w);
        w.write(" */");
        printLineFeed(w);
    }

    protected static void printField(Field f, Writer w, int indentation)
        throws IOException {
        printComment(f.getComment(), w, indentation);
        printIndentation(indentation, w);
        printModifiers(f, w);
        w.write(getSourceTypeName(f.getType()));
        if ( f.getTypeArgumentList() != null ) {
            w.write(getSourceTypeName(f.getTypeArgumentList()));
        }

        for (int degree = 0; f.isArray() && (degree < f.getDegree());
                degree++) {
            w.write("[]");
        }

        w.write(" ");
        w.write(f.getName());

        if (f.getValue() != null) {
            w.write("=");
            print(f.getValue(), w);
        } else if ( f.isFinal() ) {
            // final fields need initializing if they weren't already.
            w.write("=");
            printInitialValue(f.getType(), w);
        }

        w.write(";");
        printLineFeed(w);
    }

    protected static void printEnumConst(EnumConst ec, Writer w, int indentation, boolean isLast)
        throws IOException {
        printComment(ec.getComment(), w, indentation);
        printIndentation(indentation, w);

        w.write(ec.getName());

        w.write(isLast ? ";" : ",");
        printLineFeed(w);
    }

    protected static void printInitialValue(String type, Writer w) throws IOException {
        if ("boolean".equals(type)) {
            w.write("false");
        } else if ("float".equals(type)) {
            w.write("0.0f");
        } else if ("double".equals(type)) {
            w.write("0.0d");
        } else if ("long".equals(type)) {
            w.write("0l");
        } else if ("int".equals(type)) {
            w.write("0");
        } else if ("short".equals(type)) {
            w.write("0");
        } else if ("byte".equals(type)) {
            w.write("0");
        } else if ("char".equals(type)) {
            w.write("(char)0");
        } else {
            w.write("null");
        }
    }

    protected static void print(Object value, Writer w)
        throws IOException {
        if (value instanceof String) {
            w.write("\"" + value + "\"");
        } else if (value instanceof Float) {
            w.write("" + value + "f");
        } else if (value instanceof Double) {
            w.write("" + value + "d");
        } else if (value instanceof Long) {
            w.write("" + value + "l");
        } else if (value instanceof Character) {
            char c = ((Character)value).charValue();
            w.write("(char)" + (int)c );
        } else {
            w.write("" + value);
        }
    }

    protected static void printMethod(Type t, Method m, Writer w,
        boolean isConstructor, int indentation) throws IOException {
        printComment(m.getComment(), w, indentation);
        printIndentation(indentation, w);
        printModifiers(m, w);
        if ( m.getTypeParameters() != null) {
            w.write(getSourceTypeName(m.getTypeParameters()));
            w.write(" ");
        }

        if (!isConstructor) {
            w.write(getSourceTypeName(m.getReturnParameter().getType()));

            for (int degree = 0;
                    m.getReturnParameter().isArray()
                    && (degree < m.getReturnParameter().getDegree());
                    degree++) {
                w.write("[]");
            }

            w.write(" ");
            w.write(m.getName());
        } else {
            w.write(t.getShortName());
        }

        w.write("(");

        for (int pi = 0; pi < m.getParameterList().size(); pi++) {
            Parameter p = m.getParameterList().get(pi);

            if (pi != 0) {
                w.write(", ");
            }

            w.write(getSourceTypeName(p.getType()));
            if ( p.getTypeArgumentList() != null ) {
                w.write(getSourceTypeName(p.getTypeArgumentList()));
            }

            for (int degree = 0; p.isArray() && (degree < p.getDegree());
                    degree++) {
                w.write("[]");
            }

            w.write(" ");
            if ( p.getName() != null ) {
                // jd1.4 java.rmi.activation.ActivationGroup_Stub has some classes
                // without parameter name in the list of parameters for some methods.
                w.write(p.getName());
            }
        }

        w.write(")");

        for (int thr = 0; thr < m.getThrowsList().size(); thr++) {
            String throwsName = m.getThrowsList().get(thr);

            if (thr == 0) {
                w.write(" throws ");
            } else {
                w.write(", ");
            }

            w.write(getSourceTypeName(throwsName));
        }

        if (m.getDefaultValue() != null ) {
            w.write(" default ");
            w.write(m.getDefaultValue());
        }

        if (m.isAbstract()) {
            w.write(";");
            printLineFeed(w);
        } else {
            printMethodContents(m.getReturnParameter(), w, indentation);
        }
    }

    protected static void printMethodContents(Parameter returnParameter,
        Writer w, int indentation) throws IOException {
        String contentText = "";

        if (returnParameter != null) {
            String type = returnParameter.getType();

            if (returnParameter.isArray()) {
                contentText = "return null;";
            } else if ("boolean".equals(type)) {
                contentText = "return false;";
            } else if ("char".equals(type)) {
                contentText = "return ' ';";
            } else if ("short".equals(type)) {
                contentText = "return 0;";
            } else if ("int".equals(type)) {
                contentText = "return 0;";
            } else if ("long".equals(type)) {
                contentText = "return 0l;";
            } else if ("float".equals(type)) {
                contentText = "return 0.0f;";
            } else if ("double".equals(type)) {
                contentText = "return 0.0d;";
            } else if ("void".equals(type)) {
                contentText = "return;";
            } else if ("byte".equals(type)) {
                contentText = "return 0;";
            } else {
                contentText = "return null;";
            }
        }

        w.write("{");
        printLineFeed(w);
        indentation += 4;

        printIndentation(indentation, w);
        w.write(contentText);
        w.write(" ");
        printTodo(w);
        printLineFeed(w);
        indentation -= 4;
        printIndentation(indentation, w);
        w.write("}");
        printLineFeed(w);
    }

    protected static void printTodo(Writer w) throws IOException {
        w.write("//TODO codavaj!!");
    }

    protected static String getSourceTypeName( String name ) {
        return name.replace('$', '.');
    }

    protected static void print(Type t, Writer w, int indentation)
        throws IOException {
        if (t.getEnclosingType() == null) {
            // print package statement
            printIndentation(indentation, w);
            w.write("package " + t.getPackage().getName() + ";");
            printLineFeed(w);
        }

        printComment(t.getComment(), w, indentation);
        printIndentation(indentation, w);
        printModifiers(t, w);

        if (t.isAnnotation()) {
            w.write("@");
            w.write(LABEL_INTERFACE);
            w.write(" ");
        } else if (t.isInterface()) {
            w.write(LABEL_INTERFACE);
            w.write(" ");
        } else if(t.isEnum()) {
            w.write(LABEL_ENUM);
            w.write(" ");
        } else {
            w.write(LABEL_CLASS);
            w.write(" ");
        }

        w.write(t.getShortName());
        if ( t.getTypeParameters() != null ) {
            w.write(getSourceTypeName(t.getTypeParameters()));
        }

        if (t.getSuperType() != null && !"java.lang.Object".equals(t.getSuperType()) && !(t.isEnum() && t.getSuperType().startsWith("java.lang.Enum"))) {
            w.write(" extends ");
            w.write(getSourceTypeName(t.getSuperType()));
        }

        if ((t.getImplementsList() != null)
                && (t.getImplementsList().size() > 0)) {
            if (t.isInterface()) {
                w.write(" extends ");
            } else {
                w.write(" implements ");
            }

            for (int i = 0; i < t.getImplementsList().size(); i++) {
                if (i != 0) {
                    w.write(", ");
                }

                w.write(getSourceTypeName(t.getImplementsList().get(i)));
            }
        }

        w.write("{");
        printLineFeed(w);
        indentation += 4;

        for (int i = 0; (t.getEnumConstList() != null) && (i < t.getEnumConstList().size()); i++) {
            EnumConst ec = t.getEnumConstList().get(i);
            printEnumConst(ec, w, indentation, i == t.getEnumConstList().size()-1);
            printLineFeed(w);
        }

        for (int i = 0;
                (t.getFieldList() != null) && (i < t.getFieldList().size());
                i++) {
            Field f = t.getFieldList().get(i);
            printField(f, w, indentation);
            printLineFeed(w);
        }

        for (int i = 0;
                (t.getConstructorList() != null)
                && (i < t.getConstructorList().size()); i++) {
            Method m = t.getConstructorList().get(i);
            printMethod(t, m, w, true, indentation);
            printLineFeed(w);
        }

        for (int i = 0;
                (t.getMethodList() != null) && (i < t.getMethodList().size());
                i++) {
            Method m = t.getMethodList().get(i);
            // Enums have a special case where valueOf() and values() appear
            // in the JavaDocs but will generate a compiler error if you
            // override them because they're static methods.
            if(!(t.isEnum() && (m.getName().equals("valueOf") || m.getName().equals("values")))) {
                printMethod(t, m, w, false, indentation);
                printLineFeed(w);
            }
        }

        for (int i = 0;
                (t.getInnerTypeList() != null)
                && (i < t.getInnerTypeList().size()); i++) {
            Type innertype = t.getInnerTypeList().get(i);
            print(innertype, w, indentation);
        }

        indentation -= 4;
        printIndentation(indentation, w);
        w.write("}");
        printLineFeed(w);
    }
}
