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

package jenkins.scm.impl;

import java.util.Collections;
import jenkins.scm.api.SCMSource;
import jenkins.scm.impl.mock.MockSCM;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMDiscoverBranches;
import jenkins.scm.impl.mock.MockSCMDiscoverChangeRequests;
import jenkins.scm.impl.mock.MockSCMDiscoverTags;
import jenkins.scm.impl.mock.MockSCMHead;
import jenkins.scm.impl.mock.MockSCMNavigator;
import jenkins.scm.impl.mock.MockSCMRevision;
import jenkins.scm.impl.mock.MockSCMSource;
import jenkins.scm.impl.trait.RegexSCMHeadFilterTrait;
import jenkins.scm.impl.trait.RegexSCMSourceFilterTrait;
import jenkins.scm.impl.trait.WildcardSCMHeadFilterTrait;
import jenkins.scm.impl.trait.WildcardSCMSourceFilterTrait;
import org.jenkinsci.plugins.structs.describable.DescribableModel;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

public class SymbolAnnotationsTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Test
    public void given__mockScm__when__uninstantiating__then__noRaw$class() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("test");
            MockSCM instance = new MockSCM(c, "test", new MockSCMHead("master"), new MockSCMRevision(
                    new MockSCMHead("master"), c.getRevision("test", "master")));
            assertThat(DescribableModel.uninstantiate2_(instance).toString(), allOf(
                    startsWith("@"),
                    not(containsString(", $")),
                    not(containsString("=$")),
                    not(containsString("[$")),
                    not(containsString("{$"))
            ));
            assertThat(DescribableModel.uninstantiate2_(instance).toString(), is("@mockScm("
                    + "controllerId=" + c.getId() + ","
                    + "head=master,"
                    + "repository=test,"
                    + "revision=" + c.getRevision("test", "master")
                    + ")"));
        }
    }

    @Test
    public void given__mockScmSource__when__uninstantiating__then__noRaw$class() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("test");
            MockSCMSource instance = new MockSCMSource(c, "test", new MockSCMDiscoverBranches(),
                    new MockSCMDiscoverChangeRequests(), new MockSCMDiscoverTags(),
                    new WildcardSCMHeadFilterTrait("*", "ignore"), new RegexSCMHeadFilterTrait("i.*"));
            assertThat(DescribableModel.uninstantiate2_(instance).toString(), allOf(
                    startsWith("@"),
                    not(containsString(", $")),
                    not(containsString("=$")),
                    not(containsString("[$")),
                    not(containsString("{$"))
            ));
            assertThat(DescribableModel.uninstantiate2_(instance).toString(), is("@mockScm("
                    + "controllerId=" + c.getId() + ","
                    + "id=" + instance.getId() + ","
                    + "repository=test,"
                    + "traits=["
                    + "@discoverBranches$MockSCMDiscoverBranches(), "
                    + "@discoverChangeRequests$MockSCMDiscoverChangeRequests(strategiesStr=HEAD, ), "
                    + "@discoverTags$MockSCMDiscoverTags(), "
                    + "@headWildcardFilter$WildcardSCMHeadFilterTrait(excludes=ignore,includes=*), "
                    + "@headRegexFilter$RegexSCMHeadFilterTrait(regex=i.*)"
                    + "]"
                    + ")"));
        }
    }

    @Test
    public void given__mockScmNavigator__when__uninstantiating__then__noRaw$class() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("test");
            MockSCMNavigator instance = new MockSCMNavigator(c,
                    new MockSCMDiscoverBranches(),
                    new MockSCMDiscoverChangeRequests(),
                    new MockSCMDiscoverTags(),
                    new WildcardSCMHeadFilterTrait("*", "ignore"),
                    new RegexSCMHeadFilterTrait("i.*"),
                    new WildcardSCMSourceFilterTrait("i*", "ignored"),
                    new RegexSCMSourceFilterTrait("ig.*")
            );
            assertThat(DescribableModel.uninstantiate2_(instance).toString(), allOf(
                    startsWith("@"),
                    not(containsString(", $")),
                    not(containsString("=$")),
                    not(containsString("[$")),
                    not(containsString("{$"))
            ));
            assertThat(DescribableModel.uninstantiate2_(instance).toString(), is("@mockScm("
                    + "controllerId=" + c.getId() + ",traits=["
                    + "@discoverBranches$MockSCMDiscoverBranches(), "
                    + "@discoverChangeRequests$MockSCMDiscoverChangeRequests(strategiesStr=HEAD, ), "
                    + "@discoverTags$MockSCMDiscoverTags(), "
                    + "@headWildcardFilter$WildcardSCMHeadFilterTrait(excludes=ignore,includes=*), "
                    + "@headRegexFilter$RegexSCMHeadFilterTrait(regex=i.*), "
                    + "@sourceWildcardFilter$WildcardSCMSourceFilterTrait(excludes=ignored,includes=i*), "
                    + "@sourceRegexFilter$RegexSCMSourceFilterTrait(regex=ig.*)"
                    + "]"
                    + ")"));
        }
    }

    @Test
    public void given__singleScmNavigator__when__uninstantiating__then__noRaw$class() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("test");
            SingleSCMNavigator instance = new SingleSCMNavigator("foo",
                    Collections.singletonList(new MockSCMSource(c, "test"))
            );
            assertThat(DescribableModel.uninstantiate2_(instance).toString(), allOf(
                    startsWith("@"),
                    not(containsString(", $")),
                    not(containsString("=$")),
                    not(containsString("[$")),
                    not(containsString("{$"))
            ));
            assertThat(DescribableModel.uninstantiate2_(instance).toString(), is("@fromSource(name=foo,"
                    + "sources=[@mockScm$MockSCMSource("
                    + "controllerId=" + c.getId() + ","
                    + "id=" + instance.getSources().get(0).getId() + ","
                    + "repository=test,"
                    + "traits=[]"
                    + ")"
                    + "]"
                    + ")"));
        }
    }

    @Test
    public void given__singleScmSource__when__uninstantiating__then__noRaw$class() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("test");
            SingleSCMSource instance = new SingleSCMSource("foo", "foo", new MockSCM(c, "test",
                    new MockSCMHead("master"), new MockSCMRevision(
                    new MockSCMHead("master"), c.getRevision("test", "master")))
            );
            assertThat(DescribableModel.uninstantiate2_(instance).toString(), allOf(
                    startsWith("@"),
                    not(containsString(", $")),
                    not(containsString("=$")),
                    not(containsString("[$")),
                    not(containsString("{$"))
            ));
            assertThat(DescribableModel.uninstantiate2_(instance).toString(), is("@fromScm("
                    + "id=foo,"
                    + "name=foo,"
                    + "scm=@mockScm$MockSCM("
                    + "controllerId=" + c.getId() + ","
                    + "head=master,"
                    + "repository=test,"
                    + "revision=" + c.getRevision("test", "master")
                    + ")"
                    + ")"));
        }
    }

}
