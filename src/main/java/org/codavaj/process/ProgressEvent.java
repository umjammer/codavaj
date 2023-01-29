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

/**
 * DOCUMENT ME!
 */
public class ProgressEvent {

    private int done;
    private int todo;
    private String message;

    /**
     * Creates a new ProgressEvent object.
     *
     * @param message DOCUMENT ME!
     */
    public ProgressEvent(String message) {
        this.message = message;
    }

    /**
     * Creates a new ProgressEvent object.
     *
     * @param done DOCUMENT ME!
     * @param todo DOCUMENT ME!
     * @param message DOCUMENT ME!
     */
    public ProgressEvent(int done, int todo, String message) {
        this.message = message;
        this.done = done;
        this.todo = todo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getDone() {
        return done;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getMessage() {
        return message;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getTodo() {
        return todo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        if (todo == 0) {
            return getMessage();
        } else {
            return "[" + getDone() + "/" + getTodo() + "] " + getMessage();
        }
    }
}
