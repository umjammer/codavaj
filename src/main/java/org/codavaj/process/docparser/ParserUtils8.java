/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.codavaj.process.docparser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.codavaj.type.Type;
import org.codavaj.type.TypeFactory;
import org.cyberneko.html.filters.ElementRemover;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import static com.rainerhahnekamp.sneakythrow.Sneaky.sneaked;

/**
 * for version 1.8.x
 *
 * TODO enum param (impossible?)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/06 umjammer initial version <br>
 */
public class ParserUtils8 extends ParserUtils {

    /** details */
    protected void determineComment(List<?> allNodes, List<String> commentText) {
        for (int i = 0; (allNodes != null) && (i < allNodes.size()); i++) {
            Node node = (Node) allNodes.get(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if ("DIV".equals(node.getName())) {
                    String[] lines = node.getText().split("\\n");
                    for (String line : lines) {
                        commentText.add(line.trim());
                    }
                } else if ("DL".equals(node.getName())) {
                    List<Node> nodes = node.selectNodes("*[name()='DT' or name()='DD']");
                    int j = 0;
                    // text modification mode
                    int m;
                    do {
                        Node dt = nodes.get(j++);
                        m = 0;
                        String tag;
                        if (dt.getText().indexOf(rb.getString("token.version")) >= 0) {
                            tag = "version";
                        } else if (dt.getText().indexOf(rb.getString("token.author")) >= 0) {
                            tag = "author";
                        } else if (dt.getText().indexOf(rb.getString("token.return")) >= 0) {
                            tag = "return";
                        } else if (dt.getText().indexOf(rb.getString("token.see")) >= 0) {
                            tag = "see";
                        } else if (dt.getText().indexOf(rb.getString("token.type_parameter")) >= 0) {
                            // WARNNING depends on order of conditions, should be before of token.parameter
                            tag = "param";
                            m = 2;
                        } else if (dt.getText().indexOf(rb.getString("token.parameter")) >= 0) {
                            tag = "param";
                            m = 1;
                        } else if (dt.getText().indexOf(rb.getString("token.exception")) >= 0) {
                            tag = "exception";
                            m = 1;
                        } else {
System.err.println("ignore: " + dt.getText());
                            tag = "ignore";
                        }
                        do {
                            Node dd = nodes.get(j++);
                            if (!"ignore".equals(tag)) {
                                String text = dd.valueOf("normalize-space(.)").trim();
                                switch (m) {
                                case 1: // for @param, @exception
                                    text = text.replaceFirst(" - ", " ");
                                    break;
                                case 2: // for type parameter @param at class description
                                    text = text.replaceFirst("(\\w+) - ", "<$1> ");
                                    break;
                                }
                                commentText.add("@" + tag + " " + text); // TODO a tag
                            }
                        } while(j < nodes.size() && nodes.get(j).getName().equals("DD"));
                    } while (j < nodes.size());
                }
            }
        }
    }

    /*
     * details
     * a field comment
     * a method comment
     */
    protected List<String> determineComment(Element enclosingNode) throws Exception {
        if (enclosingNode == null) {
            return null;
        }

        // LI/DIV, DL...
        List<?> allNodes = enclosingNode.selectNodes("*[name()='DIV' or name()='DL']");
        List<String> commentText = new ArrayList<>();

        determineComment(allNodes, commentText);

        return commentText;
    }

    /*
     * details
     * a field comment
     * a method comment
     */
    protected String determineDefault(Element enclosingNode) throws Exception {
        if (enclosingNode == null) {
            return null;
        }
        Node node = enclosingNode.selectSingleNode("DL/DT[contains(text(), '" + rb.getString("token.default") + "')]/../DD");
        return node != null ? node.getText() : null;
    }

    /* class comment */
    public void determineClassComment(Type type, Document typeXml, List<?> externalLinks) throws Exception {
        List<String> commentText = new ArrayList<>();

        List<?> allNodes = typeXml.selectNodes("//LI/text()[contains(.,'" + type.getLabelString() + " " + type.getShortName() + "')]/following-sibling::*[name()='DIV' or name()='DL']");
        determineComment(allNodes, commentText);

        allNodes = typeXml.selectNodes("//DL/DT[contains(text(),'" + rb.getString("token.type_parameter") + "')]/..");
//allNodes.forEach(System.err::println);
        determineComment(allNodes, commentText);

        type.setComment(commentText);
    }

