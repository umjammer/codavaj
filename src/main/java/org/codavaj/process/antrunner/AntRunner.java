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

package org.codavaj.process.antrunner;

import org.apache.tools.ant.launch.Launcher;

import org.codavaj.ProcessException;
import org.codavaj.process.Progressive;

import java.io.File;

/**
 * DOCUMENT ME!
 */
public class AntRunner implements Progressive<Void> {

    private String antFileName;
    private String antTarget;

    /**
     * DOCUMENT ME!
     *
     * @throws ProcessException DOCUMENT ME!
     */
    public Void process() throws ProcessException {
        File antFile = new File(antFileName);

        if (!antFile.exists()) {
            throw new ProcessException(antFileName + " does not exist.");
        }
        // org.apache.tools.ant.launch.Launcher
        //weaken
        //generatemapping
        //reverse
        String[] args = new String[] { "-v", "-f", antFileName, antTarget };
        Launcher.main(args);

        return null;
    }
}
