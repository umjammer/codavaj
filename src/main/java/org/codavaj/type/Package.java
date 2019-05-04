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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A reflection-like representation of a java.lang.Package.
 */
public class Package {
    static final String defaultPackageName = "";
    private String name;
    private Package parentPackage;
    private Map<String, Type> types = new HashMap<>();
    private Map<String, Package> packages = new HashMap<>();

    Package(String packagename) {
        name = packagename;
    }

    /**
     * The '.' dot separated fully qualified name of the Package.
     * i.e. a.b.c.d
     *
     * @return the fully qualified Package name.
     */
    public String getName() {
        return name;
    }

    /**
     * Return all Types in the Package.
     *
     * @return all Types in the Package.
     */
    public List<Type> getTypes() {
        return new ArrayList<>(types.values());
    }

    /**
     * Return all sub Packages.
     *
     * @return all sub Packages.
     */
    public List<Package> getPackages() {
        return new ArrayList<>(packages.values());
    }

    void addType(Type type) {
        if (types.get(type.getTypeName()) == null) {
            types.put(type.getTypeName(), type);
        }
    }

    void addPackage(Package pckg) {
        if (packages.get(pckg.getName()) == null) {
            packages.put(pckg.getName(), pckg);
        }
    }

    String getParentPackageName() {
        if (name.indexOf(".") != -1) {
            // there is a parent package if there is a package separator in the name
            return name.substring(0, name.lastIndexOf("."));
        }

        return "";
    }

    /**
     * Return the parent Package, null if the Package is the default package.
     *
     * @return the parent Package.
     */
    public Package getParentPackage() {
        return parentPackage;
    }

    void setParentPackage(Package parentPackage) {
        this.parentPackage = parentPackage;
    }
}
