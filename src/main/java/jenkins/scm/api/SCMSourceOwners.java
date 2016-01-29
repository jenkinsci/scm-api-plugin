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

import com.google.common.collect.Iterators;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Provides a means to lookup the {@link SCMSourceOwners} that own {@link SCMSource} instances.
 * @author Stephen Connolly
 */
public abstract class SCMSourceOwners implements ExtensionPoint, Iterable<SCMSourceOwner> {

    /**
     * Lookup the {@link SCMSourceOwners}.
     *
     * @return the {@link SCMSourceOwners}.
     */
    @NonNull
    public static Iterable<SCMSourceOwner> all() {
        return new All();
    }

    /**
     * Extension point to allow for access to embedded {@link SCMSourceOwner} instances that
     * {@link SCMSourceOwners.JenkinsItemEnumerator} will not find.
     */
    public static abstract class Enumerator implements ExtensionPoint, Iterable<SCMSourceOwner> {
    }

    /**
     * Provide all the Jenkins items that are {@link SCMSourceOwners}.
     */
    @Extension
    @SuppressWarnings("unused")// instantiated by Jenkins
    public static class JenkinsItemEnumerator extends Enumerator {

        /**
         * {@inheritDoc}
         */
        public Iterator<SCMSourceOwner> iterator() {
            Jenkins j = Jenkins.getInstance(); // TODO 1.590+ getActiveInstance
            if (j == null) {
                return Iterators.emptyIterator();
            }
            return j.getAllItems(SCMSourceOwner.class).iterator();
        }
    }

    /**
     * A lazy {@link Iterable} of all {@link SCMSourceOwner} instances.
     */
    private static class All implements Iterable<SCMSourceOwner> {
        /**
         * {@inheritDoc}
         */
        public Iterator<SCMSourceOwner> iterator() {
            Jenkins j = Jenkins.getInstance();
            if (j == null) {
                return Iterators.emptyIterator();
            }
            return new IteratorImpl(j.getExtensionList(Enumerator.class)); // TODO 1.572+ ExtensionList.lookup
        }

        /**
         * A lazy iterator of {@link SCMSourceOwner}.
         */
        private static class IteratorImpl implements Iterator<SCMSourceOwner> {
            /**
             * The chain of sources.
             */
            private final Iterator<Enumerator> enumerators;

            /**
             * The current chain of {@link SCMSourceOwner}.
             */
            private Iterator<SCMSourceOwner> owners;

            /**
             * Constructor.
             *
             * @param enumerators the chain of enumerators to follow.
             */
            private IteratorImpl(Collection<Enumerator> enumerators) {
                this.enumerators = enumerators.iterator();
            }

            /**
             * {@inheritDoc}
             */
            public boolean hasNext() {
                while (true) {
                    if (owners != null && owners.hasNext()) {
                        return true;
                    }
                    if (enumerators.hasNext()) {
                        owners = enumerators.next().iterator();
                    } else {
                        return false;
                    }
                }
            }

            /**
             * {@inheritDoc}
             */
            public SCMSourceOwner next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return owners.next();
            }

            /**
             * {@inheritDoc}
             */
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

    }
}
