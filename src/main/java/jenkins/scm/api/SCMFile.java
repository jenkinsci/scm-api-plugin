/*
 * The MIT License
 *
 * Copyright (c) 2011-2016, CloudBees, Inc., Stephen Connolly.
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

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.WebApp;

/**
 * A file/directory inspected by {@link SCMFileSystem}.
 *
 * @author Stephen Connolly
 * @author Kohsuke Kawaguchi
 */
public abstract class SCMFile {

    /**
     * Cache of the last modified timestamp, to allow repeated calls to minimize the number of network round trips.
     */
    private Long modified;
    /**
     * Cache of the file type information, to allow repeated calls to minimize the number of network round trips.
     */
    private Type type;

    /**
     * Gets the file name of this file without any path portion, such as just "foo.txt"
     * <p>This method is the equivalent of {@link File#getName()}.</p>
     *
     * @return the file name of this file without any path portion.
     */
    @NonNull
    public abstract String getName();

    /**
     * Constructs a child/descendant {@link SCMFile} instance path relative from this object.
     *
     * @param path Relative path of the child to return.
     * @return The instance.
     */
    @NonNull
    public abstract SCMFile child(String path);

    /**
     * If this object represents a directory, lists up all the immediate children.
     * <p>This method is the equivalent of {@link File#listFiles()}.</p>
     *
     * @return Always non-null. If this method is not a directory, this method returns
     * an empty iterable.
     * @throws IOException          if an error occurs while performing the operation.
     * @throws InterruptedException if interrupted while performing the operation.
     */
    @NonNull
    public abstract Iterable<SCMFile> children() throws IOException, InterruptedException;

    /**
     * Returns the time that the {@link SCMFile} was last modified.
     *
     * @return A <code>long</code> value representing the time the file was last modified, measured in milliseconds
     * since the epoch (00:00:00 GMT, January 1, 1970) or {@code 0L} if the operation is unsupported.
     * @throws FileNotFoundException if this {@link SCMFile} instance does not exist in the remote system (e.g. if you
     *                               created a nonexistent instance via {@link #child(String)})
     * @throws IOException           if an error occurs while performing the operation.
     * @throws InterruptedException  if interrupted while performing the operation.
     */
    public final long lastModified() throws IOException, InterruptedException {
        return modified != null ? modified : (modified = modified());
    }

    /**
     * Proactively seeds the last modified information where that has been already obtained in a different request.
     *
     * @param modified the time that the {@link SCMFile} was last modified.
     * @since 2.0
     */
    protected final void modified(long modified) {
        this.modified = modified;
    }

    /**
     * Returns the time that the {@link SCMFile} was last modified.
     *
     * @return A <code>long</code> value representing the time the file was last modified, measured in milliseconds
     * since the epoch (00:00:00 GMT, January 1, 1970) or {@code 0L} if the operation is unsupported.
     * @throws FileNotFoundException if this {@link SCMFile} instance does not exist in the remote system (e.g. if you
     *                               created a nonexistent instance via {@link #child(String)})
     * @throws IOException           if an error occurs while performing the operation.
     * @throws InterruptedException  if interrupted while performing the operation.
     */
    protected abstract long modified() throws IOException, InterruptedException;

    /**
     * Returns true if this object represents something that exists.
     * <p>This method is the equivalent of {@link File#exists()}.</p>
     * <p>NOTE: Typically to minimize round trips, {@link #getType()} would be preferred</p>
     *
     * @return true if this object represents something that exists.
     * @throws IOException          if an error occurs while performing the operation.
     * @throws InterruptedException if interrupted while performing the operation.
     * @see #getType()
     */
    public final boolean exists() throws IOException, InterruptedException {
        return !Type.NONEXISTENT.equals(getType());
    }

    /**
     * Returns true if this object represents a file.
     * <p>This method is the equivalent of {@link File#isFile()}.</p>
     * <p>NOTE: Typically to minimize round trips, {@link #getType()} would be preferred</p>
     *
     * @return true if this object represents a file.
     * @throws IOException          if an error occurs while performing the operation.
     * @throws InterruptedException if interrupted while performing the operation.
     * @see #getType()
     */
    public final boolean isFile() throws IOException, InterruptedException {
        return Type.REGULAR_FILE.equals(getType());
    }

    /**
     * Returns true if this object represents a directory.
     * <p>This method is the equivalent of {@link File#isDirectory()}.</p>
     * <p>NOTE: Typically to minimize round trips, {@link #getType()} would be preferred</p>
     *
     * @return true if this object represents a directory.
     * @throws IOException          if an error occurs while performing the operation.
     * @throws InterruptedException if interrupted while performing the operation.
     * @see #getType()
     */
    public final boolean isDirectory() throws IOException, InterruptedException {
        return Type.DIRECTORY.equals(getType());
    }

    /**
     * The type of this object.
     *
     * @return the {@link Type} of this object, specifically {@link Type#NONEXISTENT} if this {@link SCMFile} instance
     * does not exist in the remote system (e.g. if you created a nonexistent instance via {@link #child(String)})
     * @throws IOException          if an error occurs while performing the operation.
     * @throws InterruptedException if interrupted while performing the operation.
     */
    @NonNull
    public final Type getType() throws IOException, InterruptedException {
        return type != null ? type : (type = type());
    }

