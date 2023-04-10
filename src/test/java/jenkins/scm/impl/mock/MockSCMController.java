/*
 * The MIT License
 *
 * Copyright (c) 2016-2017 CloudBees, Inc.
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

package jenkins.scm.impl.mock;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.FilePath;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jenkins.scm.api.SCMFile;
import jenkins.scm.api.SCMNavigatorOwner;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jvnet.hudson.test.recipes.LocalData;

public class MockSCMController implements Closeable {

    private static Map<String, MockSCMController> instances = new WeakHashMap<>();

    private final String id;

    private Map<String, Repository> repositories = new TreeMap<>();
    private List<MockFailure> faults = new ArrayList<>();
    private List<MockSCMNavigatorSaveListener> navigatorSaveListeners = new ArrayList<>();
    private List<MockSCMSourceSaveListener> sourceSaveListeners = new ArrayList<>();
    @NonNull
    private MockLatency latency = MockLatency.none();
    private String displayName;
    private String description;
    private String url;
    private String repoIconClassName;
    private String orgIconClassName;

    private MockSCMController() {
        this(UUID.randomUUID().toString());
    }

    public MockSCMController(String id) {
        this.id = id;
    }

    public static MockSCMController create() {
        MockSCMController c = new MockSCMController();
        synchronized (instances) {
            instances.put(c.id, c);
        }
        return c;
    }

    public MockSCMController withLatency(@NonNull MockLatency latency) {
        this.latency = latency;
        return this;
    }

    public MockSCMController withSaveListener(MockSCMNavigatorSaveListener listener) {
        this.navigatorSaveListeners.add(listener);
        return this;
    }

    public MockSCMController withSaveListener(MockSCMSourceSaveListener listener) {
        this.sourceSaveListeners.add(listener);
        return this;
    }

    /**
     * (Re)creates a {@link MockSCMController} for use when you are running a data migration test.
     * It will be the callers responsibility to restore the state of the {@link MockSCMController} accordingly.
     * Only use this if you are using {@link LocalData} which contains references to specific {@link MockSCMController}
     * ids and you need to re-wire up the link.
     *
     * @param id the ID of the controller used when seeding the original data.
     * @return the {@link MockSCMController}.
     * @deprecated (not actually deprecated) a warning that you should only be using this from a test annotated with
     * {@link LocalData}
     */
    @Deprecated // only intended for use from data migration tests where you have a pre-provisioned home
    public static MockSCMController recreate(String id) {
        MockSCMController c = new MockSCMController(id);
        synchronized (instances) {
            if (instances.containsKey(id)) {
                return instances.get(id);
            }
            instances.put(id, c);
        }
        return c;

    }

    public static List<MockSCMController> all() {
        synchronized (instances) {
            return new ArrayList<>(instances.values());
        }
    }

    public static MockSCMController lookup(String id) {
        synchronized (instances) {
            return instances.get(id);
        }
    }

    public String getId() {
        return id;
    }

    @Override
    public void close() {
        synchronized (instances) {
            instances.remove(id);
        }
        repositories.clear();
    }

    public String getRepoIconClassName() {
        return repoIconClassName;
    }

    public void setRepoIconClassName(String repoIconClassName) {
        this.repoIconClassName = repoIconClassName;
    }

    public String getOrgIconClassName() {
        return orgIconClassName;
    }

    public void setOrgIconClassName(String orgIconClassName) {
        this.orgIconClassName = orgIconClassName;
    }

    public String getDescription() throws IOException {
        return description;
    }

    public void setDescription(String description) throws IOException {
        this.description = description;
    }

    public String getDisplayName() throws IOException {
        return displayName;
    }

    public void setDisplayName(String displayName) throws IOException {
        this.displayName = displayName;
    }

    public String getUrl() throws IOException {
        return url;
    }

    public void setUrl(String url) throws IOException {
        this.url = url;
    }

    public synchronized void addFault(MockFailure fault) {
        this.faults.add(fault);
    }

    public synchronized void clearFaults() {
        this.faults.clear();
    }

    public void applyLatency() throws InterruptedException {
        MockLatency latency;
        synchronized (this) {
            latency = this.latency;
        }
        latency.apply();
    }

    public void checkFaults(@CheckForNull String repository, @CheckForNull String branch,
                                         @CheckForNull String revision, boolean actions)
            throws IOException, InterruptedException {
        List<MockFailure> faults;
        synchronized (this) {
            faults = new ArrayList<>(this.faults);
        }
        for (MockFailure fault: faults) {
            fault.check(repository, branch, revision, actions);
        }
    }

    public synchronized void createRepository(String name, MockRepositoryFlags... flags) throws IOException {
        repositories.put(name, new Repository(flags));
        createBranch(name, "master");
    }

    public synchronized void deleteRepository(String name) throws IOException {
        repositories.remove(name);
    }

    public synchronized List<String> listRepositories() throws IOException {
        return new ArrayList<>(repositories.keySet());
    }

    public String getDescription(String repository) throws IOException {
        return resolve(repository).description;
    }

    public Set<MockRepositoryFlags> getFlags(String repository) throws IOException {
        return Collections.unmodifiableSet(resolve(repository).flags);
    }

    public void setDescription(String repository, String description) throws IOException {
        resolve(repository).description = description;
    }

    public String getDisplayName(String repository) throws IOException {
        return resolve(repository).displayName;
    }

    public void setDisplayName(String repository, String displayName) throws IOException {
        resolve(repository).displayName = displayName;
    }

    public String getUrl(String repository) throws IOException {
        return resolve(repository).url;
    }

    public void setUrl(String repository, String url) throws IOException {
        resolve(repository).url = url;
    }

    public synchronized void createBranch(String repository, String branch) throws IOException {
        State state = new State();
        Repository repo = resolve(repository);
        repo.revisions.put(state.getHash(), state);
        repo.heads.put(branch, state.getHash());
    }

    public synchronized void cloneBranch(String repository, String srcBranch, String dstBranch) throws IOException {
        Repository repo = resolve(repository);
        repo.heads.put(dstBranch, repo.heads.get(srcBranch));
    }

    public synchronized void deleteBranch(String repository, String branch) throws IOException {
        resolve(repository).heads.remove(branch);
    }

    public synchronized void setPrimaryBranch(String repository, @CheckForNull String branch) throws IOException {
        Repository repo = resolve(repository);
        repo.primaryBranch = branch;
    }

    public synchronized boolean isPrimaryBranch(String repository, @CheckForNull String branch) throws IOException {
        Repository repo = resolve(repository);
        return StringUtils.equals(repo.primaryBranch, branch);
    }

    @CheckForNull
    public synchronized String getPrimaryBranch(String repository) throws IOException {
        Repository repo = resolve(repository);
        return repo.primaryBranch;
    }

    public synchronized long createTag(String repository, String branch, String tag) throws IOException {
        Repository repo = resolve(repository);
        long timestamp = System.currentTimeMillis();
        repo.tags.put(tag, resolve(repository, branch).getHash());
        repo.tagDates.put(tag, timestamp);
        return timestamp;
    }

    public synchronized void createTag(String repository, String branch, String tag, long when) throws IOException {
        Repository repo = resolve(repository);
        repo.tags.put(tag, resolve(repository, branch).getHash());
        repo.tagDates.put(tag, when);
    }

    public synchronized void deleteTag(String repository, String tag) throws IOException {
        resolve(repository).tags.remove(tag);
        resolve(repository).tagDates.remove(tag);
    }

    public synchronized Integer openChangeRequest(String repository, String branch, MockChangeRequestFlags... flags) throws IOException {
        Repository repo = resolve(repository);
        String hash = resolve(repository, branch).getHash();
        Integer crNum = ++repo.lastChangeRequest;
        repo.changes.put(crNum, hash);
        repo.changeBaselines.put(crNum, branch);
        Set<MockChangeRequestFlags> flagsSet = EnumSet.noneOf(MockChangeRequestFlags.class);
        for (MockChangeRequestFlags flag: flags) {
            if (flag.isApplicable(repo.flags)) {
                flagsSet.add(flag);
            }
        }
        repo.changeFlags.put(crNum, flagsSet);
        return crNum;

    }

    public synchronized void closeChangeRequest(String repository, Integer crNum) throws IOException {
        Repository r = resolve(repository);
        r.changes.remove(crNum);
        r.changeBaselines.remove(crNum);
        r.changeFlags.remove(crNum);
    }

    public synchronized String getTarget(String repository, Integer crNum) throws IOException {
        return resolve(repository).changeBaselines.get(crNum);
    }

    public synchronized Set<MockChangeRequestFlags> getFlags(String repository, Integer crNum) throws IOException {
        return Collections.unmodifiableSet(resolve(repository).changeFlags.get(crNum));
    }

    public synchronized List<String> listBranches(String repository) throws IOException {
        return new ArrayList<>(resolve(repository).heads.keySet());
    }

    public synchronized List<String> listTags(String repository) throws IOException {
        return new ArrayList<>(resolve(repository).tags.keySet());
    }

    public synchronized List<Integer> listChangeRequests(String repository) throws IOException {
        return new ArrayList<>(resolve(repository).changes.keySet());
    }

    public synchronized String getRevision(String repository, String branch) throws IOException {
        return resolve(repository, branch).getHash();
    }

    public synchronized void addFile(String repository, String branchOrCR, String message, String path, byte[] content)
            throws IOException {
        Repository repo = resolve(repository);
        String branchName;
        Integer crNum;
        String hash;
        // check branch first
        hash = repo.heads.get(branchOrCR);
        if (hash == null) {
            branchName = null;
            Matcher m = Pattern.compile("change-request/(\\d+)").matcher(branchOrCR);
            if (m.matches()) {
                crNum = Integer.valueOf(m.group(1));
                hash = repo.changes.get(crNum);
                if (hash == null) {
                    throw new IOException("Unknown change request: " + crNum + " in repository " + repository);
                }
            } else {
                throw new IOException("Unknown branch: " + branchOrCR + " in repository " + repository);
            }
        } else {
            branchName = branchOrCR;
            crNum = null;
        }
        State base = repo.revisions.get(hash);
        State state = new State(base, message, Collections.singletonMap(path, content), Collections.emptySet());
        repo.revisions.put(state.getHash(), state);
        if (branchName != null) {
            repo.heads.put(branchName, state.getHash());
        }
        if (crNum != null) {
            repo.changes.put(crNum, state.getHash());
        }
    }

    public synchronized void rmFile(String repository, String branchOrCR, String message, String path)
            throws IOException {
        Repository repo = resolve(repository);
        String branchName;
        Integer crNum;
        String hash;
        // check branch first
        hash = repo.heads.get(branchOrCR);
        if (hash == null) {
            branchName = null;
            Matcher m = Pattern.compile("change-request/(\\d+)").matcher(branchOrCR);
            if (m.matches()) {
                crNum = Integer.valueOf(m.group(1));
                hash = repo.changes.get(crNum);
                if (hash == null) {
                    throw new IOException("Unknown change request: " + crNum + " in repository " + repository);
                }
            } else {
                throw new IOException("Unknown branch: " + branchOrCR + " in repository " + repository);
            }
        } else {
            branchName = branchOrCR;
            crNum = null;
        }
        State base = repo.revisions.get(hash);
        State state =
                new State(base, message, Collections.emptyMap(), Collections.singleton(path));
        repo.revisions.put(state.getHash(), state);
        if (branchName != null) {
            repo.heads.put(branchName, state.getHash());
        }
        if (crNum != null) {
            repo.changes.put(crNum, state.getHash());
        }
    }


    public synchronized String checkout(File workspace, String repository, String identifier) throws IOException {
        State state = resolve(repository, identifier);

        for (Map.Entry<String, byte[]> entry : state.files.entrySet()) {
            FileUtils.writeByteArrayToFile(new File(workspace, entry.getKey()), entry.getValue());
        }
        return state.getHash();
    }

    public synchronized String checkout(FilePath workspace, String repository, String identifier)
            throws IOException, InterruptedException {
        State state = resolve(repository, identifier);

        for (Map.Entry<String, byte[]> entry : state.files.entrySet()) {
            workspace.child(entry.getKey()).copyFrom(new ByteArrayInputStream(entry.getValue()));
        }
        return state.getHash();
    }

    private synchronized State resolve(String repository, String identifier) throws IOException {
        Repository repo = resolve(repository);
        // check hash first
        String hash = repo.revisions.containsKey(identifier) ? identifier : null;
        if (hash != null) {
            return repo.revisions.get(hash);
        }
        // now check for a named branch
        hash = repo.heads.get(identifier);
        if (hash != null) {
            return repo.revisions.get(hash);
        }
        // now check for a named tag
        hash = repo.tags.get(identifier);
        if (hash != null) {
            return repo.revisions.get(hash);
        }
        // now check for a change request
        Matcher m = Pattern.compile("change-request/(\\d+)").matcher(identifier);
        if (m.matches()) {
            Integer crNum = Integer.valueOf(m.group(1));
            hash = repo.changes.get(crNum);
            if (hash != null) {
                return repo.revisions.get(hash);
            }
            throw new IOException("Unknown change request: " + crNum + " in repository " + repository);
        }
        throw new IOException("Unknown branch/tag/revision: " + identifier + " in repository " + repository);
    }

    private Repository resolve(String repository) throws IOException {
        Repository repo = repositories.get(repository);
        if (repo == null) {
            throw new IOException("Unknown repository: " + repository);
        }
        return repo;
    }

    public synchronized List<LogEntry> log(String repository, String identifier) throws IOException {
        State state = resolve(repository, identifier);
        List<LogEntry> result = new ArrayList<>();
        while (state != null) {
            result.add(new LogEntry(state.getHash(), state.timestamp, state.message, state.files.keySet()));
            state = state.parent;
        }
        return result;
    }

    public synchronized SCMFile.Type stat(String repository, String identifier, String path) throws IOException {
        State state = resolve(repository, identifier);
        if (state == null) {
            return SCMFile.Type.NONEXISTENT;
        }
        if (state.files.containsKey(path)) {
            return SCMFile.Type.REGULAR_FILE;
        }
        for (String p : state.files.keySet()) {
            if (p.startsWith(path + "/")) {
                return SCMFile.Type.DIRECTORY;
            }
        }
        return SCMFile.Type.NONEXISTENT;
    }

    public synchronized long lastModified(String repository, String identifier) {
        try {
            State state = resolve(repository, identifier);
            if (state == null) {
                return 0L;
            }
            return state.timestamp;
        } catch (IOException e) {
            return 0L;
        }
    }

    public synchronized long getTagTimestamp(String repository, String tag) throws IOException {
        Repository repo = repositories.get(repository);
        if (repo == null) {
            throw new IOException("Unknown repository: " + repository);
        }
        Long date = repo.tagDates.get(tag);
        if (tag == null) {
            throw new IOException("Unknown tag: null in repository " + repository);
        }
        return date;
    }

    public void afterSave(MockSCMSource source) {
        for (MockSCMSourceSaveListener listener: sourceSaveListeners) {
            listener.afterSave(source);
        }
    }

    public void afterSave(MockSCMNavigator navigator, SCMNavigatorOwner owner) {
        for (MockSCMNavigatorSaveListener listener: navigatorSaveListeners) {
            listener.afterSave(navigator, owner);
        }
    }

    private static class Repository {
        private Map<String, State> revisions = new TreeMap<>();
        private Map<String, String> heads = new TreeMap<>();
        private Map<String, String> tags = new TreeMap<>();
        private Map<String, Long> tagDates = new TreeMap<>();
        private Map<Integer, String> changes = new TreeMap<>();
        private Map<Integer, Set<MockChangeRequestFlags>> changeFlags = new TreeMap<>();
        private Map<Integer, String> changeBaselines = new TreeMap<>();
        private int lastChangeRequest;
        private String description;
        private String displayName;
        private String url;
        private String primaryBranch;
        private Set<MockRepositoryFlags> flags;

        private Repository(MockRepositoryFlags... flags) {
            this.flags = flags.length == 0
                    ? Collections.emptySet()
                    : EnumSet.copyOf(Arrays.asList(flags));
        }
    }

    private static class State {
        private final State parent;
        private final String message;
        private final long timestamp;
        private final Map<String, byte[]> files;
        private transient String hash;

        public State() {
            this.parent = null;
            this.message = null;
            this.timestamp = System.currentTimeMillis();
            this.files = new TreeMap<>();
        }

        public State(State parent, String message, Map<String, byte[]> added, Set<String> removed) {
            this.parent = parent;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
            Map<String, byte[]> files = parent != null
                    ? new TreeMap<>(parent.files)
                    : new TreeMap<>();
            files.keySet().removeAll(removed);
            files.putAll(added);
            this.files = files;
        }

        public String getHash() {
            if (hash == null) {
                try {
                    MessageDigest sha = MessageDigest.getInstance("SHA-1");
                    if (parent != null) {
                        sha.update(new BigInteger(parent.getHash(), 16).toByteArray());
                    }
                    sha.update(StringUtils.defaultString(message).getBytes(StandardCharsets.UTF_8));
                    sha.update((byte) (timestamp & 0xff));
                    sha.update((byte) ((timestamp >> 8) & 0xff));
                    sha.update((byte) ((timestamp >> 16) & 0xff));
                    sha.update((byte) ((timestamp >> 24) & 0xff));
                    sha.update((byte) ((timestamp >> 32) & 0xff));
                    sha.update((byte) ((timestamp >> 40) & 0xff));
                    sha.update((byte) ((timestamp >> 48) & 0xff));
                    sha.update((byte) ((timestamp >> 56) & 0xff));
                    for (Map.Entry<String, byte[]> e : files.entrySet()) {
                        sha.update(e.getKey().getBytes(StandardCharsets.UTF_8));
                        sha.update(e.getValue());
                    }
                    this.hash = toHexBinary(sha.digest());
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException("SHA-1 message digest mandated by JLS");
                }
            }
            return hash;
        }
    }

    static String toHexBinary(byte[] bytes) {
        return new String(Hex.encodeHex(bytes));
    }

    public static final class LogEntry {
        private final String hash;
        private final long timestamp;
        private final String message;
        private final Set<String> files;

        private LogEntry(String hash, long timestamp, String message, Set<String> files) {
            this.hash = hash;
            this.timestamp = timestamp;
            this.message = message;
            this.files = Collections.unmodifiableSet(files);
        }

        public String getHash() {
            return hash;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getMessage() {
            return message;
        }

        public Set<String> getFiles() {
            return files;
        }

        @Override
        public String toString() {
            return String.format("Commit %s%nDate: %tc%n%s%n", hash, timestamp, message);
        }
    }
}
