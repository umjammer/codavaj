/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.codavaj.process.docparser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Override
    protected void determineComment(Type t, List<Node> allNodes, List<String> commentText, List<String> externalLinks) {
        for (int i = 0; (allNodes != null) && (i < allNodes.size()); i++) {
            Node node = allNodes.get(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if ("DIV".equals(node.getName())) {
                    // TODO {@link} a tag
                    String text = node.asXML().replace("<DIV>", "").replace("</DIV>", "").trim();
                    if (!text.contains(rb.getString("token.comment.exclude"))) {
                        String[] lines = text.split("\\n");
                        for (String line : lines) {
                            commentText.add(line.trim());
                        }
                    } else {
System.err.println("ignore 1: " + text);
                    }
                } else if ("DL".equals(node.getName())) {
                    List<Node> nodes = node.selectNodes("*[name()='DT' or name()='DD']");
                    int j = 0;
                    do {
                        Node dt = nodes.get(j++);
                        String tag = getTag(dt.getText());
                        do {
                            Node dd = nodes.get(j++);
                            String text = dd.asXML().replace("<DD>", "").replace("</DD>", "").trim();
                            switch (tag) { //.equals(tag)) {
                            case "param":
                                text = text.replaceFirst(" - ", " ");
                                break;
                            case "typeparam": // for type parameter @param at class description
                                tag = "param";
                                text = text.replaceFirst("([\\w\\$_\\.\\<\\>]+) - ", "<$1> ");
                                break;
                            case "exception":
                                Node typeNode = dd.selectSingleNode("A");
                                String comment;
                                String typeName;
                                if (typeNode != null) {
                                    comment = dd.getText().replaceFirst(" - ", " ");
                                    typeName = convertNodesToString(typeNode, externalLinks);
                                } else {
                                    int p = text.indexOf(" - ");
                                    if (p >= 0) {
                                        comment = dd.getText().substring(text.indexOf(" - ") + " -".length());
                                        typeName = dd.getText().substring(0, p);
                                    } else {
                                        comment = "";
                                        typeName = dd.getText().trim();
                                    }
                                }
                                text = toFQDN(t, typeName) + comment;
                                break;
                            case "see":
                                if (text.contains(rb.getString("token.see.exclude.1")) ||
                                    text.contains(rb.getString("token.see.exclude.2"))) {
System.err.println("ignore 3: " + dd.selectSingleNode("A").getText());
                                    continue;
                                }
                                break;
                            default:
                                break;
                            case "ignore":
                                continue;
                            }
                            commentText.add("@" + tag + " " + text);
                        } while (j < nodes.size() && "DD".equals(nodes.get(j).getName()));
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
    @Override
    protected List<String> determineComment(Type t, Element enclosingNode, List<String> externalLinks) throws Exception {
        if (enclosingNode == null) {
            return null;
        }

        // LI/DIV, DL...
        List<Node> allNodes = enclosingNode.selectNodes("*[name()='DIV' or name()='DL']");
        List<String> commentText = new ArrayList<>();

        determineComment(t, allNodes, commentText, externalLinks);

        return commentText;
    }

    /*
     * details
     * a field comment
     * a method comment
     */
    @Override
    protected String determineDefault(Element enclosingNode) throws Exception {
        if (enclosingNode == null) {
            return null;
        }
        Node node = enclosingNode.selectSingleNode("DL/DT[contains(text(), '" + rb.getString("token.default") + "')]/../DD");
        return node != null ? node.getText() : null;
    }

    /* class comment */
    @Override
    public void determineClassComment(Type type, Document typeXml, List<String> externalLinks) throws Exception {
        List<String> commentText = new ArrayList<>();

        // for others
        List<Node> allNodes = typeXml.selectNodes("//LI/text()[contains(.,'" + type.getLabelString() + " " + type.getShortName() + "')]/following-sibling::*[name()='DIV' or name()='DL']");
        determineComment(type, allNodes, commentText, externalLinks);

        // for type parameter
        allNodes = typeXml.selectNodes("//DL/DT[contains(text(),'" + rb.getString("token.type_parameter") + "')]/..");
//allNodes.forEach(System.err::println);
        determineComment(type, allNodes, commentText, externalLinks);

        type.setComment(commentText);
    }

    /* constants */
    @Override
    public void determineConstants(Document allconstants, TypeFactory typeFactory, List<String> externalLinks, boolean lenient) {
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
    @Override
    public void determineDetails(Type type, Document typeXml, List<String> externalLinks) throws Exception {

        Function<Context, Boolean> f = c -> {
            c.parseOn = true;
            return c.parseDone;
        };

        List<Node> constructorDetails = typeXml.selectNodes("//" + getDetailsXpath() + "[contains(text(),'" + rb.getString("token.constructor_details") + "')]/following-sibling::LI");
        for (int i = 0; constructorDetails != null && i < constructorDetails.size(); i++) {
            Node node = constructorDetails.get(i);

            List<Node> allNodes = ((Element) node).content();
            determineDetails(allNodes, externalLinks, f, sneaked(c -> {
                String name = getDetailsName(c.text);
                determineMethodDetails(type, c.text, name, (Element) node, externalLinks);
                c.parseDone = true;
            }));
        }

        List<?> methodDetails = typeXml.selectNodes("//" + getDetailsXpath() + "[contains(text(),'" + rb.getString("token.method_details") + "')]/following-sibling::LI");
        for (int i = 0; methodDetails != null && i < methodDetails.size(); i++) {
            Node node = (Node) methodDetails.get(i);

            List<Node> allNodes = ((Element) node).content();
            determineDetails(allNodes, externalLinks, f, sneaked(c -> {
                String name = getDetailsName(c.text);
                determineMethodDetails(type, c.text, name, (Element) node, externalLinks);
                c.parseDone = true;
            }));
        }

        List<?> fieldDetails = typeXml.selectNodes("//" + getDetailsXpath() + "[contains(text(),'" + rb.getString("token.field_details") + "')]/following-sibling::LI");
        for (int i = 0; fieldDetails != null && i < fieldDetails.size(); i++) {
            Node node = (Node) fieldDetails.get(i);

            List<Node> allNodes = ((Element) node).content();
            determineDetails(allNodes, externalLinks, f, sneaked(c -> {
                String name = getDetailsName(c.text);
                determineFieldDetails(type, c.text, name, (Element) node, externalLinks);
                c.parseDone = true;
            }));
        }

        // annotation
        List<Node> elementDetails = typeXml.selectNodes("//" + getDetailsXpath() + "[contains(text(),'" + rb.getString("token.element_details") + "')]/../../LI");
        for (int i = 0; elementDetails != null && i < elementDetails.size(); i++) {
            Node node = elementDetails.get(i);
            List<Node> allNodes = ((Element) node.selectSingleNode("LI")).content();
            determineDetails(allNodes, externalLinks, f, sneaked(c -> {
                String name = getDetailsName(c.text);
                determineFieldDetails(type, c.text, name, (Element) node.selectSingleNode("LI"), externalLinks);
                c.parseDone = true;
            }));
        }

        // enum
        List<Node> enumCOnstantDetails = typeXml.selectNodes("//" + getDetailsXpath() + "[contains(text(),'" + rb.getString("token.enum_constant_details") + "')]/following-sibling::LI");
        for (int i = 0; enumCOnstantDetails != null && i < enumCOnstantDetails.size(); i++) {
            Node node = enumCOnstantDetails.get(i);

            List<Node> allNodes = ((Element) node).content();
            determineDetails(allNodes, externalLinks, f, sneaked(c -> {
                String name = c.text.substring(1, c.text.indexOf('\n', 1));
                determineFieldDetails(type, c.text, name, (Element) node, externalLinks);
                c.parseOn = false;
            }));
        }
    }

    /* field */
    @Override
    protected String getFieldsXpath() {
        return "//TABLE[contains(text(),'" + rb.getString("token.field") + "')]/TR[position()>1]";
    }

    /* field */
    @Override
    public void determineFields(Type type, Document typeXml, List<String> externalLinks) throws Exception {
        determineFields(type, typeXml, externalLinks, "TD[position()=2]/A");
    }

    /* enum */
    @Override
    public void determineEnumConsts(Type type, Document typeXml, List<String> externalLinks) throws Exception {
        String enumConstsXpath = "//TABLE[contains(text(),'" + rb.getString("token.enum_constant") + "')]/TR[position()>1]";
        determineEnumConsts(enumConstsXpath, type, typeXml, externalLinks, "TD[position()=1]/A");
    }

    /* constructor */
    @Override
    public void determineConstructors(Type type, Document typeXml, List<String> externalLinks) throws Exception {
        String constructorsXpath = "//TABLE[contains(text(),'" + rb.getString("token.constructor") + "')]/TR[position()>1]";
        determineConstructors(constructorsXpath, type, typeXml, externalLinks);
    }

    /* method */
    @Override
    public void determineMethods(Type type, Document typeXml, List<String> externalLinks) throws Exception {
        String methodXpath = "//TABLE[contains(text(),'" + rb.getString("token.all_methods") + "')]/TR[position()>1]";
        determineMethods(methodXpath, type, typeXml, externalLinks);
    }

    /** */
    @Override
    protected String[] getElementsXpaths() {
        return new String[] {
            "//TABLE[contains(text(),'" + rb.getString("token.required_element") + "')]/TR[position()>1]",
            "//TABLE[contains(text(),'" + rb.getString("token.optional_element") + "')]/TR[position()>1]"
        };
    }

    /* annotation */
    @Override
    public void determineElements(Type type, Document typeXml, List<String> externalLinks ) throws Exception {
        determineElements(type, typeXml, externalLinks, "TD[position()=2]/A");
    }

    /** extends umm... */
    @Override
    public void extendedType(Type t, Document typeXml, List<String> externalLinks ) {
        final String keyword = "extends";
        Node aNode = typeXml.selectSingleNode("//LI/text()[contains(.,'" + keyword + "')]/following-sibling::A[1]");
        if (aNode != null) {
            Node liNode = typeXml.selectSingleNode("//LI[text()[contains(.,'" + keyword + "')]]");
            // selectNodes doesn't contain text...
            List<Node> l = ((Element) liNode).content();
            List<Node> nodes = l.stream()
                    .filter(n -> "A".equals(n.getName()) ||
                            "DIV".equals(n.getName()) ||
                            n.getNodeType() == Node.TEXT_NODE && !n.getText().trim().replace("\r", "").isEmpty() ||
                            n.getNodeType() == Node.ENTITY_REFERENCE_NODE)
                    .collect(Collectors.toList());

            String combinedText = "";
            for (Node n : nodes) {
                if ("DIV".equals(n.getName())) {
                    break;
                }
                combinedText += convertNodesToString(n, externalLinks);
            }
            combinedText = combinedText.trim().replaceFirst("^.+\\s*" + keyword + "\\s+([\\w_\\$\\.\\<\\>]+)\\s*.*$", "$1");
            if (!combinedText.isEmpty()) {
                String typeName = toFQDN(t, combinedText);
                t.setSuperType(typeName);
            }
        } else {
            String nodeText = typeXml.selectSingleNode("//LI/text()[contains(.,'" + keyword + "')]").getText().trim();
            nodeText = nodeText.replaceFirst(".+\\s" + keyword + "\\s+([\\w_\\$\\.\\<\\>]+)\\s*.*", "$1");
            t.setSuperType(nodeText);
        }
    }

    /* modifiers */
    @Override
    public void determineTypeModifiers(Type type, Document typeXml, List<String> externalLinks) {
//System.err.println("type: " + type.getShortName() + ", " + type.getTypeString());
        String typeDescriptorXpath = "//" + getLabelXpath() + "[contains(text(),'" + getLabelString(type) + "') and contains(text(),'" + type.getShortName() + "')]";
        Node typeDescriptorNode = typeXml.selectSingleNode(typeDescriptorXpath);
//System.err.println(typeDescriptorNode.asXML());
        String typeDescriptor1 = convertNodesToString(typeDescriptorNode, externalLinks);
        typeDescriptor1 = typeDescriptor1.replace(getLabelString(type) + " ", "").trim();
//System.err.println(typeDescriptor1);
        String typeDescriptor2 = typeXml.selectSingleNode("//LI/text()[contains(.,'" + type.getLabelString() + "') and contains(.,'" + type.getShortName() + "')]").getText();
        String typeDescriptor = typeDescriptor2.replace(type.getShortName(), typeDescriptor1);
//System.err.println(typeDescriptor);

        determineTypeModifiers(typeDescriptor, type, typeXml, externalLinks);
    }

    /* implements */
    @Override
    public void determineImplementsList(Type t, Document typeXml, List<String> externalLinks) {
        String extension = t.isInterface() ? "extends" : "implements";

        List<?> implementsTypeAs = typeXml.selectNodes("//LI/text()[contains(.,'" + extension + "')]/following-sibling::A");
        if (implementsTypeAs != null && implementsTypeAs.size() > 0) {
            for (int i = 0; i < implementsTypeAs.size(); i++) {
                Node node = (Node) implementsTypeAs.get(i);

                String combinedText = convertNodesToString(node, externalLinks);
                if (combinedText != null) {
                    combinedText = combinedText.trim();

                    if (!combinedText.isEmpty()) {
                        List<String> words = tokenizeWordListWithTypeParameters(combinedText, " ,\t\n\r\f");
                        for (String typeName : words) {
                            typeName = toFQDN(t, typeName);
                            t.addImplementsType(typeName);
                        }
                    } else {
                        break; // TODO too easy
                    }
                }
            }
        } else {
            Node node = typeXml.selectSingleNode("//LI/text()[contains(.,'" + extension + "')]");
            if (node != null) {
                String text = node.getText().trim();
                text = text.substring(text.indexOf(extension) + extension.length() + 1);
                String[] types = text.split("[\\s,]+");
                for (String type : types) {
                    t.addImplementsType(type);
                }
            }
        }
    }

    /** add li, dd, div */
    @Override
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
