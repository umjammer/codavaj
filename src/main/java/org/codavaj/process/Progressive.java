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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codavaj.ProcessException;

/**
 * DOCUMENT ME!
 */
public interface Progressive<T> {

    // TODO should be protected
    Map<Progressive<?>, List<ProgressListener>> listeners = new HashMap<>();

    /**
     * DOCUMENT ME!
     *
     * @param lstnr DOCUMENT ME!
     */
    default void addProgressListener(ProgressListener lstnr) {
        List<ProgressListener> l = listeners.computeIfAbsent(this, k -> new LinkedList<>());
        l.add(lstnr);
    }

    /**
     * DOCUMENT ME!
     *
     * @param lstnr DOCUMENT ME!
     */
    default void removeProgressListener(ProgressListener lstnr) {
        List<ProgressListener> l = listeners.get(this);
        if (l != null) {
            l.remove(lstnr);
        }
    }

    // TODO should be protected
    default void notifyListeners(ProgressEvent event) {
        List<ProgressListener> ll = listeners.get(this);
        if (ll != null) {

            for (ProgressListener l : ll) {
                l.notify(event);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws ProcessException DOCUMENT ME!
     */
    T process() throws ProcessException;
}
