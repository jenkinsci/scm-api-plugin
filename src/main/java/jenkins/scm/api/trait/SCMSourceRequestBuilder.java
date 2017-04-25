/*
 * The MIT License
 *
 * Copyright (c) 2017 CloudBees, Inc.
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

package jenkins.scm.api.trait;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;

public abstract class SCMSourceRequestBuilder<B extends SCMSourceRequestBuilder<B, R>, R extends SCMSourceRequest> {
    private final SCMSource source;
    @NonNull
    private final List<SCMSourceCriteria> criteria = new ArrayList<SCMSourceCriteria>();
    @NonNull
    private final List<SCMHeadPrefilter> prefilters = new ArrayList<SCMHeadPrefilter>();
    @NonNull
    private final List<SCMHeadFilter> filters = new ArrayList<SCMHeadFilter>();
    @NonNull
    private final List<SCMHeadAuthority<?, ?>> authorities = new ArrayList<SCMHeadAuthority<?, ?>>();
    @NonNull
    private SCMHeadObserver observer;

    public SCMSourceRequestBuilder(@NonNull SCMSource source, @CheckForNull SCMSourceCriteria criteria,
                                   @NonNull SCMHeadObserver observer) {
        this.source = source;
        withCriteria(criteria);
        this.observer = observer;
    }

    @SuppressWarnings("unchecked")
    public B withCriteria(@CheckForNull SCMSourceCriteria criteria) {
        if (criteria != null) {
            this.criteria.add(criteria);
        }
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withAuthority(@CheckForNull SCMHeadAuthority authority) {
        if (authority != null) {
            this.authorities.add(authority);
        }
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withFilter(@CheckForNull SCMHeadFilter filter) {
        if (filter != null) {
            this.filters.add(filter);
        }
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withPrefilter(@CheckForNull SCMHeadPrefilter prefilter) {
        if (prefilter != null) {
            this.prefilters.add(prefilter);
        }
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withPrefilters(@CheckForNull Collection<SCMHeadPrefilter> prefilters) {
        if (prefilters != null) {
            this.prefilters.addAll(prefilters);
        }
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withTrait(@NonNull SCMSourceTrait trait) {
        observer = trait.applyToObserver(observer);
        trait.applyToRequest((B) this);
        return (B) this;
    }

    public B withTraits(@NonNull SCMSourceTrait... traits) {
        return withTraits(Arrays.asList(traits));
    }

    @SuppressWarnings("unchecked")
    public B withTraits(@NonNull Collection<SCMSourceTrait> traits) {
        for (SCMSourceTrait trait : traits) {
            withTrait(trait);
        }
        return (B) this;
    }

    @NonNull
    public List<SCMSourceCriteria> criteria() {
        return Collections.unmodifiableList(criteria);
    }

    @NonNull
    public List<SCMHeadFilter> filters() {
        return Collections.unmodifiableList(filters);
    }

    @NonNull
    public List<SCMHeadPrefilter> prefilters() {
        return Collections.unmodifiableList(prefilters);
    }

    @NonNull
    public SCMHeadObserver observer() {
        return observer;
    }

    @NonNull
    public List<SCMHeadAuthority<?, ?>> authorities() {
        return Collections.unmodifiableList(authorities);
    }

    public SCMSource source() {
        return source;
    }

    public abstract R build(@NonNull TaskListener listener);
}
