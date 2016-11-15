/*
 * The MIT License
 *
 * Copyright 2016 CloudBees, Inc.
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

package jenkins.scm.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceObserver;

/**
 * A {@link SCMSourceObserver.ProjectObserver} that does nothing.
 *
 * @since FIXME
 */
public class NoOpProjectObserver extends SCMSourceObserver.ProjectObserver {
    /**
     * The singleton instance.
     */
    private static final NoOpProjectObserver INSTANCE = new NoOpProjectObserver();

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance.
     */
    public static NoOpProjectObserver instance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSource(@NonNull SCMSource source) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAttribute(@NonNull String key, @Nullable Object value)
            throws IllegalArgumentException, ClassCastException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void complete() throws IllegalStateException, InterruptedException {

    }
}
