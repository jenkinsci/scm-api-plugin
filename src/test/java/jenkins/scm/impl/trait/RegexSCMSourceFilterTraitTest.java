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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

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
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class RegexSCMSourceFilterTraitTest {

    @Test
    void given_navigatorWithIncludeRegexRule_when_scanning_then_ruleApplied(JenkinsRule r) throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            c.createRepository("bar");
            c.createRepository("manchu");
            final MockSCMNavigator src = new MockSCMNavigator(c, new RegexSCMSourceFilterTrait("[fb].+"));
            SimpleSCMSourceObserver observer = new SimpleSCMSourceObserver();
            src.visitSources(observer);
            assertThat(observer.getNames(), containsInAnyOrder("foo", "bar"));
        }
    }

    private static class SimpleSCMSourceObserver extends SCMSourceObserver {
        Set<String> names = new HashSet<>();
        LogTaskListener listener =
                new LogTaskListener(Logger.getLogger(RegexSCMSourceFilterTrait.class.getName()), Level.INFO);

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
                throws IllegalArgumentException, ClassCastException {}

        public Set<String> getNames() {
            return names;
        }
    }
}
