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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.init.Terminator;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.ClassLoaderSanityThreadFactory;
import hudson.util.DaemonThreadFactory;
import hudson.util.NamingThreadFactory;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.HttpServletRequest;
import jenkins.security.ImpersonatingScheduledExecutorService;
import jenkins.util.SystemProperties;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.Stapler;

/**
 * Base class for all events from a SCM system.
 * <p>
 * <strong>NOTE:</strong> Not every SCM system will be able to support all possible events. Not every plugin building
 * on top of the SCM API
 * will be able to respond to every event.
 * <h2>Information for SCM API consumers</h2>
 * <p>
 * Consumers of events should assume that the data from an event is unverified / untrusted.
 * Events should be used to scope queries against the {@link SCMNavigator} / {@link SCMSource} to which they relate.
 * In all other respects, consumers should treat the event payload and information derived from the payload as
 * being <em>just a rumour</em>.
 * <p>
 * NOTE: Implementations may retain their own internal tracking of portions of the event payload that are verified /
 * trusted and use such internal information to assist in optimization of the processing of scoped queried, but at this
 * time we are not exposing such partial trust information to consumers until we have established some patterns.
 * <p>
 * The priority for consumers of the SCM API, in other words, implementations of {@link SCMNavigatorOwner} and
 * {@link SCMSourceOwner}, is to avoid full rescans as a means of detecting:
 * <ul>
 * <li>Creation of new {@link SCMSource} instances within a {@link SCMNavigator}, i.e. a {@link SCMNavigatorOwner}
 * implementation should make best effort to handle a {@link SCMSourceEvent} of {@link Type#CREATED} without
 * resorting to {@link SCMNavigator#visitSources(SCMSourceObserver)}</li>
 * <li>Creation of new {@link SCMHead} instances within a {@link SCMSource}, i.e. a {@link SCMSourceOwner}
 * implementation should make best effort to handle a {@link SCMHeadEvent} of {@link Type#CREATED} without
 * resorting to {@link SCMSource#fetch(SCMHeadObserver, TaskListener)} or
 * {@link SCMSource#fetch(SCMSourceCriteria, SCMHeadObserver, TaskListener)}</li>
 * <li>The update of a {@link SCMHead} instance to point to a new {@link SCMRevision}, i.e. a {@link SCMHeadEvent} of
 * {@link Type#UPDATED}. <strong>NOTE:</strong> typically existing legacy SCM implementations will handle this
 * out-of-band, but it would be nice if the notification could be consolidated.</li>
 * </ul>
 * <h2>Information for SCM API providers</h2>
 * <p>
 * The priority for implementers of the SCM API, in other words, implementations of {@link SCMNavigator} and
 * {@link SCMSource}, is to ensure that consumers do not have to trigger full rescans.
 * <p>
 * Where implementers can obtain some form of trust / verification about event payloads, please share your patterns
 * with the maintainers of this API so that a generic set of patterns can be abstracted for exposure to consumers.
 *
 * @param <P> the type of (provider specific) payload.
 * @since 2.0
 */
public abstract class SCMEvent<P> {

    /**
     * An empty array of {@linkplain Cause}s.
     */
    private static final Cause[] EMPTY_CAUSES = new Cause[0];
    /**
     * An unknown origin.
     *
     * @since 2.0.3
     */
    public static final String ORIGIN_UNKNOWN = "?";

    private static final int EVENT_THREAD_POOL_SIZE = SystemProperties
        .getInteger(SCMEvent.class.getName() + ".EVENT_THREAD_POOL_SIZE", 10);
    /**
     * The event type.
     */
    @NonNull
    private final Type type;

    /**
     * The timestamp of the event.
     */
    private final long timestamp;

    /**
     * The (provider specific) payload of the event
     */
    @NonNull
    private final P payload;

    /**
     * The optional (provider specific) origin of the event.
     *
     * @since 2.0.3
     */
    @CheckForNull
    private final String origin;

