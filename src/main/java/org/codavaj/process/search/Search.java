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

package org.codavaj.process.search;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.codavaj.ProcessException;
import org.codavaj.process.Progressive;
import org.codavaj.type.TypeFactory;

/**
 * DOCUMENT ME!
 */
public class Search implements Progressive<Void> {

    private static final Logger logger = Logger.getLogger(Search.class.getName());

    private TypeFactory javadocTypeFactory;
    private TypeFactory jarTypeFactory;
    private SearchContext searchResult;

    /**
     * Creates a new Search object.
     */
    public Search() {
    }

    /**
     * DOCUMENT ME!
     *
     * @throws ProcessException DOCUMENT ME!
     */
    public Void process() throws ProcessException {
        try {
            searchResult = new SearchContext();
            SearchAlgorithm algorithm = new SearchAlgorithm( javadocTypeFactory, jarTypeFactory, searchResult);
            algorithm.search();

            // the ctx holds the result!
            return null;
        } catch ( Exception e ) {
            logger.log(Level.WARNING,  "Search failed!", e);
            throw new ProcessException(e);
        }
    }

    public TypeFactory getJarTypeFactory() {
        return jarTypeFactory;
    }

    public void setJarTypeFactory(TypeFactory jarTypeFactory) {
        this.jarTypeFactory = jarTypeFactory;
    }

    public TypeFactory getJavadocTypeFactory() {
        return javadocTypeFactory;
    }

    public void setJavadocTypeFactory(TypeFactory javadocTypeFactory) {
        this.javadocTypeFactory = javadocTypeFactory;
    }

    public SearchContext getSearchResult() {
        return searchResult;
    }

}
