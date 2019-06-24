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

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.model.BuildableItem;
import hudson.model.BuildableItemWithBuildWrappers;
import hudson.model.Describable;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.JobProperty;
import hudson.model.Queue;
import hudson.model.TaskListener;
import hudson.model.TopLevelItem;
import hudson.model.listeners.ItemListener;
import hudson.util.ListBoxModel;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import jenkins.model.Jenkins;
import net.jcip.annotations.GuardedBy;
import org.acegisecurity.Authentication;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * An {@link Item} that owns {@link SCMSource} instances. Any {@link SCMSource} instances accessed through a
 * {@link SCMSourceOwner} will have had {@link SCMSource#setOwner(SCMSourceOwner)} called with a non-null
 * owner before being exposed through either {@link #getSCMSources()} or {@link #getSCMSource(String)}.
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
    default SCMSource getSCMSource(@CheckForNull String sourceId) {
        return sourceId == null
                ? null
                : getSCMSources().stream()
                        .filter(s -> Objects.equals(sourceId, s.getId()))
                        .findFirst()
                        .orElse(null);
    }

    /**
     * Called when a source has received notification of an update. Implementations are required to assume that
     * the set of the {@link SCMHead} instances returned by {@link SCMSource#fetch(SCMHeadObserver, TaskListener)}
     * may now be invalid / incomplete and consequently requires a full refresh. <strong>Implementations must provide
     * stern looks of disapproval to anyone calling this method.</strong>
     *
     * @param source the source
     * @deprecated implementations of {@link SCMSourceOwner} would prefer the {@link SCMEventListener} extension point
     * which allows for more fine-grained response to events, so prefer delivering event notification through
     * {@link SCMHeadEvent#fireNow(SCMHeadEvent)}, {@link SCMSourceEvent#fireNow(SCMSourceEvent)} or
     * {@link SCMNavigatorEvent#fireNow(SCMNavigatorEvent)} as appropriate.
     */
    @Deprecated
    void onSCMSourceUpdated(@NonNull SCMSource source);

    /**
     * Returns the criteria for determining if a candidate head is relevant for consumption.
     *
     * @param source the source to get the criteria for.
     * @return the criteria for determining if a candidate head is relevant for consumption.
     */
    @CheckForNull
    default SCMSourceCriteria getSCMSourceCriteria(@NonNull SCMSource source) {
        return null;
    }

    /**
     * Creates a proxy {@link SCMSourceOwner} from an {@link Item} and a List of {@link SCMSource} instances. The caller
     * is responsible for setting the owner.
     *
     * <p>In certain circumstances you may need to use a {@link SCMSource} from a concrete {@link Item} that cannot
     * implement {@link  SCMSourceOwner}. For example, if you were having a {@link JobProperty} that retrieves things
     * from a {@link SCMSource} you cannot make {@link FreeStyleProject} implement {@link SCMSource} yet you need to
     * provide the {@link SCMSource} with access to its {@link SCMSourceOwner}.
     *
     * <p><strong>Only use this method if you absolutely cannot make the owning item a {@link SCMSourceOwner}</strong>.
     *
     * @param item the {@link Item} that owns the sources.
     * @param proxyGetter a function that can retrieve the current proxy owner from a specified item. For example if
     *         the proxy is created by a {@link JobProperty} the function would look up the property and return the
     *         value of the (transient) field in the property that holds the proxy. If you do not need {@link SCMEvent}
     *         support, then {@code null} will create a single-shot proxy owner. In the event that you provide a getter,
     *         the returned proxy will implement {@link AutoCloseable} which callers can use to eagerly release the
     *         proxy owner if it is no longer required.
     * @param sourcesSupplier a {@link Supplier} that returns the current owned sources. The returned list will be
     *         defensively copied before each use.
     * @param criteriaSupplier the criteria that is applied to the sources.
     * @return the proxy {@link SCMSourceOwner} instance.
     */
    static SCMSourceOwner proxyFromItem(@NonNull Item item,
                                        @CheckForNull Function<Item, SCMSourceOwner> proxyGetter,
                                        @NonNull Supplier<List<SCMSource>> sourcesSupplier,
                                        @CheckForNull Supplier<SCMSourceCriteria> criteriaSupplier) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(sourcesSupplier);
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        interfaces.add(SCMSourceOwner.class);
        // marker interface to allow us to track
        interfaces.add(ProxyEnumerator.SCMProxySourceOwner.class);
        // important item related interfaces
        for (Class<?> interfaze : ProxyEnumerator.ITEM_CLASSES) {
            if (interfaze.isInstance(item)) {
                interfaces.add(interfaze);
            }
        }
        ProxyEnumerator enumerator;
        if (proxyGetter != null) {
            // marker interface to allow early release
            interfaces.add(AutoCloseable.class);
            // interfaces related to SCMEvent handling
            enumerator = ExtensionList.lookup(SCMSourceOwners.Enumerator.class).get(ProxyEnumerator.class);
            for (Class<?> interfaze : ProxyEnumerator.EVENT_CLASSES) {
                if (interfaze.isInstance(item)) {
                    interfaces.add(interfaze);
                }
            }
        } else {
            enumerator = null;
        }
        ProxyEnumerator.SCMProxySourceOwner owner = (ProxyEnumerator.SCMProxySourceOwner) Proxy.newProxyInstance(
                SCMSourceOwner.class.getClassLoader(),
                interfaces.toArray(new Class[0]),
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == SCMSourceOwner.class) {
                        switch (method.getName()) {
                            case "getSCMSources":
                                return Collections.unmodifiableList(new ArrayList<>(Util.fixNull(sourcesSupplier.get())));
                            case "getSCMSource":
                                return args[0] == null
                                        ? null
                                        : sourcesSupplier.get().stream()
                                                .filter(s -> Objects.equals(args[0], s.getId()))
                                                .findFirst()
                                                .orElse(null);
                            case "onSCMSourceUpdated":
                                // no-op. We could have exposed a callback, but we want to discourage this old
                                // method so instead we just treat it as a no-op.
                                return null;
                            case "getSCMSourceCriteria":
                                return criteriaSupplier == null ? null : criteriaSupplier.get();
                            default:
                                throw new UnsupportedOperationException("Unsupported method: " + method.getName());
                        }
                    } else if (method.getDeclaringClass() == ProxyEnumerator.SCMProxySourceOwner.class) {
                        switch (method.getName()) {
                            case "__proxySourceOwner__isAttached":
                                return proxyGetter != null && proxyGetter.apply(item) == proxy;
                            case "__proxySourceOwner__getItem":
                                return item;
                            case "__proxySourceOwner__getItemGroup":
                                return item instanceof ItemGroup ? item : null;
                            default:
                                throw new UnsupportedOperationException("Unsupported method: " + method.getName());
                        }
                    } else if (method.getDeclaringClass() == AutoCloseable.class) {
                        if (enumerator != null) {
                            enumerator.deregister(item, (ProxyEnumerator.SCMProxySourceOwner) proxy);
                        }
                        return null;
                    } else {
                        return method.invoke(item, args);
                    }
                });
        if (enumerator != null) {
            enumerator.register(item, owner);
        }
        return owner;
    }

    /**
     * Allow the proxy owners to be retrieved.
     */
    @Restricted(NoExternalUse.class)
    @Extension
    class ProxyEnumerator extends SCMSourceOwners.Enumerator {

        /**
         * Classes that we should expose in case of instanceof checks.
         */
        static final Class[] ITEM_CLASSES = {
                TopLevelItem.class,
                Describable.class,
                ItemGroup.class,
                };
        /**
         * Classes that we should expose in case of instanceof checks if the proxy is registered for events.
         */
        static final Class[] EVENT_CLASSES = {
                BuildableItemWithBuildWrappers.class,
                BuildableItem.class,
                Queue.Task.class,
                };
        /**
         * The map of proxy instances keyed by owning item.
         */
        @GuardedBy("proxies")
        private final Map<Item, List<SCMProxySourceOwner>> proxies = new WeakHashMap<>();

        /**
         * {@inheritDoc}
         */
        @Override
        @NonNull
        public Iterator<SCMSourceOwner> iterator() {
            final Jenkins jenkins = Jenkins.get();
            return new Iterator<SCMSourceOwner>() {
                final Iterator<Map.Entry<Item, List<SCMProxySourceOwner>>> entryIterator =
                        proxies.entrySet().iterator();
                Iterator<SCMProxySourceOwner> ownerIterator = null;
                SCMProxySourceOwner next;

                @Override
                public boolean hasNext() {
                    if (next != null) {
                        return true;
                    }
                    while (ownerIterator != null && ownerIterator.hasNext()) {
                        next = ownerIterator.next();
                        if (next.__proxySourceOwner__isAttached()) {
                            return true;
                        }
                    }
                    ITEMS:
                    while (entryIterator.hasNext()) {
                        Map.Entry<Item, List<SCMProxySourceOwner>> entry = entryIterator.next();
                        if (!entry.getValue().isEmpty()) {
                            // we only return it if it is still present
                            Item probe = entry.getKey();
                            ItemGroup<? extends Item> parent;
                            do {
                                parent = probe.getParent();
                                if (parent == null) {
                                    // all parents have to be rooted at Jenkins
                                    continue ITEMS;
                                }
                                if (parent.getItem(probe.getName()) != probe) {
                                    // this will also skip items we are not allowed to access because of ACLs
                                    // but we want that behaviour... and anyway most callers should be ACL.SYSTEM
                                    continue ITEMS;
                                }
                                if (parent instanceof Item) {
                                    probe = (Item) parent;
                                } else if (!(parent instanceof Jenkins)) {
                                    // all parents have to be rooted at Jenkins
                                    continue ITEMS;
                                }
                            } while (jenkins != parent);
                            // the path to this item is still valid
                            ownerIterator = entry.getValue().iterator();
                            while (ownerIterator != null && ownerIterator.hasNext()) {
                                next = ownerIterator.next();
                                if (next.__proxySourceOwner__isAttached()) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }

                @Override
                public SCMSourceOwner next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    try {
                        return next;
                    } finally {
                        next = null;
                    }
                }
            };
        }

        void register(Item item, SCMProxySourceOwner owner) {
            synchronized (proxies) {
                final List<SCMProxySourceOwner> owners =
                        proxies.computeIfAbsent(item, (x) -> new ArrayList<>());
                // most common reason for adding is to replace a prior one
                owners.removeIf(s -> !s.__proxySourceOwner__isAttached());
                owners.add(owner);
            }
        }

        void deregister(Item item, SCMProxySourceOwner owner) {
            synchronized (proxies) {
                final List<SCMProxySourceOwner> sourceOwners = proxies.get(item);
                if (sourceOwners != null) {
                    sourceOwners.remove(owner);
                    sourceOwners.removeIf(s -> !s.__proxySourceOwner__isAttached());
                    if (sourceOwners.isEmpty()) {
                        proxies.remove(item);
                    }
                }
            }
        }

        void deregister(Item item) {
            synchronized (proxies) {
                proxies.remove(item);
            }
        }

        void refresh(Item item) {
            synchronized (proxies) {
                final List<SCMProxySourceOwner> sourceOwners = proxies.get(item);
                if (sourceOwners != null) {
                    sourceOwners.removeIf(s -> !s.__proxySourceOwner__isAttached());
                    if (sourceOwners.isEmpty()) {
                        proxies.remove(item);
                    }
                }
            }
        }

        /**
         * Marker interface that allows us access to detect if this is a proxy.
         */
        @Restricted(NoExternalUse.class)
        protected interface SCMProxySourceOwner extends SCMSourceOwner, AutoCloseable {
            /**
             * Checks if the prxoy owner is still attached to the item.
             *
             * @return {@code true} if the proxy owner instance is still attached to the item through it's creator.
             */
            boolean __proxySourceOwner__isAttached();

            /**
             * Gets the backing owning item instance.
             *
             * @return the backing owning item instance.
             */
            Item __proxySourceOwner__getItem();

            /**
             * Gets the backing owning item instance as an {@link ItemGroup} (if it is one).
             *
             * @return the backing owning item instance as an {@link ItemGroup} (if it is one).
             */
            ItemGroup __proxySourceOwner__getItemGroup();
        }
    }

    /**
     * Listens for {@link Item} instances being removed to proactively empty the {@link ProxyEnumerator#proxies}.
     */
    @Restricted(NoExternalUse.class)
    @Extension
    class ProxyItemListener extends ItemListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onDeleted(Item item) {
            final ProxyEnumerator enumerator =
                    ExtensionList.lookup(SCMSourceOwners.Enumerator.class).get(ProxyEnumerator.class);
            if (enumerator != null) {
                enumerator.deregister(item);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onUpdated(Item item) {
            final ProxyEnumerator enumerator =
                    ExtensionList.lookup(SCMSourceOwners.Enumerator.class).get(ProxyEnumerator.class);
            if (enumerator != null) {
                enumerator.refresh(item);
            }
        }
    }

    /**
     * Exposes credentials correctly from the proxy.
     */
    @Restricted(NoExternalUse.class)
    @Extension
    class ProxyCredentialsProvider extends CredentialsProvider {
        @Override
        public boolean isEnabled(Object context) {
            return context instanceof ProxyEnumerator.SCMProxySourceOwner;
        }

        private Item ctx(ProxyEnumerator.SCMProxySourceOwner context) {
            return context.__proxySourceOwner__getItem();
        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public <C extends Credentials> List<C> getCredentials(
                @NonNull Class<C> type,
                @Nullable ItemGroup itemGroup,
                @Nullable Authentication authentication) {
            return getCredentials(
                    type,
                    itemGroup,
                    authentication,
                    Collections.emptyList()
            );
        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public <C extends Credentials> List<C> getCredentials(
                @NonNull Class<C> type,
                @Nullable ItemGroup itemGroup,
                @Nullable Authentication authentication,
                @NonNull List<DomainRequirement> domainRequirements) {
            if (itemGroup instanceof ProxyEnumerator.SCMProxySourceOwner) {
                return lookupCredentials(
                        type,
                        ((ProxyEnumerator.SCMProxySourceOwner) itemGroup).__proxySourceOwner__getItemGroup(),
                        authentication,
                        domainRequirements
                );
            }
            return Collections.emptyList();
        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public <C extends IdCredentials> ListBoxModel getCredentialIds(
                @NonNull Class<C> type,
                @Nullable ItemGroup itemGroup,
                @Nullable Authentication authentication,
                @NonNull List<DomainRequirement> domainRequirements,
                @NonNull CredentialsMatcher matcher) {
            if (itemGroup instanceof ProxyEnumerator.SCMProxySourceOwner) {
                return listCredentials(
                        type,
                        ((ProxyEnumerator.SCMProxySourceOwner) itemGroup).__proxySourceOwner__getItemGroup(),
                        authentication,
                        domainRequirements,
                        matcher
                );
            }
            return new ListBoxModel();
        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public <C extends Credentials> List<C> getCredentials(
                @NonNull Class<C> type,
                @NonNull Item item,
                @Nullable Authentication authentication) {
            return getCredentials(
                    type,
                    item,
                    authentication,
                    Collections.emptyList()
            );
        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public <C extends Credentials> List<C> getCredentials(
                @NonNull Class<C> type,
                @NonNull Item item,
                @Nullable Authentication authentication,
                @NonNull List<DomainRequirement> domainRequirements) {
            if (item instanceof ProxyEnumerator.SCMProxySourceOwner) {
                return lookupCredentials(
                        type,
                        ((ProxyEnumerator.SCMProxySourceOwner) item).__proxySourceOwner__getItem(),
                        authentication,
                        domainRequirements);
            }
            return Collections.emptyList();
        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public <C extends IdCredentials> ListBoxModel getCredentialIds(
                @NonNull Class<C> type,
                @NonNull Item item,
                @Nullable Authentication authentication,
                @NonNull List<DomainRequirement> domainRequirements,
                @NonNull CredentialsMatcher matcher) {
            if (item instanceof ProxyEnumerator.SCMProxySourceOwner) {
                return listCredentials(
                        type,
                        ((ProxyEnumerator.SCMProxySourceOwner) item).__proxySourceOwner__getItem(),
                        authentication,
                        domainRequirements,
                        matcher);
            }
            return new ListBoxModel();
        }
    }
}
