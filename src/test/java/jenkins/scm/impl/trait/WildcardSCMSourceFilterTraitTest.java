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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.model.TaskListener;
import hudson.util.LogTaskListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.impl.NoOpProjectObserver;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMNavigator;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class WildcardSCMSourceFilterTraitTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Test
    public void given_navigatorWithIncludeWildcardRule_when_scanning_then_ruleApplied() throws Exception {
        MockSCMController c = MockSCMController.create();
        try {
            c.createRepository("foo");
            c.createRepository("bar");
            c.createRepository("manchu");
            final MockSCMNavigator src = new MockSCMNavigator(c, new WildcardSCMSourceFilterTrait("foo b*", ""));
            SimpleSCMSourceObserver observer = new SimpleSCMSourceObserver();
            src.visitSources(observer);
            assertThat(observer.getNames(), containsInAnyOrder("foo", "bar"));
        } finally {
            c.close();
        }
    }

    @Test
    public void given_navigatorWithExcludeWildcardRule_when_scanning_then_ruleApplied() throws Exception {
        MockSCMController c = MockSCMController.create();
        try {
            c.createRepository("foo");
            c.createRepository("bar");
            c.createRepository("manchu");
            final MockSCMNavigator src = new MockSCMNavigator(c, new WildcardSCMSourceFilterTrait("*", "fo*"));
            SimpleSCMSourceObserver observer = new SimpleSCMSourceObserver();
            src.visitSources(observer);
            assertThat(observer.getNames(), containsInAnyOrder("bar", "manchu"));
        } finally {
            c.close();
        }
    }
    @Test
    public void given_navigatorWithIncludeExcludeWildcardRule_when_scanning_then_ruleApplied() throws Exception {
        MockSCMController c = MockSCMController.create();
        try {
            c.createRepository("foo");
            c.createRepository("fu");
            c.createRepository("bar");
            c.createRepository("manchu");
            final MockSCMNavigator src = new MockSCMNavigator(c, new WildcardSCMSourceFilterTrait("f* bar", "fo*"));
            SimpleSCMSourceObserver observer = new SimpleSCMSourceObserver();
            src.visitSources(observer);
            assertThat(observer.getNames(), containsInAnyOrder("bar", "fu"));
        } finally {
            c.close();
        }
    }

    private static class SimpleSCMSourceObserver extends SCMSourceObserver {
        Set<String> names = new HashSet<String>();
        LogTaskListener listener =
                new LogTaskListener(Logger.getLogger(WildcardSCMSourceFilterTrait.class.getName()),
                        Level.INFO);

        @NonNull
        @Override
        public SCMSourceOwner getContext() {
            return null;
        }

        @NonNull
        @Override
        public TaskListener getListener() {
            return listener;
        }

        @NonNull
        @Override
        public ProjectObserver observe(@NonNull String projectName)
                throws IllegalArgumentException, IOException, InterruptedException {
            names.add(projectName);
            return NoOpProjectObserver.instance();
        }

        @Override
        public void addAttribute(@NonNull String key, @Nullable Object value)
                throws IllegalArgumentException, ClassCastException {
        }

        public Set<String> getNames() {
            return names;
        }
    }
}
