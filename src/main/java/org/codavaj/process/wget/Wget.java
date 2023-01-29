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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codavaj.ProcessException;
import org.codavaj.process.ProgressEvent;
import org.codavaj.process.Progressive;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebImage;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * DOCUMENT ME!
 */
public class Wget implements Progressive<Void> {

    private static final Logger logger = Logger.getLogger(LinkUtils.class.getName());

    private static final String CONST_INDEX_HTML = "index.html";
    private static final String CONST_INDEX_ALL_HTML = "index-all.html";
    private static final String CONST_CONSTANTS_HTML = "constant-values.html";
    private static final String CONST_SERIALIZED_FORM_HTML = "serialized-form.html";

    private static final String HTTP_SRC_ATTRIBUTE = "src";
    private static final String HTML_CONTENT = "text/html";

    private String rootUrl;
    private String javadocDirName;
    private boolean overwriteFiles = false;
    private int retryCount = 5;
    private long retryWait = 10000;
    private LinkUtils linkUtil = new LinkUtils();

    /**
     * Wget processes the project by downloading the javadoc tree and  saving
     * it into the javadoc directory of the basedir
     *
     * @throws ProcessException DOCUMENT ME!
     */
    public Void process() throws ProcessException {
        File javadocDir = new File(javadocDirName);

        if (!javadocDir.exists()) {
            javadocDir.mkdirs();
        }
        if (!javadocDir.isDirectory()) {
            throw new ProcessException(javadocDir + " must be a directory.");
        }

        if (!rootUrl.startsWith("http://") && !rootUrl.startsWith("https://")) {
            throw new ProcessException(
                "Only http or https protocol accepted - " + rootUrl);
        }
        if (rootUrl.endsWith(CONST_INDEX_HTML)) {
            throw new ProcessException("rootUrl must not end with index.html");
        }
        rootUrl = linkUtil.normalizeUrl(rootUrl);
        try {
            // create the conversation object which will maintain state for us
            WebConversation wc = new WebConversation();

            // stack to hold URL's which need fetching
            Stack<String> got = new Stack<>();
            Stack<String> fetch = new Stack<>();

            fetch.push(CONST_INDEX_HTML);
            fetch.push(CONST_INDEX_ALL_HTML);
            fetch.push(CONST_CONSTANTS_HTML);
            fetch.push(CONST_SERIALIZED_FORM_HTML);

            while (fetch.size() > 0) {
                getPage(wc, got, fetch);
            }
        } catch (Exception e) {
            throw new ProcessException(e);
        }

        return null;
    }

    private void getPage(WebConversation wc, Stack<String> got, Stack<String> fetch)
        throws Exception {
        String relativePath = fetch.pop();
        got.push(relativePath);

        String url = rootUrl + relativePath;
        notifyListeners(new ProgressEvent(got.size(), got.size() + fetch.size(), url));

        try {
            WebResponse response = fetchPage(wc, url);

            // save the retrieved contents to a file, relative to the javadoc basedir
            saveContent(relativePath, response);

            if (!HTML_CONTENT.equalsIgnoreCase(response.getContentType())) {
                logger.fine("not html " + url);

                return;
            }

            logger.fine("The page " + relativePath + " contains "
                + response.getLinks().length + " links");

            if (response.getFrameNames() != null) {
                // we have frames, so we need to put the link to each frame onto the
                for (int i = 0; i < response.getFrameNames().length; i++) {
                    String framename = response.getFrameNames()[i];
                    logger.fine("Frame " + framename);

                    HTMLElement[] frame = response.getElementsWithName(framename);

                    for (int j = 0; (frame.length != 0) && (j < frame.length); j++) {
                        HTMLElement f = frame[j];
                        String src = f.getAttribute(HTTP_SRC_ATTRIBUTE);
                        addRelativeUrl(url, src, got, fetch);
                    }

                    logger.info(frame[0].toString());
                }
            }

            if (response.getLinks() != null) {
                for (int i = 0; i < response.getLinks().length; i++) {
                    WebLink link = response.getLinks()[i];

                    addRelativeUrl(url, link.getURLString(), got, fetch);
                }
            }

            if (response.getImages() != null) {
                for (int i = 0; i < response.getImages().length; i++) {
                    WebImage image = response.getImages()[i];

                    addRelativeUrl(url, image.getSource(), got, fetch);
                }
            }

            if (response.getExternalStyleSheet() != null) {
                addRelativeUrl(url, response.getExternalStyleSheet(), got, fetch);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to get " + relativePath, e);
        }
    }

    private WebResponse fetchPage(WebConversation wc, String url)
        throws InterruptedException, ProcessException {
        Exception lastException = null;

        for (int i = 0; i < retryCount; i++) {
            try {
                WebRequest request = new GetMethodWebRequest(url);
                WebResponse response = wc.getResponse(request);

                return response;
            } catch (Exception e) {
                lastException = e;

                if (i < retryCount) {
                    logger.fine("Failed to get page " + url + " ... retrying"
                        + e.getMessage());
                    Thread.sleep(retryWait);
                }
            }
        }

        throw new ProcessException("Failed to retrieve " + url, lastException);
    }

    private void addRelativeUrl(String url, String link, Stack<String> got, Stack<String> fetch) {
        String relativeUrl = linkUtil.relativeUrl(rootUrl, url, link);

        if (relativeUrl == null) {
            return;
        }

        if (got.contains(relativeUrl)) {
            // link will be retrieved
            logger.fine("skipping fetched link " + relativeUrl);
        } else if (fetch.contains(relativeUrl)) {
            logger.fine("skipping planned link " + relativeUrl);
        } else {
            logger.fine("adding link " + relativeUrl);
            fetch.push(relativeUrl);
        }
    }

    private void saveContent(String relativePath, WebResponse response)
        throws Exception {
        logger.fine("saving " + relativePath);

        // make sure the directory we want to write to exist
        String directoryName = linkUtil.relativeDirectoryOfLink(relativePath);
        String fullDirName = javadocDirName + File.separator
            + directoryName;
        File directory = new File(fullDirName);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                logger.warning("Unable to create directory " + fullDirName);
            }
        }

        String baseName = linkUtil.basenameOfLink(relativePath);
        String fullFilename = fullDirName + File.separator + baseName;
        File outputFile = new File(fullFilename);

        if (outputFile.exists() && !overwriteFiles) {
            logger.fine(fullFilename + " skipped since exists locally.");

            return;
        }

        FileOutputStream fos = new FileOutputStream(outputFile);

        // TODO buffered variant
        InputStream is = response.getInputStream();
        int b = 0;

        while ((b = is.read()) != -1) {
            fos.write(b);
        }

        fos.close();
    }

    public String getJavadocDirName() {
        return javadocDirName;
    }

    public void setJavadocDirName(String javadocDirName) {
        this.javadocDirName = javadocDirName;
    }

    public boolean isOverwriteFiles() {
        return overwriteFiles;
    }

    public void setOverwriteFiles(boolean overwriteFiles) {
        this.overwriteFiles = overwriteFiles;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public long getRetryWait() {
        return retryWait;
    }

    public void setRetryWait(long retryWait) {
        this.retryWait = retryWait;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }
}
