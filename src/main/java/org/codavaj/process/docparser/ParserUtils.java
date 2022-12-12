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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codavaj.type.EnumConst;
import org.codavaj.type.Field;
import org.codavaj.type.Method;
import org.codavaj.type.Modifiable;
import org.codavaj.type.Parameter;
import org.codavaj.type.Type;
import org.cyberneko.html.filters.ElementRemover;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultText;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static com.rainerhahnekamp.sneakythrow.Sneaky.sneaked;

/**
 * for version ~ 1.6.x
 */
public class ParserUtils implements Parser {

    private static final Logger logger = Logger.getLogger(ParserUtils.class.getName());

    /**
     * Return the classname from a filename.
     *
     * @param filename filename - "java/lang/String.html"
     * @return the typename - java.lang.String
     */
    private static String typenameFromFilename(String filename) {
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
     * @param typename the typename - "java.lang.String"
     * @return filename "java/lang/String.html"
     */
    private static String filenameFromTypename(String typename) {
        typename = typename.replace('.', '/');
        typename = typename.replace('$', '.');

        return typename += ".html";
    }

    /** taglet */
    protected String getTag(String text) {
        if (text.contains(rb.getString("token.version"))) {
            return "version";
        } else if (text.contains(rb.getString("token.author"))) {
            return "author";
        } else if (text.contains(rb.getString("token.return"))) {
            return "return";
        } else if (text.contains(rb.getString("token.see"))) {
            return "see";
        } else if (text.contains(rb.getString("token.type_parameter"))) {
            // WARNNING depends on order of conditions, should be before of token.parameter
            return "typeparam";
        } else if (text.contains(rb.getString("token.parameter"))) {
            return "param";
        } else if (text.contains(rb.getString("token.since"))) {
            return "since";
        } else if (text.contains(rb.getString("token.exception"))) {
            return "throws";
        } else if (text.contains(rb.getString("token.deprecated"))) {
            return "deprecated";
        } else if (text.contains(rb.getString("token.specified_by"))) {
            return "ignore";
        } else if (text.contains(rb.getString("token.overrides"))) {
            return "ignore";
        } else if (text.contains(rb.getString("token.default"))) {
            return "ignore";
        } else {
logger.fine("unhandled tag: " + text);
            return text;
        }
    }

    /** without self tag, first, last white spaces */
    protected String tidyText(Node node, boolean trim) {
        String name = node.getName();
        if (trim) {
            return node.asXML().replace("<" + name + ">", "").replace("</" + name + ">", "").replaceAll("^\\s*", "").replaceAll("\\s*$", "");
        } else {
            return node.asXML().replace("<" + name + ">", "").replace("</" + name + ">", "");
        }
    }

    /** */
    protected void processDD(Type t, Node dd,String tag, List<String> commentText) {
        String text = tidyText(dd, true);
        switch (tag) { //.equals(tag)) {
        case "param":
            replaceA(((Element) dd), true);
            text = tidyText(dd, true).replaceFirst(" - ", " ");
            break;
        case "typeparam": // for type parameter @param at class description
            tag = "param";
            replaceA(((Element) dd), true);
            text = tidyText(dd, true).replaceFirst("([\\w\\$_\\.\\<\\>\\[\\]]+) - ", "<$1> ");
            break;
        case "exception":
        case "throws":
            Node typeNode = dd.selectSingleNode("*[position()=1]");
            String comment;
            String typeName;
            if (typeNode != null && typeNode.getNodeType() == Node.ELEMENT_NODE && "A".equals(typeNode.getName())) {
                typeName = convertNodesToString(typeNode);
                typeNode.detach();
                replaceA(((Element) dd), true);
                text = tidyText(dd, false);
                int p = text.indexOf(" - ");
                if (p >= 0) {
                    comment = text.substring(text.indexOf(" - ") + " -".length());
                } else {
                    comment = "";
                }
            } else {
                replaceA(((Element) dd), true);
                text = tidyText(dd, true);
                int p = text.indexOf(" - ");
                if (p >= 0) {
                    comment = text.substring(text.indexOf(" - ") + " -".length());
                    typeName = text.substring(0, p);
                } else {
                    comment = "";
                    typeName = text.trim();
                }
            }
            text = fqnm.toFullyQualifiedName(t, typeName) + comment.replaceAll("\\s$", "");
            break;
        case "see":
            if (text.contains(rb.getString("token.see.exclude.1")) ||
                text.contains(rb.getString("token.see.exclude.2"))) {
logger.fine("ignore 3: " + dd.asXML());
                return;
            }
            replaceA(((Element) dd), true); // TODO no need to replace?
            text = tidyText(dd, true);
            break;
        case "return":
            replaceA(((Element) dd), true);
            text = tidyText(dd, true);
            break;
        default:
            break;
        case "ignore":
            return;
        }
        String[] lines = text.split("\\n");
        commentText.add("@" + tag + " " + lines[0]);
        commentText.addAll(Arrays.asList(lines).subList(1, lines.length));
    }

    /**
     * Processes DD... in DT
     * @param nodes nodes include dt, dd...
     * @param tag dt text
     * @param commentText output
     * @return update index
     */
    private int processDT(Type t, List<Node> nodes, int j, String tag, List<String> commentText) {
        while (j < nodes.size() && "DD".equals(nodes.get(j).getName())) {
            Node dd = nodes.get(j++);
            processDD(t, dd, tag, commentText);
        }
        return j;
    }

    /** Processes A */
    private String processA(Node a) {
logger.fine("A: " + a.asXML());
        String href = a.valueOf("@href");
        if ((href.startsWith("http") || href.startsWith("../")) &&
                href.replace(".html#", ".").contains(a.getText().replaceAll("\\([\\w$_\\.,\\s\\[\\]]*\\)", ""))) {
            String link = hrefToLink(href);
            return "{@link " + link.replace("$", ".") + "}";
        } else {
            return a.asXML().replaceAll("^\\s*", "").replaceAll("\\s*$", "");
        }
    }

    /**
     * @param url javadoc link
     * @return a class name with field or method
     */
    private String hrefToLink(String url) {
        if (url.startsWith("http:") || url.startsWith("https:")) {
            if (isDefaultJavadocUrl(url) && url.contains("/api/")) {
                // standard sun api links - no need to explicitly mention these
                url = url.substring(url.indexOf("/api/") + "/api/".length());
            } else {
                for (String externalLink : externalLinks) {
                    if (url.startsWith(externalLink)) {
                        url = url.substring(externalLink.length());
                        if (url.startsWith("/")) {
                            url = url.substring(1);
                        }
                        break;
                    }
                }
            }
        }

        while (url.startsWith("../")) {
            url = url.substring("../".length());
        }

        String member = "";
        if (url.contains("#")) {
            member = url.substring(url.indexOf("#"));
            url = url.substring(0, url.indexOf("#"));
        }

        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }

        String typeName = typenameFromFilename(url);

        return typeName + member;
    }

