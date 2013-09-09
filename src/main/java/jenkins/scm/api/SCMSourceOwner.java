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
import hudson.model.Item;

import java.util.List;

/**
 * An {@link Item} that owns {@link SCMSource} instances.
 * @author Stephen Connolly
 */
public interface SCMSourceOwner extends Item {
    /**
     * Returns the {@link SCMSource} instances that this item is consuming.
     *
     * @return the {@link SCMSource} instances that this item is consuming.
     */
    @NonNull
    List<SCMSource> getSCMSources();

    /**
     * Gets the source with the specified {@link SCMSource#getId()}.
     *
     * @param sourceId the {@link SCMSource#getId()}
     * @return the corresponding {@link SCMSource} or {@code null} if no matching source.
     */
    @CheckForNull
    SCMSource getSCMSource(@CheckForNull String sourceId);

    /**
     * Called when a source has received notification of an update.
     *
     * @param source the source
     */
    void onSCMSourceUpdated(@NonNull SCMSource source);

    /**
     * Returns the criteria for determining if a candidate head is relevant for consumption.
     *
     * @param source the source to get the criteria for.
     * @return the criteria for determining if a candidate head is relevant for consumption.
     */
    @CheckForNull
    SCMSourceCriteria getSCMSourceCriteria(@NonNull SCMSource source);

}
