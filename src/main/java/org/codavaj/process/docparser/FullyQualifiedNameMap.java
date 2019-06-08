/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.codavaj.process.docparser;

import java.util.HashMap;

import org.codavaj.type.Type;

import static org.codavaj.Logger.warning;

/**
 * FullyQualifiedNameMap.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/06/07 umjammer initial version <br>
 */
public class FullyQualifiedNameMap extends HashMap<String, String> {

    /** { short name, fully qualified name } */
    public FullyQualifiedNameMap() {
        put(Void.TYPE.toString(), Void.TYPE.toString());
        put(Boolean.TYPE.toString(), Boolean.TYPE.toString());
        put(Integer.TYPE.toString(), Integer.TYPE.toString());
        put(Short.TYPE.toString(), Short.TYPE.toString());
        put(Byte.TYPE.toString(), Byte.TYPE.toString());
        put(Long.TYPE.toString(), Long.TYPE.toString());
        put(Float.TYPE.toString(), Float.TYPE.toString());
        put(Double.TYPE.toString(), Double.TYPE.toString());
        put(Character.TYPE.toString(), Character.TYPE.toString());
    }

    /**
     * @param fqn fully qualified name
     */
    public void add(String fqn) {
        if (!containsValue(fqn)) {
            put(fqn.substring(fqn.lastIndexOf(".") + 1), fqn);
        }
    }

    /** */
    public String toFullyQualifiedName(Type type, String typeName) {
        if (containsKey(typeName)) {
            return get(typeName);
        } else {
            if (typeName.indexOf(".") != -1) {
                add(typeName);
                return typeName;
            } else {
                String className = (type.getPackageName() != "" ? type.getPackageName() + "." : "") + typeName;
                if (containsKey(className)) {
                    put(typeName, className);
                    return className;
                } else {
                    return guess(typeName);
                }
            }
        }
    }

    /**
     * @return if guess is failed, return self
     */
    public String guess(String typeName) {
        try {
            Class<?> clazz = Class.forName("java.lang." + typeName);
            String className = clazz.getName();
            put(typeName, className);
            return className;
        } catch (ClassNotFoundException e) {
            warning("not found: " + typeName);
            return typeName;
        }
    }
}

/* */
