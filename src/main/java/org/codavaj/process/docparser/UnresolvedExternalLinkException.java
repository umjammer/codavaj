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

import java.io.Serial;


/**
 * Unresolved external references to javadocs prohibit correct
 * type name determination.
 */
public class UnresolvedExternalLinkException extends ParseException {
    @Serial
    private static final long serialVersionUID = 6822744609916808575L;

    /**
     * Creates a new UnresolvedExternalLinkException object.
     */
    public UnresolvedExternalLinkException() {
        super();
    }

    /**
     * Creates a new UnresolvedExternalLinkException object.
     *
     * @param arg0 problem description.
     */
    public UnresolvedExternalLinkException(String arg0) {
        super(arg0);
    }

    /**
     * Creates a new UnresolvedExternalLinkException object.
     *
     * @param arg0 causing exception.
     */
    public UnresolvedExternalLinkException(Throwable arg0) {
        super(arg0);
    }

    /**
     * Creates a new UnresolvedExternalLinkException object.
     *
     * @param arg0 problem description.
     * @param arg1 causing exception.
     */
    public UnresolvedExternalLinkException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
