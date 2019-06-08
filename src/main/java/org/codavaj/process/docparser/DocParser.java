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

package org.codavaj.process.docparser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.codavaj.ProcessException;
import org.codavaj.process.ProgressEvent;
import org.codavaj.process.Progressive;
import org.codavaj.type.Type;
import org.codavaj.type.TypeFactory;

import static org.codavaj.Logger.error;

/**
 * Read an entire javadoc file tree and construct a reflection-like
 * representation of all it's constituent parts ( Classes, Interfaces ... ) in
 * a TypeFactory.
 */
public class DocParser implements Progressive<TypeFactory> {

    /**
     * directory to find javadoc root.
     */
    private String javadocDirName;

    /**
     *  javadocClassName used by Tests to parse single classes instead of all docs
     */
    private Pattern javadocClassName;
    private List<String> externalLinks;

    /**
     * Identify all classes from the javadoc and then analyze each one in turn
     * to parse its information into Types.
     *
     * @throws ProcessException failure to construct a TypeFactory.
     */
    public TypeFactory process() throws ProcessException {

        TypeFactory typeFactory = new TypeFactory();

        ParserUtils parserUtil;

Map<Type, Exception> errors = new HashMap<>();

        try {
            // load and then process the list of all classes javadoc

            parserUtil = ParserUtils.factory(javadocDirName);
            parserUtil.setExternalLinks(externalLinks);

            for (int i = 0; i < parserUtil.getClasses().size(); i++) {
                String typeName = parserUtil.getClasses().get(i);
                if (javadocClassName == null || javadocClassName.matcher(typeName).find()) {
                    typeFactory.createType(typeName);
                }
            }
        } catch (Exception e) {
            error("All class determination failed!", e);
            throw new ProcessException(e);
        }

        // now the typeFactory is loaded with the type names
        // we must go through each in turn
        List<Type> alltypes = typeFactory.getTypes();

        for (int i = 0; (alltypes != null) && (i < alltypes.size()); i++) {
            Type type = alltypes.get(i);
            notifyListeners(new ProgressEvent(i + 1, alltypes.size(), type.getTypeName()));

            try {
                parserUtil.processType(type);
            } catch (Exception e) {
                error("Class parsing failed on " + type.getTypeName());
errors.put(type, e);
//                throw new ProcessException(e);
            }
        }

errors.entrySet().forEach(e -> {
 System.err.println("******************: " + e.getKey().getShortName());
 e.getValue().printStackTrace();
 System.err.println(e.getValue().getMessage());
});

        try {
            // try and determine all constants
            //info( parserUtil.prettyPrint(allconstants));
            parserUtil.processConstant(typeFactory.getTypeMap(), javadocClassName != null);
        } catch (Exception e) {
            error("All constant determination failed!", e);
            throw new ProcessException(e);
        }

        typeFactory.link();
        typeFactory.setFullyQualifiedNameMap(parserUtil.getFullyQualifiedNameMap());

        return typeFactory;
    }

    /**
     * Set the list of externally linked references which are used to resolve
     * Type names in the javadoc.
     *
     * @param externalLinks the external references list.
     */
    public void setExternalLinks(List<String> externalLinks) {
        this.externalLinks = externalLinks;
    }

    /**
     * Set the directory where the javadoc file tree resides.
     *
     * @param javadocDirName the directory of the javadoc.
     */
    public void setJavadocDirName(String javadocDirName) {
        this.javadocDirName = javadocDirName;
    }

    /**
     * @param javadocClassName the regex pattern matching javadoc class name to set
     */
    public void setJavadocClassName(String javadocClassName) {
        this.javadocClassName = Pattern.compile(javadocClassName);
    }
}
