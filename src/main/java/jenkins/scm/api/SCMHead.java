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
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.Item;
import hudson.model.ItemGroup;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.TransientActionFactory;
import jenkins.scm.api.actions.ChangeRequestAction;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Represents a named SCM branch, tag or mainline.
 *
 * @author Stephen Connolly
 */
@ExportedBean
public class SCMHead implements Comparable<SCMHead>, Serializable {

    private static final Logger LOGGER = Logger.getLogger(SCMHead.class.getName());

    /**
     * Ensure consistent serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The name.
     */
    @NonNull
    private final String name;

    /**
     * Constructor.
     *
     * @param name the name.
     */
    public SCMHead(@NonNull String name) {
        name.getClass(); // throw NPE if null
        this.name = name;
    }

    /**
     * Returns the name.
     *
     * @return the name.
     */
    @Exported
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SCMHead scmHead = (SCMHead) o;

        if (!name.equals(scmHead.name)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(SCMHead o) {
        return getName().compareTo(o.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SCMHead{'");
        sb.append(name).append("'}");
        return sb.toString();
    }

    /**
     * Gets all actions used to decorate the behavior of this branch.
     * May be overridden to create a new list, perhaps with additions.
     * @return a list of all actions associated with this branch (by default, an unmodifiable list searching {@link TransientActionFactory}s)
     * @see Actionable#getAllActions
     * @since 1.1
     */
    @NonNull
    @Exported(name="actions")
    public List<? extends Action> getAllActions() {
        List<Action> actions = new ArrayList<Action>();
        for (TransientActionFactory<?> taf : ExtensionList.lookup(TransientActionFactory.class)) {
            if (taf.type().isInstance(this)) {
                try {
                    actions.addAll(createFor(taf));
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Could not load actions from " + taf + " for " + this, e);
                }
            }
        }
        return Collections.unmodifiableList(actions);
    }
    private <T> Collection<? extends Action> createFor(TransientActionFactory<T> taf) {
        return taf.createFor(taf.type().cast(this));
    }

    /**
     * Gets a specific action used to decorate the behavior of this branch.
     * May be overridden but suffices to override {@link #getAllActions}.
     * @param <T> a desired action type to query, such as {@link ChangeRequestAction}
     * @param type type token
     * @return an instance of that action interface (by default, filters {@link #getAllActions})
     * @see Actionable#getAction(Class)
     * @since 1.1
     */
    @CheckForNull
    public <T extends Action> T getAction(@NonNull Class<T> type) {
        for (Action action : getAllActions()) {
            if (type.isInstance(action)) {
                return type.cast(action);
            }
        }
        return null;
    }

    /**
     * Means of locating a head given an item.
     * @since 0.3-beta-2
     */
    public static abstract class HeadByItem implements ExtensionPoint {

        /**
         * Checks whether a given item corresponds to a particular SCM head.
         * @param item such as a {@linkplain ItemGroup#getItems child} of an {@link SCMSourceOwner}
         * @return a corresponding SCM head, or null if this information is unavailable
         */
        @CheckForNull
        public abstract SCMHead getHead(Item item);

        /**
         * Runs all registered implementations.
         * @param item an item, such as a branch project
         * @return the corresponding head, if known
         */
        @CheckForNull
        public static SCMHead findHead(Item item) {
            for (HeadByItem ext : ExtensionList.lookup(HeadByItem.class)) {
                SCMHead head = ext.getHead(item);
                if (head != null) {
                    return head;
                }
            }
            return null;
        }

    }

}
