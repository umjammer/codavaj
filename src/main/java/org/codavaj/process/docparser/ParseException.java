/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.codavaj.process.docparser;

/**
 * ParseException.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/31 umjammer initial version <br>
 */
public class ParseException extends RuntimeException {

    /**
     * Creates a new UnresolvedExternalLinkException object.
     */
    public ParseException() {
        super();
    }

    /**
     * Creates a new UnresolvedExternalLinkException object.
     *
     * @param arg0 problem description.
     */
    public ParseException(String m) {
        super(m);
    }

    /**
     * Creates a new UnresolvedExternalLinkException object.
     *
     * @param t causing exception.
     */
    public ParseException(Throwable t) {
        super(t);
    }

    /**
     * Creates a new UnresolvedExternalLinkException object.
     *
     * @param m problem description.
     * @param t causing exception.
     */
    public ParseException(String m, Throwable t) {
        super(m, t);
    }
}

/* */
