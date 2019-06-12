/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.codavaj.process.docparser;

import java.util.HashMap;
import java.util.Optional;

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
//System.err.println("1: " + fqn);
            put(fqn.substring(fqn.lastIndexOf(".") + 1), fqn);
        }
    }

    /**
     * @param typeName short name
     */
    public String toFullyQualifiedName(Type type, String typeName) {
//System.err.println("2: " + typeName);
        typeName = typeName.replaceAll("<[\\w\\.\\[\\]\\$\\_\\s,\\*\\?]+>", "");
        if (containsKey(typeName)) {
            return get(typeName);
        } else {
            if (typeName.indexOf(".") != -1) {
                add(typeName);
                return typeName;
            } else {
                if (type.isTypeParameter(typeName)) {
//System.err.println("0: " + typeName);
                    return typeName;
                } else {
                    String className = (type.getPackageName() != "" ? type.getPackageName() + "." : "") + typeName;
                    if (containsKey(className)) {
//System.err.println("3: " + typeName + ", " + className);
                        put(typeName, className);
                        return className;
                    } else {
                        return guess(typeName);
                    }
                }
            }
        }
    }

    /**
     * @param typeName short name
     * @return if guess is failed, return self
     */
    public String guess(String typeName) {
        try {
            Class<?> clazz = Class.forName("java.lang." + typeName);
            String className = clazz.getName();
            put(typeName, className);
            return className;
        } catch (ClassNotFoundException e) {
//System.err.println("4: ---");
//values().stream().forEach(s -> System.err.println("4: " + s));
            Optional<String> found = values().stream().map(n -> n + "$" + typeName).filter(tn ->
                values().stream().anyMatch(n -> n.equals(tn))
            ).findFirst();
            if (found.isPresent()) {
                return found.get();
            } else {
                warning("not found: " + typeName);
                return typeName;
            }
        }
    }
}

/* */
