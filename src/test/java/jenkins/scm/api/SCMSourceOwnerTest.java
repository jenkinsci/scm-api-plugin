/*
 * The MIT License
 *
 * Copyright (c) 2019, Stephen Connolly.
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
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Util;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.ListBoxModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import jenkins.scm.impl.NullSCMSource;
import org.acegisecurity.Authentication;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.DataBoundConstructor;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SCMSourceOwnerTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Test
    public void given__proxy__when__getSCMSources__then__returned() throws Exception {
        final FreeStyleProject item = r.createFreeStyleProject();
        List<SCMSource> sources = new ArrayList<>(Collections.singletonList(new NullSCMSource()));
        SCMSourceOwner owner = SCMSourceOwner.proxyFromItem(item, null, () -> sources, null);
        try {
            assertThat(owner.getSCMSources(), is(sources));
        } finally {
            if (owner instanceof AutoCloseable) ((AutoCloseable) owner).close();
        }
    }

    @Test
    public void given__proxy__when__getSCMSource__then__returned() throws Exception {
        final FreeStyleProject item = r.createFreeStyleProject();
        final NullSCMSource source = new NullSCMSource();
        List<SCMSource> sources = Collections.singletonList(source);
        SCMSourceOwner owner = SCMSourceOwner.proxyFromItem(item, null, () -> sources, null);
        try {
            assertThat(owner.getSCMSource(source.getId()), is(source));
        } finally {
            if (owner instanceof AutoCloseable) ((AutoCloseable) owner).close();
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void given__proxy__when__onSCMSourceUpdated__then__doesnt_throw_unsupported() throws Exception {
        final FreeStyleProject item = r.createFreeStyleProject();
        final NullSCMSource source = new NullSCMSource();
        List<SCMSource> sources = Collections.singletonList(source);
        SCMSourceOwner owner = SCMSourceOwner.proxyFromItem(item, null, () -> sources, null);
        try {
            // just want to be sure this method doesn't cause unsupported operation
            owner.onSCMSourceUpdated(source);
        } finally {
            if (owner instanceof AutoCloseable) ((AutoCloseable) owner).close();
        }
    }

    @Test
    public void given__proxy__when__getSCMSourceCriteria__then__returned() throws Exception {
        final FreeStyleProject item = r.createFreeStyleProject();
        final NullSCMSource source = new NullSCMSource();
        List<SCMSource> sources = Collections.singletonList(source);
        final SCMSourceCriteria criteria = (SCMSourceCriteria) (probe, listener) -> false;
        SCMSourceOwner owner = SCMSourceOwner.proxyFromItem(item, null, () -> sources, () -> criteria);
        try {
            assertThat(owner.getSCMSourceCriteria(source), is(criteria));
        } finally {
            if (owner instanceof AutoCloseable) ((AutoCloseable) owner).close();
        }
    }

    @Test
    public void given__proxy__when__getName__then__returned() throws Exception {
        final FreeStyleProject item = r.createFreeStyleProject();
        final NullSCMSource source = new NullSCMSource();
        List<SCMSource> sources = Collections.singletonList(source);
        SCMSourceOwner owner = SCMSourceOwner.proxyFromItem(item, null, () -> sources, null);
        try {
            assertThat(owner.getName(), is(item.getName()));
        } finally {
            if (owner instanceof AutoCloseable) ((AutoCloseable) owner).close();
        }
    }

    @Test
    public void given__proxy__when__getParent__then__returned() throws Exception {
        final FreeStyleProject item = r.createFreeStyleProject();
        final NullSCMSource source = new NullSCMSource();
        List<SCMSource> sources = Collections.singletonList(source);
        SCMSourceOwner owner = SCMSourceOwner.proxyFromItem(item, null, () -> sources, null);
        try {
            assertThat(owner.getParent(), is(item.getParent()));
        } finally {
            if (owner instanceof AutoCloseable) ((AutoCloseable) owner).close();
        }
    }

    @Test
    public void given__attached_proxy__when__searching_for_all__then__found() throws Exception {
        final FreeStyleProject item = r.createFreeStyleProject();
        final NullSCMSource source = new NullSCMSource();
        List<SCMSource> sources = Collections.singletonList(source);
        Map<Item,SCMSourceOwner> ownerMap = new HashMap<>();
        SCMSourceOwner owner = SCMSourceOwner.proxyFromItem(item, ownerMap::get, () -> sources, null);
        ownerMap.put(item, owner);
        assertThat(StreamSupport.stream(SCMSourceOwners.all().spliterator(), false).anyMatch(s -> s == owner), is(true));
    }

    @Test
    public void given__single_shot_proxy__when__searching_for_all__then__not_found() throws Exception {
        final FreeStyleProject item = r.createFreeStyleProject();
        final NullSCMSource source = new NullSCMSource();
        List<SCMSource> sources = Collections.singletonList(source);
        SCMSourceOwner owner = SCMSourceOwner.proxyFromItem(item, null, () -> sources, null);
        assertThat(StreamSupport.stream(SCMSourceOwners.all().spliterator(), false).anyMatch(s -> s == owner), is(false));
    }

    @Test
    public void given__item_with_own_credentials_store__when__listing_credentials_from_proxy__then__credentials_found() throws Exception {
        final FreeStyleProject item = r.createFreeStyleProject();
        final UsernamePasswordCredentialsImpl credentials =
                new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, "foo", "probe", "bob", "secret");
        item.addProperty(new CredentialJobProperty(Collections.singletonList(credentials)));
        ListBoxModel options = CredentialsProvider.listCredentials(
                IdCredentials.class,
                item,
                null,
                Collections.emptyList(),
                CredentialsMatchers.withId("foo"));
        // i'd use assumeThat but this is a critical test case
        assertThat("Test setup item scoped credentials correctly", options, hasSize(1));
        assertThat("Test setup item scoped credentials correctly", options.get(0).value, is("foo"));

        final NullSCMSource source = new NullSCMSource();
        List<SCMSource> sources = Collections.singletonList(source);
        SCMSourceOwner owner = SCMSourceOwner.proxyFromItem(item, null, () -> sources, null);

        options = CredentialsProvider.listCredentials(
                IdCredentials.class,
                owner,
                null,
                Collections.emptyList(),
                CredentialsMatchers.withId("foo"));
        assertThat("We have correctly proxyed item scoped credentials", options, hasSize(1));
        assertThat("We have correctly proxyed item scoped credentials", options.get(0).value, is("foo"));
    }

    @Test
    public void given__item_with_own_credentials_store__when__retrieving_credentials_from_proxy__then__credentials_found() throws Exception {
        final FreeStyleProject item = r.createFreeStyleProject();
        final UsernamePasswordCredentialsImpl credentials =
                new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, "foo", "probe", "bob", "secret");
        item.addProperty(new CredentialJobProperty(Collections.singletonList(credentials)));
        List<IdCredentials> list = CredentialsProvider.lookupCredentials(
                IdCredentials.class,
                item,
                null,
                Collections.emptyList());
        // i'd use assumeThat but this is a critical test case
        assertThat("Test setup item scoped credentials correctly", list, hasItem(credentials));

        final NullSCMSource source = new NullSCMSource();
        List<SCMSource> sources = Collections.singletonList(source);
        SCMSourceOwner owner = SCMSourceOwner.proxyFromItem(item, null, () -> sources, null);

        list = CredentialsProvider.lookupCredentials(
                IdCredentials.class,
                owner,
                null,
                Collections.emptyList());
        assertThat("We have correctly proxyed item scoped credentials", list, hasItem(credentials));
    }

    public static class CredentialJobProperty extends JobProperty<FreeStyleProject> {
        private final List<Credentials> credentials;

        @DataBoundConstructor
        public CredentialJobProperty(List<Credentials> credentials) {
            this.credentials = new ArrayList<>(Util.fixNull(credentials));
        }

        public List<Credentials> getCredentials() {
            return Collections.unmodifiableList(credentials);
        }

        @TestExtension
        public static class CredentialsJobPropertyDescriptor extends JobPropertyDescriptor {
        }
    }

    /**
     * Avoiding a dependency on folders plugin while also giving a bonus of testing in the event some crazy person
     * adds credentials directly to items.
     */
    @TestExtension
    public static class JobCredentialsProvider extends CredentialsProvider {

        @NonNull
        @Override
        public <C extends Credentials> List<C> getCredentials(@NonNull Class<C> type, @Nullable ItemGroup itemGroup,
                                                              @Nullable Authentication authentication) {
            return Collections.emptyList();
        }

        @Override
        public boolean isEnabled(Object context) {
            return context instanceof FreeStyleProject
                    && ((FreeStyleProject)context).getProperty(CredentialJobProperty.class) != null;
        }

        @NonNull
        @Override
        public <C extends Credentials> List<C> getCredentials(@NonNull Class<C> type, @NonNull Item item,
                                                              @Nullable Authentication authentication) {
            return getCredentials(type, item, authentication, Collections.emptyList());
        }

        @NonNull
        @Override
        public <C extends Credentials> List<C> getCredentials(@NonNull Class<C> type, @NonNull Item item,
                                                              @Nullable Authentication authentication,
                                                              @NonNull List<DomainRequirement> domainRequirements) {
            if (item instanceof FreeStyleProject) {
                CredentialJobProperty jobProperty = ((FreeStyleProject) item).getProperty(CredentialJobProperty.class);
                if (jobProperty != null) {
                    return jobProperty.credentials.stream()
                            .filter(type::isInstance)
                            .map(type::cast)
                            .collect(Collectors.toList());
                }

            }
            return Collections.emptyList();
        }
    }

}
