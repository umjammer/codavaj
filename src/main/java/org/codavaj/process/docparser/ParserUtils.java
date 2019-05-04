/*
 *   Copyright 2005,2009 Peter Klauser
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

package org.codavaj.process.docparser;

import org.codavaj.AbstractLogger;

import org.codavaj.type.Field;
import org.codavaj.type.EnumConst;
import org.codavaj.type.Method;
import org.codavaj.type.Modifiable;
import org.codavaj.type.Parameter;
import org.codavaj.type.Type;
import org.codavaj.type.TypeFactory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import org.dom4j.io.DOMReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Iterator;

/**
 * DOCUMENT ME!
 */
public class ParserUtils extends AbstractLogger {
    /**
     * Creates a new ParserUtils object.
     */
    public ParserUtils() {
    }

    /**
     * Return the classname from a filename.
     *
     * @param filename filename - java/lang/String.html
     *
     * @return the typename - java.lang.String
     */
    public String typenameFromFilename(String filename) {
        if (filename.endsWith(".html")) {
            filename = filename.substring(0,
                    filename.length() - ".html".length());
        }

        filename = filename.replace('\\', '/'); // only use forward-slash
        filename = filename.replace('.', '$'); // inner classes
        filename = filename.replace('/', '.'); // use . separation

        return filename;
    }

    /**
     * Return the filename from the typename.
     *
     * @param typename the typename - java.lang.String
     *
     * @return filename java/lang/String.html
     */
    public String filenameFromTypename(String typename) {
        typename = typename.replace('.', '/');
        typename = typename.replace('$', '.');

        return typename += ".html";
    }

    /**
     * DOCUMENT ME!
     *
     * @param enclosingNode DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public List<String> determineComment(Element enclosingNode)
        throws Exception {
        if (enclosingNode == null) {
            return null;
        }

        /*
         a field comment
         a method comment
         */
        List<?> allNodes = enclosingNode.content();
        List<String> commentText = new ArrayList<>();

        for (int i = 0; (allNodes != null) && (i < allNodes.size()); i++) {
            Node node = (Node) allNodes.get(i);

            if ((node.getNodeType() == Node.ELEMENT_NODE)
                    && "P".equals(node.getName())) {
                String commentLine = node.valueOf("normalize-space(.)");

                if ((commentLine != null) && !commentLine.trim().equals("")) {
                    commentText.add(commentLine.trim());
                }
            }

            if (node.getNodeType() == Node.TEXT_NODE) {
                String commentLine = node.valueOf("normalize-space(.)");

                if ((commentLine != null) && !commentLine.trim().equals("")) {
                    commentText.add(commentLine.trim());
                }
            }
        }

