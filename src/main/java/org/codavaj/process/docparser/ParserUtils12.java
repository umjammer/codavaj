/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.codavaj.process.docparser;

import org.codavaj.type.Type;
import org.dom4j.Document;


/**
 * for version 12.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/08 umjammer initial version <br>
 */
public class ParserUtils12 extends ParserUtils11 {

    /* method (1st entry) */
    @Override
    protected void determineMethods(Type type, Document typeXml) {
        String methodXpath = "//DIV[contains(text(),'" + rb.getString("token.all_methods") + "')]/../DIV[2]/TABLE/TR[position()>1]";
        determineMethods(methodXpath, type, typeXml);
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

    @Override
    public boolean isSuitableVersion(String version) {
        return versionComparator.compare(version, "12.0.0") >= 0 && versionComparator.compare(version, "13.0.0") < 0;
    }
}

/* */
