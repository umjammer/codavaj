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

import org.codavaj.ProcessException;

import org.codavaj.process.AbstractProcess;
import org.codavaj.type.TypeFactory;


/**
 * DOCUMENT ME!
 */
public class Search extends AbstractProcess {

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
    public void process() throws ProcessException {
        try {
        	searchResult = new SearchContext();
        	SearchAlgorithm algorithm = new SearchAlgorithm( javadocTypeFactory, jarTypeFactory, searchResult);
        	algorithm.search();
        	
        	// the ctx holds the result!
        } catch ( Exception e ) {
            warning( "Search failed!", e );
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
