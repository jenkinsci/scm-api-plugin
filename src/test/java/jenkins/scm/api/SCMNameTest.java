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
 */

package jenkins.scm.api;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

public class SCMNameTest {
    @Test
    public void given__url_without_hostname__when__naming__then__no_name_inferred() throws Exception {
        assertThat(SCMName.fromUrl("file:///some/random/file"), is(nullValue()));
    }

    @Test
    public void given__url_with_hostname__when__naming__then__public_tld_removed() throws Exception {
        assertThat(SCMName.fromUrl("http://scm.example.com"), is("scm example"));
    }

    @Test
    public void given__url_with_hostname__when__naming__then__public_sld_removed() throws Exception {
        assertThat(SCMName.fromUrl("http://scm.example.co.uk"), is("scm example"));
    }

    @Test
    public void given__url_with_hostname__when__naming__then__prefix_is_removed() throws Exception {
        assertThat(SCMName.fromUrl("http://scm.example.ie", "scm"), is("example"));
    }

    @Test
    public void given__url_with_punycode__when__naming__then__hostname_is_decoded() throws Exception {
        assertThat(SCMName.fromUrl("http://xn--e1afmkfd.xn--p1ai/"), is("пример"));
    }

    @Test
    public void given__url_with_idn__when__naming__then__punycode_is_roundtripped() throws Exception {
        assertThat(SCMName.fromUrl("http://\u043F\u0440\u0438\u043C\u0435\u0440.\u0440\u0444" /*пример.рф*/),
                is("\u043F\u0440\u0438\u043C\u0435\u0440" /*пример*/));
    }

    @Test
    public void given__url_with_idn__when__naming__then__punycode_is_roundtripped2() throws Exception {
        assertThat(SCMName.fromUrl("http://\u4F8B\u5B50.\u4E2D\u56FD/"), is("\u4F8B\u5B50"));
    }

}