    /* constants */
    public void determineConstants(Document allconstants, TypeFactory typeFactory, List<?> externalLinks, boolean lenient) {
        String xpath = "//TABLE/TR[position() != 1]";
        determineConstants(xpath, allconstants, typeFactory, externalLinks, lenient);
    }

    /** details */
    protected String getDetailsXpath() {
        return "H3";
    }

    /** details */
    protected String getDetailsName(String text) {
        text = text.trim();
        return text.substring(0, text.indexOf('\n', 1));
    }

    /* details */
    public void determineDetails(Type type, Document typeXml, List<?> externalLinks) throws Exception {

        Function<Context, Boolean> f = c -> {
            c.parseOn = true;
            return c.parseDone;
        };

        List<?> constructorDetails = typeXml.selectNodes("//" + getDetailsXpath() + "[contains(text(),'" + rb.getString("token.constructor_details") + "')]/following-sibling::LI");
        for (int i = 0; constructorDetails != null && i < constructorDetails.size(); i++) {
            Node node = (Node) constructorDetails.get(i);

            List<?> allNodes = ((Element) node).content();
            determineDetails(allNodes, externalLinks, f, sneaked(c -> {
                String name = getDetailsName(c.text);
                determineMethodDetails(type, c.text, name, (Element) node);
                c.parseDone = true;
            }));
        }

        List<?> methodDetails = typeXml.selectNodes("//" + getDetailsXpath() + "[contains(text(),'" + rb.getString("token.method_details") + "')]/following-sibling::LI");
        for (int i = 0; methodDetails != null && i < methodDetails.size(); i++) {
            Node node = (Node) methodDetails.get(i);

            List<?> allNodes = ((Element) node).content();
            determineDetails(allNodes, externalLinks, f, sneaked(c -> {
                String name = getDetailsName(c.text);
                determineMethodDetails(type, c.text, name, (Element) node);
                c.parseDone = true;
            }));
        }

        List<?> fieldDetails = typeXml.selectNodes("//" + getDetailsXpath() + "[contains(text(),'" + rb.getString("token.field_details") + "')]/following-sibling::LI");
        for (int i = 0; fieldDetails != null && i < fieldDetails.size(); i++) {
            Node node = (Node) fieldDetails.get(i);

            List<?> allNodes = ((Element) node).content();
            determineDetails(allNodes, externalLinks, f, sneaked(c -> {
                String name = getDetailsName(c.text);
                determineFieldDetails(type, c.text, name, (Element) node);
                c.parseDone = true;
            }));
        }

        // annotation
        List<?> elementDetails = typeXml.selectNodes("//" + getDetailsXpath() + "[contains(text(),'" + rb.getString("token.element_details") + "')]/../../LI");
        for (int i = 0; elementDetails != null && i < elementDetails.size(); i++) {
            Node node = (Node) elementDetails.get(i);
            List<?> allNodes = ((Element) node.selectSingleNode("LI")).content();
            determineDetails(allNodes, externalLinks, f, sneaked(c -> {
                String name = getDetailsName(c.text);
                determineFieldDetails(type, c.text, name, (Element) node.selectSingleNode("LI"));
                c.parseDone = true;
            }));
        }

        // enum
        List<?> enumCOnstantDetails = typeXml.selectNodes("//" + getDetailsXpath() + "[contains(text(),'" + rb.getString("token.enum_constant_details") + "')]/following-sibling::LI");
        for (int i = 0; enumCOnstantDetails != null && i < enumCOnstantDetails.size(); i++) {
            Node node = (Node) enumCOnstantDetails.get(i);

            List<?> allNodes = ((Element) node).content();
            determineDetails(allNodes, externalLinks, f, sneaked(c -> {
                String name = c.text.substring(1, c.text.indexOf('\n', 1));
                determineFieldDetails(type, c.text, name, (Element) node);
                c.parseOn = false;
            }));
        }
    }

    /* field */
    public void determineFields(Type type, Document typeXml, List<?> externalLinks) throws Exception {
        String fieldsXpath = "//TABLE[contains(text(),'" + rb.getString("token.field") + "')]/TR[position()>1]";
        determineFields(fieldsXpath, type, typeXml, externalLinks, "TD[position()=2]/A");
    }

    /* enum */
    public void determineEnumConsts(Type type, Document typeXml, List<?> externalLinks) throws Exception {
        String enumConstsXpath = "//TABLE[contains(text(),'" + rb.getString("token.enum_constant") + "')]/TR[position()>1]";
        determineEnumConsts(enumConstsXpath, type, typeXml, externalLinks, "TD[position()=1]/A");
    }

