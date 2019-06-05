/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.codavaj.type;

import java.util.List;
import java.util.Optional;


/**
 * Commentable.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/06/04 umjammer initial version <br>
 */
public interface Commentable {

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    List<String> getComment();

    /**
     * DOCUMENT ME!
     *
     * @param comment DOCUMENT ME!
     */
    void setComment(List<String> comment);

    /** @return with javadoc comment marker */
    default Optional<String> getCommentAsString() {
        if (getComment() != null && getComment().size() > 0) {
            StringBuilder sb = new StringBuilder("/**\n");
            for (String line : getComment()) {
                sb.append(" * ");
                sb.append(line);
                sb.append("\n");
            }
            sb.append(" */\n");
            return Optional.of(sb.toString());
        } else {
            return Optional.empty();
        }
    }

    /** @return without javadoc comment marker */
    default Optional<String> getInnerCommentAsString() {
        if (getComment() != null && getComment().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String line : getComment()) {
                sb.append(line);
                sb.append("\n");
            }
            return Optional.of(sb.toString());
        } else {
            return Optional.empty();
        }
    }
}

/* */
