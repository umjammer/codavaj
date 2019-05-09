/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.test.codavaj;

import java.awt.event.ActionEvent;
import java.io.Serializable;

import javax.swing.AbstractAction;


/**
 * description of this class.
 * more description of this class.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/05 umjammer initial version <br>
 */
public class Test1 extends AbstractAction implements Runnable, Serializable, Cloneable {

    /**
     * description of this field.
     * more description of this field.
     */
    public int field1;

    /**
     * description of this field.
     * more description of this field.
     */
    protected long field2;

    /**
     * description of this field.
     * more description of this field.
     */
    public static final int CONSTANT1 = 1;

    /**
     * description of this constructor.
     * more description of this constructor.
     * @param arg1 description of arg1
     * @throws Exception description of this exception
     */
    public Test1(Object arg1) throws Exception {
    }

    /*
     * @see java.lang.Runnable#run()
     */
    public void run() {
    }

    /* @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent) */
    @Override
    public void actionPerformed(ActionEvent e) {
    }

    /**
     * description of this method.
     * more description of this method.
     * @param arg1 description of arg1
     * @param arg2 description of arg2
     * @return  description of return value
     * @throws IllegalStateException description of this exception
     * @throws IllegalArgumentException description of this exception
     */
    public Object method1(Object arg1, Object arg2) throws IllegalStateException, IllegalArgumentException  {
        return null;
    }
}

/* */