    /**
     * The scheduled executor thread pool. This is initialized lazily since it may be never needed.
     */
    private static ScheduledExecutorService executorService;

    /**
     * Constructor to use when the timestamp is available from the external SCM.
     *
     * @param type      the type of event.
     * @param timestamp the timestamp from the external SCM (see {@link System#currentTimeMillis()} for start and units)
     * @param payload   the original provider specific payload.
     * @deprecated use {@link #SCMEvent(Type, long, Object, String)}
     */
    @Deprecated
    public SCMEvent(@NonNull Type type, long timestamp, @NonNull P payload) {
        this(type, timestamp, payload, originOf(Stapler.getCurrentRequest()));
    }

    /**
     * Constructor to use when the timestamp is available from the external SCM.
     *
     * @param type      the type of event.
     * @param timestamp the timestamp from the external SCM (see {@link System#currentTimeMillis()} for start and units)
     * @param payload   the original provider specific payload.
     * @param origin    the (optional) origin of the event, e.g. a hostname, etc. It is recommended to use
     *                  {@link #originOf(HttpServletRequest)} where the event originates from a
     *                  {@link HttpServletRequest} and the request is available when the event is being created.
     * @since 2.0.3
     */
    public SCMEvent(@NonNull Type type, long timestamp, @NonNull P payload, @CheckForNull String origin) {
        this.type = type;
        this.timestamp = timestamp;
        this.payload = payload;
        this.origin = ORIGIN_UNKNOWN.equals(origin) ? null : origin;
    }

    /**
     * Constructor to use when the timestamp is not available from the external SCM. The timestamp will be set
     * using {@link System#currentTimeMillis()}
     *
     * @param type    the type of event.
     * @param payload the original provider specific payload.
     * @deprecated use {@link #SCMEvent(Type, Object, String)}
     */
    @Deprecated
    public SCMEvent(@NonNull Type type, @NonNull P payload) {
        this(type, System.currentTimeMillis(), payload);
    }

    /**
     * Constructor to use when the timestamp is not available from the external SCM. The timestamp will be set
     * using {@link System#currentTimeMillis()}
     *
     * @param type    the type of event.
     * @param payload the original provider specific payload.
     * @param origin  the (optional) origin of the event, e.g. a hostname, etc
     * @since 2.0.3
     */
    public SCMEvent(@NonNull Type type, @NonNull P payload, @CheckForNull String origin) {
        this(type, System.currentTimeMillis(), payload, origin);
    }

    /**
     * Copy constructor which may be required in cases where sub-classes need to implement {@code readResolve}
     *
     * @param copy the event to clone.
     */
    protected SCMEvent(SCMEvent<P> copy) {
        this(copy.getType(), copy.getTimestamp(), copy.getPayload(), copy.origin);
    }

    /**
     * The {@link ScheduledExecutorService} that events should be fired on.
     *
     * @return a {@link ScheduledExecutorService}.
     */
    @NonNull
    protected static synchronized ScheduledExecutorService executorService() {
        if (executorService == null) {
            executorService = new ImpersonatingScheduledExecutorService(
                new ScheduledThreadPoolExecutor(
                    EVENT_THREAD_POOL_SIZE,
                    new NamingThreadFactory(
                        new ClassLoaderSanityThreadFactory(new DaemonThreadFactory()), "SCMEvent")
                ),
                ACL.SYSTEM2);
        }
        return executorService;
    }


