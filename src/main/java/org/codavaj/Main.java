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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.codavaj.process.docparser.DocParser;
import org.codavaj.process.srcwriter.SrcWriter;
import org.codavaj.process.wget.Wget;
import org.codavaj.type.TypeFactory;

/**
 * DOCUMENT ME!
 */
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static final String usage_wget = "wget <url> <output-dir>";
    private static final String usage_parse = "codavaj <input-dir> <output-dir> {<external-link>}*";

    /**
     * Derive a reflection-like API from a javadoc source tree. Resolve any type names
     * to external javadoc links. External links to Sun's JDK javadoc apis are
     * automatically resolved ( i.e. http://java.sun.com/j2se/X/docs/api/ )
     *
     * @param javadocdir the javadoc tree root
     * @param externalLinks a list of 'http://..' strings representing external javadoc refs.
     *
     * @return a TypeFactory handle on the resulting api
     * @throws ProcessException any problem.
     */
    public static TypeFactory analyze( String javadocdir, List<String> externalLinks ) throws ProcessException {

        DocParser dp = new DocParser();
        dp.setJavadocDirName(javadocdir);
        dp.setExternalLinks(externalLinks);
        dp.addProgressListener(System.err::println);

        return dp.process();
    }

    /**
     * DOCUMENT ME!
     *
     * @param args
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("usage alternatives: \n\t" + usage_wget + "\n\t"
                + usage_parse);

            return;
        }

        String cmd = args[0];
        String input = args[1];
        String output = args[2];

        // all subsequent arguments become external javadoc URL references
        List<String> externalLinks = new ArrayList<>();
        for( int i = 3; i < args.length; i++) {
            externalLinks.add(args[i]);
        }

        if ("wget".equals(cmd)) {
            Wget wget = new Wget();
            wget.setRootUrl(input);
            wget.setJavadocDirName(output);
            wget.addProgressListener(System.err::println);
            wget.process();
        } else if ("codavaj".equals(cmd)) {
            DocParser dp = new DocParser();
            dp.setJavadocDirName(input);
            dp.setExternalLinks(externalLinks);
            dp.addProgressListener(System.err::println);
            TypeFactory tf = dp.process();

            SrcWriter sw = new SrcWriter();
            sw.setSrcDirName(output);
            // link the previously parsed javadocs with the writer
            sw.setTypeFactory(tf);
            sw.addProgressListener(System.err::println);
            sw.process();
        } else {
            System.err.println("usage alternatives: \n\t" + usage_wget + "\n\t"
                + usage_parse);

            return;
        }
    }
}
