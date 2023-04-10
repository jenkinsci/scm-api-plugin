/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc.
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

import jenkins.scm.api.metadata.AvatarMetadataAction;

import java.util.Objects;

public class MockAvatarMetadataAction extends AvatarMetadataAction {
    private final String iconClassName;

    public MockAvatarMetadataAction( String iconClassName) {
        this.iconClassName = iconClassName;
    }

    @Override
    public String getAvatarIconClassName() {
        return iconClassName;
    }

    @Override
    public String getAvatarDescription() {
        return iconClassName == null ? null : "Mock SCM";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MockAvatarMetadataAction that = (MockAvatarMetadataAction) o;

        return Objects.equals(iconClassName, that.iconClassName);
    }

    @Override
    public int hashCode() {
        return iconClassName != null ? iconClassName.hashCode() : 0;
    }
}
