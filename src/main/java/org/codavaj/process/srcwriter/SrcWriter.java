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

package org.codavaj.process.srcwriter;

import org.codavaj.Main;
import org.codavaj.MissingParameterException;
import org.codavaj.ProcessException;

import org.codavaj.process.AbstractProcess;
import org.codavaj.process.ProgressEvent;

import org.codavaj.type.Type;
import org.codavaj.type.TypeFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.List;


/**
 * DOCUMENT ME!
 */
public class SrcWriter extends AbstractProcess {
    /**
     * DOCUMENT ME!
     */
    private String srcDirName;
    private TypeFactory typeFactory = null;

    /**
     * Creates a new SrcWriter object.
     */
    public SrcWriter() {
    }

    /**
     * Write all parsed javadoc files back as java source files.
     *
     * @throws ProcessException DOCUMENT ME!
     * @throws MissingParameterException DOCUMENT ME!
     */
    public void process() throws ProcessException {
        if (typeFactory == null) {
            throw new MissingParameterException("no type factory");
        }
        File srcDir = new File(srcDirName);
        if (!srcDir.exists()) {
            srcDir.mkdirs();
        }
        if (!srcDir.isDirectory()) {
            throw new ProcessException("" + srcDir + " must be a directory.");
        }

        List<?> alltypes = typeFactory.getTypes();

        for (int i = 0; (alltypes != null) && (i < alltypes.size()); i++) {
            Type type = (Type) alltypes.get(i);

            notifyListeners(new ProgressEvent(i + 1, alltypes.size(),
                    type.getTypeName()));

            try {
                if ( type.getEnclosingType() != null ) {
                    continue; // dont write inner types into own files
                }
                String packageName = type.getPackage().getName();

                if (packageName != null) {
                    packageName = packageName.replace('.', '/');
                } else {
                    packageName = "";
                }

                String packageDirName = srcDirName + Main.FILE_SEPARATOR
                    + packageName;

                //debug( packageDirName );
                File packageDir = new File(packageDirName);

                if (!packageDir.exists()) {
                    packageDir.mkdirs();
                }

                String filename = packageDirName + Main.FILE_SEPARATOR
                    + type.getShortName() + ".java";
                FileWriter fw = new FileWriter(filename);
                BufferedWriter bw = new BufferedWriter(fw);
                WriterUtils.print(type, bw);
                bw.flush();
                bw.close();
            } catch (IOException iox) {
                error("Error processing " + type.getTypeName(), iox);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param types DOCUMENT ME!
     */
    public void setTypeFactory(TypeFactory types) {
        typeFactory = types;
    }

    public String getSrcDirName() {
        return srcDirName;
    }

    public void setSrcDirName(String srcDirName) {
        this.srcDirName = srcDirName;
    }
}
