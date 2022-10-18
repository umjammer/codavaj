/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.codavaj.process.docparser;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.codavaj.type.Type;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;


/**
 * for version 11.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/08 umjammer initial version <br>
 */
public class ParserUtils11 extends ParserUtils8 {

    /* annotation (1st entry) */
    @Override
    protected void determineElements(Type type, Document typeXml) {
        determineElements(type, typeXml, "A");
    }

    /* constructor */
    @Override
    protected List<Node> getConstructorParamlistNodes(Node methodNode) {
        return getMethodParamlistNodes(methodNode);
    }

    /* method */
    @Override
    protected List<Node> getMethodParamlistNodes(Node methodNode) {
        // TODO methodNode.selectNodes("*[name()!='DT']") excludes text nodes...
        List<Node> l = ((Element) methodNode).content();
        return l.stream()
                .filter(n -> !"TD".equals(n.getName()))
                .filter(n -> !(n.getNodeType() == Node.TEXT_NODE && n.getText().trim().isEmpty()))
                .collect(Collectors.toList());
    }

    /* field (1st entry) */
    @Override
    protected void determineFields(Type type, Document typeXml) {
        determineFields(type, typeXml, "A");
    }

    /* enum (1st entry) */
    @Override
    protected void determineEnumConsts(Type type, Document typeXml) {
        String keyword = rb.getString("token.enum_constant").replace(rb.getString("token.type"), ""); // umm...
        String enumConstsXpath = "//TABLE[contains(text(),'" + keyword + "')]/TR[position()>1]";
        determineEnumConsts(enumConstsXpath, type, typeXml, "A");
    }

    /* constants */
    @Override
    protected String[] getConstantsXpaths() {
        return new String[] {
            "A/@href",
            "A",
            "TD[position()=2]",
        };
    }

    @Override
    protected String getLabelString(Type type) {
        if (type.isEnum()) {
            return rb.getString("token.enum") + (isLanguageOf(Locale.JAPANESE) ? rb.getString("token.type") : ""); // umm...
        } else {
            return super.getLabelString(type);
        }
    }

    /* class list */
    @Override
    public List<String> getAllFqTypenames(Document alltypesXml) {
        return getAllFqTypenames(alltypesXml, "//TABLE/TR/TD/A/@href");
    }

    @Override
    public boolean isSuitableVersion(String version) {
        return versionComparator.compare(version, "11.0.0") >= 0 && versionComparator.compare(version, "12.0.0") < 0;
    }

    @Override
    public String getFirstIndexFileName() {
        return "allclasses-index.html";
    }
}

/* */
