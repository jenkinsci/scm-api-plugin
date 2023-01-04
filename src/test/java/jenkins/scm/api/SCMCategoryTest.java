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

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import jenkins.scm.api.mixin.TagSCMHead;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.TagSCMHeadCategory;
import jenkins.scm.impl.UncategorizedSCMHeadCategory;
import jenkins.util.NonLocalizable;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.jvnet.localizer.Localizable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class SCMCategoryTest {
    @Test
    public void toDisplayName() throws Exception {
        assertThat(SCMCategory.toDisplayName(UncategorizedSCMHeadCategory.DEFAULT, ChangeRequestSCMHeadCategory.DEFAULT,
                TagSCMHeadCategory.DEFAULT).toString(
                Locale.ENGLISH), is("Branches / Change requests / Tags"));
        assertThat(SCMCategory.toDisplayName(UncategorizedSCMHeadCategory.DEFAULT,
                TagSCMHeadCategory.DEFAULT).toString(
                Locale.ENGLISH), is("Branches / Tags"));
        assertThat(SCMCategory.toDisplayName(UncategorizedSCMHeadCategory.DEFAULT).toString(
                Locale.ENGLISH), is("Branches"));
    }

    @Test
    public void toDisplayName1() throws Exception {
        assertThat(SCMCategory.toDisplayName(Arrays.asList(ChangeRequestSCMHeadCategory.DEFAULT,
                TagSCMHeadCategory.DEFAULT, UncategorizedSCMHeadCategory.DEFAULT)).toString(
                Locale.ENGLISH), is("Branches / Change requests / Tags"));

    }

    @Test
    public void toShortUrl() throws Exception {
        assertThat(SCMCategory.toShortUrl(UncategorizedSCMHeadCategory.DEFAULT, ChangeRequestSCMHeadCategory.DEFAULT,
                TagSCMHeadCategory.DEFAULT), is("change-requests_default_tags"));

    }

    @Test
    public void toShortUrl1() throws Exception {
        assertThat(SCMCategory.toShortUrl(Arrays.asList(ChangeRequestSCMHeadCategory.DEFAULT,
                TagSCMHeadCategory.DEFAULT, UncategorizedSCMHeadCategory.DEFAULT)), is("change-requests_default_tags"));
        assertThat(SCMCategory.toShortUrl(Arrays.asList(TagSCMHeadCategory.DEFAULT, UncategorizedSCMHeadCategory.DEFAULT)), is("default_tags"));
        assertThat(SCMCategory.toShortUrl(Collections.singletonList(TagSCMHeadCategory.DEFAULT)), is("tags"));

    }

    @Test
    public void group() throws Exception {
        UncategorizedSCMHeadCategory u1 = UncategorizedSCMHeadCategory.DEFAULT;
        UncategorizedSCMHeadCategory u2 = new UncategorizedSCMHeadCategory(new NonLocalizable("foo"));
        ChangeRequestSCMHeadCategory c1 = ChangeRequestSCMHeadCategory.DEFAULT;
        ChangeRequestSCMHeadCategory c2 = new ChangeRequestSCMHeadCategory(new NonLocalizable("bar"));
        ChangeRequestSCMHeadCategory c3 = new ChangeRequestSCMHeadCategory(new NonLocalizable("manchu"));
        Map<String, List<SCMHeadCategory>> result = SCMCategory.group(u1, c1, c2, c3, u2);
        assertThat(result.entrySet(), hasSize(2));
        assertThat(result.get("default"), Matchers.<SCMCategory>containsInAnyOrder(u1, u2));
        assertThat(result.get("change-requests"), Matchers.<SCMCategory>containsInAnyOrder(c1, c2, c3));
    }

    @Test
    public void group1() throws Exception {
        UncategorizedSCMHeadCategory u1 = UncategorizedSCMHeadCategory.DEFAULT;
        UncategorizedSCMHeadCategory u2 = new UncategorizedSCMHeadCategory(new NonLocalizable("foo"));
        ChangeRequestSCMHeadCategory c1 = ChangeRequestSCMHeadCategory.DEFAULT;
        ChangeRequestSCMHeadCategory c2 = new ChangeRequestSCMHeadCategory(new NonLocalizable("bar"));
        ChangeRequestSCMHeadCategory c3 = new ChangeRequestSCMHeadCategory(new NonLocalizable("manchu"));
        TagSCMHeadCategory t1 = new TagSCMHeadCategory(new NonLocalizable("foomanchu"));
        Map<String, List<SCMHeadCategory>> result = SCMCategory.group(Arrays.asList(u1, c1, t1, c2, c3, u2));
        assertThat(result.entrySet(), hasSize(3));
        assertThat(result.get("default"), Matchers.<SCMCategory>containsInAnyOrder(u1, u2));
        assertThat(result.get("change-requests"), Matchers.<SCMCategory>containsInAnyOrder(c1, c2, c3));
        assertThat(result.get("tags"), Matchers.<SCMCategory>contains(t1));

    }

    @Test
    public void getName() throws Exception {
        assertThat(new MySCMCategory("foomanchu", new NonLocalizable("Fu Manchu"), new NonLocalizable("Fu Manchu")).getName(), is("foomanchu"));
    }

    @Test
    public void getDisplayName() throws Exception {
        assertThat(new MySCMCategory("foomanchu", new NonLocalizable("Fu Manchu"), new NonLocalizable("Fu Manchu")).getDisplayName().toString(Locale.ENGLISH), is("Fu Manchu"));
    }

    @Test
    public void defaultDisplayName() throws Exception {
        assertThat(new MySCMCategory("foomanchu", null, new NonLocalizable("Fu Manchu"))
                .getDisplayName().toString(Locale.ENGLISH), is("Fu Manchu"));
    }

    @Test
    public void isMatch() throws Exception {
        UncategorizedSCMHeadCategory u = UncategorizedSCMHeadCategory.DEFAULT;
        ChangeRequestSCMHeadCategory c = ChangeRequestSCMHeadCategory.DEFAULT;
        TagSCMHeadCategory t = TagSCMHeadCategory.DEFAULT;
        final SCMHead mh = new SCMHead("basic");
        class TagSCMHeadImpl extends SCMHead implements TagSCMHead {
            public TagSCMHeadImpl(@NonNull String name) {
                super(name);
            }

            @Override
            public long getTimestamp() {
                return 0;
            }
        }
        SCMHead th = new TagSCMHeadImpl("basic");
        class ChangeRequestSCMHeadImpl extends SCMHead implements ChangeRequestSCMHead {
            public ChangeRequestSCMHeadImpl(@NonNull String name) {
                super(name);
            }

            @NonNull
            @Override
            public String getId() {
                return "mock";
            }

            @NonNull
            @Override
            public SCMHead getTarget() {
                return mh;
            }
        }
        SCMHead ch = new ChangeRequestSCMHeadImpl("basic");
        assertThat(u.isMatch(mh, Arrays.asList(u, c, t)), is(true));
        assertThat(u.isMatch(ch, Arrays.asList(c, u, t)), is(false));
        assertThat(u.isMatch(th, Arrays.asList(c, u, t)), is(false));

        assertThat(c.isMatch(mh, Arrays.asList(u, c, t)), is(false));
        assertThat(c.isMatch(ch, Arrays.asList(c, u, t)), is(true));
        assertThat(c.isMatch(th, Arrays.asList(c, u, t)), is(false));

        assertThat(t.isMatch(mh, Arrays.asList(u, c, t)), is(false));
        assertThat(t.isMatch(ch, Arrays.asList(c, u, t)), is(false));
        assertThat(t.isMatch(th, Arrays.asList(c, u, t)), is(true));

    }

    private static class MySCMCategory extends SCMCategory<Object> {

        private NonLocalizable defaultDisplayName;

        public MySCMCategory(String name, NonLocalizable displayName, NonLocalizable defaultDisplayName) {
            super(name, displayName);
            this.defaultDisplayName = defaultDisplayName;
        }

        @NonNull
        @Override
        protected Localizable defaultDisplayName() {
            return defaultDisplayName;
        }

        @Override
        public boolean isMatch(@NonNull Object instance) {
            return instance.hashCode() % 31 == 7;
        }
    }
}
