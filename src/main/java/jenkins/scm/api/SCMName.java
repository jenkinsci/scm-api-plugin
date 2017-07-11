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

import com.google.common.net.InternetDomainName;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang.StringUtils;

/**
 * Utility class to help with naming SCM related things.
 *
 * @since 2.2.0
 */
public final class SCMName {

    /**
     * Utility class
     */
    private SCMName() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Makes best effort to guess a "sensible" display name from the hostname in the URL.
     *
     * @param url the URL.
     * @return the display name or {@code null}
     */
    public static String fromUrl(@NonNull String url, @CheckForNull String... ignoredPrefixes) {
        try {
            return innerFromUrl(url, ignoredPrefixes.length == 0 ? null : Arrays.asList(ignoredPrefixes));
        } catch (LinkageError e) {
            return null;
        }
    }

    /**
     * Makes best effort to guess a "sensible" display name from the hostname in the URL.
     *
     * @param url the URL.
     * @return the display name or {@code null}
     */
    public static String fromUrl(@NonNull String url, @CheckForNull List<String> ignoredPrefixes) {
        try {
            return innerFromUrl(url, ignoredPrefixes);
        } catch (LinkageError e) {
            return null;
        }
    }

    /**
     * Makes best effort to guess a "sensible" display name from the hostname in the URL.
     *
     * @param url the URL.
     * @return the display name or {@code null}
     * @throws LinkageError if Guava changes their API that we have depended on.
     */
    @CheckForNull
    private static String innerFromUrl(@NonNull String url, List<String> ignoredPrefixes) throws LinkageError {
        String hostName;
        try {
            URL serverUri = new URL(url);
            hostName = serverUri.getHost();
            if (StringUtils.isBlank(hostName)) {
                return null;
            }
            // let's see if we can make this more "friendly"
            InternetDomainName host = InternetDomainName.from(IDN.toASCII(hostName));
            if (host.hasPublicSuffix()) {
                String publicName = host.publicSuffix().name();
                hostName = StringUtils.removeEnd(StringUtils.removeEnd(host.name(), publicName), ".")
                        .toLowerCase(Locale.ENGLISH);
            } else {
                hostName = StringUtils.removeEnd(host.name(), ".").toLowerCase(Locale.ENGLISH);
            }
            if (ignoredPrefixes != null) {
                for (String prefix : ignoredPrefixes) {
                    if (prefix.endsWith(".")) {
                        if (hostName.startsWith(prefix)) {
                            hostName = hostName.substring(prefix.length());
                            break;
                        }
                    } else {
                        if (hostName.startsWith(prefix + ".")) {
                            hostName = hostName.substring(prefix.length() + 1);
                            break;
                        }
                    }
                }
            }
            if (StringUtils.isNotBlank(hostName)) {
                hostName = IDN.toUnicode(hostName).replace('.', ' ').replace('-', ' ');
            }
        } catch (MalformedURLException e) {
            // ignore, best effort
            hostName = null;
        }
        return hostName;
    }
}
