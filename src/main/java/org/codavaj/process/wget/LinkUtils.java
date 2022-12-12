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

package org.codavaj.process.wget;

import java.util.logging.Logger;

/**
 * A utility set for working with URL's from javadocs.
 */
public class LinkUtils {

    private static final Logger logger = Logger.getLogger(LinkUtils.class.getName());

    /** Separator in URL's within javadocs. */
    protected static final String URL_SEPARATOR = "/";

    /**
     * A Normalized URL ends with a URL_SEPARATOR.
     *
     * @param url the URL to normalize.
     *
     * @return the normalized URL.
     */
    public String normalizeUrl(String url) {
        if (!url.endsWith(URL_SEPARATOR)) {
            url = url + URL_SEPARATOR;
        }

        return url;
    }

    /**
     * DOCUMENT ME!
     *
     * @param rootUrl DOCUMENT ME!
     * @param url DOCUMENT ME!
     * @param link DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String relativeUrl(String rootUrl, String url, String link) {
        if ((link == null) || (rootUrl == null) || (url == null)) {
            return null;
        }

        if (!rootUrl.endsWith(URL_SEPARATOR)) {
            logger.warning("Root url " + rootUrl + " is not normalized");

            return null;
        }

        if ("".equals(link.trim())) {
            return null;
        }

        if (link.contains("#")) {
            // java/awt/geom/RectangularShape.html#getCenterX() -> java/awt/geom/RectangularShape.html
            link = link.substring(0, link.indexOf("#"));
        }

        String path = "";

        //String basename = "";
        if (url.lastIndexOf(URL_SEPARATOR) != -1) {
            path = url.substring(0, url.lastIndexOf(URL_SEPARATOR) + 1);

            //basename = url.substring(url.lastIndexOf(URL_SEPARATOR)+1);
        }

        // Basis for calculating a "normalized" URL for the link
        // rootUrl : http://a/b/c/
        //     url : http://a/b/c/d/e.html
        //    path : http://a/b/c/d/
        //basename :                e.html
        // (a)link : http://a/b/c/d/e.html -> d/e.html
        // (b)link : http://a/b/c/d
        //    link : f/g
        //         : ./f/g
        //         : ../../f/g -> http://a/b/c/d/e/../../f/g
        if (link.startsWith(URL_SEPARATOR)) {
            logger.fine("link absolute " + link);

            return null;
        }

        while (link.endsWith(URL_SEPARATOR)) { // strip trailing /
            link = link.substring(0, link.length() - 1);
        }

        while (link.startsWith("." + URL_SEPARATOR)) { // strip leading ./
            link = link.substring(2);
        }

        // TODO - strip directories back off url for ../

        /*
        info( "rooturl          :" + rootUrl );
        info( "url              :" + url );
        info( "path             :" + path );
        info( "basename         :" + basename);
        info( "            link :" + link);
        */
        if (link.startsWith(path)) { // a

            String rel = link.substring(path.length());
            logger.fine("(a)link: " + link + " -> " + rel);
            link = rel;
        }

        if (link.startsWith("http") || link.startsWith("ftp")
                || link.startsWith("email")) {
            // link is not relative
            logger.fine("link " + link + " is not relative - skipping.");

            return null;
        }

        if (link.contains("../")) {
            logger.fine("cannot handle relative links with ../ - " + link);

            return null;
        }

        String absoluteUrl = path + link;
        String relativeUrl = absoluteUrl.substring(rootUrl.length());

        logger.info("relativeUrl " + relativeUrl);

        return relativeUrl;
    }

    /**
     * Return the directory of the URL. For instance a/b/c/D.ext gives a/b/c/
     *
     * @param url the full URL.
     *
     * @return the directory part of the URL.
     */
    public String relativeDirectoryOfLink(String url) {
        if (url == null) {
            return "";
        }

        if (url.contains(URL_SEPARATOR)) {
            return url.substring(0, url.lastIndexOf(URL_SEPARATOR) + 1);
        }

        return "";
    }

    /**
     * DOCUMENT ME!
     *
     * @param url DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String basenameOfLink(String url) {
        if (url == null) {
            return null;
        }

        if (url.contains(URL_SEPARATOR)) {
            return url.substring(url.lastIndexOf(URL_SEPARATOR) + 1
            );
        }

        return url;
    }
}