    /**
     * Proactively seeds the type information where that has been already obtained in a different request.
     *
     * @param type the type of this object.
     * @since 2.0
     */
    protected final void type(@NonNull Type type) {
        this.type = type;
    }

    /**
     * The type of this object.
     *
     * @return the {@link Type} of this object, specifically {@link Type#NONEXISTENT} if this {@link SCMFile} instance
     * does not exist in the remote system (e.g. if you created a nonexistent instance via {@link #child(String)})
     * @throws IOException          if an error occurs while performing the operation.
     * @throws InterruptedException if interrupted while performing the operation.
     * @since 2.0
     */
    @NonNull
    protected abstract Type type() throws IOException, InterruptedException;

    /**
     * Reads the content of this file.
     *
     * @return an open stream to read the file content. The caller must close the stream.
     * @throws FileNotFoundException if this {@link SCMFile} instance does not exist in the remote system (e.g. if you
     *                               created a nonexistent instance via {@link #child(String)})
     * @throws IOException           if this object represents a directory or if an error occurs while performing the
     *                               operation.
     * @throws InterruptedException  if interrupted while performing the operation.
     */
    @NonNull
    public abstract InputStream content() throws IOException, InterruptedException;

    /**
     * A convenience method that reads the content and then turns it into a byte array.
     *
     * @return the file content as a byte array.
     * @throws FileNotFoundException if this {@link SCMFile} instance does not exist in the remote system (e.g. if you
     *                               created a nonexistent instance via {@link #child(String)})
     * @throws IOException           if this object represents a directory or if an error occurs while performing the
     *                               operation.
     * @throws InterruptedException  if interrupted while performing the operation.
     */
    @NonNull
    public byte[] contentAsBytes() throws IOException, InterruptedException {
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
     * @throws FileNotFoundException if this {@link SCMFile} instance does not exist in the remote system (e.g. if you
     *                               created a nonexistent instance via {@link #child(String)})
     * @throws IOException           if this object represents a directory or if an error occurs while performing the
     *                               operation.
     * @throws InterruptedException  if interrupted while performing the operation.
     */
    @NonNull
    public String contentAsString() throws IOException, InterruptedException {
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
     * @throws FileNotFoundException if this {@link SCMFile} instance does not exist in the remote system (e.g. if you
     *                               created a nonexistent instance via {@link #child(String)})
     * @throws IOException           if an error occurs while performing the operation.
     * @throws InterruptedException  if interrupted while performing the operation.
     */
    @NonNull
    public String contentMimeType() throws IOException, InterruptedException {
        return getMimeType(getName());
    }

    /**
     * Checks if this file is a binary file.
     * <p>What exactly is a binary file is up to the implementation. Some SCMs (such as Subversion)
     * has a way of letting users mark files as binaries.</p>
     *
     * @return true if this file is a binary file.
     * @throws FileNotFoundException if this {@link SCMFile} instance does not exist in the remote system (e.g. if you
     *                               created a nonexistent instance via {@link #child(String)})
     * @throws IOException           if an error occurs while performing the operation.
     * @throws InterruptedException  if interrupted while performing the operation.
     */
    public boolean isContentBinary() throws IOException, InterruptedException {
        return !isContentText();
    }

    /**
     * The opposite of {@link #isContentBinary()}
     *
     * @return true if this file is not a binary file.
     * @throws FileNotFoundException if this {@link SCMFile} instance does not exist in the remote system (e.g. if you
     *                               created a nonexistent instance via {@link #child(String)})
     * @throws IOException           if an error occurs while performing the operation.
     * @throws InterruptedException  if interrupted while performing the operation.
     */
    public boolean isContentText() throws IOException, InterruptedException {
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
     * @throws FileNotFoundException if this {@link SCMFile} instance does not exist in the remote system (e.g. if you
     *                               created a nonexistent instance via {@link #child(String)})
     * @throws IOException           if an error occurs while performing the operation.
     * @throws InterruptedException  if interrupted while performing the operation.
     */
    @NonNull
    public Charset contentEncoding() throws IOException, InterruptedException {
        return Charset.defaultCharset();
    }

    /**
     * Looks up the servlet container's mime type mapping for the provided filename.
     *
     * @param fileName the file name.
     * @return the mime type.
     */
    @NonNull
    private static String getMimeType(@NonNull String fileName) {
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

    /**
     * Represents the type of a {@link SCMFile}.
     *
     * @since 2.0
     */
    public enum Type {
        /**
         * The {@link SCMFile} does not exist.
         */
        NONEXISTENT,
        /**
         * The {@link SCMFile} is a regular file.
         */
        REGULAR_FILE,
        /**
         * The {@link SCMFile} is a regular directory.
         */
        DIRECTORY,
        /**
         * The {@link SCMFile} is a link.
         */
        LINK,
        /**
         * The {@link SCMFile} is something else, but it exists
         */
        OTHER
    }
}
