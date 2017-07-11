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

package jenkins.scm.impl.avatars;

import hudson.ExtensionList;
import hudson.model.UnprotectedRootAction;
import hudson.util.HttpResponses;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.HttpResponse;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class AvatarCacheTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Test
    public void fakeUrl() throws Exception {
        // start by adding a block of tasks to the worker so we can prevent the image from being fetched
        CountDownLatch requestsPending = new CountDownLatch(AvatarCache.CONCURRENT_REQUEST_LIMIT + 1);
        CountDownLatch blockReady = new CountDownLatch(AvatarCache.CONCURRENT_REQUEST_LIMIT);
        for (Callable<Void> c : fakeWork(requestsPending, blockReady)) {
            worker().submit(c);
        }
        blockReady.await(10, TimeUnit.SECONDS);

        // now we generate the URL
        String url = callBuildUrl("about:blank", "32x32");
        assertThat(url, is(r.getURL().toString() + "avatar-cache/0ffc09cf03c14f063afa7f03a5f4b074.png?size=32x32"));

        // now we can test the pending responses

        // check that non-http or https urls are given a "final" response
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.connect();
            assertThat(c.getResponseCode(), is(200));
            assertThat(c.getLastModified(), greaterThan(0L));
            assertThat(c.getHeaderField("Cache-Control"), is("max-age=365000000, immutable, public"));
        } finally {
            c.disconnect();
        }

        // now release the block
        requestsPending.countDown();

        // wait for the queue to empty
        CountDownLatch requestsComplete = new CountDownLatch(AvatarCache.CONCURRENT_REQUEST_LIMIT);
        worker().invokeAll(fakeWork(requestsComplete, blockReady));

        // now check that we have still been given a "final" response
        String lastModified;
        c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.connect();
            assertThat(c.getResponseCode(), is(200));
            lastModified = c.getHeaderField("Last-Modified");
            assertThat(c.getLastModified(), greaterThan(0L));
            assertThat(c.getHeaderField("Cache-Control"), is("max-age=365000000, immutable, public"));
        } finally {
            c.disconnect();
        }
        // now check the If-Last-Modified behaviour
        c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.setRequestProperty("If-Modified-Since", lastModified);
            c.connect();
            assertThat(c.getResponseCode(), is(304));
            assertThat(c.getLastModified(), greaterThan(0L));
            assertThat(c.getHeaderField("Cache-Control"), is("max-age=365000000, immutable, public"));
        } finally {
            c.disconnect();
        }
    }

    @Test
    public void realUrl() throws Exception {
        // start by adding a block of tasks to the worker so we can prevent the image from being fetched
        CountDownLatch requestsPending = new CountDownLatch(AvatarCache.CONCURRENT_REQUEST_LIMIT + 1);
        CountDownLatch blockReady = new CountDownLatch(AvatarCache.CONCURRENT_REQUEST_LIMIT);
        for (Callable<Void> c : fakeWork(requestsPending, blockReady)) {
            worker().submit(c);
        }
        blockReady.await(10, TimeUnit.SECONDS);

        // now we generate the URL
        String url = callBuildUrl(r.getURL().toString() + "images/24x24/search.png", "32x32");
        assertThat(url, allOf(startsWith(r.getURL().toString() + "avatar-cache/"), endsWith(".png?size=32x32")));

        // now we can test the pending responses

        // check that http urls are given a "temporary" response
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.connect();
            assertThat(c.getResponseCode(), is(200));
            assertThat(c.getLastModified(), is(0L));
            assertThat(c.getHeaderField("Cache-Control"), is("no-cache, public"));
        } finally {
            c.disconnect();
        }

        // now release the block
        requestsPending.countDown();

        // wait for the queue to empty
        CountDownLatch requestsComplete = new CountDownLatch(AvatarCache.CONCURRENT_REQUEST_LIMIT);
        worker().invokeAll(fakeWork(requestsComplete, blockReady));

        // now check that we have been given a "real" response
        String lastModified;
        c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.connect();
            assertThat(c.getResponseCode(), is(200));
            lastModified = c.getHeaderField("Last-Modified");
            assertThat(c.getLastModified(), greaterThan(0L));
            assertThat(c.getHeaderField("Cache-Control"), is("max-age=3600, public"));
        } finally {
            c.disconnect();
        }

        // now check the If-Last-Modified behaviour
        c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.setRequestProperty("If-Modified-Since", lastModified);
            c.connect();
            assertThat(c.getResponseCode(), is(304));
            assertThat(c.getLastModified(), greaterThan(0L));
            assertThat(c.getHeaderField("Cache-Control"), is("max-age=3600, public"));
        } finally {
            c.disconnect();
        }
    }

    private ExecutorService worker() {
        return ExtensionList.lookup(UnprotectedRootAction.class).get(AvatarCache.class).service;
    }

    private List<Callable<Void>> fakeWork(final CountDownLatch latch, final CountDownLatch running) {
        final List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < AvatarCache.CONCURRENT_REQUEST_LIMIT; i++) {
            tasks.add(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    running.countDown();
                    latch.countDown();
                    latch.await(10, TimeUnit.SECONDS);
                    return null;
                }
            });
        }
        return tasks;
    }

    /**
     * Invokes {@link AvatarCache#buildUrl(String, String)} from a request thread.
     *
     * @param url  the url.
     * @param size the size.
     * @return the response
     * @throws IOException if there is an error.
     */
    public String callBuildUrl(String url, String size) throws IOException {
        ProbeAction a = ExtensionList.lookup(UnprotectedRootAction.class).get(ProbeAction.class);
        assertThat(a, notNullValue());
        synchronized (a) {
            a.url = url;
            a.size = size;
            HttpURLConnection c =
                    (HttpURLConnection) new URL(r.getURL().toString() + a.getUrlName() + "/").openConnection();
            try {
                c.connect();
                return IOUtils.toString(c.getInputStream(), "UTF-8").trim();
            } finally {
                c.disconnect();
            }
        }
    }

    @TestExtension
    public static class ProbeAction implements UnprotectedRootAction {

        private String url;
        private String size;

        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getUrlName() {
            return "probe-action";
        }

        public HttpResponse doIndex() {
            return HttpResponses.plainText(AvatarCache.buildUrl(url, size));
        }
    }

}
