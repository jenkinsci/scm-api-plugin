/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc.
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
 *
 */

package jenkins.scm.api;

import hudson.ExtensionPoint;

/**
 * Base class for listeners to {@link SCMEvent}s
 *
 * @since FIXME
 */
public class SCMEventListener implements ExtensionPoint {

    /**
     * Callback for a {@link SCMHeadEvent}
     *
     * @param event the event.
     */
    public void onSCMHeadEvent(SCMHeadEvent<?> event) {

    }

    /**
     * Callback for a {@link SCMNavigatorEvent}
     *
     * @param event the event.
     */
    public void onSCMNavigatorEvent(SCMNavigatorEvent<?> event) {

    }

    /**
     * Callback for a {@link SCMSourceEvent}
     *
     * @param event the event.
     */
    public void onSCMSourceEvent(SCMSourceEvent<?> event) {

    }
}
