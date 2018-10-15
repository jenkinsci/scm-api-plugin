/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
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

package jenkins.scm.impl.trait;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMDiscoverBranches;
import jenkins.scm.impl.mock.MockSCMSource;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class WildcardSCMHeadFilterTraitTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Test
    public void given_sourceWithIncludeWildcardRule_when_scanning_then_ruleApplied() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            c.createBranch("foo", "fork");
            c.createBranch("foo", "alt");
            MockSCMSource src = new MockSCMSource(c, "foo", new MockSCMDiscoverBranches(), new WildcardSCMHeadFilterTrait("master fo*", ""));
            Map<SCMHead, SCMRevision> result = src.fetch(null, SCMHeadObserver.collect(), null, null).result();
            Set<String> names = new TreeSet<String>();
            for (SCMHead h: result.keySet()) {
                names.add(h.getName());
            }
            assertThat(names, containsInAnyOrder("master", "fork"));
        }
    }

    @Test
    public void given_sourceWithExcludeWildcardRule_when_scanning_then_ruleApplied() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            c.createBranch("foo", "fork");
            c.createBranch("foo", "alt");
            MockSCMSource src = new MockSCMSource(c, "foo", new MockSCMDiscoverBranches(), new WildcardSCMHeadFilterTrait("*", "fo*"));
            Map<SCMHead, SCMRevision> result = src.fetch(null, SCMHeadObserver.collect(), null, null).result();
            Set<String> names = new TreeSet<String>();
            for (SCMHead h: result.keySet()) {
                names.add(h.getName());
            }
            assertThat(names, containsInAnyOrder("master", "alt"));
        }
    }
    @Test
    public void given_sourceWithIncludeExcludeWildcardRule_when_scanning_then_ruleApplied() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            c.createBranch("foo", "fork");
            c.createBranch("foo", "foo");
            c.createBranch("foo", "alt");
            MockSCMSource src = new MockSCMSource(c, "foo", new MockSCMDiscoverBranches(), new WildcardSCMHeadFilterTrait("master fo*", "foo"));
            Map<SCMHead, SCMRevision> result = src.fetch(null, SCMHeadObserver.collect(), null, null).result();
            Set<String> names = new TreeSet<String>();
            for (SCMHead h: result.keySet()) {
                names.add(h.getName());
            }
            assertThat(names, containsInAnyOrder("master", "fork"));
        }
    }
}
