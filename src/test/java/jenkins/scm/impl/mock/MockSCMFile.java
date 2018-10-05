/*
 * The MIT License
 *
 * Copyright (c) 2018, CloudBees, Inc.
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

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class MockSCMFile extends SCMFile {
    public MockSCMFile() {
        super();
    }

    @Override
    @NonNull
    public SCMFile newChild(@NonNull String name, boolean assumeIsDirectory) {
        return new MockSCMFile();
    }

    @Override
    @NonNull
    public Iterable<SCMFile> children() throws IOException, InterruptedException {
        return Collections.emptyList();
    }

    @Override
    public long lastModified() throws IOException, InterruptedException {
        return 1;
    }

    @Override
    @NonNull
    public Type type() throws IOException, InterruptedException {
        return Type.OTHER;
    }

    @Override
    @NonNull
    public InputStream content() throws IOException, InterruptedException {
        return new ByteArrayInputStream("no content".getBytes());
    }

}