    /**
     * details
     *
     * @param allNodes input
     * @param commentText output
     */
    protected void determineComment(Type t, List<Node> allNodes, List<String> commentText) {
        for (int i = 0; (allNodes != null) && (i < allNodes.size()); i++) {
            Node node = allNodes.get(i);
logger.finer("node: " + (node.getName() == null ? "TEXT" : node.getName()) + ": " + node.asXML());

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if ("DT".equals(node.getName())) {
                    i = processDT(t, allNodes, i + 1, getTag(node.getText()), commentText);
                } else if ("DD".equals(node.getName())) {
                    determineComment(t, replaceA(((Element) node), false), commentText);
                } else if ("P".equals(node.getName())) {
                    determineComment(t, replaceA(((Element) node), false), commentText);
                } else if ("DL".equals(node.getName())) {
                    List<Node> nodes = node.selectNodes("*[name()='DT' or name()='DD']");
                    int j = 0;
                    while (j < nodes.size()) {
                        Node dt = nodes.get(j++);
                        if (dt.getText().isEmpty()) {
                            continue; // <DT/> in class comment type parameter
                        } else if ("DD".equals(dt.getName())) {
                            determineComment(t, ((Element) dt).content(), commentText);
                            continue;
                        }

                        j = processDT(t, nodes, j, getTag(dt.getText()), commentText);
                    }
                } else if ("A".equals(node.getName())) {
                    // TODO this makes unexpected new lines
                    commentText.add(processA(node));
                } else {
logger.fine("unhandled node: " + node.getName());
                    replaceA(((Element) node), true);
                    String text = node.asXML();
                    String[] lines = text.split("\\n");
                    for (String line : lines) {
                        commentText.add(line.trim());
                    }
                }
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                String text = node.getText().replaceAll("^\\n", "").replaceAll("\\n$", "");
                if (text.contains(rb.getString("token.comment.exclude.1")) ||
                    text.contains(rb.getString("token.comment.exclude.2"))) {
                    if (i + 1 < allNodes.size() && "A".equals(allNodes.get(i + 1).getName())) {
logger.fine("ignore 1.1: " + text + allNodes.get(i + 1).asXML());
                        i++;
                    } else {
logger.fine("ignore 1.2: " + text);
                    }
                } else {
                    if (!text.isEmpty()) {
                        String[] lines = text.split("\\n");
                        for (String line : lines) {
                            commentText.add(line.trim());
                        }
                    }
                }
            } else {
logger.fine("ignore 5: " + node.asXML());
            }
        }
    }

    /** replace a tag to @link */
    protected List<Node> replaceA(Element element, boolean recursive) {
        List<Node> nodes = element.content();
        for (Node node : nodes) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                boolean ignore = false;
                if ("A".equals(node.getName())) {
                    int index = nodes.indexOf(node);
logger.finer("index: " + index);
                    if (index > 0) {
                        Node before = nodes.get(index - 1);
logger.finer("before: " + before.asXML());
                        if (before.getNodeType() == Node.TEXT_NODE && (
                            before.getText().contains(rb.getString("token.comment.exclude.1")) ||
                            before.getText().contains(rb.getString("token.comment.exclude.2")))) {
                            ignore = true;
logger.finer("ignore 1.0: " + before.getText() + node.asXML());
                        }
                    }
                    if (!ignore) {
                        String string = processA(node);
                        if (string.startsWith("{@link")) {
                            nodes.set(nodes.indexOf(node), new DefaultText(string));
                        }
                    }
                } else {
                    if (recursive) {
                        replaceA(((Element) node), true);
                    }
                }
            }
        }
        return nodes;
    }

    /*
     * details
     * a field comment
     * a method comment
     */
    protected List<String> determineComment(Type t, Element enclosingNode) throws ParseException {
        if (enclosingNode == null) {
            return null;
        }

        // LI/DL...
        List<Node> allNodes = enclosingNode.content();
        List<String> commentText = new ArrayList<>();

//System.err.println("---------------------");
//allNodes.forEach(n -> System.err.println(n.asXML() + " *****"));
        determineComment(t, allNodes, commentText);

        return commentText;
    }

    /*
     * details (constant)
     * a field comment
     * a method comment
     */
    protected String determineDefault(Element enclosingNode) throws ParseException {
        if (enclosingNode == null) {
            return null;
        }
        Node node = enclosingNode.selectSingleNode(".//DL/DT[contains(text(), '" + rb.getString("token.default") + "')]/../DD");
        return node != null ? node.getText() : null;
    }

    /**
     * Processes class comment. (1st entry)
     *
     * @param type type of this class
     * @param typeXml xml file of this class
     */
    protected void determineClassComment(Type type, Document typeXml) {
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
        List<Node> allNodes = typeXml.getRootElement().content();

        // H2 indicates class descriptor
        boolean parseOn = false;
        List<Node> commentNodes = new ArrayList<>();

        for (int i = 0; (allNodes != null) && (i < allNodes.size()); i++) {
            Node node = allNodes.get(i);

            if (!parseOn && (node.getNodeType() == Node.ELEMENT_NODE)
                    && "H2".equals(node.getName())) {
                // H2 starts the parsing off
                while (i < allNodes.size() - 1) {
                    i++;
                    node = allNodes.get(i);

                    if ((node.getNodeType() == Node.ELEMENT_NODE)
                            && "HR".equals(node.getName())) {
                        break;
                    }
                }

                parseOn = true;

                continue;
            }

            if (parseOn) {
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if ("P".equals(node.getName())) {
                        commentNodes.add(node);
                    } else if ("HR".equals(node.getName())) {
                        parseOn = false;
                    }
                } if (node.getNodeType() == Node.TEXT_NODE) {
                    commentNodes.add(node);
                }
            }
        }

        List<String> commentText = new ArrayList<>();
