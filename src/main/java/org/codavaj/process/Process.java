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

package org.codavaj.process;

import org.codavaj.ProcessException;

/**
 * DOCUMENT ME!
 *
 */
public interface Process {
    /**
     * DOCUMENT ME!
     *
     * @param lstnr DOCUMENT ME!
     */
    public void addProgressListener(ProgressListener lstnr);

    /**
     * DOCUMENT ME!
     *
     * @param lstnr DOCUMENT ME!
     */
    public void removeProgressListener(ProgressListener lstnr);

    /**
     * DOCUMENT ME!
     *
     * @throws ProcessException DOCUMENT ME!
     */
    public void process() throws ProcessException;
}