    /**
     * Shutdown the timer and throw it away.
     */
    @Terminator
    public static synchronized void closeExecutorService() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }

    /**
     * Gets the type of event.
     *
     * @return the type of event.
     */
    @NonNull
    public Type getType() {
        return type;
    }

    /**
     * Gets the timestamp of the event (see {@link System#currentTimeMillis()} for start and units).
     *
     * @return the timestamp of the event.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the timestamp of the event as a {@link Date}.
     *
     * @return the timestamp of the event.
     */
    @NonNull
    public Date getDate() {
        return new Date(timestamp);
    }

    /**
     * Gets the provider specific event payload.
     *
     * @return the provider specific event payload.
     */
    @NonNull
    public P getPayload() {
        return payload;
    }

    /**
     * Gets the origin of the event, e.g. a hostname, etc.
     *
     * @return the origin of the event (or {@link #ORIGIN_UNKNOWN} if the origin is unknown)
     * @since 2.0.3
     */
    @NonNull
    public String getOrigin() {
        return StringUtils.defaultIfBlank(origin, ORIGIN_UNKNOWN);
    }

    /**
     * Return a description of the event.
     *
     * @return the description or {@code null} if no description can be provided.
     * @since 2.1.1
     */
    @CheckForNull
    public String description() {
        return null;
    }

    /**
     * If this event is being used to trigger a build, what - if any - {@linkplain Cause}s should be added to the
     * triggered build.
     * <strong>The {@link Cause} instances should probably be new instances each time, see
     * {@link Cause#onAddedTo(Run)}.</strong>
     *
     * @return the {@link Cause} instances to add to any builds triggered by this event.
     */
    @NonNull
    public Cause[] asCauses() {
        return EMPTY_CAUSES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SCMEvent<?> scmEvent = (SCMEvent<?>) o;

        if (type != scmEvent.type) {
            return false;
        }
        if (timestamp != scmEvent.timestamp) {
            return false;
        }
        return payload.equals(scmEvent.payload);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + payload.hashCode();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
                "SCMEvent{type=%s, timestamp=%tc, payload=%s, origin=%s}",
                type,
                timestamp,
                payload,
                StringUtils.defaultIfBlank(origin, ORIGIN_UNKNOWN)
        );
    }

    /**
     * Helper method to get the origin of an event from a {@link HttpServletRequest}. The current format is the
     * list of hostname / ip addresses from the request (parsing {@code X-Forwarded-For} headers) separated by
     * {@code →} followed by a {@code ⇒} and finally the requested URL (omitting the query portion of the URL).
     *
     * @param req the {@link HttpServletRequest} or {@code null} (this is to allow passing
     *            {@link Stapler#getCurrentRequest()} without having to check for {@code null})
     * @return the origin of the event or {@code null} if the {@link HttpServletRequest} is null.
     * @since 2.0.3
     */
    @CheckForNull
    public static String originOf(@CheckForNull HttpServletRequest req) {
        if (req == null) {
            return null;
        }
        String last = null;
        StringBuilder result = new StringBuilder();
        // TODO RFC 7239 support once standard is approved
        String header = req.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(header)) {
            for (String remote : header.split("(,\\s*)")) {
                if (StringUtils.isBlank(remote)) {
                    continue;
                }
                if (last != null) {
                    result.append(" → ");
                }
                last = StringUtils.trim(remote);
                result.append(last);
            }
        }
        String remoteHost = req.getRemoteHost();
        String remoteAddr = req.getRemoteAddr();
        if (last == null || (!(StringUtils.equals(last, remoteHost) || StringUtils.equals(last, remoteAddr)))) {
            if (last != null) {
                result.append(" → ");
            }
            if (!StringUtils.isBlank(remoteHost) && !remoteHost.equals(remoteAddr)) {
                result.append(remoteHost);
                result.append('/');
            }
            result.append(remoteAddr);
        }
        result.append(" ⇒ ");
        String scheme = StringUtils.defaultIfBlank(req.getHeader("X-Forwarded-Proto"), req.getScheme());
        result.append(scheme);
        result.append("://");
        result.append(req.getServerName());
        String portStr = req.getHeader("X-Forwarded-Port");
        int port;
        if (portStr != null) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                port = req.getLocalPort();
            }
        } else {
            port = req.getLocalPort();
        }
        if (!("http".equals(scheme) && port == 80 || "https".equals(scheme) && port == 443)) {
            result.append(':');
            result.append(port);
        }
        result.append(req.getRequestURI());
        // omit query as may contain "secrets"
        return result.toString();
    }

    /**
     * The type of event.
     */
    public enum Type {
        /**
         * Signifies the creation of a new thing:
         * <ul>
         * <li>An existing {@link SCMNavigator} getting a new {@link SCMSource} would trigger a
         * {@link SCMSourceEvent}</li>
         * <li>An existing {@link SCMSource} getting a new {@link SCMHead} would trigger a {@link SCMHeadEvent}</li>
         * </ul>
         */
        CREATED,
        /**
         * Signifies the update of metadata of existing thing:
         * <ul>
         * <li>An existing {@link SCMNavigator}'s metadata changing would trigger a {@link SCMNavigatorEvent}</li>
         * <li>An existing {@link SCMSource}'s metadata changing would trigger a {@link SCMSourceEvent}</li>
         * <li>An existing {@link SCMHead}'s metadata changing (including the {@link SCMRevision} that the
         * {@link SCMHead} points to) would trigger a {@link SCMHeadEvent}</li>
         * </ul>
         */
        UPDATED,
        /**
         * Signifies the removal of an existing thing:
         * <ul>
         * <li>An existing {@link SCMNavigator} being removed would trigger a {@link SCMNavigatorEvent}</li>
         * <li>An existing {@link SCMSource} being removed would trigger a {@link SCMSourceEvent}</li>
         * <li>An existing {@link SCMHead} being removed would trigger a {@link SCMHeadEvent}</li>
         * </ul>
         */
        REMOVED
    }

    protected abstract static class Dispatcher<E extends SCMEvent<?>> implements Runnable {
        @Restricted(NoExternalUse.class)
        /*package*/ static final AtomicLong lastId = new AtomicLong();
        @Restricted(NoExternalUse.class)
        /*package*/ static final Lock lock = new ReentrantLock();
        @Restricted(NoExternalUse.class)
        /*package*/ static final Condition started = lock.newCondition();
        @Restricted(NoExternalUse.class)
        /*package*/ static final Condition finished = lock.newCondition();
        @Restricted(NoExternalUse.class)
        /*package*/ static long startedId = 0L;
        @Restricted(NoExternalUse.class)
        /*package*/ static long finishedId = 0L;
        @Restricted(NoExternalUse.class)
        /*package*/ static int inFlight = 0;

        private final long id;
        private final E event;

        public Dispatcher(E event) {
            this.event = event;
            this.id = lastId.incrementAndGet();
        }

        protected abstract void log(SCMEventListener l, Throwable e);

        protected abstract void fire(SCMEventListener l, E event);

        @Override
        public void run() {
            lock.lock();
            try {
                if (startedId < id) {
                    startedId = id;
                    started.signalAll();
                }
                inFlight++;
            } finally {
                lock.unlock();
            }
            String oldName = Thread.currentThread().getName();
            try {
                Thread.currentThread().setName(String.format("%s %tc / %s",
                        event.getClass(), event.getTimestamp(), oldName)
                );
                for (final SCMEventListener l : ExtensionList.lookup(SCMEventListener.class)) {
                    SecurityContext context = ACL.impersonate(ACL.SYSTEM);
                    try {
                        fire(l, event);
                    } catch (LinkageError e) {
                        log(l, e);
                    } catch (Error e) {
                        throw e;
                    } catch (Throwable e) {
                        log(l, e);
                    } finally {
                        SecurityContextHolder.setContext(context);
                    }
                }
            } finally {
                Thread.currentThread().setName(oldName);
                lock.lock();
                try {
                    if (finishedId < id) {
                        finishedId = id;
                    }
                    inFlight--;
                    if (inFlight <= 0) {
                        inFlight = 0;
                        finished.signalAll();
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }

}
