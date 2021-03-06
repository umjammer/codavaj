package org.codavaj.process.search;

import java.util.HashMap;
import java.util.Map;

import org.codavaj.type.Type;
import org.codavaj.type.Package;

public class SearchContext {

    private Map<String, Type> types = new HashMap<>();
    private Map<String, Package> packages = new HashMap<>();

    public void setMap( Package javadocPackage, Package jarPackage ) {
        if ( packages.containsKey(jarPackage.getName())) {
            throw new IllegalArgumentException("Package " + jarPackage.getName() + " is already mapped.");
        }
        packages.put(jarPackage.getName(), javadocPackage);
    }

    public void removeMap( Package javadocPackage, Package jarPackage ) {
        packages.remove(jarPackage.getName());
    }

    public void setMap( Type javadocType, Type jarType ) {
        if ( types.containsKey(jarType.getTypeName())) {
            throw new IllegalArgumentException("Type " + jarType.getTypeName() + " is already mapped.");
        }
        types.put(jarType.getTypeName(), javadocType);
    }

    public Package getMap( Package jarPackage ) {
        return packages.get(jarPackage.getName());
    }

    public Type getMap( Type jarType ) {
        return types.get(jarType.getTypeName());
    }

    public void removeMap( Type javadocType, Type jarType ) {
        types.remove(jarType.getTypeName());
    }

    public Map<String, Package> getPackageMap() {
        return packages;
    }

    public Map<String, Type> getTypeMap() {
        return types;
    }
}
