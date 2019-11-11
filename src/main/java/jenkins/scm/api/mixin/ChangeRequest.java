package jenkins.scm.api.mixin;

import org.kohsuke.stapler.export.Exported;

import javax.annotation.Nonnull;

/**
 * The interface carry with the information of a change request,
 *  such as pull request or merge request.
 * @since TODO
 */
public interface ChangeRequest {
    /**
     * Returns the title of the change request
     * @return title of the change request
     */
    @Exported
    @Nonnull
    String getTitle();
}
