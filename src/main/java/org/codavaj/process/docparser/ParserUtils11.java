/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.codavaj.process.docparser;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.codavaj.type.Type;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;


/**
 * for version 11.
 *
 * TODO token.see.exclude.#
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/08 umjammer initial version <br>
 */
public class ParserUtils11 extends ParserUtils8 {

    /* annotation */
    public void determineElements(Type type, Document typeXml, List<?> externalLinks) throws Exception {
        String requiredMethodXpath = "//TABLE[contains(text(),'" + rb.getString("token.required_element") + "')]/TR[position()>1]";
        String optionalMethodXpath = "//TABLE[contains(text(),'" + rb.getString("token.optional_element") + "')]/TR[position()>1]";
        determineElements(requiredMethodXpath, optionalMethodXpath, type, typeXml, externalLinks, "A");
    }

    /** constructor */
    protected List<?> getConstructorParamlistNodes(Node methodNode) {
        return getMethodParamlistNodes(methodNode);
    }

    /** method */
    protected List<?> getMethodParamlistNodes(Node methodNode) {
        // methodNode.selectNodes("*[name()!='DT']") excludes text nodes...
        List<Node> l = ((Element) methodNode).content();
        return l.stream().filter(n -> !"TD".equals(n.getName()))
                .filter(n -> !(n.getNodeType() == Node.TEXT_NODE && n.getText().trim().isEmpty()))
                .collect(Collectors.toList());
    }

    /* field */
    public void determineFields(Type type, Document typeXml, List<?> externalLinks) throws Exception {
        String fieldsXpath = "//TABLE[contains(text(),'" + rb.getString("token.field") + "')]/TR[position()>1]";
        determineFields(fieldsXpath, type, typeXml, externalLinks, "A");
    }

    /* enum */
    public void determineEnumConsts(Type type, Document typeXml, List<?> externalLinks) throws Exception {
        String keyword = rb.getString("token.enum_constant").replace(rb.getString("token.type"), ""); // umm...
        String enumConstsXpath = "//TABLE[contains(text(),'" + keyword + "')]/TR[position()>1]";
        determineEnumConsts(enumConstsXpath, type, typeXml, externalLinks, "A");
    }

    /** constants */
    protected String[] getConstantsXpath() {
        return new String[] {
            "A/@href",
            "A",
            "TD[position()=2]",
        };
    }

    /**
     * @return "class" or "interface" or "enum" or "annotation".
     */
    protected String getLabelString(Type type) {
        if (type.isEnum()) {
            // TODO check en
            return rb.getString("token.enum") + (isLanguageOf(Locale.JAPANESE) ? "" : " ") + rb.getString("token.type"); // umm...
        } else {
            return super.getLabelString(type);
        }
    }

    /* class list */
    public List<String> getAllFqTypenames(Document alltypesXml) {
        return getAllFqTypenames(alltypesXml, "//A/@href");
    }

    /* class list */
    public Document loadFileAsDom(String filename)
        throws SAXException, IOException, DocumentException {
        if (!Files.exists(Paths.get(filename))) {
            filename = filename.replace("-frame", ""); // umm...
        }
        return loadHtmlAsDom(new InputSource(new FileInputStream(filename)));
    }

}

/* */
