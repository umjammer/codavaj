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
 * Logger interface.
 *
 * @author Peter
 */
public interface Logger {
    /**
     * Log an info message.
     *
     * @param message the message
     */
    public void info(String message);

    /**
     * Log a debug message.
     *
     * @param message the message
     */
    public void debug(String message);

    /**
     * Log a warning message.
     *
     * @param message the message
     */
    public void warning(String message);

    /**
     * Log a warning message with stacktrace.
     *
     * @param message the message
     * @param e the exception
     */
    public void warning(String message, Throwable e);

    /**
     * Log an info message.
     *
     * @param message the message
     */
    public void error(String message);

    /**
     * Log an error message and stacktrace.
     *
     * @param message the message
     * @param e the exception
     */
    public void error(String message, Throwable e);
}
