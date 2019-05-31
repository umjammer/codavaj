/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.codavaj.process.docparser;

/**
 * for version 13.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/08 umjammer initial version <br>
 */
public class ParserUtils13 extends ParserUtils12 {

    /** details */
    @Override
    protected String getDetailsXpath() {
        return "H2";
    }

    /** label */
    @Override
    protected String getLabelXpath() {
        return "H1";
    }

    @Override
    protected boolean isSuitableVersion(String version) {
        return versionComparator.compare(version, "13.0.0") >= 0;
    }
}

/* */