//System.err.println("---------------------");
//System.err.println(typeXml.asXML());
//commentNodes.forEach(n -> System.err.println(n.asXML() + " *****"));
        determineComment(type, commentNodes, commentText);

        // for type parameter
        allNodes = typeXml.selectNodes("//DL/DT[contains(text(),'" + rb.getString("token.type_parameter") + "')]/..");
        determineComment(type, allNodes, commentText);

        type.setComment(commentText);
    }

    /**
     * Processes constants. (1st entry)
     *
     * @param allConstants xml file for constant.
     * @param types {@link Type} objects already parsed.
     * @param lenient whether to warn when type not found
     */
    protected void determineConstants(Document allConstants, Map<String, Type> types, boolean lenient) {
        String xpath = "//P/TABLE/TR[position() != 1]";
        determineConstants(xpath, allConstants, types, lenient);
    }

    /** constants */
    protected String[] getConstantsXpaths() {
        return new String[] {
            "TD[position()=2]/A/@href",
            "TD[position()=2]/A",
            "TD[position()=3]",
        };
    }

    /** constants */
    protected void determineConstants(String xpath, Document allConstants, Map<String, Type> types, boolean lenient) {
        List<Node> constantList = allConstants.selectNodes(xpath);
        for (int i = 0; (constantList != null) && (i < constantList.size());
                i++) {
            Node constantNode = constantList.get(i);

            /*
             each row has field with the modifiers and the type
             then one with the type ( href ) and field name text()
             and then the constant itself
            <TR>
              <A/>
              <TD>public† static† final† java.lang.String</TD>
              <TD>
                <A href="org/codavaj/process/antrunner/AntRunner.html#PROPERTY_ANTRUNNER_TARGET">PROPERTY_ANTRUNNER_TARGET</A>
              </TD>
              <TD>"org.codavaj.antrunner.target"</TD>
            </TR>
            */
            String[] xpaths = getConstantsXpaths();
            String typeName = javadocLinkToTypename(constantNode.valueOf(xpaths[0]));
            String fieldName = constantNode.valueOf(xpaths[1]);
            String constantValue = constantNode.valueOf(xpaths[2]);

            //logger.fine( typeName + "#" + fieldName +"=" +constantValue );
            Type type = types.get(typeName);

            if (type == null) {
                if ( !lenient ) {
                    logger.log(Level.WARNING, "Unable to find type " + typeName);
                }

                continue;
            }

            Field field = type.lookupFieldByName(fieldName);

            if (field == null) {
                logger.log(Level.WARNING, "Unable to find field " + typeName + "#" + fieldName);

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
    private Object determineConstantValue(String typeName, String constantvalue) {
        //( Boolean, Byte, Char, Double, Float, Integer, Long, Short )
        Object value = null;

        if ("java.lang.String".equals(typeName)) {
            if ((constantvalue.charAt(0) != '"')
                    && (constantvalue.charAt(constantvalue.length() - 1) != '"')) {
                logger.log(Level.WARNING, 
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
            return (char) Integer.valueOf(constantvalue).intValue();
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
            if (constantvalue.contains("l")) {
                constantvalue = constantvalue.substring(0, constantvalue.indexOf("l"));
            }

            if (constantvalue.contains("L")) {
                constantvalue = constantvalue.substring(0, constantvalue.indexOf("L"));
            }

            return Long.valueOf(constantvalue);
        } else if ("short".equals(typeName)) {
            return Short.valueOf(constantvalue);
        }

        logger.log(Level.WARNING, "unknown constant type: " + typeName);

        return value;
    }

    /**
     * Processes details. (1st entry)
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     */
    protected void determineDetails(Type type, Document typeXml) {
        /*
         an example method
        <H3>process</H3> public void process() throws
        <A href="../../../../org/codavaj/ProcessException.html">ProcessException</A>
        <DL>...
         an example field
        <H3>PROPERTY_ANTRUNNER_FILENAME</H3> public static final java.lang.String PROPERTY_ANTRUNNER_FILENAME
        <DL>...
         */

        // H3 indicates method or field names, the following text up to the next NON 'A' element
        // belongs to the summary
        List<Node> allNodes = typeXml.getRootElement().content();
        determineDetails(allNodes, c -> {
                if (!c.parseOn && (c.node.getNodeType() == Node.ELEMENT_NODE)
                        && "H3".equals(c.node.getName())) {
                    // H3 starts the parsing off
                    c.parseOn = true;
                    c.name = c.node.getText().trim();
                    c.text = "";

                    return true;
                } else {
                    return false;
                }
            },
            sneaked(c -> {
                // finished
                Element commentNode = c.node.getName().equals("DL")
                    ? (Element) c.node : null;

                c.parseOn = false;

                if ((c.text.contains("(")) && (c.text.contains(")"))
                        && (c.text.indexOf(")") >= c.text.indexOf("("))) {
                    determineMethodDetails(type, c.text, c.name, commentNode);
                } else {
                    // Could be either a field, enum constant, or element detail
                    determineFieldDetails(type, c.text, c.name, commentNode);
                }

                c.text = "";
                c.name = "";
            }));
    }

    /** context for detail parsing */
    protected static class Context {
        /** umm... */
        boolean parseDone = false;
        boolean parseOn = false;
        String name = "";
        String text = "";
        Node node;
    }

    /**
     * Creates method signature, html tags are removed.
     *
     * @param allNodes
     * @param starter parser on
     * @param ender parser off
     */
    protected void determineDetails(List<Node> allNodes, Function<Context, Boolean> starter, Consumer<Context> ender) {
        Context c = new Context();

        for (int i = 0; (allNodes != null) && (i < allNodes.size()); i++) {
            c.node = allNodes.get(i);

            if (starter.apply(c)) {
                continue;
            }

            if (c.parseOn) {
                if (c.node.getNodeType() == Node.TEXT_NODE) {
                    c.text += c.node.getStringValue();
                } else if ((c.node.getNodeType() == Node.ELEMENT_NODE) && "A".equals(c.node.getName()) && c.node.getText().length() > 1 && c.node.valueOf("@href").contains(c.node.getText())) {
                    c.text += javadocLinkToTypename(c.node.valueOf("@href"));
                } else if ((c.node.getNodeType() == Node.ELEMENT_NODE) && "A".equals(c.node.getName())) {
                    c.text += convertNodesToString((Element) c.node);
                } else if ((c.node.getNodeType() == Node.ELEMENT_NODE) && "H3".equals(c.node.getName())) { // v.13
                    c.text += convertNodesToString((Element) c.node);
                } else if (c.node.getNodeType() == Node.COMMENT_NODE) {
                    continue;
                } else if (c.node.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                    if (c.node.getStringValue().equals("&lt;")) {
                        c.text += "<";
                    } else if (c.node.getStringValue().equals("&gt;")) {
                        c.text += ">";
                    } else if (c.node.getStringValue().equals("&nbsp;")) {
                        c.text += " ";
                    } else {
                        continue;
                    }
                } else if (c.node.getNodeType() == Node.ELEMENT_NODE) {
                    ender.accept(c);
                }
            }
        }
    }

    /** method */
    protected void determineMethodDetails(Type type, String text, String name, Element commentNode) throws ParseException {
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

        List<Parameter> params = determineMethodParameterList(type, methodParams);
        Method m = type.lookupMethodByName(name, params);

        if (m == null) {
            // try looking up constructors
            m = type.lookupConstructor(params);
        }

        if (m != null) {
            extractMethodModifiers(m, text);

            //parse the throws list and assign the found exceptions to the method
            determineThrowsList(throwsParams, m);
            m.setComment(determineComment(type, commentNode));
        } else {
            logger.log(Level.WARNING, 
                "failed to find method or constructor with name "
                + name + " in type " + type.getTypeName());
         }
    }

    /** Could be either a field, enum constant, or element detail */
    protected void determineFieldDetails(Type type, String text, String name, Element commentNode) {
        Field f = type.lookupFieldByName(name);
        if (f != null) {
            // field
            extractFieldModifiers(f, text);
            f.setComment(determineComment(type, commentNode));
        } else {
            EnumConst ec = type.lookupEnumConstByName(name);

            if (ec != null) {
                // enum constant
                ec.setComment(determineComment(type, commentNode));
            } else {
                Method m = type.lookupMethodByName(name, new ArrayList<>()); // annotation elements have no params

                if ( m != null ) {
                    // method
                    extractMethodModifiers(m, text);
                    m.setComment(determineComment(type, commentNode));
                    m.setDefaultValue(determineDefault(commentNode));
                } else {
                    logger.log(Level.WARNING, "No field, enum constant, or annotation element with name " + name + " in type " + type.getTypeName());
                }
            }
        }
    }

    private List<Parameter> determineMethodParameterList(Type t, String methodParams) {
        List<Parameter> params = new ArrayList<>();

        List<String> words = tokenizeWordListWithTypeParameters(methodParams, ",");
        for (String word : words) {
            Parameter p = determineParameter(t, word, true);
            params.add(p);
        }

        return params;
    }

    protected List<String> tokenizeWordListWithTypeParameters(String text, String delimeters)
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

    /** split by " \t\n\r\f" */
    private void extractMethodModifiers(Method m, String text) {
        List<String> words = tokenizeWordListWithTypeParameters(text, " \t\n\r\f");
        for (String word : words) {
            m.assignModifier(word);
        }
    }

    /** split by " \t\n\r\f" */
    private void determineThrowsList(String throwsParams, Method m) {
        List<String> words = tokenizeWordListWithTypeParameters(throwsParams, " ,\t\n\r\f");
        for (String word : words) {
            m.addThrows(word);
        }
    }

    /** split by " \t\n\r\f" */
    private void extractFieldModifiers(Field f, String text) {
        List<String> words = tokenizeWordListWithTypeParameters(text, " \t\n\r\f");
        for (String word : words) {
            f.assignModifier(word);
        }
    }

    /**
     * DOCUMENT ME! (1st entry)
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     */
    protected void determineFields(Type type, Document typeXml) {
        // get the method details out of the Field Summary table
        // this information is missing the modifier info which is only
        // availible in the field details

        /* javadoc pre 1.5
        <TR>
        <TD> †
          <A href="../../../../org/codavaj/javadoc/input/DefaultInterface.html">DefaultInterface</A>[]
        </TD>
        <TD>
          <A href="../../../../org/codavaj/javadoc/input/ClassWithFields.html#defintarray_">defintarray_</A>  †††††††††††
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
        determineFields(type, typeXml, "TD[position()=2]/A");
    }

    /** fields */
    protected String getFieldsXpath() {
        return "//TR[contains(parent::TABLE/TR[1], '" + rb.getString("token.field_summary") + "')][position()>1]";
    }

    /** fields */
    protected void determineFields(Type type, Document typeXml, String nameXpath) {
        String fieldsXpath = getFieldsXpath();
        List<Node> fieldList = typeXml.selectNodes(fieldsXpath);
if (fieldList == null || fieldList.isEmpty()) {
 // TODO so f*cking
 // if jdk supported xpath2.0, we could use regex for xpath
 if (rb.getLocale().equals(Locale.JAPANESE)) {
  if (!fieldsXpath.equals(fieldsXpath.replace("サマリー", "概要"))) {
   logger.warning("no fields: " + fieldsXpath);
   fieldsXpath = fieldsXpath.replace("サマリー", "概要");
   fieldList = typeXml.selectNodes(fieldsXpath);
   logger.fine("fields: " + fieldList.size());
  }
 }
}

        for (int i = 0; fieldList != null && i < fieldList.size(); i++) {
            Field field = type.createField();

            Node fieldNode = fieldList.get(i);

            // get the return type description for known types
            Element fieldtypeNode = (Element) fieldNode.selectSingleNode("TD[position()=1]");

            String fieldtypeParam = convertNodesToString(fieldtypeNode);

//            logger.fine("fieldtype: " + fieldtypeParam);
            Parameter temp = determineParameter(type, fieldtypeParam, false);
            field.setType(temp.getType());
            field.setArray(temp.isArray());
            field.setDegree(temp.getDegree());
            field.setName(temp.getName());
            field.setTypeArgumentList(temp.getTypeArgumentList());

            // now we get the parameter list
            Element fieldNameNode = (Element) fieldNode.selectSingleNode(nameXpath);
            String fieldName = fieldNameNode.getText();

//            logger.fine("fieldname: " + fieldName);
            field.setName(fieldName);
        }
    }

    /**
     * inner types (1st entry)
     */
    protected void determineInnerTypes(Type type, Document typeXml) throws IOException {
        determineInnerTypes(type, typeXml, "TD[position()=2]/A");
    }

    /** inner types */
    protected String getInnerTypesXpath() {
        return "//TR[contains(parent::TABLE/TR[1], '" + rb.getString("token.nested_class_summary") + "')][position()>1]";
    }

    /** inner types */
    protected void determineInnerTypes(Type type, Document typeXml, String nameXpath) throws IOException {
        List<Node> innerTypeNodes = typeXml.selectNodes(getInnerTypesXpath());

        for (int i = 0; innerTypeNodes != null && i < innerTypeNodes.size(); i++) {
            Node innerTypeNode = innerTypeNodes.get(i);

            String innerTypeName = convertNodesToString(innerTypeNode.selectSingleNode(nameXpath));
logger.fine(innerTypeName);

            Type innerType = type.createInnerType();
            innerType.setTypeName(innerTypeName);

            processType(innerType);
        }
    }

    /**
     * enum constants (1st entry)
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     */
    protected void determineEnumConsts(Type type, Document typeXml) {
        String enumConstsXpath = "//TR[contains(parent::TABLE/TR[1], '" + rb.getString("token.enum_constant_summary") + "')][position()>1]";
        determineEnumConsts(enumConstsXpath, type, typeXml, "TD[position()=1]/A");
    }

    /** enum constants */
    protected void determineEnumConsts(String enumConstsXpath, Type type, Document typeXml, String nameXpath) {
        List<Node> enumConstList = typeXml.selectNodes( enumConstsXpath );

        for (int i = 0; (enumConstList != null) && (i < enumConstList.size()); i++) {
            EnumConst enumConst = type.createEnumConst();

            Node enumConstNode = enumConstList.get(i);

            // now we get the parameter list
            Element enumConstNameNode = (Element) enumConstNode.selectSingleNode(nameXpath);
            String enumConstName = enumConstNameNode.getText();

            //logger.fine( "enumConstName: " + enumConstName );
            enumConst.setName(enumConstName);
        }
    }

    /**
     * DOCUMENT ME! (1st entry)
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     */
    protected void determineConstructors(Type type, Document typeXml) {
        // get the constructor details out of the Constructor Summary table
        // the first table field pertains to the "return type" which is always blank
        // since the constructor return is the class itself.

        /* Javadoc pre 1.5
        <TR>
        <TD> †</TD>
        <TD>
          <A href="../../../../org/codavaj/javadoc/input/ClassWithConstructors.html#ClassWithConstructors(org.codavaj.javadoc.input.ArrayReturnClass)">ClassWithConstructors</A>(
          <A href="../../../../org/codavaj/javadoc/input/ArrayReturnClass.html">ArrayReturnClass</A>†p1) †††††††††††
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
        String constructorsXpath = "//TR[contains(parent::TABLE/TR,'" + rb.getString("token.constructor_summary") + "')][position()>1]";
        determineConstructors(constructorsXpath, type, typeXml);
    }

    /** constructor */
    protected List<Node> getConstructorParamlistNodes(Node methodNode) {
        // now we get the parameter list
        Element paramlistNode = (Element) methodNode.selectSingleNode(
                "TD[position()=2]");

        if (paramlistNode == null) {
            // constructor table is simplified if all constructors are public
            paramlistNode = (Element) methodNode.selectSingleNode(
                    "TD[position()=1]");
        }

        return paramlistNode.content();
    }

    /** constructor */
    protected void determineConstructors(String constructorsXpath, Type type, Document typeXml) {
        List<Node> methodList = typeXml.selectNodes(constructorsXpath);
if (methodList == null || methodList.isEmpty()) {
 // TODO so f*cking
 // if jdk supported xpath2.0, we could use regex for xpath
 if (rb.getLocale().equals(Locale.JAPANESE)) {
  if (!constructorsXpath.equals(constructorsXpath.replace("サマリー", "概要"))) {
   logger.warning("no constructors: " + constructorsXpath);
   constructorsXpath = constructorsXpath.replace("サマリー", "概要");
   methodList = typeXml.selectNodes(constructorsXpath);
   logger.fine("constructors: " + methodList.size());
  }
 }
}

        for (int i = 0; (methodList != null) && (i < methodList.size()); i++) {
            Method method = type.createConstructor();

            Node methodNode = methodList.get(i);

            List<Node> paramlistNodes = getConstructorParamlistNodes(methodNode);
            determineMethodParameters(type, method, paramlistNodes);
        }
    }

    /** parameter */
    private void determineMethodParameters(Type t, Method method, List<Node> paramlistNodes) {
        Element methodNameElement = (Element) paramlistNodes.get(0); // link in the same file
        method.setName(methodNameElement.getText());

        //logger.fine(" methodname: " + method.getMethodName());
        StringBuilder methodParams = new StringBuilder();

        for (int paramIdx = 1; paramIdx < paramlistNodes.size(); paramIdx++) {
            //expect the methodName in the first parameter, so skip it
            Node paramNode = paramlistNodes.get(paramIdx);

            // logger.fine( paramNode.getNodeTypeName()+" "+paramNode.getStringValue() );
            // need to combine method description into a single text which can then
            // be parsed easily. TypeVariables tend to have length 1 ( E, V, K etc. ) which can easily match one character of the link to the generic parent
            if (paramNode.getNodeType() == Node.ELEMENT_NODE && "A".equals(paramNode.getName()) && paramNode.getText().length() > 1 && paramNode.valueOf("@href").contains(paramNode.getText())) {
                // reference to type
                methodParams.append(javadocLinkToTypename(paramNode.valueOf("@href")));
            } else if (paramNode.getNodeType() == Node.ELEMENT_NODE) {
                // reference to a parameterized type - use just the name
                methodParams.append(convertNodesToString((Element) paramNode));
            } else if (paramNode.getNodeType() == Node.TEXT_NODE) {
                methodParams.append(paramNode.getStringValue());

                if (methodParams.toString().contains("(")) {
                    methodParams = new StringBuilder(methodParams.substring(methodParams.indexOf(
                            "(") + 1));
                }
            } else if (paramNode.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                if(paramNode.getStringValue().equals("&lt;"))
                    methodParams.append("<");
                else if(paramNode.getStringValue().equals("&gt;"))
                    methodParams.append(">");
                else if(paramNode.getStringValue().equals("&nbsp;"))
                    methodParams.append(" ");
            }

            if (methodParams.toString().contains(")")) {
                // reached end of useful description - skip method summary
                methodParams = new StringBuilder(methodParams.substring(0,
                        methodParams.indexOf(")")));

                break;
            }
        }

        // now we parse "type name" comma separated pairs from the result
        List<Parameter> params = determineMethodParameterList(t, methodParams.toString());
        for (Parameter param : params) {
            method.addParameter(param);
        }
    }

    /**
     * parameter
     * @throws ParseException
     */
    private Parameter determineParameter(Type t, String parameterText, boolean parseName) {
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
                } else if (word.contains("<")) {
                    // parameterized type with type parameter arguments
                    p.setTypeArgumentList(word.substring(word.indexOf("<")));
                    p.setType(fqnm.toFullyQualifiedName(t, word.substring(0, word.indexOf("<"))));
                } else {
                    p.setType(fqnm.toFullyQualifiedName(t, word));
                }

                if (parseName && it.hasNext()) {
                    // name comes after the type
                    String name = it.next();
                    p.setName(name);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "failed to parse parameterText: " + parameterText);
            throw new ParseException(parameterText, e);
        }

        if ( parseName && p.getName() == null ) {
            logger.log(Level.WARNING, "failed to parse parameter name from : " + parameterText);
        }
        return p;
    }

    /**
     * method return
     * @throws ParseException
     */
    private Parameter determineMethodReturnParameter(Type t, Method m, String parameterText) {
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
            for (String word : words) {
                if (Modifiable.isModifier(word)) {
                    // skip modifiers
                    continue;
                } else if (word.startsWith("<")) {
                    m.setTypeParameters(word);
                } else {
                    p.setType(fqnm.toFullyQualifiedName(t, word));
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "failed to parse method return parameter in parameterText: " + parameterText);
            throw new ParseException(parameterText, e);
        }

        return p;
    }

    /**
     * Converts 'A' links to a type and mixes content on same level together so
     * plain text can be parsed.
     *
     * @param contentElement element who's top level content is merged.
     * @return plain text to be parsed.
     */
    private String convertNodesToString(Element contentElement) {
        List<Node> returnparamNodes = contentElement.content();

        StringBuilder text = new StringBuilder();

        for (int paramIdx = 0;
                (returnparamNodes != null)
                && (paramIdx < returnparamNodes.size()); paramIdx++) {
            Node paramNode = returnparamNodes.get(paramIdx);
            text.append(convertNodesToString(paramNode));
        }

        return text.toString();
    }

    /** */
    protected String convertNodesToString(Node paramNode) {
        // need to combine method description into a single text which can then
        // be parsed easily. If we link to another type rather than a generic type variable, the name of the link's text matches the classname
        if (paramNode.getNodeType() == Node.ELEMENT_NODE && "A".equals(paramNode.getName()) && paramNode.getText().length() > 1 && paramNode.valueOf("@href").contains(paramNode.getText())) {
            // reference to type
            return javadocLinkToTypename(paramNode.valueOf("@href"));
        } else if (paramNode.getNodeType() == Node.ELEMENT_NODE) {
            return convertNodesToString((Element)paramNode);
        } else if (paramNode.getNodeType() == Node.TEXT_NODE) {
            return paramNode.getStringValue();
        } else if (paramNode.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
            if (paramNode.getStringValue().equals("&lt;")) {
                return "<";
            } else if(paramNode.getStringValue().equals("&gt;")) {
                return ">";
            } else if(paramNode.getStringValue().equals("&nbsp;")) {
                return " ";
            }
        }
        return ""; // umm...
    }

    /**
     * get the method details out of the Method Summary table
     * this information is missing the throws info which is only
     * available in the method details
     * (1st entry)
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     */
    protected void determineMethods(Type type, Document typeXml) {
        // "//TR[contains(parent::TABLE/TR/TD,'Method Summary') and not(contains(TD,'Method Summary'))]"
        // the first column gives the return type
        // the second column gives the method name in a link, followed by (
        // and then a comma separated list of either links or non-included typenames
        // followed by a ) and a textual summary of the return.

        /* Javadoc pre 1.5
        <TR>
        <TD> †
          <A href="../../../java/lang/String.html">String</A>[]
        </TD>
        <TD>
          <A href="../../../org/w3c/dom/Element.html#getAttributeNS(java.lang.String, java.lang.String)">getAttributeNS</A>(
          <A href="../../../java/lang/String.html">String</A>†namespaceURI,
          <A href="../../../java/lang/String.html">String</A>†localName) †††††††††† Retrieves an attribute value by local name and namespace URI.
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
              <A href="../../../../org/codavaj/javadoc/input/KillerGenericsStuff.html#max(java.util.Collection)">max</A>(java.util.Collection< ? extends T>† coll) †††††††††† Returns the maximum element of the given collection, according to the
              <I>natural ordering</I> of its elements.
            </TD>
          </TR>
         */
        String methodXpath = "//TR[contains(parent::TABLE/TR,'" + rb.getString("token.method_summary") + "')][position()>1]";
        determineMethods(methodXpath, type, typeXml);
    }

    /** method */
    protected List<Node> getMethodParamlistNodes(Node methodNode) {
        // now we get the parameter list
        Element paramlistNode = (Element) methodNode.selectSingleNode("TD[position()=2]");
        return paramlistNode.content();
    }

    /** method */
    protected void determineMethods(String methodXpath, Type type, Document typeXml) {

        List<Node> methodList = typeXml.selectNodes(methodXpath);
if (methodList == null || methodList.isEmpty()) {
 // TODO so f*cking
 // if jdk supported xpath2.0, we could use regex for xpath
 if (rb.getLocale().equals(Locale.JAPANESE)) {
  if (!methodXpath.equals(methodXpath.replace("サマリー", "概要"))) {
   logger.warning("no methods: " + methodXpath);
   methodXpath = methodXpath.replace("サマリー", "概要");
   methodList = typeXml.selectNodes(methodXpath);
   logger.fine("methods: " + methodList.size());
  }
 }
}

        for (int i = 0; (methodList != null) && (i < methodList.size()); i++) {
            Method method = type.createMethod();

            Node methodNode = methodList.get(i);

            // get the return type
            Element returnparamNode = (Element) methodNode.selectSingleNode("TD[position()=1]");

            String methodReturnParam = convertNodesToString(returnparamNode);

            Parameter returnType = determineMethodReturnParameter(type, method, methodReturnParam);

            method.setReturnParameter(returnType);

            //if ( paramlistNode == null ) continue; // no method
            List<Node> paramlistNodes = getMethodParamlistNodes(methodNode);

            determineMethodParameters(type, method, paramlistNodes);
        }
    }

    /**
     * annotation elements (1st entry).
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     */
    protected void determineElements(Type type, Document typeXml) {

        /* Javadoc 1.6
    <TABLE>
      <TR>Required Element Summary</TR>
      <TR>
        <TD> †
          <A href="../../../../org/codavaj/javadoc/input/AnnotationDefault.html">AnnotationDefault</A>
        </TD>
        <TD>
          <A href="../../../../org/codavaj/javadoc/input/AnnotationParameterized.html#annotParam()">annotParam</A>  †††††††††††
        </TD>
      </TR>...
    </TABLE>  †
         */
        determineElements(type, typeXml, "TD[position()=2]/A");
    }

    /** */
    protected String[] getElementsXpaths() {
        return new String[] {
            "//TR[contains(parent::TABLE/TR,'" + rb.getString("token.required_element_summary") + "')][position()>1]",
            "//TR[contains(parent::TABLE/TR,'" + rb.getString("token.optional_element_summary") + "')][position()>1]"
        };
    }

    /** annotation elements. */
    protected void determineElements(Type type, Document typeXml, String nameXpath) {
        String[] xpaths = getElementsXpaths();
        List<Node> methodList = typeXml.selectNodes(xpaths[0]);

        for (int i = 0; (methodList != null) && (i < methodList.size()); i++) {
            Method method = type.createMethod();

            Node methodNode = methodList.get(i);

            // get the return type
            Element returnparamNode = (Element) methodNode.selectSingleNode("TD[position()=1]");

            String methodReturnParam = convertNodesToString(returnparamNode);

            Parameter returnType = determineMethodReturnParameter(type, method, methodReturnParam );

            method.setReturnParameter(returnType);

            // now we get the element name
            Element elemenNameNode = (Element) methodNode.selectSingleNode(nameXpath);

            String elementName = elemenNameNode.getText();
            method.setName(elementName);
        }

        methodList = typeXml.selectNodes(xpaths[1]);

        for (int i = 0; (methodList != null) && (i < methodList.size()); i++) {
            Method method = type.createMethod();

            Node methodNode = methodList.get(i);

            // get the return type
            Element returnparamNode = (Element) methodNode.selectSingleNode("TD[position()=1]");

            String methodReturnParam = convertNodesToString(returnparamNode);

            Parameter returnType = determineMethodReturnParameter(type, method, methodReturnParam);

            method.setReturnParameter(returnType);

            // now we get the element name
            Element elemenNameNode = (Element) methodNode.selectSingleNode(nameXpath);

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
     * @return a classname corresponding to a relative javadoc link.
     * @throws UnresolvedExternalLinkException if an absolute link is not resolved in the external links
     */
    private String javadocLinkToTypename(String link) {
        if (link == null) return null;
        if (link.startsWith("http:") || link.startsWith("https:")) {
            // external link - try to resolve any java.sun.com external links
            // http://java.sun.com/j2se/1.3/docs/api/
            // http://java.sun.com/j2se/1.4.2/docs/api/
            // http://java.sun.com/j2se/1.5.0/docs/api/
            if (isDefaultJavadocUrl(link) && link.contains("/api/")) {
                // standard sun api links - no need to explicitly mention these
                link = link.substring(link.indexOf("/api/") + "/api/".length());
            } else {
                boolean found = false;
                for (int i = 0; externalLinks != null && i < externalLinks.size(); i++) {
                    String externalLink = externalLinks.get(i);
                    if (link.startsWith(externalLink)) {
                        link = link.substring(externalLink.length());
                        if (link.startsWith("/")) {
                            link = link.substring(1);
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new UnresolvedExternalLinkException("External link " + link + " not specified in program arguments.");
                }
            }
        }

        while (link.startsWith("../")) {
            link = link.substring("../".length());
        }

        if (link.contains("#")) {
            link = link.substring(0, link.indexOf("#"));
        }

        if (link.contains("?")) {
            link = link.substring(0, link.indexOf("?"));
        }

        return typenameFromFilename(link);
    }

    /** */
    private static final String[] defaultJavadocUrls = {
        "http://java.sun.com/",
        "https://docs.oracle.com/",
    };

    /** */
    private boolean isDefaultJavadocUrl(String url) {
        return Arrays.stream(defaultJavadocUrls).anyMatch(url::startsWith);
    }

    /**
     * Extract all class names from the "allclasses-frame.html" file.
     *
     * @param alltypesXml the DOM of the "allclasses-frame.html" file
     * @return a list of fully qualified class names.
     */
    protected List<String> getAllFqTypenames(Document alltypesXml) {
        String xpath = "//A[@target='classFrame']/@href";
        return getAllFqTypenames(alltypesXml, xpath);
    }

    /** */
    protected List<String> getAllFqTypenames(Document alltypesXml, String xpath) {
        List<Node> classes = alltypesXml.selectNodes(xpath);
        List<String> result = new ArrayList<>();

        for (Node node : classes) {
            String file = node.getText();
            String typeName = typenameFromFilename(file);

            fqnm.add(typeName);
            result.add(typeName);
        }

        return result;
    }

    /** */
    private boolean containsToken(String token, String input) {
        if (isLanguageOf(Locale.JAPANESE)) {
            // japanese is not separated by white space.
            return input.contains(token);
        } else {
            StringTokenizer tokenizer = new StringTokenizer(input);
            while (tokenizer.hasMoreTokens() ) {
                if (tokenizer.nextToken().equals(token)) {
                    return true;
                }
            }
            return false;
        }
    }

    /** label */
    protected String getLabelXpath() {
        return "H2";
    }

    /**
     * Identify whether the HTML represents an interface.
     *
     * @param typeXml DOM of the type.
     * @return whether the HTML represents an interface.
     */
    private boolean isInterface(Document typeXml) {
        String classHeader = typeXml.valueOf("//" + getLabelXpath());

        // <H2>org.jumpi.spi.component Interface SequenceGenerator</H2>
        if (classHeader != null) {
            return containsToken(rb.getString("token.interface"), classHeader);
        }

        return false;
    }

    /**
     * Identify whether the HTML represents an interface.
     *
     * @param typeXml DOM of the type.
     * @return whether the HTML represents an interface.
     */
    private boolean isAnnotation(Document typeXml) {
        String classHeader = typeXml.valueOf("//" + getLabelXpath());
        //<H2>org.codavaj.javadoc.input Annotation Type AnnotationClass</H2>
        if (classHeader != null) {
            return containsToken(rb.getString("token.annotation"), classHeader ) && containsToken(rb.getString("token.type"), classHeader);
        }

        return false;
    }

    /**
     * Identify whether the HTML represents a class.
     *
     * @param typeXml DOM of the type.
     * @return whether the HTML represents a class.
     */
    private boolean isClass(Document typeXml) {
        String classHeader = typeXml.valueOf("//" + getLabelXpath());

        // <H2>org.jumpi.impl.connector.mpi11 Class MpiDestination</H2>
        if (classHeader != null) {
            return containsToken(rb.getString("token.class"), classHeader);
        }
        return false;
    }

    /**
     * Identify whether the HTML represents an enum.
     *
     * @param typeXml DOM of the type.
     * @return whether the HTML represents an enum.
     */
    private boolean isEnum(Document typeXml) {
        String classHeader = typeXml.valueOf("//" + getLabelXpath());

        if (classHeader != null) {
            return containsToken(rb.getString("token.enum"), classHeader);
        }
        return false;
    }

    /**
     * Establish the name of the type from which the input type extends.
     *
     * @param t the Type
     * @param typeXml DOM of the type.
     */
    protected void extendedType(Type t, Document typeXml) {
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
        String xpath = "//DT[starts-with(normalize-space(text()),'extends')]";
        List<Node> extendedTypeDTs = typeXml.selectNodes(xpath);
        // there should only be one
        if (extendedTypeDTs != null && extendedTypeDTs.size() > 1) {
            logger.log(Level.WARNING, "There should only be one extends");
        }
        for (int i = 0; (extendedTypeDTs != null) && (i < extendedTypeDTs.size()); i++) {
            Node node = extendedTypeDTs.get(i);

            if ((node.getNodeType() == Node.ELEMENT_NODE && node.getName().equalsIgnoreCase("DT"))) {
                String combinedText = convertNodesToString((Element)node);
                if (combinedText.startsWith("extends")) {
                    combinedText = combinedText.substring("extends".length());
                    combinedText = combinedText.trim();

                    if (!combinedText.isEmpty()) {
                        t.setSuperType(combinedText);
                    }
                }
            }
        }
    }

    /**
     * type modifier (1st entry)
     *
     * @param type DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     */
    protected void determineTypeModifiers(Type type, Document typeXml) {
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

        Element typeDescriptorElement = (Element) typeXml.selectSingleNode("//DT[parent::DL/preceding-sibling::H2 and string-length(.) > 0 and not(contains(.,'All')) and not(contains(.,'Enclosing')) and not(contains(.,'Direct')) and not(contains(.,'Type Parameters:')) and not(contains(.,'Parameters:')) and not(contains(.,'Returns:'))]");
        if (typeDescriptorElement == null) {
            String typeDescriptorXpath = "//DT[contains(text(),'" + type.getLabelString() + " " + type.getShortName() + "')]";
            typeDescriptorElement = (Element) typeXml.selectSingleNode(typeDescriptorXpath);
//System.err.println(typeDescriptorElement.asXML());
        }
        String typeDescriptor = convertNodesToString(typeDescriptorElement);

        determineTypeModifiers(typeDescriptor, type);
    }

    /**
     * type modifier
     *
     * @param typeDescriptor "public interface Handle", includes generics
     * @param type to be modified
     */
    protected void determineTypeModifiers(String typeDescriptor, Type type) {
        // take the generics type parameters
        if (typeDescriptor.contains("<") && typeDescriptor.lastIndexOf(">") != -1 && typeDescriptor.indexOf("<") < typeDescriptor.lastIndexOf(">")) {
            String typeParameters = typeDescriptor.substring(typeDescriptor.indexOf("<"), typeDescriptor.lastIndexOf(">")+1);
            type.setTypeParameters(typeParameters);
        }

        // strip off the type name
        String shortname = type.getShortName();

        if (typeDescriptor.contains(shortname)) {
            typeDescriptor = typeDescriptor.substring(0, typeDescriptor.indexOf(shortname));
        }

        //info( "visibility : " + typeDescriptor);
        if (typeDescriptor.contains(Type.MODIFIER_PUBLIC)) {
            type.setPublic(true);
        } else if (typeDescriptor.contains(Type.MODIFIER_PROTECTED)) {
            type.setProtected(true);
        } else if (typeDescriptor.contains(Type.MODIFIER_PRIVATE)) {
            type.setPrivate(true);
        } else {
            // default visibility
        }

        if (typeDescriptor.contains(Type.MODIFIER_ABSTRACT)) {
            type.setAbstract(true);
        }

        if (typeDescriptor.contains(Type.MODIFIER_STATIC)) {
            type.setStatic(true);
        }

        if (typeDescriptor.contains(Type.MOFIFIER_FINAL)) {
            type.setFinal(true);
        }

        if (typeDescriptor.contains(Type.MODIFIER_STRICTFP)) {
            type.setStrictFp(true);
        }
    }

    /**
     * DOCUMENT ME! (1st entry)
     *
     * @param t DOCUMENT ME!
     * @param typeXml DOCUMENT ME!
     */
    protected void determineImplementsList(Type t, Document typeXml) {
        /*
        "//DT[starts-with(normalize-space(text()),'implements')]/descendant::A/@href"
        <DT>implements java.lang.Runnable,
        <A href="../../../../org/jumpi/spi/component/TaskScheduler.html">TaskScheduler</A>
        </DT>

        Modified to handle parameterized types.
        */
        String extension = t.isInterface() ? "extends" : "implements";

        List<Node> implementsTypeDTs = typeXml.selectNodes("//DT[starts-with(normalize-space(text()),'" + extension + "')]");
        for (int i = 0; (implementsTypeDTs != null) && (i < implementsTypeDTs.size()); i++) {
            Node node = implementsTypeDTs.get(i);

            String combinedText = convertNodesToString((Element)node);
            if (combinedText.startsWith(extension)) {
                combinedText = combinedText.substring(extension.length());
                combinedText = combinedText.trim();

                if (!combinedText.isEmpty()) {
                    List<String> words = tokenizeWordListWithTypeParameters(combinedText, " ,\t\n\r\f");
                    for (String typeName : words) {
logger.finest("token: " + typeName);
                        t.addImplementsType(typeName);
                    }
                }
            }
        }
    }

    /**
     * @return a i18d word in "class", "interface", "enum" or "annotation".
     */
    protected String getLabelString(Type type) {
        if (type.isEnum()) {
            return rb.getString("token.enum");
        } else if (type.isAnnotation()) {
            // TODO check en
            return rb.getString("token.annotation") + (isLanguageOf(Locale.JAPANESE) ? "" : " ") + rb.getString("token.type");
        } else if (type.isInterface()) {
            return rb.getString("token.interface");
        } else {
            return rb.getString("token.class");
        }
    }

    /** i18n */
    protected static ResourceBundle rb;

    /** determines language */
    protected boolean isLanguageOf(Locale locale) {
        // umm...
logger.finer("lang: " + rb.getLocale().getLanguage());
        return rb.getLocale().getLanguage().equals(locale.getLanguage());
    }

    /** version comparator like 1.8.3 */
    protected static VersionComparator versionComparator = new VersionComparator();

    /** */
    public boolean isSuitableVersion(String version) {
        return versionComparator.compare(version, "1.8.0") < 0;
    }

    /** the class name index file name */
    public String getFirstIndexFileName() {
        return "allclasses-frame.html";
    }

    /** */
    private static String fileSeparator(String dir) {
        if (dir.startsWith("http")) {
            return "/";
        } else {
            return File.separator;
        }
    }

    /** gets a class name index file name */
    private static String getFirstIndexFilePath(String dir) {
        ServiceLoader<Parser> loader = ServiceLoader.load(Parser.class);
        for (Parser parser : loader) {
            String file = dir + fileSeparator(dir) + parser.getFirstIndexFileName();
            if (exists(file)) {
                return file;
            }
        }
        throw new NoSuchElementException(dir);
    }

    /** factory */
    private static Parser getParser(String version) {
        ServiceLoader<Parser> loader = ServiceLoader.load(Parser.class);
        for (Parser parser : loader) {
            if (parser.isSuitableVersion(version)) {
                return parser;
            }
        }
        throw new NoSuchElementException(version);
    }

    /** from the index file, TODO TypeFactory */
    private List<String> classes;

    /** classes listed in the index file, TODO TypeFactory */
    public List<String> getClasses() {
        return classes;
    }

    /** list of s linked javadoc references. */
    protected List<String> externalLinks;

    /**
     * @param externalLinks list of s linked javadoc references.
     */
    public void setExternalLinks(List<String> externalLinks) {
        this.externalLinks = externalLinks;
    }

    /** base path name */
    private String javadocDirName;

    /** check an url (including local path) is exist or not */
    private static boolean exists(String url) {
        URI uri;
        if (!url.startsWith("http:")) {
            return Files.exists(Paths.get(url));
        } else {
            try {
                uri = URI.create(url);
                InputStream is = uri.toURL().openStream();
                is.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    /**
     * @throws IllegalArgumentException url is illegal syntax
     * @throws IOException cannot open the url
     */
    private static InputSource getInputSource(String url) throws IOException {
        URI uri = null;
        if (!url.startsWith("http:")) {
            uri = Paths.get(url).toUri();
        } else {
            uri = URI.create(url);
        }
        InputSource is = new InputSource(uri.toURL().openStream());
        String encoding = System.getProperty("codavaj.file.encoding", System.getProperty("file.encoding"));
logger.finer("encoding: " + encoding);
        is.setEncoding(encoding);
        return is;
    }

    /**
     * Creates suitable parser.
     *
     * @param dir the dir to parse.
     * @throws java.util.NoSuchElementException index file was not found.
     * @throws IllegalStateException SAXException
     * @throws IOException cannot load all classes file
     */
    public static ParserUtils factory(String dir) throws IOException {

        try {
            Locale.setDefault(Locale.ENGLISH); // for token.properties

            String allClassesFilename = getFirstIndexFilePath(dir);
            Document document = loadHtmlMetadataAsDom(getInputSource(allClassesFilename));
            Node langNode = document.selectSingleNode("/HTML/@lang");

            ParserUtils parserUtil;

            if (langNode != null) {
                String lang = langNode.getText();

                rb = ResourceBundle.getBundle("token", new Locale(lang));
logger.info("lang >6: " + rb.getLocale().getLanguage());

                String versionText = document.selectSingleNode("//comment()[contains(., \"Generated by javadoc\")]").getText();
                int firstBracket = versionText.indexOf('(');
                int secondBracket = versionText.indexOf(')', firstBracket);
                String version = versionText.substring(firstBracket + 1, secondBracket).replaceFirst("[_\\-]\\s*\\w+", "");
                parserUtil = (ParserUtils) getParser(version);
            } else {
                // w/o lang maybe v6
                String language = System.getProperty("codavaj.language");
logger.info("language: " + language);
                if (language != null) {
                    rb = ResourceBundle.getBundle("token", new Locale(language));
                } else {
                    rb = ResourceBundle.getBundle("token");
                }
logger.info("lang =6: " + rb.getLocale().getLanguage());

                parserUtil = new ParserUtils();
            }

            Document allClasses = parserUtil.loadHtmlAsDom(getInputSource(allClassesFilename));
//String x = Paths.get(allClassesFilename).getFileName().toString();
//BufferedWriter w = Files.newBufferedWriter(Paths.get("tmp/1.9.16", "/input-" + x));
//w.write(prettyPrint(allClasses));
//w.flush();
            parserUtil.classes = parserUtil.getAllFqTypenames(allClasses);

            parserUtil.javadocDirName = dir;
logger.fine("parser: " + parserUtil.getClass().getName());
            return parserUtil;
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * processes type parsing.
     *
     * @param type model to set up
     * @throws ParseException something wrong, see {@link Exception#getCause()}
     * @throws IllegalStateException when a SAXException occurs
     */
    public void processType(Type type) throws IOException {
        Document typeXml = null;
        try {
            String filename = javadocDirName + fileSeparator(javadocDirName) + filenameFromTypename(type.getTypeName());
            typeXml = loadHtmlAsDom(getInputSource(filename));

            if (isAnnotation(typeXml)) {
                type.setAnnotation(true);
            } else if (isInterface(typeXml)) {
                type.setInterface(true);
            } else if (isEnum(typeXml)) {
                type.setEnum(true);
                extendedType(type, typeXml);
            } else if (isClass(typeXml)) {
                extendedType(type, typeXml);
            } else {
                throw new ParseException("type " + type.getTypeName()
                    + " is neither class, interface, enum or annotation.");
            }

            determineImplementsList(type, typeXml);

            determineTypeModifiers(type, typeXml);

            determineElements(type, typeXml);

            determineMethods(type, typeXml);

            determineFields(type, typeXml);

            determineEnumConsts(type, typeXml);

            determineConstructors(type, typeXml);

            determineInnerTypes(type, typeXml);

            determineDetails(type, typeXml);

            determineClassComment(type, typeXml);
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        } catch (Exception e) {
e.printStackTrace();
            throw new ParseException(typeXml.asXML(), e);
        }
    }

    /**
     * processes constants parsing.
     *
     * @param maps constants
     * @param lenient whether to warn when type not found
     * @throws IllegalStateException when a SAXException occurs
     */
    public void processConstant(Map<String, Type> maps, boolean lenient) throws IOException {
        try {
            String allConstantsFilename = javadocDirName + fileSeparator(javadocDirName) + "constant-values.html";
            Document allConstants = loadHtmlAsDom(getInputSource(allConstantsFilename));

            determineConstants(allConstants, maps, lenient);
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * load javadoc metadata
     *
     * @param html javadoc
     *
     * @return includes only html tag and comments.
     *
     * @throws SAXException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private static Document loadHtmlMetadataAsDom(InputSource html) throws SAXException, IOException {
        org.cyberneko.html.parsers.DOMParser parser = new org.cyberneko.html.parsers.DOMParser();

        //XMLParserConfiguration parser = new HTMLConfiguration();
        parser.setFeature("http://cyberneko.org/html/features/augmentations", true);
        parser.setFeature("http://cyberneko.org/html/features/report-errors", false);
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        parser.setProperty("http://cyberneko.org/html/properties/names/attrs", "lower");

        parser.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
        parser.setFeature("http://cyberneko.org/html/features/scanner/notify-builtin-refs", true);

        parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);

        org.cyberneko.html.filters.ElementRemover remover = new org.cyberneko.html.filters.ElementRemover();

        // set which elements to accept
        remover.acceptElement("html", new String[] { "lang" });

        // completely remove script elements
        remover.removeElement("script");
        remover.removeElement("link");

        org.apache.xerces.xni.parser.XMLDocumentFilter[] filters = new org.apache.xerces.xni.parser.XMLDocumentFilter[] {
            new org.cyberneko.html.filters.Purifier(),
            remover,
        };
        parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
        parser.parse(html);

        DOMReader xmlReader = new DOMReader();
        Document result = xmlReader.read(parser.getDocument());

//        logger.info("XML " + prettyPrint(result));
        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return dom
     */
    private Document loadHtmlAsDom(InputSource html) throws SAXException, IOException {
        org.cyberneko.html.parsers.DOMParser parser = new org.cyberneko.html.parsers.DOMParser();

        //XMLParserConfiguration parser = new HTMLConfiguration();
        parser.setFeature("http://cyberneko.org/html/features/augmentations", true);
        parser.setFeature("http://cyberneko.org/html/features/report-errors", false);
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        parser.setProperty("http://cyberneko.org/html/properties/names/attrs", "lower");

        parser.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
        parser.setFeature("http://cyberneko.org/html/features/scanner/notify-builtin-refs", true);

        parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        org.apache.xerces.xni.parser.XMLDocumentFilter[] filters = new org.apache.xerces.xni.parser.XMLDocumentFilter[] {
            new org.cyberneko.html.filters.Purifier(),
            getRemover(),
            new org.cyberneko.html.filters.Writer(baos, "UTF-8")
        };
        parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
        parser.parse(html);

        //String html = new String(baos.toByteArray(), "UTF-8");
        //logger.info(html);
        DOMReader xmlReader = new DOMReader();
        Document result = xmlReader.read(parser.getDocument());

        //logger.info("XML " + prettyPrint(result));
        return result;
    }

    /** lists elements to be removed */
    protected ElementRemover getRemover() {
        ElementRemover remover = new ElementRemover();

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
        remover.acceptElement("dd", null);

        // completely remove script elements
        remover.removeElement("script");
        remover.removeElement("link");

        return remover;
    }

    /** */
    protected FullyQualifiedNameMap fqnm = new FullyQualifiedNameMap();

    /** */
    public FullyQualifiedNameMap getFullyQualifiedNameMap() {
        return fqnm;
    }

    /**
     * Pretty print the XML to a String
     *
     * @param doc source xml
     * @return formatted xml string
     */
    protected static String prettyPrint(Document doc) {
        try {
            ByteArrayOutputStream html = new ByteArrayOutputStream();
            OutputFormat outformat = OutputFormat.createPrettyPrint();
            outformat.setEncoding("UTF-8");

            XMLWriter writer = new XMLWriter(html, outformat);
            writer.write(doc);
            writer.flush();

            return html.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
           logger.log(Level.WARNING, "Unable to pretty print.", e);
        }

        return doc.asXML();
    }

    /**
     * @see "https://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java"
     * TODO vavi-commons
     */
    protected static class VersionComparator implements Comparator<String> {
        /**
         * Compares two version strings.
         *
         * Use this instead of String.compareTo() for a non-lexicographical
         * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
         *
         * @param v1 a string of alpha numerals separated by decimal points.
         * @param v2 a string of alpha numerals separated by decimal points.
         * @return The result is 1 if v1 is greater than v2.
         *         The result is -1 if v2 is greater than v1.
         *         The result is zero if the strings are equal.
         * @throws IllegalArgumentException if the version format is unrecognized.
         */
        public int compare(String v1, String v2) {
            String[] v1Str = v1.split("\\.");
            String[] v2Str = v2.split("\\.");
            int v1Len = v1Str.length;
            int v2Len = v2Str.length;

            if (v1Len != v2Len) {
                int count = Math.abs(v1Len - v2Len);
                if (v1Len > v2Len)
                    for (int i = 1; i <= count; i++)
                        v2 += ".0";
                else
                    for (int i = 1; i <= count; i++)
                        v1 += ".0";
            }

            if (v1.equals(v2))
                return 0;

            for (int i = 0; i < v1Str.length; i++) {
                StringBuilder str1 = new StringBuilder();
                StringBuilder str2 = new StringBuilder();
                for (char c : v1Str[i].toCharArray()) {
                    if (Character.isLetter(c)) {
                        int u = c - 'a' + 1;
                        if (u < 10) {
                            str1.append("0").append(u);
                        } else {
                            str1.append(u);
                        }
                    } else {
                        str1.append(c);
                    }
                }
                for (char c : v2Str[i].toCharArray()) {
                    if (Character.isLetter(c)) {
                        int u = c - 'a' + 1;
                        if (u < 10) {
                            str2.append("0").append(u);
                        } else {
                            str2.append(u);
                        }
                    } else {
                        str2.append(c);
                    }
                }
                v1Str[i] = "1" + str1;
                v2Str[i] = "1" + str2;

                int num1 = Integer.parseInt(v1Str[i]);
                int num2 = Integer.parseInt(v2Str[i]);

                if (num1 != num2) {
                    if (num1 > num2)
                        return 1;
                    else
                        return -1;
                }
            }
            throw new IllegalArgumentException();
        }
    }
}
