/*
 *   Copyright 2005 Peter Klauser
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.codavaj.type;

/**
 * DOCUMENT ME!
 */
public class Modifiable {
    /**
     * DOCUMENT ME!
     */
    public static final String MODIFIER_PUBLIC = "public";

    /**
     * DOCUMENT ME!
     */
    public static final String MODIFIER_PROTECTED = "protected";

    /**
     * DOCUMENT ME!
     */
    public static final String MODIFIER_PRIVATE = "private";

    /**
     * DOCUMENT ME!
     */
    public static final String MODIFIER_STATIC = "static";

    /**
     * DOCUMENT ME!
     */
    public static final String MODIFIER_ABSTRACT = "abstract";

    /**
     * DOCUMENT ME!
     */
    public static final String MOFIFIER_FINAL = "final";

    /**
     * DOCUMENT ME!
     */
    public static final String MODIFIER_STRICTFP = "strictfp";

    /**
     * DOCUMENT ME!
     */
    public static final String MODIFIER_DEFAULT = "(package private)";

    /**
     * DOCUMENT ME!
     */
    public static final int PUBLIC = 1;

    /**
     * DOCUMENT ME!
     */
    public static final int PRIVATE = 2;

    /**
     * DOCUMENT ME!
     */
    public static final int PROTECTED = 4;

    /**
     * DOCUMENT ME!
     */
    public static final int STATIC = 8;

    /**
     * DOCUMENT ME!
     */
    public static final int FINAL = 16;

    /**
     * DOCUMENT ME!
     */
    public static final int SYNCHRONIZED = 32;

    /**
     * DOCUMENT ME!
     */
    public static final int VOLATILE = 64;

    /**
     * DOCUMENT ME!
     */
    public static final int TRANSIENT = 128;

    /**
     * DOCUMENT ME!
     */
    public static final int NATIVE = 256;

    /**
     * DOCUMENT ME!
     */
    public static final int INTERFACE = 512;

    /**
     * DOCUMENT ME!
     */
    public static final int ABSTRACT = 1024;

    /**
     * DOCUMENT ME!
     */
    public static final int STRICT = 2048;

    /**
     * DOCUMENT ME!
     */
    public static final int ENUM = 4096;

    /**
     * DOCUMENT ME!
     */
    public static final int ANNOTATION = 8192;
    private int modifiers;

    /**
     * Creates a new Modifiable object.
     */
    public Modifiable() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isInterface() {
        return (modifiers & INTERFACE) != 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param interfaceType DOCUMENT ME!
     */
    public void setInterface(boolean interfaceType) {
        if (interfaceType && !isInterface()) {
            this.modifiers += INTERFACE;
        } else if (!interfaceType && isInterface()) {
            this.modifiers -= INTERFACE;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isEnum() {
        return (modifiers & ENUM) != 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param enumType DOCUMENT ME!
     */
    public void setEnum(boolean enumType) {
        if (enumType && !isEnum()) {
            this.modifiers += ENUM;
        } else if (!enumType && isEnum()) {
            this.modifiers -= ENUM;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isAnnotation() {
        return (modifiers & ANNOTATION) != 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param annoType DOCUMENT ME!
     */
    public void setAnnotation(boolean annoType) {
        if (annoType && !isAnnotation()) {
            this.modifiers += ANNOTATION;
        } else if (!annoType && isAnnotation()) {
            this.modifiers -= ANNOTATION;
        }
    }
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isAbstract() {
        return (modifiers & ABSTRACT) != 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param abstractType DOCUMENT ME!
     */
    public void setAbstract(boolean abstractType) {
        if (abstractType && !isAbstract()) {
            this.modifiers += ABSTRACT;
        } else if (!abstractType && isAbstract()) {
            this.modifiers -= ABSTRACT;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isFinal() {
        return (modifiers & FINAL) != 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param finalType DOCUMENT ME!
     */
    public void setFinal(boolean finalType) {
        if (finalType && !isFinal()) {
            this.modifiers += FINAL;
        } else if (!finalType && isFinal()) {
            this.modifiers -= FINAL;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isStrictFp() {
        return (modifiers & STRICT) != 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param strictFp DOCUMENT ME!
     */
    public void setStrictFp(boolean strictFp) {
        if (strictFp && !isStrictFp()) {
            this.modifiers += STRICT;
        } else if (!strictFp && isStrictFp()) {
            this.modifiers -= STRICT;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isStatic() {
        return (modifiers & STATIC) != 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param staticType DOCUMENT ME!
     */
    public void setStatic(boolean staticType) {
        if (staticType && !isStatic()) {
            this.modifiers += STATIC;
        } else if (!staticType && isStatic()) {
            this.modifiers -= STATIC;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isPrivate() {
        return (modifiers & PRIVATE) != 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param privateModifier DOCUMENT ME!
     */
    public void setPrivate(boolean privateModifier) {
        if (privateModifier && !isPrivate()) {
            this.modifiers += PRIVATE;
        } else if (!privateModifier && isPrivate()) {
            this.modifiers -= PRIVATE;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isProtected() {
        return (modifiers & PROTECTED) != 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param protectedModifier DOCUMENT ME!
     */
    public void setProtected(boolean protectedModifier) {
        if (protectedModifier && !isProtected()) {
            this.modifiers += PROTECTED;
        } else if (!protectedModifier && isProtected()) {
            this.modifiers -= PROTECTED;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isPublic() {
        return (modifiers & PUBLIC) != 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param publicModifier DOCUMENT ME!
     */
    public void setPublic(boolean publicModifier) {
        if (publicModifier && !isPublic()) {
            this.modifiers += PUBLIC;
        } else if (!publicModifier && isPublic()) {
            this.modifiers -= PUBLIC;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getModifiers() {
        return modifiers;
    }

    /**
     * DOCUMENT ME!
     *
     * @param modifiers DOCUMENT ME!
     */
    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * DOCUMENT ME!
     *
     * @param s DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static boolean isModifier(String s) {
        if (MODIFIER_ABSTRACT.equals(s) || MODIFIER_PRIVATE.equals(s)
                || MODIFIER_PROTECTED.equals(s) || MODIFIER_PUBLIC.equals(s)
                || MODIFIER_STRICTFP.equals(s) || MOFIFIER_FINAL.equals(s)
                || MODIFIER_STATIC.equals(s) || MODIFIER_DEFAULT.equals(s)) {
            return true;
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param modifier DOCUMENT ME!
     */
    public void assignModifier(String modifier) {
        if (Modifiable.MODIFIER_ABSTRACT.equals(modifier)) {
            setAbstract(true);
        } else if (Modifiable.MODIFIER_PRIVATE.equals(modifier)) {
            setPrivate(true);
        } else if (Modifiable.MODIFIER_PROTECTED.equals(modifier)) {
            setProtected(true);
        } else if (Modifiable.MODIFIER_PUBLIC.equals(modifier)) {
            setPublic(true);
        } else if (Modifiable.MODIFIER_STRICTFP.equals(modifier)) {
            setStrictFp(true);
        } else if (Modifiable.MOFIFIER_FINAL.equals(modifier)) {
            setFinal(true);
        } else if (Modifiable.MODIFIER_STATIC.equals(modifier)) {
            setStatic(true);
        }

        // default (package private) is absence of public / protected / private
    }
}
