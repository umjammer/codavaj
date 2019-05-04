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

package org.codavaj.type;

/**
 * DOCUMENT ME!
 */
public class Parameter extends Modifiable {
    private String name;
    private String type;
    private String typeArgumentList; // Generics ParameterizedTypes, see http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.5
    private boolean array;
    private int degree;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        if (name != null) {
            sb.append(" ");
            sb.append(name);
        }
        return sb.toString();
    }

    /**
     * Creates a new Parameter object.
     */
    public Parameter() {
        super();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getType() {
        return type;
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isArray() {
        return array;
    }

    /**
     * DOCUMENT ME!
     *
     * @param array DOCUMENT ME!
     */
    public void setArray(boolean array) {
        this.array = array;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getDegree() {
        return degree;
    }

    /**
     * DOCUMENT ME!
     *
     * @param degree DOCUMENT ME!
     */
    public void setDegree(int degree) {
        this.degree = degree;
    }

    /**
     * @return the typeArgumentList
     */
    public String getTypeArgumentList() {
        return typeArgumentList;
    }

    /**
     * @param typeArgumentList the typeArgumentList to set
     */
    public void setTypeArgumentList(String typeArgumentList) {
        this.typeArgumentList = typeArgumentList;
    }
}
