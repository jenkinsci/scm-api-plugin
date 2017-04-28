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
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.model.TaskListener;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jdk.nashorn.internal.runtime.ScriptObject;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.impl.NoOpProjectObserver;

/**
 * Represents the context of an individual request for a call to
 * {@link SCMNavigator#visitSources(SCMSourceObserver)} or an equivalent method.
 *
 * @since 2.2.0
 */
public abstract class SCMNavigatorRequest implements Closeable {

    private static final Witness[] NO_WITNESSES = new Witness[0];
    /**
     * The {@link SCMNavigator} to use when applying the {@link #prefilters}.
     */
    @NonNull
    private final SCMNavigator navigator;

    /**
     * The filters requiring context of the {@link SCMNavigatorRequest}, typically because the decision to filter may
     * require making remote requests.
     */
    @NonNull
    private final List<SCMSourceFilter> filters;

    /**
     * The filters that do not require context of the {@link SCMNavigatorRequest} and only require the {@link SCMNavigator}
     * and {@code projectName} to decide exclusion - typically filtering based on the name.
     */
    @NonNull
    private final List<SCMSourcePrefilter> prefilters;

    /**
     * The {@link SCMSourceObserver} for this request.
     */
    @NonNull
    private final SCMSourceObserver observer;

    /**
     * The {@link SCMSourceObserver#getIncludes()} of {@link #observer}.
     */
    @CheckForNull
    private final Set<String> observerIncludes;

    /**
     * The {@link SCMSourceTrait} instances to apply to {@link SCMSourceBuilder} instances.
     */
    @NonNull
    private final List<SCMSourceTrait> traits;

    /**
     * The {@link SCMSourceDecorator} instances to apply to {@link SCMSourceBuilder} instances.
     */
    @NonNull
    private final List<SCMSourceDecorator<?,?>> decorators;

    /**
     * Any {@link Closeable} objects that should be closed with the request.
     */
    // TODO widen type to AutoClosable once Java 7+
    @NonNull
    private final List<Closeable> managedClosables = new ArrayList<Closeable>();

    /**
     * Constructor.
     *
     * @param source  the source.
     * @param context  the context.
     * @param observer the observer.
     */
    protected SCMNavigatorRequest(@NonNull SCMNavigator source, @NonNull SCMNavigatorContext<?, ?> context,
                                  @NonNull SCMSourceObserver observer) {
        this.navigator = source;
        this.filters = Collections.unmodifiableList(new ArrayList<SCMSourceFilter>(context.filters()));
        this.prefilters = Collections.unmodifiableList(new ArrayList<SCMSourcePrefilter>(context.prefilters()));
        this.observer = observer;
        this.observerIncludes = this.observer.getIncludes();
        this.traits = Collections.unmodifiableList(context.traits());
        this.decorators = Collections.unmodifiableList(context.decorators());
    }

    @NonNull
    public final List<SCMSourceTrait> traits() {
        return traits;
    }

    @NonNull
    public final List<SCMSourceDecorator<?, ?>> decorators() {
        return decorators;
    }

    /**
     * Records a processing result to the {@linkplain Witness}es.
     *
     * @param projectName      the project name.
     * @param isMatch   {@code true} if the projectName pair was sent to the {@link #observer}.
     * @param witnesses the {@link Witness} instances to notify.
     */
    @SuppressWarnings("unchecked")
    private static void record(@NonNull String projectName, boolean isMatch,
                               @NonNull Witness... witnesses) {
        for (Witness witness : witnesses) {
            witness.record(projectName, isMatch);
        }
    }

    /**
     * Tests if the project name is excluded from the request.
     *
     * @param projectName the project name.
     * @return {@code true} if the {@link SCMHead} is excluded.
     * @throws IOException          if there is an I/O error.
     * @throws InterruptedException if the operation was interrupted.
     */
    public final boolean isExcluded(@NonNull String projectName) throws IOException, InterruptedException {
        if (observerIncludes != null && !observerIncludes.contains(projectName)) {
            return true;
        }
        if (!prefilters.isEmpty()) {
            for (SCMSourcePrefilter prefilter : prefilters) {
                if (prefilter.isExcluded(navigator, projectName)) {
                    return true;
                }
            }
        }
        if (filters.isEmpty()) {
            return false;
        }
        for (SCMSourceFilter filter : filters) {
            if (filter.isExcluded(this, projectName)) {
                return true;
            }
        }
        return false;
    }

    public boolean process(@NonNull String projectName, @NonNull SourceFactory sourceFactory,
                           @CheckForNull AttributeFactory attributeFactory,
                           Witness... witnesses)
            throws IllegalArgumentException, IOException, InterruptedException {
        return process(
                projectName,
                Collections.singletonList(sourceFactory),
                attributeFactory == null ? null : Collections.singletonList(attributeFactory),
                witnesses
        );
    }

    public boolean process(@NonNull String projectName, @NonNull List<SourceFactory> sourceFactories,
                           @CheckForNull List<AttributeFactory> attributeFactories,
                           Witness... witnesses)
            throws IllegalArgumentException, IOException, InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        if (isExcluded(projectName)) {
            // not included
            record(projectName, false, witnesses);
            return !observer.isObserving();
        }
        record(projectName, true, witnesses);
        SCMSourceObserver.ProjectObserver po = observer.observe(projectName);
        if (po instanceof NoOpProjectObserver) {
            // we know this is safe to break contract with
            return !observer.isObserving();
        }
        for (SourceFactory s: sourceFactories) {
            po.addSource(s.create(projectName));
        }
        if (attributeFactories != null) {
            for (AttributeFactory attributeFactory: attributeFactories) {
                for (Map.Entry<String, Object> entry : attributeFactory.create(projectName).entrySet()) {
                    po.addAttribute(entry.getKey(), entry.getValue());
                }
            }
        }
        po.complete();
        return !observer.isObserving();
    }

    /**
     * Adds managing a {@link Closeable} into the scope of the {@link SCMNavigatorRequest}
     *
     * @param closeable the {@link Closeable} to manage.
     */
    public void manage(@CheckForNull Closeable closeable) {
        if (closeable != null) {
            managedClosables.add(closeable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        IOException ioe = null;
        for (Closeable c : managedClosables) {
            try {
                c.close();
            } catch (IOException e) {
                if (ioe == null) {
                    ioe = e;
                } else {
                    // TODO replace with direct call to addSuppressed once baseline Java is 1.7
                    try {
                        Method addSuppressed = Throwable.class.getMethod("addSuppressed", Throwable.class);
                        addSuppressed.invoke(ioe, e);
                    } catch (NoSuchMethodException e1) {
                        // ignore, best effort
                    } catch (IllegalAccessException e1) {
                        // ignore, best effort
                    } catch (InvocationTargetException e1) {
                        // ignore, best effort
                    }
                }
            }
        }
        if (ioe != null) {
            throw ioe;
        }
    }

    public interface Witness {
        void record(@NonNull String projectName, boolean isMatch);
    }

    public interface SourceFactory {
        SCMSource create(String projectName);
    }

    public interface AttributeFactory {
        Map<String,Object> create(String projectName);
    }
}
