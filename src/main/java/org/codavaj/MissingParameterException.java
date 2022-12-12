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

package org.codavaj;

import java.io.Serial;


/**
 * DOCUMENT ME!
 */
public class MissingParameterException extends ProcessException {
    @Serial
    private static final long serialVersionUID = -7654967901729855056L;

    /**
     * Creates a new MissingParameterException object.
     *
     * @param propertyname DOCUMENT ME!
     */
    public MissingParameterException(String propertyname) {
        super("Missing property " + propertyname);
    }
}
