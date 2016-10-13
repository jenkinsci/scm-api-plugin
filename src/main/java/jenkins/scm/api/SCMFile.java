/*
 * The MIT License
 *
 * Copyright (c) 2011-2013, CloudBees, Inc., Stephen Connolly.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.scm.api;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.WebApp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;

/**
 * A file/directory inspected by {@link SCMFileSystem}.
 *
 * @author Stephen Connolly
 * @author Kohsuke Kawaguchi
 */
public abstract class SCMFile {
    /**
     * Gets the file name of this file without any path portion, such as just "foo.txt"
     * <p>This method is the equivalent of {@link File#getName()}.</p>
     *
     * @return the file name of this file without any path portion.
     */
    @NonNull
    public abstract String getName();

    /**
     * Gets a child/descendant path relative from this object.
     *
     * @param path Relative path of the child to return.
     * @return null if there's no file/directory at the path represented by it.
     * @throws IOException if an error occurs while performing the operation.
     */
    @CheckForNull
    public abstract SCMFile get(String path) throws IOException;

    /**
     * If this object represents a directory, lists up all the immediate children.
     * <p>This method is the equivalent of {@link File#listFiles()}.</p>
     *
     * @return Always non-null. If this method is not a directory, this method returns
     *         an empty iterable.
     * @throws IOException if an error occurs while performing the operation.
     */
    @NonNull
    public abstract Iterable<SCMFile> children() throws IOException;

    /**
     * Returns true if this object represents a file.
     * <p>This method is the equivalent of {@link File#isFile()}.</p>
     *
     * @return true if this object represents a file.
     * @throws IOException if an error occurs while performing the operation.
     */
    public abstract boolean isFile() throws IOException;

    /**
     * Returns true if this object represents a directory.
     * <p>This method is the equivalent of {@link File#isDirectory()}.</p>
     *
     * @return true if this object represents a directory.
     * @throws IOException if an error occurs while performing the operation.
     */
    public boolean isDirectory() throws IOException {
        return !isFile();
    }

    /**
     * Reads the content of this file.
     *
     * @return an open stream to read the file content. The caller must close the stream.
     * @throws IOException if this object represents a directory.
     */
    @NonNull
    public abstract InputStream content() throws IOException;

    /**
     * A convenience method that reads the content and then turns it into a byte array.
     *
     * @return the file content as a byte array.
     * @throws IOException if this object represents a directory.
     */
    @NonNull
    public byte[] contentAsBytes() throws IOException {
        final InputStream is = content();
        try {
            return IOUtils.toByteArray(is);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * A convenience method that reads the content and then turns it into a string.
     *
     * @return the file content as a string.
     * @throws IOException if this object represents a directory.
     */
    @NonNull
    public String contentAsString() throws IOException {
        final InputStream is = content();
        try {
            return IOUtils.toString(is, contentEncoding());
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Returns the MIME type of this file.
     * <p>The default implementation infers this based on the file name, but
     * sophisticated server might provide this information from different sources,
     * such as "svn:mime-type" in Subversion.</p>
     *
     * @return the MIME type of this file.
     * @throws IOException if an error occurs while performing the operation.
     */
    @NonNull
    public String contentMimeType() throws IOException {
        return getMimeType(getName());
    }

    /**
     * Checks if this file is a binary file.
     * <p>What exactly is a binary file is up to the implementation. Some SCMs (such as Subversion)
     * has a way of letting users mark files as binaries.</p>
     *
     * @return true if this file is a binary file.
     * @throws IOException if an error occurs while performing the operation.
     */
    public boolean isContentBinary() throws IOException {
        return !isContentText();
    }

    /**
     * The opposite of {@link #isContentBinary()}
     *
     * @return true if this file is not a binary file.
     * @throws IOException if an error occurs while performing the operation.
     */
    public boolean isContentText() throws IOException {
        return StringUtils.startsWithIgnoreCase(contentMimeType(), "text/");
    }

    /**
     * Encoding of this file.
     * <p>This is used to interpret text files.</p>
     * <p>Some SCM implementations allow users to mark content encoding of files, and this method
     * may provide those. As a fallback, the default implementation returns the platform
     * default encoding.</p>
     *
     * @return the encoding of this file.
     * @throws IOException if an error occurs while performing the operation.
     */
    @NonNull
    public Charset contentEncoding() throws IOException {
        return Charset.defaultCharset();
    }

    /**
     * Looks up the servlet container's mime type mapping for the provided filename.
     *
     * @param fileName the file name.
     * @return the mime type.
     */
    @NonNull
    private String getMimeType(@NonNull String fileName) {
        int idx = fileName.lastIndexOf('/');
        fileName = fileName.substring(idx + 1);
        idx = fileName.lastIndexOf('\\');
        fileName = fileName.substring(idx + 1);

        WebApp webApp = WebApp.getCurrent();

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        String mimeType = webApp.mimeTypes.get(extension);
        if (mimeType == null) {
            mimeType = webApp.context.getMimeType(fileName);
        }
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        if (webApp.defaultEncodingForStaticResources.containsKey(mimeType)) {
            mimeType += ";charset=" + webApp.defaultEncodingForStaticResources.get(mimeType);
        }
        return mimeType;
    }

}
