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
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.TaskListener;
import hudson.util.AlternativeUiTextProvider;
import java.util.Collections;
import java.util.List;
import jenkins.scm.api.actions.ChangeRequestAction;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import jenkins.scm.api.mixin.SCMHeadMixin;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Represents a named SCM branch, change request, tag or mainline. This class is intended to be used as a typed key
 * rather than passing a {@link String} around. Each {@link SCMSource} implementation may want to have their own
 * subclasses in order assist to differentiating between different classes of head via the {@link SCMHeadMixin}
 * interfaces.
 * <p>
 * <strong>Please note the equality contract for {@link SCMHeadMixin} implementations:</strong>
 * Two {@link SCMHeadMixin} implementations are equal if and only if:
 * <ul>
 *     <li>They both are the same class</li>
 *     <li>They both have the same {@link SCMHeadMixin#getName()}</li>
 *     <li>For each implemented {@link SCMHeadMixin} sub-interface, they both return the same values from all Java
 *     Bean property getters declared on the sub-interface. Thus, for example {@link ChangeRequestSCMHead}
 *     implementations are only considered equal if {@link ChangeRequestSCMHead#getId()} and
 *     {@link ChangeRequestSCMHead#getTarget()} are also equal</li>
 * </ul>
 * The contract for {@link Object#hashCode()} is:
 * <ul>
 *     <li>{@link Object#hashCode()} for a {@link SCMHeadMixin} implementation must be equal to the
 *     {@link String#hashCode()} of {@link SCMHeadMixin#getName()}</li>
 * </ul>
 * The {@link SCMHead#equals(Object)} and {@link SCMHead#hashCode()} methods enforce the above requirements and
 * are final.
 *
 * @author Stephen Connolly
 */
@ExportedBean
public class SCMHead implements SCMHeadMixin {

    /**
     * Replaceable pronoun of that points to a {@link SCMHead}. Defaults to {@code null} depending on the context.
     *
     * @since 2.0
     */
    public static final AlternativeUiTextProvider.Message<SCMHead> PRONOUN
            = new AlternativeUiTextProvider.Message<SCMHead>();

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
     * {@inheritDoc}
     */
    @Override
    @Exported
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Exported
    @NonNull
    public SCMHeadOrigin getOrigin() {
        return SCMHeadOrigin.DEFAULT;
    }

    /**
     * Get the term used in the UI to represent this kind of {@link SCMHead}. Must start with a capital letter.
     *
     * @return the term or {@code null} to fall back to the calling context's default.
     * @since 2.0
     */
    @CheckForNull
    public String getPronoun() {
        return AlternativeUiTextProvider.get(PRONOUN, this, null);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two {@link SCMHeadMixin} implementations are equal if and only if:
     * <ul>
     *     <li>They both are the same class</li>
     *     <li>They both have the same {@link SCMHeadMixin#getName()}</li>
     *     <li>For each implemented {@link SCMHeadMixin} sub-interface, they both return the same values from all Java
     *     Bean property getters declared on the sub-interface. Thus, for example {@link ChangeRequestSCMHead}
     *     implementations are only considered equal if {@link ChangeRequestSCMHead#getId()} and
     *     {@link ChangeRequestSCMHead#getTarget()} are also equal</li>
     * </ul>
     * <p>
     *     By way of example, any implementation of {@link ChangeRequestSCMHead} will have their equals behave like so:
     * <pre>
     *     public static class MyChangeRequestSCMHead extends SCMHead implements ChangeRequestSCMHead {
     *         //...
     *         // this method is implemented for you, but if you had to write it this is what you would
     *         // have to write
     *         public boolean equals(Object o) {
     *             if (!super.equals(o)) {
     *                 return false;
     *             }
     *             // can only be equal if they are the same class
     *             MyChangeRequestSCMHead that = (MyChangeRequestSCMHead)o;
     *             // because we implement ChangeRequestSCMHead and ChangeRequestSCMHead has a getId() method
     *             String id1 = this.getId();
     *             String id2 = that.getId();
     *             if (id1 == null ? id2 != null : !id1.equals(id2)) {
     *                 return false;
     *             }
     *             // because we implement ChangeRequestSCMHead and ChangeRequestSCMHead has a getTarget() method
     *             SCMHead t1 = this.getTarget();
     *             SCMHead t2 = that.getTarget();
     *             if (t1 == null ? t2 != null : !t1.equals(t2)) {
     *                 return false;
     *             }
     *             // we do not implement any other interfaces extending SCMHeadMixin, so we must be equal
     *             return true;
     *         }
     *     }
     * </pre>
     * @param o the object to compare with.
     * @return true if and only if the two objects are equal.
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
        if (!SCMHeadMixinEqualityGenerator.getOrCreate(getClass()).equals(this,scmHead)) {
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
        return "SCMHead{'" + name + "'}";
    }

    /**
     * Returns an empty list.
     * @return an empty list
     * @since 1.1
     * @deprecated this was added to the API in error. Retained for backwards binary compatibility only. Use
     * {@link SCMSource#fetchActions(SCMHead, SCMHeadEvent, TaskListener)} to get the actions associated with a
     * {@link SCMHead}
     */
    @Restricted(DoNotUse.class)
    @Deprecated // this is do not use because you should not use it
    @NonNull
    public List<? extends Action> getAllActions() {
        // TODO should be deleted but old versions of blue ocean api call this method
        return Collections.emptyList();
    }

    /**
     * Returns {@code null}.
     * @param <T> a desired action type to query, such as {@link ChangeRequestAction}
     * @param type type token
     * @return {@code null}
     * @since 1.1
     * @deprecated this was added to the API in error. Retained for backwards binary compatibility only. Use
     * {@link SCMSource#fetchActions(SCMHead, SCMHeadEvent, TaskListener)} to get the actions associated with a
     * {@link SCMHead}
     */
    @Restricted(DoNotUse.class)
    @Deprecated // this is do not use because you should not use it
    @CheckForNull
    public <T extends Action> T getAction(@NonNull Class<T> type) {
        // TODO should be deleted but old versions of blue ocean api call this method
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
