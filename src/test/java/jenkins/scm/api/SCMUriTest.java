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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collections;
import org.junit.jupiter.api.Test;

class SCMUriTest {

    @Test
    void given__url_with_custom_scheme__when__normalizing__then__custom_default_port_elided() {
        assertThat(
                SCMUri.normalize("myscm://myscm.EXAMPLE.COM:6352/", Collections.singletonMap("myscm", 6352)),
                is("myscm://myscm.example.com"));
        assertThat(SCMUri.normalize("myscm://myscm.EXAMPLE.COM:6352/"), is("myscm://myscm.example.com:6352"));
    }

    @Test
    void given__url_without_hostname__when__normalizing__then__no_name_inferred() {
        assertThat(SCMUri.normalize("FILE:///some/random/file"), is("file:///some/random/file"));
    }

    @Test
    void given__normalized_url__when__normalizing__then__verbatim() {
        assertThat(SCMUri.normalize("https://example.com/foo"), is("https://example.com/foo"));
        assertThat(SCMUri.normalize("https://example.com:8443/foo"), is("https://example.com:8443/foo"));
        assertThat(SCMUri.normalize("https://bob@example.com/foo"), is("https://bob@example.com/foo"));
        assertThat(SCMUri.normalize("https://bob:pass@example.com/foo"), is("https://bob:pass@example.com/foo"));
        assertThat(SCMUri.normalize("https://bob%25smith@example.com/foo"), is("https://bob%25smith@example.com/foo"));
        assertThat(
                SCMUri.normalize("https://bob%25smith:pass@example.com/foo"),
                is("https://bob%25smith:pass@example.com/foo"));
    }

    @Test
    void given__url_with_punycode__when__normalizing__then__punycode_retained() {
        assertThat(
                SCMUri.normalize("http://xn--e1afmkfd.xn--p1ai/"),
                is("http://xn--e1afmkfd.xn--p1ai" /*http://пример.рф*/));
    }

    @Test
    void given__url_with_idn__when__normalizing__then__hostname_is_decoded() {
        assertThat(
                SCMUri.normalize("http://\u043F\u0440\u0438\u043C\u0435\u0440.\u0440\u0444/"), /*пример.рф*/
                is("http://xn--e1afmkfd.xn--p1ai"));
    }

    @Test
    void given__url_with_idn_and_port__when__normalizing__then__hostname_is_decoded() {
        assertThat(
                SCMUri.normalize("http://\u043F\u0440\u0438\u043C\u0435\u0440.\u0440\u0444:8080/"), /*пример.рф*/
                is("http://xn--e1afmkfd.xn--p1ai:8080"));
    }

    @Test
    void given__url_with_user_and_idn__when__normalizing__then__hostname_is_decoded() {
        assertThat(
                SCMUri.normalize("http://bob@\u043F\u0440\u0438\u043C\u0435\u0440.\u0440\u0444:8080/"), /*пример.рф*/
                is("http://bob@xn--e1afmkfd.xn--p1ai:8080"));
    }

    @Test
    void given__url_with_userpass_and_idn__when__normalizing__then__hostname_is_decoded() {
        assertThat(SCMUri.normalize("http://bob:secret@пример.рф/"), is("http://bob:secret@xn--e1afmkfd.xn--p1ai"));
    }

    @Test
    void given__url_with_userpass_and_idn_and_port__when__normalizing__then__hostname_is_decoded() {
        assertThat(
                SCMUri.normalize("http://bob:secret@пример.рф:8080/"),
                is("http://bob:secret@xn--e1afmkfd.xn--p1ai:8080"));
    }

    @Test
    void given__url_with_everything_messed_up__when__normalizing__then__everything_is_fixed() {
        // the scheme is upper case, we expect that to be fixed
        // the user part should be ...
        // the host is IDN so that should be fixed
        // the port here is ๔๔๓ which is the Thai digits for 443, and as the scheme is https
        // the path should be normalized
        // the trailing slash should be removed
        assertThat(
                SCMUri.normalize(
                        "HTTPS://böb:seçret@\u043F\u0440\u0438\u043C\u0435\u0440.\u0440\u0444:\u0E54\u0E54\u0E53/foo/../"),
                is("https://b%C3%B6b:se%C3%A7ret@xn--e1afmkfd.xn--p1ai"));
    }
}
