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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.FormValidation;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.trait.SCMNavigatorContext;
import jenkins.scm.api.trait.SCMNavigatorRequest;
import jenkins.scm.api.trait.SCMNavigatorTrait;
import jenkins.scm.api.trait.SCMNavigatorTraitDescriptor;
import jenkins.scm.api.trait.SCMSourcePrefilter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class RegexSCMSourceFilterTrait extends SCMNavigatorTrait {

    private final String regex;
    @CheckForNull
    private transient Pattern pattern;

    @DataBoundConstructor
    public RegexSCMSourceFilterTrait(String regex) {
        pattern = Pattern.compile(regex);
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }

    @NonNull
    private Pattern getPattern() {
        if (pattern == null) {
            // idempotent
            pattern = Pattern.compile(regex);
        }
        return pattern;
    }

    @Override
    protected <B extends SCMNavigatorContext<B, R>, R extends SCMNavigatorRequest> void decorateContext(B context) {
        context.withPrefilter(new SCMSourcePrefilter() {
            @Override
            public boolean isExcluded(@NonNull SCMNavigator source, @NonNull String projectName) {
                return !getPattern().matcher(projectName).matches();
            }
        });
    }

    @Extension
    public static class DescriptorImpl extends SCMNavigatorTraitDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.RegexSCMSourceFilterTrait_DisplayName();
        }

        public FormValidation doCheckRegex(@QueryParameter String value) {
            try {
                Pattern.compile(value);
                return FormValidation.ok();
            } catch (PatternSyntaxException e) {
                return FormValidation.error(e.getMessage());
            }
        }
    }
}
