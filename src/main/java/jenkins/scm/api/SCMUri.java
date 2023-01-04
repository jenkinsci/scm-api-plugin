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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class to help with SCM URI related things.
 *
 * @since 2.2.5
 */
public final class SCMUri {

    /**
     * The default ports for common URI schemes that it is safe to perform hostname normalization on.
     */
    private static final Map<String, Integer> commonDefaultPorts;

    /**
     * Utility class
     */
    private SCMUri() {
        throw new IllegalAccessError("Utility class");
    }

    static {
        commonDefaultPorts = Map.of("ftp", 21, "ssh", 22, "telnet", 22, "http", 80, "https", 443);
    }

    /**
     * Returns the common default ports.
     *
     * @return the common default ports.
     */
    public static Map<String, Integer> commonDefaultPorts() {
        return commonDefaultPorts;
    }

    /**
     * Normalize an URI. Normalization will apply the following changes:
     * <ul>
     * <li>The scheme will be forced to lowercase (scheme is case insensitive, so should not matter)</li>
     * <li>For schemes that use a hostname or user authority plus hostname format (such as {@code ssh},
     * {@code http} and {@code https} The hostname will be converted from IDNA to ASCII if necessary and forced to
     * lowercase and the PCT-encoding of the user authority will be enforced (note that this may affect the case where
     * a username in a user authority contains a {@code :} as the {@link URI} class does not provide a means to
     * destructure and restructure a URI without encoding/decoding the username, consequently the {@code %3A} will
     * be converted to {@code :} and not converted back)</li>
     * <li>Any redundant path navigation segments will be removed, for example a path of {@code /foo/../bar} will
     * be normalized to {@code /bar}</li>
     * <li>Any trailing {@code /} will be removed</li>
     * </ul>
     *
     * @param uri the URI.
     * @return the normalized api URI.
     */
    @CheckForNull
    public static String normalize(@CheckForNull String uri) {
        return normalize(uri, commonDefaultPorts);
    }

    /**
     * Normalize an URI. Normalization will apply the following changes:
     * <ul>
     * <li>The scheme will be forced to lowercase (scheme is case insensitive, so should not matter)</li>
     * <li>For schemes in the supplied default ports that use a hostname or user authority plus hostname format (such
     * as {@code ssh},
     * {@code http} and {@code https} The hostname will be converted from IDNA to ASCII if necessary and forced to
     * lowercase and the PCT-encoding of the user authority will be enforced (note that this may affect the case where
     * a username in a user authority contains a {@code :} as the {@link URI} class does not provide a means to
     * destructure and restructure a URI without encoding/decoding the username, consequently the {@code %3A} will
     * be converted to {@code :} and not converted back)</li>
     * <li>Any redundant path navigation segments will be removed, for example a path of {@code /foo/../bar} will
     * be normalized to {@code /bar}</li>
     * <li>Any trailing {@code /} will be removed</li>
     * </ul>
     *
     * @param uri the URI
     * @param defaultPorts the URI scheme to default port mapping (a varargs list to make combination easier). Normally
     *                     the caller would use {@link #commonDefaultPorts()} as one of the arguments.
     * @return the normalized api URI.
     */
    @CheckForNull
    public static String normalize(@CheckForNull String uri, Map<String, Integer>... defaultPorts) {
        if (uri == null) {
            return null;
        }
        try {
            URI u = new URI(uri).normalize();
            String scheme = u.getScheme().toLowerCase(Locale.ENGLISH);
            Integer defaultPort = null;
            for (Map<String, Integer> ports : defaultPorts) {
                defaultPort = ports.get(scheme);
                if (defaultPort != null) {
                    break;
                }
            }
            if (defaultPort != null) {
                // this is a scheme where we know the authority is server based, i.e. [userinfo@]server[:port]
                // DNS names must be US-ASCII and are case insensitive, so we force all to lowercase

                String host = u.getHost();
                if (host != null) {
                    host = host.toLowerCase(Locale.ENGLISH);
                    int port = u.getPort();
                    uri = new URI(
                            scheme,
                            u.getUserInfo(),
                            host,
                            port == defaultPort ? -1 : port,
                            u.getPath(),
                            u.getQuery(),
                            u.getFragment()
                    ).toASCIIString();
                } else {
                    String authority = u.getAuthority();
                    if (authority != null) {
                        int at = authority.indexOf('@');
                        int colon = authority.lastIndexOf(':');
                        if (colon != -1 && at > colon) {
                            // if the @ is after the last : then the : is not prefixing a port, rather it is probably
                            // the password
                            colon = -1;
                        }
                        if (colon != -1) {
                            for (int i = colon + 1; i < authority.length(); i++) {
                                if (!Character.isDigit(authority.codePointAt(i))) {
                                    colon = -1;
                                    break;
                                }
                            }
                        }
                        String userPart;
                        String userSep;
                        String hostPart;
                        String portSep;
                        String portPart;
                        if (at == -1) {
                            userPart = "";
                            userSep = "";
                        } else {
                            userPart = authority.substring(0, at);
                            userSep = "@";
                        }
                        if (colon == -1) {
                            portSep = "";
                            portPart = "";
                        } else {
                            int port = Integer.parseInt(authority.substring(colon + 1));
                            if (port != defaultPort) {
                                portSep = ":";
                                portPart = Integer.toString(port);
                            } else {
                                portSep = "";
                                portPart = "";
                            }
                        }
                        hostPart = IDN.toASCII(authority.substring(at + 1, colon == -1 ? authority.length() : colon))
                                .toLowerCase(Locale.ENGLISH);
                        authority = userPart + userSep + hostPart + portSep + portPart;
                    }
                    uri = new URI(scheme,
                            authority,
                            u.getPath(), u.getQuery(),
                            u.getFragment()).toASCIIString();
                }
            } else {
                String host = u.getHost();
                if (u.getHost() != null) {
                    host = host.toLowerCase(Locale.ENGLISH);
                    uri = new URI(
                            scheme,
                            u.getUserInfo(),
                            host,
                            u.getPort(),
                            u.getPath(),
                            u.getQuery(),
                            u.getFragment()
                    ).toASCIIString();
                } else {
                    uri = new URI(scheme,
                            u.getSchemeSpecificPart(),
                            u.getFragment()).toASCIIString();
                }
            }
        } catch (URISyntaxException e) {
            // ignore, this was a best effort tidy-up
        }
        return uri.replaceAll("/$", "");
    }

}
