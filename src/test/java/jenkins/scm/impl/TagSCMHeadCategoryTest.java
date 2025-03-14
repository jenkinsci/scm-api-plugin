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

package jenkins.scm.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.stream.Stream;
import jenkins.scm.impl.mock.MockChangeRequestSCMHead;
import jenkins.scm.impl.mock.MockSCMHead;
import jenkins.scm.impl.mock.MockTagSCMHead;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TagSCMHeadCategoryTest {

    static Stream<Arguments> instance() {
        return Stream.of(
                Arguments.of(Named.of("default", TagSCMHeadCategory.DEFAULT)),
                Arguments.of(Named.of("custom", new TagSCMHeadCategory(Messages._TagSCMHeadCategory_DisplayName()))));
    }

    @ParameterizedTest
    @MethodSource("instance")
    void given_tagHead_when_isMatch_then_confirmMatch(TagSCMHeadCategory instance) {
        assertThat(instance.isMatch(new MockTagSCMHead("1.0", 0L)), is(true));
    }

    @ParameterizedTest
    @MethodSource("instance")
    void given_regularHead_when_isMatch_then_rejectMatch(TagSCMHeadCategory instance) {
        assertThat(instance.isMatch(new MockSCMHead("master")), is(false));
    }

    @ParameterizedTest
    @MethodSource("instance")
    void given_changeRequestHead_when_isMatch_then_rejectMatch(TagSCMHeadCategory instance) {
        assertThat(instance.isMatch(new MockChangeRequestSCMHead(1, "master")), is(false));
    }
}
