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

package jenkins.scm.api.trait;

import hudson.model.Descriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.SCMHeadMixin;
import org.jvnet.tiger_types.Types;

public abstract class SCMHeadAuthorityDescriptor extends Descriptor<SCMHeadAuthority<?, ?>> {

    private final Class<? extends SCMSourceRequest> requestClass;
    private final Class<? extends SCMHeadMixin> headClass;

    protected SCMHeadAuthorityDescriptor(Class<? extends SCMHeadAuthority<?, ?>> clazz,
                                         Class<? extends SCMSourceRequest> requestClass,
                                         Class<? extends SCMHeadMixin> headClass) {
        super(clazz);
        this.requestClass = requestClass;
        this.headClass = headClass;
    }

    protected SCMHeadAuthorityDescriptor() {
        Type bt = Types.getBaseClass(clazz, SCMHeadAuthority.class);
        if (bt instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) bt;
            // this 'headClass' is the closest approximation of T of SCMHeadAuthority<T>.
            requestClass = Types.erasure(pt.getActualTypeArguments()[0]);
            headClass = Types.erasure(pt.getActualTypeArguments()[1]);
            if (!SCMSourceRequest.class.isAssignableFrom(requestClass)) {
                throw new AssertionError(
                        "Failed to correctly detect SCMSourceRequest specialization. Use the constructor that takes "
                                + "the "
                                + "Class objects explicitly");
            }
            if (!SCMHeadMixin.class.isAssignableFrom(headClass)) {
                throw new AssertionError(
                        "Failed to correctly detect SCMHead specialization. Use the constructor that takes the Class "
                                + "objects explicitly");
            }
        } else {
            throw new AssertionError(
                    "Failed to correctly detect specialization. Use the constructor that takes the Class objects "
                            + "explicitly");
        }
    }

    public boolean isApplicableToHead(SCMHead head) {
        return isApplicableToHead(head.getClass());
    }

    public boolean isApplicableToHead(Class<? extends SCMHead> headClass) {
        return this.headClass.isAssignableFrom(headClass);
    }

    public boolean isApplicableToRequest(SCMSourceRequest request) {
        return requestClass.isInstance(request);
    }

    public boolean isApplicableToRequest(Class<? extends SCMSourceRequest> requestClass) {
        return this.requestClass.isAssignableFrom(requestClass);
    }
}
