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

package jenkins.scm.impl.mock;

import jenkins.scm.api.trait.SCMSourceBuilder;

public class MockSCMSourceBuilder extends SCMSourceBuilder<MockSCMSourceBuilder, MockSCMSource> {
    private final String id;
    private final String controllerId;
    private final MockSCMController controller;
    private final String repository;

    public MockSCMSourceBuilder(String id, MockSCMController controller, String repository) {
        super(MockSCMSource.class, repository);
        this.id = id;
        this.controllerId = controller.getId();
        this.controller = controller;
        this.repository = repository;
    }

    public MockSCMSourceBuilder(String id, String controllerId, String repository) {
        super(MockSCMSource.class, repository);
        this.id = id;
        this.controllerId = controllerId;
        this.controller = null;
        this.repository = repository;
    }

    @Override
    public MockSCMSource build() {
        return controller == null
                ? new MockSCMSource(id, controllerId, repository, traits())
                : new MockSCMSource(id, controller, repository, traits());
    }
}
