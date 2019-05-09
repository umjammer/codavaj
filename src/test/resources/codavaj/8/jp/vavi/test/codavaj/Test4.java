package vavi.test.codavaj;
/**
 * description for this annotation.
 * more description.
 * @version 0.00 2019/05/07 umjammer initial version
 * @author <A href="mailto:umjammer@gmail.com">Naohide Sano</A> (umjammer)
 */
public @interface Test4{
    /**
     * description for this field1.
     * more description.
     * @return description for this field1
     */
    java.lang.String field1();

    /**
     * description for this field2.
     * more description.
     * @return description for this field2
     */
    int field2() default 10;

}
