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

package jenkins.scm.impl.trait;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import java.util.regex.Pattern;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.trait.SCMNavigatorContext;
import jenkins.scm.api.trait.SCMNavigatorTrait;
import jenkins.scm.api.trait.SCMNavigatorTraitDescriptor;
import jenkins.scm.api.trait.SCMSourcePrefilter;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Decorates a {@link SCMNavigator} with a {@link SCMSourcePrefilter} that filters project names based on
 * matching wildcard include/exclude rules.
 *
 * @since 2.2.0
 */
public class WildcardSCMSourceFilterTrait extends SCMNavigatorTrait {

    /**
     * The include rules.
     */
    @NonNull
    private final String includes;

    /**
     * The exclude rules.
     */
    @NonNull
    private final String excludes;

    /**
     * Stapler constructor.
     *
     * @param includes the include rules.
     * @param excludes the exclude rules.
     */
    @DataBoundConstructor
    public WildcardSCMSourceFilterTrait(String includes, String excludes) {
        this.includes = StringUtils.defaultIfBlank(includes, "*");
        this.excludes = StringUtils.defaultIfBlank(excludes, "");
    }

    /**
     * Returns the include rules.
     *
     * @return the include rules.
     */
    public String getIncludes() {
        return includes;
    }

    /**
     * Returns the exclude rules.
     *
     * @return the exclude rules.
     */
    public String getExcludes() {
        return excludes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void decorateContext(SCMNavigatorContext<?, ?> context) {
        context.withPrefilter(new SCMSourcePrefilter() {
            @Override
            public boolean isExcluded(@NonNull SCMNavigator source, @NonNull String projectName) {
                return !Pattern.matches(getPattern(getIncludes()), projectName)
                        || (Pattern.matches(getPattern(getExcludes()), projectName));
            }
        });
    }

    /**
     * Returns the pattern corresponding to the project name containing wildcards.
     *
     * @param branches the names of projects to create a pattern for
     * @return pattern corresponding to the projects containing wildcards
     */
    private String getPattern(String branches) {
        StringBuilder quotedBranches = new StringBuilder();
        for (String wildcard : branches.split(" ")) {
            StringBuilder quotedBranch = new StringBuilder();
            for (String branch : wildcard.split("(?=[*])|(?<=[*])")) {
                if (branch.equals("*")) {
                    quotedBranch.append(".*");
                } else if (!branch.isEmpty()) {
                    quotedBranch.append(Pattern.quote(branch));
                }
            }
            if (quotedBranches.length() > 0) {
                quotedBranches.append("|");
            }
            quotedBranches.append(quotedBranch);
        }
        return quotedBranches.toString();
    }

    /**
     * Our descriptor.
     */
    @Symbol("sourceWildcardFilter")
    @Extension
    @Selection
    public static class DescriptorImpl extends SCMNavigatorTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.WildcardSCMSourceFilterTrait_DisplayName();
        }
    }
}
