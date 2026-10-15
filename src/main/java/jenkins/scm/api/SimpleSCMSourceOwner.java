package jenkins.scm.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.search.Search;
import hudson.search.SearchIndex;
import hudson.security.ACL;

/**
 * Minimal {@link SCMSourceOwner} realization(wrapper {@link Item}) for credentials lookup purpose.
 */
public class SimpleSCMSourceOwner implements SCMSourceOwner {
    @NonNull
    private transient final SCMSource source;
    @NonNull
    private transient final Item context;

    public SimpleSCMSourceOwner(@NonNull SCMSource source, @NonNull Item context) {
        this.source = source;
        this.context = context;
    }

    @Override
    public @org.jspecify.annotations.NonNull List<SCMSource> getSCMSources() {
        return List.of(source);
    }

    @Override
    public void onSCMSourceUpdated(@org.jspecify.annotations.NonNull SCMSource source) {

    }

    @Override
    public ItemGroup<? extends Item> getParent() {
        return context.getParent();
    }

    @Override
    public Collection<? extends Job> getAllJobs() {
        return context.getAllJobs();
    }

    @Override
    public String getName() {
        return context.getName();
    }

    @Override
    public String getFullName() {
        return context.getFullName();
    }

    @Override
    public String getDisplayName() {
        return context.getDisplayName();
    }

    @Override
    public String getFullDisplayName() {
        return context.getFullDisplayName();
    }

    @Override
    public String getUrl() {
        return context.getUrl();
    }

    @Override
    public String getShortUrl() {
        return context.getShortUrl();
    }

    @Override
    public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
        context.onLoad(parent, name);
    }

    @Override
    public void onCopiedFrom(Item src) {
        context.onCopiedFrom(src);
    }

    @Override
    public void save() throws IOException {
        context.save();
    }

    @Override
    public void delete() throws IOException, InterruptedException {
        context.delete();
    }

    @Override
    public File getRootDir() {
        return context.getRootDir();
    }

    @Override
    public Search getSearch() {
        return context.getSearch();
    }

    @Override
    public String getSearchName() {
        return context.getSearchName();
    }

    @Override
    public String getSearchUrl() {
        return context.getSearchUrl();
    }

    @Override
    public SearchIndex getSearchIndex() {
        return context.getSearchIndex();
    }

    @Override
    public @org.jspecify.annotations.NonNull ACL getACL() {
        return context.getACL();
    }
}
