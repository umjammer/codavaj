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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.codavaj.type.Type;
import org.dom4j.Document;
import org.dom4j.DocumentException;


/**
 * for version 12.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/08 umjammer initial version <br>
 */
public class ParserUtils12 extends ParserUtils11 {

    /* method */
    @Override
    public void determineMethods(Type type, Document typeXml, List<?> externalLinks) throws Exception {
        String methodXpath = "//DIV[contains(text(),'" + rb.getString("token.all_methods") + "')]/../DIV[2]/TABLE/TR[position()>1]";
        determineMethods(methodXpath, type, typeXml, externalLinks);
    }

    /**
     * @return "class" or "interface" or "enum" or "annotation".
     */
    @Override
    protected String getLabelString(Type type) {
        if (type.isEnum()) {
            // TODO check en
            return rb.getString("token.enum"); // f●ck v.11 umm...
        } else {
            return super.getLabelString(type);
        }
    }

    /* class list */
    @Override
    public List<String> getAllFqTypenames(Document alltypesXml) {
        return getAllFqTypenames(alltypesXml, "//TABLE/TR/TD/A/@href");
    }

    /* class list */
    @Override
    public Document loadFileAsDom(String filename) throws SAXException, IOException, DocumentException {
        if (!Files.exists(Paths.get(filename))) {
            filename = filename.replace("-frame", "-index"); // umm...
        }
        return loadHtmlAsDom(new InputSource(new FileInputStream(filename)));
    }
}

/* */
