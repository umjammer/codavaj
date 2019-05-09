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

/**
 * DOCUMENT ME!
 */
public class Logger {
    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     */
    public static void info(String message) {
        System.out.println("INFO: " + message);
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     */
    public static void debug(String message) {
        System.out.println("DEBUG: " + message);
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     */
    public static void warning(String message) {
        System.out.println("WARN: " + message);
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     * @param e DOCUMENT ME!
     */
    public static void warning(String message, Throwable e) {
        System.err.println("WARN: " + message);
        e.printStackTrace(System.out);
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     */
    public static void error(String message) {
        System.out.println("ERROR: " + message);
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     * @param e DOCUMENT ME!
     */
    public static void error(String message, Throwable e) {
        System.err.println("ERROR: " + message);
        e.printStackTrace(System.out);
    }
}
