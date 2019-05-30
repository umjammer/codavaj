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

import java.io.File;
import java.util.List;

import org.codavaj.Main;
import org.codavaj.ProcessException;
import org.codavaj.process.Progressive;
import org.codavaj.process.ProgressEvent;
import org.codavaj.type.Type;
import org.codavaj.type.TypeFactory;
import org.dom4j.Document;

import static org.codavaj.Logger.error;
import static org.codavaj.Logger.info;

/**
 * Read an entire javadoc file tree and construct a reflection-like
 * representation of all it's constituent parts ( Classes, Interfaces ... ) in
 * a TypeFactory.
 */
public class DocParser implements Progressive {

    /**
     * directory to find javadoc root.
     */
    private String javadocDirName;
    private boolean debugFlag;

    /**
     *  javadocClassName used by Tests to parse single classes instead of all docs
     */
    private String javadocClassName;
    private List<?> externalLinks;
    private ParserUtils parserUtil;
    private TypeFactory typeFactory = new TypeFactory();

    /**
     * Creates a new DocParser object.
     */
    public DocParser() {
        debugFlag = false;
    }

    /**
     * Identify all classes from the javadoc and then analyze each one in turn
     * to parse its information into Types.
     *
     * @throws ProcessException failure to construct a TypeFactory.
     */
    public void process() throws ProcessException {
        File javadocDir = new File(javadocDirName);

        if (!javadocDir.exists()) {
            javadocDir.mkdirs();
        }

        if (!javadocDir.isDirectory()) {
            throw new ProcessException("" + javadocDir
                + " must be a directory.");
        }

        try {
            // load and then process the list of all classes javadoc
            String allclassesfilename = javadocDirName + Main.FILE_SEPARATOR
                + "allclasses-frame.html";

            parserUtil = ParserUtils.factory(allclassesfilename);

            Document allclasses = parserUtil.loadFileAsDom(allclassesfilename);

            List<?> classes = parserUtil.getAllFqTypenames(allclasses);

            for (int i = 0; i < classes.size(); i++) {
                String typeName = (String) classes.get(i);
                if ( getJavadocClassName() == null || getJavadocClassName().equals(typeName)) {
                    typeFactory.createType(typeName);
                }
            }
        } catch (Exception e) {
            error("All class determination failed!", e);

            return;
        }

        // now the typeFactory is loaded with the type names
        // we must go through each in turn
        List<?> alltypes = typeFactory.getTypes();

        for (int i = 0; (alltypes != null) && (i < alltypes.size()); i++) {
            Type type = (Type) alltypes.get(i);
            notifyListeners(new ProgressEvent(i + 1, alltypes.size(),
                    type.getTypeName()));

            Document typeXml = null;
            try {
                String filename = javadocDirName + Main.FILE_SEPARATOR
                    + parserUtil.filenameFromTypename(type.getTypeName());
                typeXml = parserUtil.loadFileAsDom(filename);

                if (parserUtil.isAnnotation(typeXml)) {
                    type.setAnnotation(true);
                } else if (parserUtil.isInterface(typeXml)) {
                    type.setInterface(true);
                } else if (parserUtil.isEnum(typeXml)) {
                    type.setEnum(true);
                    parserUtil.extendedType(type, typeXml, externalLinks);
                } else if (parserUtil.isClass(typeXml)) {
                    parserUtil.extendedType(type, typeXml, externalLinks);
                } else {
                    throw new ProcessException("type " + type.getTypeName()
                        + " is neither class, interface, enum or annotation.");
                }
                if ( isDebugFlag()) {
                    info( parserUtil.prettyPrint(typeXml) );
                }

                parserUtil.determineImplementsList(type, typeXml, externalLinks);

                parserUtil.determineTypeModifiers(type, typeXml, externalLinks);

                parserUtil.determineElements(type, typeXml, externalLinks);

                parserUtil.determineMethods(type, typeXml, externalLinks);

                parserUtil.determineFields(type, typeXml, externalLinks);

                parserUtil.determineEnumConsts(type, typeXml, externalLinks);

                parserUtil.determineConstructors(type, typeXml, externalLinks);

                parserUtil.determineDetails(type, typeXml, externalLinks);

                parserUtil.determineClassComment(type, typeXml, externalLinks);
            } catch (Exception e) {
                error("Class parsing failed on " + type.getTypeName(), e);
System.err.println(parserUtil.prettyPrint(typeXml));
System.exit(1);
            }
        }

        try {
            // try and determine all constants
            String allconstantsfilename = javadocDirName + Main.FILE_SEPARATOR
                + "constant-values.html";
            Document allconstants = parserUtil.loadFileAsDom(allconstantsfilename);

            //info( parserUtil.prettyPrint(allconstants));
            parserUtil.determineConstants(allconstants, typeFactory,
                externalLinks, getJavadocClassName() != null);
        } catch (Exception e) {
            error("All constant determination failed!", e);
        }

        typeFactory.link();
    }

    /**
     * Return the TypeFactory representing the entire Javadoc tree. Will only
     * provide a value after Docparser#process() has been called.
     *
     * @return the TypeFactory representing the entire Javadoc tree.
     */
    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    /**
     * Return the list of configured external references ( links in javadoc to
     * http://somedomain.com/ )
     *
     * @return the list of configured exteral references.
     */
    public List<?> getExternalLinks() {
        return externalLinks;
    }

    /**
     * Set the list of externally linked references which are used to resolve
     * Type names in the javadoc.
     *
     * @param externalLinks the external references list.
     */
    public void setExternalLinks(List<?> externalLinks) {
        this.externalLinks = externalLinks;
    }

    /**
     * Return the directory where the javadoc file tree is located.
     *
     * @return the directory of the javadoc.
     */
    public String getJavadocDirName() {
        return javadocDirName;
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
     * @return the javadocClassName
     */
    public String getJavadocClassName() {
        return javadocClassName;
    }

    /**
     * @param javadocClassName the javadocClassName to set
     */
    public void setJavadocClassName(String javadocClassName) {
        this.javadocClassName = javadocClassName;
    }

    /**
     * @return the debugFlag
     */
    public boolean isDebugFlag() {
        return debugFlag;
    }

    /**
     * @param debugFlag the debugFlag to set
     */
    public void setDebugFlag(boolean debugFlag) {
        this.debugFlag = debugFlag;
    }
}