        return commentText;
    }

    /**
     * DOCUMENT ME!
     *
     * @param enclosingNode DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public String determineDefault(Element enclosingNode)
        throws Exception {
        if (enclosingNode == null) {
            return null;
        }

        /*
         a field comment
         a method comment
         */
        List<?> allNodes = enclosingNode.content();
        String defaultText="";

        for (int i = 0; (allNodes != null) && (i < allNodes.size()); i++) {
            Node node = (Node) allNodes.get(i);

            if ((node.getNodeType() == Node.ELEMENT_NODE)
                    && "DL".equals(node.getName())) {
                String commentLine = node.valueOf("normalize-space(.)");

                if ( commentLine.indexOf("Default:") != -1 ) {
                    defaultText += commentLine.substring(commentLine.indexOf("Default:")+8);
                }
            }

        }
        if ( defaultText.length() > 0 )
        {
            return defaultText;
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     * @param externalLinks list of externaly linked javadoc references.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void determineClassComment(Type type, Document typeXml, List<?> externalLinks )
        throws Exception {
        /*
         a class comment
        <H2>org.codavaj Interface Logger</H2>
        <HR/>
        <DL> ... </DL>
        <P>Logger interface.</P>
        <P></P>
        <P>
        <DL>
        <DT>Author:</DT> Peter
        </DL>
        </P>
        <HR/>
         */
        List<?> allNodes = typeXml.getRootElement().content();

        // H2 indicates class descriptor
        boolean parseOn = false;
        List<String> commentText = new ArrayList<>();

        for (int i = 0; (allNodes != null) && (i < allNodes.size()); i++) {
            Node node = (Node) allNodes.get(i);

            if (!parseOn && (node.getNodeType() == Node.ELEMENT_NODE)
                    && "H2".equals(node.getName())) {
                // H2 starts the parsing off
                while (i < allNodes.size()) {
                    i++;
                    node = (Node) allNodes.get(i);

                    if ((node.getNodeType() == Node.ELEMENT_NODE)
                            && "HR".equals(node.getName())) {
                        break;
                    }
                }

                parseOn = true;

                continue;
            }

            if (parseOn) {
                if ((node.getNodeType() == Node.ELEMENT_NODE)
                        && "P".equals(node.getName())) {
                    String commentLine = node.valueOf("normalize-space(.)");

                    if ((commentLine != null) && !commentLine.trim().equals("")) {
                        commentText.add(commentLine.trim());
                    }
                } else if ((node.getNodeType() == Node.ELEMENT_NODE)
                        && "HR".equals(node.getName())) {
                    parseOn = false;
                }
            }
        }

        type.setComment(commentText);
    }

    /**
     * DOCUMENT ME!
     *
     * @param allconstants DOCUMENT ME!
     * @param typeFactory DOCUMENT ME!
     * @param externalLinks list of externaly linked javadoc references.
     * @param lenient whether to warn when type not found
     */
    public void determineConstants(Document allconstants,
        TypeFactory typeFactory, List<?> externalLinks, boolean lenient) {
        List<?> constantList = allconstants.selectNodes(
                "//P/TABLE/TR[position() != 1]");

        for (int i = 0; (constantList != null) && (i < constantList.size());
                i++) {
            Node constantNode = (Node) constantList.get(i);

            /*
             each row has field with the modifiers and the type
             then one with the type ( href ) and field name text()
             and then the constant itself
            <TR>
              <A/>
              <TD>public� static� final� java.lang.String</TD>
              <TD>
                <A href="org/codavaj/process/antrunner/AntRunner.html#PROPERTY_ANTRUNNER_TARGET">PROPERTY_ANTRUNNER_TARGET</A>
              </TD>
              <TD>"org.codavaj.antrunner.target"</TD>
            </TR>
            */
            String typeName = javadocLinkToTypename(constantNode.valueOf(
                        "TD[ position() = 2 ]/A/@href"), externalLinks );
            String fieldName = constantNode.valueOf("TD[ position() = 2 ]/A");
            String constantValue = constantNode.valueOf("TD[ position() = 3 ]");

            //debug( typeName + "#" + fieldName +"=" +constantValue );
            Type type = typeFactory.lookupType(typeName);

            if (type == null) {
                if ( !lenient ) {
                    warning("Unable to find type " + typeName);
                }

                continue;
            }

            Field field = type.lookupFieldByName(fieldName);

            if (field == null) {
                warning("Unable to find field " + typeName + "#" + fieldName);

                continue;
            }

            field.setValue(determineConstantValue(field.getType(), constantValue));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName DOCUMENT ME!
     * @param constantvalue DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object determineConstantValue(String typeName, String constantvalue) {
        //( Boolean, Byte, Char, Double, Float, Integer, Long, Short )
        Object value = null;

        if ("java.lang.String".equals(typeName)) {
            if ((constantvalue.charAt(0) != '"')
                    && (constantvalue.charAt(constantvalue.length()) != '"')) {
                warning(
                    "expect constant string value to start and end with quotes "
                    + typeName + " value " + constantvalue);
                value = constantvalue;
            } else {
                value = constantvalue.substring(1, constantvalue.length() - 1);
            }

            return value;
        } else if ("boolean".equals(typeName)) {
            return Boolean.valueOf(constantvalue);
        } else if ("byte".equals(typeName)) {
            return Byte.valueOf(constantvalue);
        } else if ("char".equals(typeName)) {
            return new Character((char) Integer.valueOf(constantvalue).intValue());
        } else if ("double".equals(typeName)) {
            try {
                value = Double.valueOf(constantvalue);
            } catch ( NumberFormatException nfe ) {
                //NaN, 0d/0d, 0d/-1d etc. cause this to fail.
                return constantvalue;
            }
            return value;
        } else if ("float".equals(typeName)) {
            try {
                value = Float.valueOf(constantvalue);
            } catch ( NumberFormatException e ) {
                // 0f/0f , -1f/-1f etc
                return constantvalue;
            }
            return Float.valueOf(constantvalue);
        } else if ("int".equals(typeName)) {
            return Integer.valueOf(constantvalue);
        } else if ("long".equals(typeName)) {
            if (constantvalue.indexOf("l") != -1) {
                constantvalue = constantvalue.substring(0,
                        constantvalue.indexOf("l"));
            }

            if (constantvalue.indexOf("L") != -1) {
                constantvalue = constantvalue.substring(0,
                        constantvalue.indexOf("L"));
            }

            return Long.valueOf(constantvalue);
        } else if ("short".equals(typeName)) {
            return Short.valueOf(constantvalue);
        }

        warning("unknown constant type: " + typeName);

        return value;
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     * @param externalLinks list of externaly linked javadoc references.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void determineDetails(Type type, Document typeXml, List<?> externalLinks )
        throws Exception {
        /*
         an example method
        <H3>process</H3> public void process() throws
        <A href="../../../../org/codavaj/ProcessException.html">ProcessException</A>
        <DL>...
         an example field
        <H3>PROPERTY_ANTRUNNER_FILENAME</H3> public static final java.lang.String PROPERTY_ANTRUNNER_FILENAME
        <DL>...
         */
        List<?> allNodes = typeXml.getRootElement().content();

        // H3 indicates method or field names, the following text up to the next NON 'A' element
        // belongs to the summary
        boolean parseOn = false;
        String name = "";
        String text = "";

        for (int i = 0; (allNodes != null) && (i < allNodes.size()); i++) {
            Node node = (Node) allNodes.get(i);

            if (!parseOn && (node.getNodeType() == Node.ELEMENT_NODE)
                    && "H3".equals(node.getName())) {
                // H3 starts the parsing off
                parseOn = true;
                name = node.getText().trim();
                text = "";

                continue;
            }

            if (parseOn) {
                if (node.getNodeType() == Node.TEXT_NODE) {
                    text += node.getStringValue();
                } else if ((node.getNodeType() == Node.ELEMENT_NODE) && "A".equals(node.getName()) && node.getText().length() > 1 && node.valueOf("@href").indexOf(node.getText()) != -1) {
                    text += javadocLinkToTypename(node.valueOf("@href"), externalLinks);
                } else if ((node.getNodeType() == Node.ELEMENT_NODE) && "A".equals(node.getName())) {
                    text += convertNodesToString((Element)node, externalLinks);
                } else if (node.getNodeType() == Node.COMMENT_NODE) {
                    continue;
                } else if (node.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                    if(node.getStringValue().equals("&lt;"))
                      text += "<";
                    else if(node.getStringValue().equals("&gt;"))
                      text += ">";
                    else if(node.getStringValue().equals("&nbsp;"))
                      text += " ";
                    else
                      continue;
                } else if (node.getNodeType() == Node.ELEMENT_NODE) {
                    // finished
                    Element commentNode = node.getName().equals("DL")
                        ? (Element) node : null;

                    parseOn = false;

                    if ((text.indexOf("(") != -1) && (text.indexOf(")") != -1)
                            && (text.indexOf(")") >= text.indexOf("("))) {
                        String methodParams = text.substring(text.indexOf("(")
                                + 1, text.indexOf(")"));

                        // the throws text comes after the 'throws' text
                        String throwsParams = "";

                        if (text.indexOf("throws", text.lastIndexOf(")")) != -1) {
                            throwsParams = text.substring(text.indexOf(
                                        "throws", text.lastIndexOf(")"))
                                    + "throws".length());
                        }

                        text = text.substring(0, text.indexOf("("));

                        List<Parameter> params = determineMethodParameterList(methodParams);
                        Method m = type.lookupMethodByName(name, params);

                        if (m == null) {
                            // try looking up constructors
                            m = type.lookupConstructor(params);
                        }

                        if (m != null) {
                            extractMethodModifiers(m, text);

                            //parse the throws list and assign the found exceptions to the method
                            determineThrowsList(throwsParams, m);
                            m.setComment(determineComment(commentNode));
                        } else {
                            warning(
                                "failed to find method or constructor with name "
                                + name + " in type " + type.getTypeName());
                        }
                    } else {
                        // Could be either a field, enum constant, or element detail
                        Field f = type.lookupFieldByName(name);

                        if (f != null) {
                            extractFieldModifiers(f, text);
                            f.setComment(determineComment(commentNode));
                        } else {
                            EnumConst ec = type.lookupEnumConstByName(name);

                            if(ec != null) {
                                ec.setComment(determineComment(commentNode));
                            } else {
                                Method m = type.lookupMethodByName(name, new ArrayList<Parameter>()); // annotation elements have no params

                                if ( m != null ) {
                                    extractMethodModifiers(m, text);
                                    m.setComment(determineComment(commentNode));
                                    m.setDefaultValue(determineDefault(commentNode));
                                } else {
                                    warning("No field, enum constant, or annotation element with name " + name + " in type " + type.getTypeName());
                                }
                            }
                        }
                    }

                    text = "";
                    name = "";
                }
            }
        }
    }

    private List<Parameter> determineMethodParameterList(String methodParams)
        throws Exception {
        List<Parameter> params = new ArrayList<>();

        List<String> words = tokenizeWordListWithTypeParameters(methodParams, ",");
        Iterator<String> it = words.iterator();
        while(it.hasNext()) {
            Parameter p = determineParameter(it.next(), true);
            params.add(p);
        }

        return(params);
    }

    private List<String> tokenizeWordListWithTypeParameters(String text, String delimeters)
    {
        int inTypeParamStackCount = 0;
        String currentWord = "";

        List<String> words = new ArrayList<>();

        // iterate character by character to see when we're in a type
        // parameter and when we're not
        for(int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if(ch == '<') {
                inTypeParamStackCount++;
                currentWord += ch;
            } else if(ch == '>') {
                inTypeParamStackCount--;
                currentWord += ch;
            } else if(inTypeParamStackCount == 0 && delimeters.indexOf(ch) >= 0) {
                // at a word delimeter
                if(!"".equals(currentWord)) {
                    words.add(currentWord);
                    currentWord = "";
                }
            } else {
              currentWord += ch;
            }
        }

        if(!"".equals(currentWord))
            words.add(currentWord);

        return(words);
    }

    private void extractMethodModifiers(Method m, String text)
        throws Exception {
        List<String> words = tokenizeWordListWithTypeParameters(text, " \t\n\r\f");
        Iterator<String> it = words.iterator();
        while(it.hasNext()) {
            m.assignModifier(it.next());
        }
    }

    private void determineThrowsList(String throwsParams, Method m) {
        List<String> words = tokenizeWordListWithTypeParameters(throwsParams, " ,\t\n\r\f");
        Iterator<String> it = words.iterator();
        while(it.hasNext()) {
            m.addThrows(it.next());
        }
    }

    private void extractFieldModifiers(Field f, String text)
        throws Exception {
        List<String> words = tokenizeWordListWithTypeParameters(text, " \t\n\r\f");
        Iterator<String> it = words.iterator();
        while(it.hasNext()) {
            f.assignModifier(it.next());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     * @param externalLinks list of externaly linked javadoc references.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void determineFields(Type type, Document typeXml, List<?> externalLinks )
        throws Exception {
        // get the method details out of the Field Summary table
        // this information is missing the modifier info which is only
        // availible in the field details

        /* javadoc pre 1.5
        <TR>
        <TD> �
          <A href="../../../../org/codavaj/javadoc/input/DefaultInterface.html">DefaultInterface</A>[]
        </TD>
        <TD>
          <A href="../../../../org/codavaj/javadoc/input/ClassWithFields.html#defintarray_">defintarray_</A>  �����������
        </TD>
        </TR>
         */

        /*
        <TR BGCOLOR="white" CLASS="TableRowColor">
        <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
        <CODE>static&nbsp;java.lang.String</CODE></FONT></TD>
        <TD><CODE><B><A HREF="../../org/codavaj/Main.html#FILE_SEPARATOR">FILE_SEPARATOR</A></B></CODE>

        <BR>
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;DOCUMENT ME!</TD>
        </TR>
         */
        String fieldsXpath = "//TR[contains(parent::TABLE/TR[1], 'Field Summary')][position()>1]";
        List<?> fieldList = typeXml.selectNodes( fieldsXpath );

        for (int i = 0; (fieldList != null) && (i < fieldList.size()); i++) {
            Field field = type.createField();

            Node fieldNode = (Node) fieldList.get(i);

            // get the return type description for known types
            Element fieldtypeNode = (Element) fieldNode.selectSingleNode(
                    "TD[position()=1]");

            String fieldtypeParam = convertNodesToString(fieldtypeNode, externalLinks);

            //debug( "fieldtype: " + fieldtypeParam);
            Parameter temp = determineParameter(fieldtypeParam, false);
            field.setType(temp.getType());
            field.setArray(temp.isArray());
            field.setDegree(temp.getDegree());
            field.setName(temp.getName());
            field.setTypeArgumentList(temp.getTypeArgumentList());

            // now we get the parameter list
            Element fieldNameNode = (Element) fieldNode.selectSingleNode(
                    "TD[position()=2]/A");
            String fieldName = fieldNameNode.getText();

            //debug( "fieldname: " + fieldName );
            field.setName(fieldName);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     * @param externalLinks list of externaly linked javadoc references.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void determineEnumConsts(Type type, Document typeXml, List<?> externalLinks )
        throws Exception {
        String enumConstsXpath = "//TR[contains(parent::TABLE/TR[1], 'Enum Constant Summary')][position()>1]";
        List<?> enumConstList = typeXml.selectNodes( enumConstsXpath );

        for (int i = 0; (enumConstList != null) && (i < enumConstList.size()); i++) {
            EnumConst enumConst = type.createEnumConst();

            Node enumConstNode = (Node) enumConstList.get(i);

            // now we get the parameter list
            Element enumConstNameNode = (Element) enumConstNode.selectSingleNode(
                    "TD[position()=1]/A");
            String enumConstName = enumConstNameNode.getText();

            //debug( "enumConstName: " + enumConstName );
            enumConst.setName(enumConstName);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     * @param externalLinks list of externaly linked javadoc references.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void determineConstructors(Type type, Document typeXml, List<?> externalLinks)
        throws Exception {
        // get the constructor details out of the Constructor Summary table
        // the first table field pertains to the "return type" which is always blank
        // since the constructor return is the class itself.

        /* Javadoc pre 1.5
        <TR>
        <TD> �</TD>
        <TD>
          <A href="../../../../org/codavaj/javadoc/input/ClassWithConstructors.html#ClassWithConstructors(org.codavaj.javadoc.input.ArrayReturnClass)">ClassWithConstructors</A>(
          <A href="../../../../org/codavaj/javadoc/input/ArrayReturnClass.html">ArrayReturnClass</A>�p1) �����������
        </TD>
        </TR>
         */

        /* Javadoc 1.5
        <TR BGCOLOR="white" CLASS="TableRowColor">
        <TD><CODE><B><A HREF="../../org/codavaj/MissingParameterException.html#MissingParameterException(java.lang.String)">MissingParameterException</A></B>(java.lang.String&nbsp;propertyname)</CODE>

        <BR>
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Creates a new MissingParameterException object.</TD>
        </TR>
         */
           String constructorsXpath = "//TR[contains(parent::TABLE/TR,'Constructor Summary')][position()>1]";
        List<?> methodList = typeXml.selectNodes( constructorsXpath );

        for (int i = 0; (methodList != null) && (i < methodList.size()); i++) {
            Method method = type.createConstructor();

            Node methodNode = (Node) methodList.get(i);

            // now we get the parameter list
            Element paramlistNode = (Element) methodNode.selectSingleNode(
                    "TD[position()=2]");

            if (paramlistNode == null) {
                // constructor table is simplified if all constructors are public
                paramlistNode = (Element) methodNode.selectSingleNode(
                        "TD[position()=1]");
            }

            List<?> paramlistNodes = paramlistNode.content();
            determineMethodParameters(method, paramlistNodes, externalLinks );
        }
    }

    private void determineMethodParameters(Method method, List<?> paramlistNodes, List<?> externalLinks )
        throws Exception {
        Element methodNameElement = (Element) paramlistNodes.get(0); // link in the same file
        method.setName(methodNameElement.getText());

        //debug(" methodname: " + method.getMethodName());
        String methodParams = "";

        for (int paramIdx = 1;
                (paramlistNodes != null) && (paramIdx < paramlistNodes.size());
                paramIdx++) {
            //expect the methodName in the first parameter, so skip it
            Node paramNode = (Node) paramlistNodes.get(paramIdx);

            //debug( paramNode.getNodeTypeName()+" "+paramNode.getStringValue() );
            // need to combine method description into a single text which can then
            // be parsed easily. TypeVariables tend to have length 1 ( E, V, K etc ) which can easily match one character of the link to the generic parent
            if (paramNode.getNodeType() == Node.ELEMENT_NODE && "A".equals(paramNode.getName()) && paramNode.getText().length() > 1 && paramNode.valueOf("@href").indexOf(paramNode.getText()) != -1 ) {
                // reference to type
                methodParams += javadocLinkToTypename(paramNode.valueOf("@href"), externalLinks);
            } else if (paramNode.getNodeType() == Node.ELEMENT_NODE) {
                // reference to a paramterized type - use just the name
                methodParams += convertNodesToString((Element)paramNode, externalLinks);
            } else if (paramNode.getNodeType() == Node.TEXT_NODE) {
                methodParams += paramNode.getStringValue();

                if (methodParams.indexOf("(") != -1) {
                    methodParams = methodParams.substring(methodParams.indexOf(
                                "(") + 1);
                }
            } else if (paramNode.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                if(paramNode.getStringValue().equals("&lt;"))
                    methodParams += "<";
                else if(paramNode.getStringValue().equals("&gt;"))
                    methodParams += ">";
                else if(paramNode.getStringValue().equals("&nbsp;"))
                    methodParams += " ";
            }

            if (methodParams.indexOf(")") != -1) {
                // reached end of useful description - skip method summary
                methodParams = methodParams.substring(0,
                        methodParams.indexOf(")"));

                break;
            }
        }

        // now we parse "type name" comma separated pairs from the result
        List<Parameter> params = determineMethodParameterList(methodParams);
        for(int i = 0; i < params.size(); i++) {
            method.addParameter(params.get(i));
        }
    }

    private Parameter determineParameter(String parameterText, boolean parseName)
        throws Exception {
        // parses a parameter with modifiers, type, with or without a name
        // private final java.lang.String[][] name
        // static a/b/c/D name
        Parameter p = new Parameter();

        // count the number of [] to determine the array degree
        while (parameterText.indexOf("[]") > 0) {
            p.setArray(true);
            p.setDegree(p.getDegree() + 1);
            parameterText = parameterText.substring(0,
                    parameterText.indexOf("[]"))
                + parameterText.substring(parameterText.indexOf("[]") + 2);
        }

        try {
            List<String> words = tokenizeWordListWithTypeParameters(parameterText, " \t\n\r\f");
            Iterator<String> it = words.iterator();
            while(it.hasNext()) {
                String word = it.next();

                if (Modifiable.isModifier(word)) {
                    // skip modifiers
                    continue;
                } else if ( word.indexOf("<") != -1 ){
                    // parameterized type with type parameter arguments
                    p.setTypeArgumentList(word.substring(word.indexOf("<"), word.length()));
                    p.setType(word.substring(0, word.indexOf("<")));
                } else {
                    p.setType(word);
                }

                if (parseName && it.hasNext()) {
                    // name comes after the type
                    String name = it.next();
                    p.setName(name);
                }
            }
        } catch (Exception e) {
            warning("failed to parse parameterText: " + parameterText);
            throw e;
        }

        if ( parseName && p.getName() == null ) {
            warning("failed to parse parameter name from : " + parameterText);
        }
        return p;
    }

    private Parameter determineMethodReturnParameter(Method m, String parameterText)
        throws Exception {
        // parses a parameter with modifiers, type, with or without a name
        // private final java.lang.String[][] name
        // static a/b/c/D name
        Parameter p = new Parameter();

        // count the number of [] to determine the array degree
        while (parameterText.indexOf("[]") > 0) {
            p.setArray(true);
            p.setDegree(p.getDegree() + 1);
            parameterText = parameterText.substring(0,
                    parameterText.indexOf("[]"))
                + parameterText.substring(parameterText.indexOf("[]") + 2);
        }

        try {
            List<String> words = tokenizeWordListWithTypeParameters(parameterText, " \t\n\r\f");
            Iterator<String> it = words.iterator();
            while(it.hasNext()) {
                String word = it.next();

                if (Modifiable.isModifier(word)) {
                    // skip modifiers
                    continue;
                } else if ( word.startsWith("<") ){
                    m.setTypeParameters(word);
                } else {
                    p.setType(word);
                }
            }
        } catch (Exception e) {
            warning("failed to parse method return parameter in parameterText: " + parameterText);
            throw e;
        }

        return p;
    }

    /**
     * Converts 'A' links to a type and mixes content on same level together so
     * plain text can be parsed.
     *
     * @param contentElement element who's top level content is merged.
     * @param externalLinks list of externaly linked javadoc references.
     *
     * @return plain text to be parsed.
     */
    private String convertNodesToString(Element contentElement, List<?> externalLinks ) {
        List<?> returnparamNodes = contentElement.content();

        String text = "";

        for (int paramIdx = 0;
                (returnparamNodes != null)
                && (paramIdx < returnparamNodes.size()); paramIdx++) {
            Node paramNode = (Node) returnparamNodes.get(paramIdx);

            // need to combine method description into a single text which can then
            // be parsed easily. If we link to another type rather than a generic type variable, the name of the link's text matches the classname
            if (paramNode.getNodeType() == Node.ELEMENT_NODE && "A".equals(paramNode.getName()) && paramNode.getText().length() > 1 && paramNode.valueOf("@href").indexOf(paramNode.getText()) != -1) {
                // reference to type
                text += javadocLinkToTypename(paramNode.valueOf("@href"), externalLinks);
            } else if (paramNode.getNodeType() == Node.ELEMENT_NODE) {
                text += convertNodesToString((Element)paramNode,externalLinks);
            } else if (paramNode.getNodeType() == Node.TEXT_NODE) {
                text += paramNode.getStringValue();
            } else if (paramNode.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                if(paramNode.getStringValue().equals("&lt;"))
                    text += "<";
                else if(paramNode.getStringValue().equals("&gt;"))
                    text += ">";
                else if(paramNode.getStringValue().equals("&nbsp;"))
                    text += " ";
            }
        }

        return text;
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     * @param externalLinks list of externaly linked javadoc references.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void determineMethods(Type type, Document typeXml, List<?> externalLinks )
        throws Exception {
        // get the method details out of the Method Summary table
        // this information is missing the throws info which is only
        // availible in the method details
        // "//TR[contains(parent::TABLE/TR/TD,'Method Summary') and not(contains(TD,'Method Summary'))]"
        // the first column gives the return type
        // the second column gives the method name in a link, followed by (
        // and then a comma separated list of either links or non-included typenames
        // followed by a ) and a textual summary of the return.

        /* Javadoc pre 1.5
        <TR>
        <TD> �
          <A href="../../../java/lang/String.html">String</A>[]
        </TD>
        <TD>
          <A href="../../../org/w3c/dom/Element.html#getAttributeNS(java.lang.String, java.lang.String)">getAttributeNS</A>(
          <A href="../../../java/lang/String.html">String</A>�namespaceURI,
          <A href="../../../java/lang/String.html">String</A>�localName) ���������� Retrieves an attribute value by local name and namespace URI.
        </TD>
        </TR>
         */

        /*
         * Javadoc 1.5
        <TR BGCOLOR="white" CLASS="TableRowColor">
        <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
        <CODE>static&nbsp;<A HREF="../../org/codavaj/type/TypeFactory.html" title="class in org.codavaj.type">TypeFactory</A></CODE></FONT>
        </TD>
        <TD><CODE><B><A HREF="../../org/codavaj/Main.html#analyze(java.lang.String, java.util.List)">analyze</A></B>(java.lang.String&nbsp;javadocdir,
                java.util.List&nbsp;externalLinks)</CODE>

        <BR>
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Derive a reflection-like API from a javadoc source tree.
        </TD>
        </TR>
         */
        /*
         * Javadoc 1.6 - with generic parameter definition
          <TR>
            <TD>static
              <TABLE>
                <TR>
                  <TD> <T extends java.lang.Object &amp; java.lang.Comparable< ? super T>>  T</TD>
                </TR>
              </TABLE>
            </TD>
            <TD>
              <A href="../../../../org/codavaj/javadoc/input/KillerGenericsStuff.html#max(java.util.Collection)">max</A>(java.util.Collection< ? extends T>� coll) ���������� Returns the maximum element of the given collection, according to the
              <I>natural ordering</I> of its elements.
            </TD>
          </TR>
         */

           String methodXpath = "//TR[contains(parent::TABLE/TR,'Method Summary')][position()>1]";
        List<?> methodList = typeXml.selectNodes( methodXpath );

        for (int i = 0; (methodList != null) && (i < methodList.size()); i++) {
            Method method = type.createMethod();

            Node methodNode = (Node) methodList.get(i);

            // get the return type
            Element returnparamNode = (Element) methodNode.selectSingleNode(
                    "TD[position()=1]");

            String methodReturnParam = convertNodesToString(returnparamNode, externalLinks );

            Parameter returnType = determineMethodReturnParameter(method, methodReturnParam);

            if (returnType == null) {
                warning("failed to determine return type: "
                    + prettyPrint(typeXml));
            }

            method.setReturnParameter(returnType);

            // now we get the parameter list
            Element paramlistNode = (Element) methodNode.selectSingleNode(
                    "TD[position()=2]");

            //if ( paramlistNode == null ) continue; // no method
            List<?> paramlistNodes = paramlistNode.content();

            determineMethodParameters(method, paramlistNodes, externalLinks);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     * @param externalLinks list of externaly linked javadoc references.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void determineElements(Type type, Document typeXml, List<?> externalLinks )
        throws Exception {

        /* Javadoc 1.6
    <TABLE>
      <TR>Required Element Summary</TR>
      <TR>
        <TD> �
          <A href="../../../../org/codavaj/javadoc/input/AnnotationDefault.html">AnnotationDefault</A>
        </TD>
        <TD>
          <A href="../../../../org/codavaj/javadoc/input/AnnotationParameterized.html#annotParam()">annotParam</A>  �����������
        </TD>
      </TR>...
    </TABLE>  �
         */

           String methodXpath = "//TR[contains(parent::TABLE/TR,'Required Element Summary')][position()>1]";
        List<?> methodList = typeXml.selectNodes( methodXpath );

        for (int i = 0; (methodList != null) && (i < methodList.size()); i++) {
            Method method = type.createMethod();

            Node methodNode = (Node) methodList.get(i);

            // get the return type
            Element returnparamNode = (Element) methodNode.selectSingleNode(
                    "TD[position()=1]");

            String methodReturnParam = convertNodesToString(returnparamNode, externalLinks );

            Parameter returnType = determineMethodReturnParameter(method, methodReturnParam );

            if (returnType == null) {
                warning("failed to determine return type: "
                    + prettyPrint(typeXml));
            }

            method.setReturnParameter(returnType);

            // now we get the element name
            Element elemenNameNode = (Element) methodNode.selectSingleNode("TD[position()=2]/A");

            String elementName = elemenNameNode.getText();
            method.setName(elementName);
        }

           methodXpath = "//TR[contains(parent::TABLE/TR,'Optional Element Summary')][position()>1]";
        methodList = typeXml.selectNodes( methodXpath );

        for (int i = 0; (methodList != null) && (i < methodList.size()); i++) {
            Method method = type.createMethod();

            Node methodNode = (Node) methodList.get(i);

            // get the return type
            Element returnparamNode = (Element) methodNode.selectSingleNode(
                    "TD[position()=1]");

            String methodReturnParam = convertNodesToString(returnparamNode, externalLinks );

            Parameter returnType = determineMethodReturnParameter(method, methodReturnParam);

            if (returnType == null) {
                warning("failed to determine return type: "
                    + prettyPrint(typeXml));
            }

            method.setReturnParameter(returnType);

            // now we get the element name
            Element elemenNameNode = (Element) methodNode.selectSingleNode("TD[position()=2]/A");

            String elementName = elemenNameNode.getText();
            method.setName(elementName);
            method.setDefaultValue("");// will be replaced by parsing details
        }
    }

    /**
     * Convert a relative javadoc link to a classname. Example
     * ../../../a/b/c/D.I.html#ref is a/b/c/D.I . Resolve links
     * to http://java.sun.com api's and any provided external
     * links.
     *
     * @param link javadoc link
     * @param externalLinks list of externaly linked javadoc references.
     *
     * @return a classname corresponding to a relative javadoc link.
     * @throws Exception if an absolute link is not resolved in the external links
     */
    public String javadocLinkToTypename(String link, List<?> externalLinks) throws UnresolvedExternalLinkException {
        if ( link == null ) return null;
        if ( link.startsWith("http:") || link.startsWith("https:")) {
            // external link - try to resolve any java.sun.com external links
            // http://java.sun.com/j2se/1.3/docs/api/
            // http://java.sun.com/j2se/1.4.2/docs/api/
            // http://java.sun.com/j2se/1.5.0/docs/api/
            if ( link.startsWith("http://java.sun.com/") && link.indexOf("/api/") != -1 ) {
                // standard sun api links - no need to explicitly mention these
                link = link.substring(link.indexOf("/api/") + "/api/".length());
            } else {
                boolean found = false;
                for( int i = 0; externalLinks != null && i < externalLinks.size(); i++ ) {
                    String externalLink = (String)externalLinks.get(i);
                    if ( link.startsWith(externalLink) ) {
                        link = link.substring(externalLink.length());
                        if ( link.startsWith("/")) {
                            link = link.substring(1);
                        }
                        found = true;
                        break;
                    }
                }
                if ( !found ) {
                    throw new UnresolvedExternalLinkException("External link " + link + " not specified in program arguments.");
                }
            }
        }

        while (link.startsWith("../")) {
            link = link.substring("../".length());
        }

        if (link.indexOf("#") != -1) {
            link = link.substring(0, link.indexOf("#"));
        }

        return typenameFromFilename(link);
    }

    /**
     * Extract all classnames from the allclasses-frame.html file.
     *
     * @param alltypesXml the DOM of the allclasses-frame.html file
     *
     * @return a list of fully qualified classnames.
     */
    public List<String> getAllFqTypenames(Document alltypesXml) {
        List<?> classes = alltypesXml.selectNodes(
                "//A[@target='classFrame']/@href");
        List<String> result = new ArrayList<>();

        for (int i = 0; i < classes.size(); i++) {
            String file = ((Node) classes.get(i)).getText();
            String typeName = typenameFromFilename(file);

            result.add(typeName);
        }

        return result;
    }

    private boolean containsToken( String token, String input ) {
        StringTokenizer tokenizer = new StringTokenizer(input);
        while( tokenizer.hasMoreTokens() ) {
            if(  tokenizer.nextToken().equals(token) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Identify whether the HTML represents an interface.
     *
     * @param typeXml DOM of the type.
     *
     * @return whether the HTML represents an interface.
     */
    public boolean isInterface(Document typeXml) {
        String classHeader = typeXml.valueOf("//H2");

        // <H2>org.jumpi.spi.component Interface SequenceGenerator</H2>
        if ((classHeader != null) ) {
            return containsToken( "Interface", classHeader );
        }

        return false;
    }

    /**
     * Identify whether the HTML represents an interface.
     *
     * @param typeXml DOM of the type.
     *
     * @return whether the HTML represents an interface.
     */
    public boolean isAnnotation(Document typeXml) {
        String classHeader = typeXml.valueOf("//H2");
        //<H2>org.codavaj.javadoc.input Annotation Type AnnotationClass</H2>
        if ((classHeader != null) ) {
            return containsToken( "Annotation", classHeader ) && containsToken( "Type", classHeader );
        }

        return false;
    }

    /**
     * Identify whether the HTML represents a class.
     *
     * @param typeXml DOM of the type.
     *
     * @return whether the HTML represents a class.
     */
    public boolean isClass(Document typeXml) {
        String classHeader = typeXml.valueOf("//H2");

        // <H2>org.jumpi.impl.connector.mpi11 Class MpiDestination</H2>
        if ( classHeader != null ) {
            return containsToken("Class", classHeader );
        }
        return false;
    }

    /**
     * Identify whether the HTML represents an enum.
     *
     * @param typeXml DOM of the type.
     *
     * @return whether the HTML represents an enum.
     */
    public boolean isEnum(Document typeXml) {
        String classHeader = typeXml.valueOf("//H2");

        if ( classHeader != null ) {
            return containsToken("Enum", classHeader );
        }
        return false;
    }

    /**
     * Establish the name of the type from which the input type extends.
     *
     * @param t the Type
     * @param typeXml DOM of the type.
     * @param externalLinks list of externaly linked javadoc references.
     */
    public void extendedType(Type t, Document typeXml, List<?> externalLinks ) {
        /* If the extended type is part of the javadoc, then it is referenced by a link
         "//DT[starts-with(normalize-space(text()),'extends')]/descendant::A/@href"
        <DL>
        <DT>public interface SequenceGenerator</DT>
        <DT>extends
        <A href="../../../../org/jumpi/spi/Component.html">Component</A>
        </DT>
        </DL>
         */

        /* Note: Modified to handle parameterized types, like this for example:
        <DL>
          <DT>
            public enum <B>HTTPMethod</B>
          </DT>
          <DT>
            extends java.lang.Enum&lt;<A HREF="../../../../../com/google/appengine/api/urlfetch/HTTPMethod.html" title="enum in com.google.appengine.api.urlfetch">HTTPMethod</A>&gt;
          </DT>
        </DL>
        */

        List<?> extendedTypeDTs = typeXml.selectNodes(
                "//DT[starts-with(normalize-space(text()),'extends')]");
        // there should only be one
        if(extendedTypeDTs != null && extendedTypeDTs.size() > 1) {
            warning("There should only be one extends");
        }
        for (int i = 0; (extendedTypeDTs != null) && (i < extendedTypeDTs.size());
                i++) {
            Node node = (Node) extendedTypeDTs.get(i);

            if((node.getNodeType() == Node.ELEMENT_NODE && ((Element)node).getName().equalsIgnoreCase("DT"))) {
                String combinedText = convertNodesToString((Element)node, externalLinks);
                if ((combinedText != null) && combinedText.startsWith("extends")) {
                    combinedText = combinedText.substring("extends".length());
                    combinedText = combinedText.trim();

                    if (!"".equals(combinedText)) {
                        t.setSuperType(combinedText);
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     */
    public void determineTypeModifiers(Type type, Document typeXml, List<?> externalLinks) {
        // "//DT[parent::DL/preceding-sibling::H2 and not(contains(.,'All')) and not(contains(.,'Enclosing')) and not(contains(.,'Direct'))]"

        /*
        <H2>org.jumpi Interface Handle</H2>
        <DL>
          <DT>All Known Subinterfaces:</DT>
          <A href="../../org/jumpi/spi/Handle.html">Handle</A>
        </DL>
        <DL>
          <DT>Direct Known Subclasses:</DT>
          <A href="../../../org/jumpi/impl/connector/dummy/DummyAsyncConnector.html">DummyAsyncConnector</A>,
        </DL>
        <DL>
          <DT>Enclosing class:</DT>
          <A href="../../../../org/codavaj/javadoc/input/BufferCapabilities.html">BufferCapabilities</A>
        </DL>
        <DL>
          <DT>All Known Implementing Classes:</DT>
          <A href="../../org/jumpi/impl/HandleImpl.html">HandleImpl</A>
        </DL>
        <HR/>
       <DL>
        <DT/>
        <DT>Type Parameters:</DT>T - the type of the object contained in this MarshalledObject
      </DL>
       <DL>
          <DT>public interface Handle</DT>
        </DL>
        */

        Element typeDescriptorElement = (Element)typeXml.selectSingleNode("//DT[parent::DL/preceding-sibling::H2 and string-length(.) > 0 and not(contains(.,'All')) and not(contains(.,'Enclosing')) and not(contains(.,'Direct')) and not(contains(.,'Type Parameters:'))]");
        String typeDescriptor = convertNodesToString(typeDescriptorElement, externalLinks);

        // take the generics type parameters
        if ( typeDescriptor.indexOf("<") != -1 && typeDescriptor.lastIndexOf(">") != -1 && typeDescriptor.indexOf("<") < typeDescriptor.lastIndexOf(">")) {
            String typeParameters = typeDescriptor.substring(typeDescriptor.indexOf("<"), typeDescriptor.lastIndexOf(">")+1);
            type.setTypeParameters(typeParameters);
        }

        // strip off the type name
        String shortname = type.getShortName();

        if (typeDescriptor.indexOf(shortname) != -1) {
            typeDescriptor = typeDescriptor.substring(0,
                    typeDescriptor.indexOf(shortname));
        }

        //info( "visibility : " + typeDescriptor);
        if (typeDescriptor.indexOf(Type.MODIFIER_PUBLIC) != -1) {
            type.setPublic(true);
        } else if (typeDescriptor.indexOf(Type.MODIFIER_PROTECTED) != -1) {
            type.setProtected(true);
        } else if (typeDescriptor.indexOf(Type.MODIFIER_PRIVATE) != -1) {
            type.setPrivate(true);
        } else {
            // default visibility
        }

        if (typeDescriptor.indexOf(Type.MODIFIER_ABSTRACT) != -1) {
            type.setAbstract(true);
        }

        if (typeDescriptor.indexOf(Type.MODIFIER_STATIC) != -1) {
            type.setStatic(true);
        }

        if (typeDescriptor.indexOf(Type.MOFIFIER_FINAL) != -1) {
            type.setFinal(true);
        }

        if (typeDescriptor.indexOf(Type.MODIFIER_STRICTFP) != -1) {
            type.setStrictFp(true);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param t DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     * @param externalLinks list of externaly linked javadoc references.
     */
    public void determineImplementsList(Type t, Document typeXml, List<?> externalLinks) {
        /*
        "//DT[starts-with(normalize-space(text()),'implements')]/descendant::A/@href"
        <DT>implements java.lang.Runnable,
        <A href="../../../../org/jumpi/spi/component/TaskScheduler.html">TaskScheduler</A>
        </DT>

        Modified to handle parameterized types.
        */
        String extension = t.isInterface() ? "extends" : "implements";

        List<?> implementsTypeDTs = typeXml.selectNodes(
                "//DT[starts-with(normalize-space(text()),'" + extension
                + "')]");
        for (int i = 0; (implementsTypeDTs != null) && (i < implementsTypeDTs.size());
                i++) {
            Node node = (Node) implementsTypeDTs.get(i);

            String combinedText = convertNodesToString((Element)node, externalLinks);
            if ((combinedText != null) && combinedText.startsWith(extension)) {
                combinedText = combinedText.substring(extension.length());
                combinedText = combinedText.trim();

                if (!"".equals(combinedText)) {
                    List<String> words = tokenizeWordListWithTypeParameters(combinedText, " ,\t\n\r\f");
                    Iterator<String> it = words.iterator();
                    while(it.hasNext()) {
                        String typeName = it.next();

                        //debug ( "token: " + typeName);
                        t.addImplementsType(typeName);
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param html DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws DocumentException DOCUMENT ME!
     */
    public Document loadHtmlAsDom(String html)
        throws SAXException, IOException, DocumentException {
        return loadHtmlAsDom(new InputSource(new StringReader(html)));
    }

    /**
     * DOCUMENT ME!
     *
     * @param html DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws DocumentException DOCUMENT ME!
     */
    public Document loadHtmlAsDom(InputSource html)
        throws SAXException, IOException, DocumentException {
        org.cyberneko.html.parsers.DOMParser parser = new org.cyberneko.html.parsers.DOMParser();

        //XMLParserConfiguration parser = new HTMLConfiguration();
        parser.setFeature("http://cyberneko.org/html/features/augmentations",
            true);
        parser.setFeature("http://cyberneko.org/html/features/report-errors",
            false);
        parser.setProperty("http://cyberneko.org/html/properties/names/elems",
            "lower");
        parser.setProperty("http://cyberneko.org/html/properties/names/attrs",
            "lower");

        parser.setFeature("http://apache.org/xml/features/scanner/notify-char-refs",
            true);
        parser.setFeature("http://cyberneko.org/html/features/scanner/notify-builtin-refs",
            true);

        parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion",
            false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        org.cyberneko.html.filters.ElementRemover remover = new org.cyberneko.html.filters.ElementRemover();

        // set which elements to accept
        remover.acceptElement("html", null);
        remover.acceptElement("p", null);
        remover.acceptElement("h1", null);
        remover.acceptElement("h2", null);
        remover.acceptElement("h3", null);
        remover.acceptElement("table", null);
        remover.acceptElement("tr", null);
        remover.acceptElement("td", null);
        remover.acceptElement("hr", null);
        remover.acceptElement("i", null);
        remover.acceptElement("u", null);
        remover.acceptElement("a", new String[] { "href", "target" });
        remover.acceptElement("dt", null);
        remover.acceptElement("dl", null);

        // completely remove script elements
        remover.removeElement("script");
        remover.removeElement("link");

        org.apache.xerces.xni.parser.XMLDocumentFilter[] filters = new org.apache.xerces.xni.parser.XMLDocumentFilter[] {
                new org.cyberneko.html.filters.Purifier(), remover,
                new org.cyberneko.html.filters.Writer(baos, "UTF-8")
            };
        parser.setProperty("http://cyberneko.org/html/properties/filters",
            filters);
        parser.parse(html);

        //String html = new String( baos.toByteArray(), "UTF-8");
        //info( html );
        DOMReader xmlReader = new DOMReader();
        Document result = xmlReader.read(parser.getDocument());

        //info ( "XML " + prettyPrint(result));
        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param filename DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws DocumentException DOCUMENT ME!
     */
    public Document loadFileAsDom(String filename)
        throws SAXException, IOException, DocumentException {
        return loadHtmlAsDom(new InputSource(new FileInputStream(filename)));
    }

    /**
     * Pretty print the XML to a String
     *
     * @param doc DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String prettyPrint(Document doc) {
        try {
            ByteArrayOutputStream html = new ByteArrayOutputStream();
            OutputFormat outformat = OutputFormat.createPrettyPrint();
            outformat.setEncoding("UTF-8");

            XMLWriter writer = new XMLWriter(html, outformat);
            writer.write(doc);
            writer.flush();

            return new String(html.toByteArray(), "UTF-8");
        } catch (Exception e) {
            warning("Unable to pretty print.", e);
        }

        return doc.asXML();
    }
}
