/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.test.codavaj;

import java.io.IOException;


/**
 * description of this class.
 * more description of this class.
 *
 * @param <T> description for this type
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/07 umjammer initial version <br>
 */
public class Test2<T> extends Test1 implements Test5 {

    /**
     * description for this constructor.
     * more description.
     * @param arg1  description for arg1
     * @throws Exception description for this exception.
     */
    public Test2(Object arg1) throws Exception {
        super(arg1);
    }

    /**
     * description for this method.
     * more description.
     * @param arg1 description for arg1
     * @param arg2 description for arg2
     * @return description for this return value.
     * @throws IOException description for this exception.
     */
    public Test3 method1(T arg1, String...arg2) throws IOException {
        return null;
    }

    /* @see vavi.test.codavaj.Test5#method5() */
    @Override
    public void method5() {
    }
}

/* */
