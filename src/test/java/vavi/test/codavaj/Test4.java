/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.test.codavaj;


/**
 * description for this annotation.
 * more description.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/07 umjammer initial version <br>
 */
public @interface Test4 {

    /**
     * description for this field1.
     * more description.
     * @return description for this field1
     */
    String field1();

    /**
     * description for this field2.
     * more description.
     * @return description for this field2
     */
    int field2() default 10;
}

/* */