    /* constructor */
    public void determineConstructors(Type type, Document typeXml, List<?> externalLinks) throws Exception {
        String constructorsXpath = "//TABLE[contains(text(),'" + rb.getString("token.constructor") + "')]/TR[position()>1]";
        determineConstructors(constructorsXpath, type, typeXml, externalLinks);
    }

    /* method */
    public void determineMethods(Type type, Document typeXml, List<?> externalLinks) throws Exception {
        String methodXpath = "//TABLE[contains(text(),'" + rb.getString("token.all_methods") + "')]/TR[position()>1]";
        determineMethods(methodXpath, type, typeXml, externalLinks);
    }

    /* annotation */
    public void determineElements(Type type, Document typeXml, List<?> externalLinks ) throws Exception {
        String requiredMethodXpath = "//TABLE[contains(text(),'" + rb.getString("token.required_element") + "')]/TR[position()>1]";
        String optionalMethodXpath = "//TABLE[contains(text(),'" + rb.getString("token.optional_element") + "')]/TR[position()>1]";
        determineElements(requiredMethodXpath, optionalMethodXpath, type, typeXml, externalLinks, "TD[position()=2]/A");
    }

    /** extends umm... */
    public void extendedType(Type t, Document typeXml, List<?> externalLinks ) {
        final String keyword = "extends";
        Node node = typeXml.selectSingleNode("//LI/text()[contains(.,'" + keyword + "')]/following-sibling::A[1]");
        if (node == null) {
            String combinedText = typeXml.selectSingleNode("//LI/text()[contains(.,'" + keyword + "')]").getText();
            combinedText = combinedText.substring(combinedText.indexOf(keyword) + keyword.length()).trim();
            t.setSuperType(combinedText);
        } else {
            String combinedText = convertNodesToString(node, externalLinks);
            if (combinedText != null) {
                combinedText = combinedText.trim();

                if (!combinedText.isEmpty()) {
                    t.setSuperType(combinedText);
                }
            }
        }
    }

    /* modifiers */
    public void determineTypeModifiers(Type type, Document typeXml, List<?> externalLinks) {
//System.err.println("type: " + type.getShortName() + ", " + type.getTypeString());
        String typeDescriptorXpath = "//" + getLabelXpath() + "[contains(text(),'" + getLabelString(type) + " " + type.getShortName() + "')]";
        Node typeDescriptorNode = typeXml.selectSingleNode(typeDescriptorXpath);
//System.err.println(typeDescriptorNode.asXML());
        String typeDescriptor1 = convertNodesToString(typeDescriptorNode, externalLinks);
        typeDescriptor1 = typeDescriptor1.replace(getLabelString(type) + " ", "").trim();
//System.err.println(typeDescriptor1);
        String typeDescriptor2 = typeXml.selectSingleNode("//LI/text()[contains(.,'" + type.getLabelString() + " " + type.getShortName() + "')]").getText();
        String typeDescriptor = typeDescriptor2.replace(type.getShortName(), typeDescriptor1);
//System.err.println(typeDescriptor);

        determineTypeModifiers(typeDescriptor, type, typeXml, externalLinks);
    }

    /* implements */
    public void determineImplementsList(Type t, Document typeXml, List<?> externalLinks) {
        String extension = t.isInterface() ? "extends" : "implements";

        List<?> implementsTypeAs = typeXml.selectNodes("//LI/text()[contains(.,'" + extension + "')]/following-sibling::A");
        for (int i = 0; implementsTypeAs != null && i < implementsTypeAs.size(); i++) {
            Node node = (Node) implementsTypeAs.get(i);

            String combinedText = convertNodesToString(node, externalLinks);
            if (combinedText != null) {
                combinedText = combinedText.trim();

                if (!combinedText.isEmpty()) {
                    List<String> words = tokenizeWordListWithTypeParameters(combinedText, " ,\t\n\r\f");
                    Iterator<String> it = words.iterator();
                    while (it.hasNext()) {
                        String typeName = it.next();

//                        debug("token: " + typeName);
                        t.addImplementsType(typeName);
                    }
                } else {
                    break; // TODO too easy
                }
            }
        }
    }

    /** add li, dd, div */
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
        remover.acceptElement("li", null);
        remover.acceptElement("dd", null);
        remover.acceptElement("div", null);

        // completely remove script elements
        remover.removeElement("script");
        remover.removeElement("link");

        return remover;
    }
}
