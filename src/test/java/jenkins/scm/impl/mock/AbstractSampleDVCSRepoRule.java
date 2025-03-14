/*
 * The MIT License
 *
 * Copyright 2015 CloudBees, Inc.
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

package jenkins.scm.impl.mock;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * Rule tailored to a DVCS which may be initialized and cloned from a local directory.
 *
 * @since 2.0.8
 */
public abstract class AbstractSampleDVCSRepoRule extends AbstractSampleRepoRule {

    /**
     * The origin clone.
     */
    protected File sampleRepo;

    @Override
    protected void before() throws Throwable {
        super.before();
        sampleRepo = tmp.newFolder();
    }

    /**
     * Write a file to {@link #sampleRepo}.
     * @param rel relative path ({@code /}-separated)
     * @param text text to write
     */
    public final void write(String rel, String text) throws IOException {
        FileUtils.write(new File(sampleRepo, rel), text, StandardCharsets.UTF_8);
    }

    /**
     * @return path of {@link #sampleRepo}
     */
    @Override
    public final String toString() {
        return sampleRepo.getAbsolutePath();
    }

    /**
     * Initialize a repository in an empty directory.
     */
    public abstract void init() throws Exception;

    /**
     * Run the specified tool inside {@link #sampleRepo}.
     * @param tool a tool like {@code git}
     * @param cmds its arguments
     */
    protected final void run(String tool, String... cmds) throws Exception {
        List<String> args = new ArrayList<>();
        args.add(tool);
        args.addAll(Arrays.asList(cmds));
        run(false, sampleRepo, args.toArray(new String[0]));
    }

    /**
     * Like {@link #fileUrl} but expressed only as a path, not a URL with protocol.
     */
    public final String bareUrl() throws UnsupportedEncodingException {
        return URLEncoder.encode(toString(), StandardCharsets.UTF_8);
    }

    /**
     * {@code file}-protocol URL to {@link #sampleRepo}.
     */
    public final String fileUrl() {
        return sampleRepo.toURI().toString();
    }

}
