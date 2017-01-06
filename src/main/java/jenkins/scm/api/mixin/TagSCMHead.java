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

package jenkins.scm.api.mixin;

import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;

/**
 * Mixin interface to identify {@link SCMHead} instances that correspond to a semi-immutable tag.
 * Tags cannot be changed once created, but it may be possible to delete a tag and recreate a new tag with the same
 * name as a previous tag.
 *
 * @since 2.0
 */
public interface TagSCMHead {
    /**
     * Returns the timestamp of the tag. The timestamp is important because when iterating a newly
     * configured {@link SCMSource} the consumer may not want to process old tags and instead may prefer to process
     * tags created after some specific date.
     * <p>
     * For example, if a consumer creates a job for every {@link SCMHead} it may not be a good idea to trigger
     * builds of old tags (especially if the build were to result in a deployment to production). However when newly
     * created tags are discovered on subsequent searches (or when reported by the events subsystem), it may be
     * the intent of the user to have that tag result in a build (eg allowing the creation of a tag to trigger
     * a release)
     * <p>
     * <strong>NOTE:</strong> Implementers are <strong>strongly recommended</strong> to use the time that the tag
     * was created as the timestamp. Where this is not possible, then implementers should use the maximum last modified
     * timestamp of the contents of the tag.
     *
     * @return the timestamp, directly comparable to {@link System#currentTimeMillis()}.
     */
    long getTimestamp();
}
