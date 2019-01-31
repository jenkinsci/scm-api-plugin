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

package jenkins.scm.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.mixin.SCMHeadMixin;
import jenkins.scm.impl.mock.MockChangeRequestSCMHead;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class SCMHeadTest {

    @Test
    public void equality() {
        SCMHead h1 = new SCMHead("h1");
        SCMHead h2 = new SCMHead("h1");
        SCMHead x = new SCMHead("h2");

        assertThat(h1, equalTo(h1));
        assertThat(h1, equalTo(h2));
        assertThat(h1, not(equalTo(x)));
    }

    @Test
    public void mixinEquality() {
        SCMHead h1 = new MockChangeRequestSCMHead(1, "h1");
        SCMHead h2 = new MockChangeRequestSCMHead(1, "h1");
        SCMHead x = new MockChangeRequestSCMHead(1, "h2");
        SCMHead y = new MockChangeRequestSCMHead(2, "h1");
        SCMHead z = new MockChangeRequestSCMHead(2, "h2");

        assertThat(h1, equalTo(h1));
        assertThat(h1, equalTo(h2));
        assertThat(h1, not(equalTo(x)));
        assertThat(h1, not(equalTo(y)));
        assertThat(h1, not(equalTo(z)));
    }

    @Test
    public void crazyMixinEquality() {
        SCMHead h1 = new CrazyHead("crazy", true, (byte)0, 'a', 0.1, 0.2f, 3, 4L, (short)5, "String");
        SCMHead h2 = new CrazyHead("crazy", true, (byte) 0, 'a', 0.1, 0.2f, 3, 4L, (short) 5, "String");
        SCMHead x0 = new CrazyHead("crazy", false, (byte) 0, 'a', 0.1, 0.2f, 3, 4L, (short) 5, "String");
        SCMHead x1 = new CrazyHead("crazy", true, (byte) 1, 'a', 0.1, 0.2f, 3, 4L, (short) 5, "String");
        SCMHead x2 = new CrazyHead("crazy", true, (byte) 0, 'b', 0.1, 0.2f, 3, 4L, (short) 5, "String");
        SCMHead x3 = new CrazyHead("crazy", true, (byte) 0, 'a', 0.2, 0.2f, 3, 4L, (short) 5, "String");
        SCMHead x4 = new CrazyHead("crazy", true, (byte) 0, 'a', 0.1, 0.3f, 3, 4L, (short) 5, "String");
        SCMHead x5 = new CrazyHead("crazy", true, (byte) 0, 'a', 0.1, 0.2f, 4, 4L, (short) 5, "String");
        SCMHead x6 = new CrazyHead("crazy", true, (byte) 0, 'a', 0.1, 0.2f, 3, 45L, (short) 5, "String");
        SCMHead x7 = new CrazyHead("crazy", true, (byte) 0, 'a', 0.1, 0.2f, 3, 4L, (short) 6, "String");
        SCMHead x8 = new CrazyHead("crazy", true, (byte) 0, 'a', 0.1, 0.2f, 3, 4L, (short) 5, "Diff");
        SCMHead x9 = new CrazyHead("crazy", true, (byte) 0, 'a', 0.1, 0.2f, 3, 4L, (short) 5, Boolean.TRUE);

        assertThat(h1, equalTo(h1));
        assertThat(h1, equalTo(h2));
        assertThat(h1, not(equalTo(x0)));
        assertThat(h1, not(equalTo(x1)));
        assertThat(h1, not(equalTo(x2)));
        assertThat(h1, not(equalTo(x3)));
        assertThat(h1, not(equalTo(x4)));
        assertThat(h1, not(equalTo(x5)));
        assertThat(h1, not(equalTo(x6)));
        assertThat(h1, not(equalTo(x7)));
        assertThat(h1, not(equalTo(x8)));
        assertThat(h1, not(equalTo(x9)));
    }

    public interface CrazyMixin extends SCMHeadMixin {
        boolean isABoolean();

        byte getAByte();

        char getAChar();

        double getADouble();

        float getAFloat();

        int getAnInt();

        long getALong();

        short getAShort();

        Object getAnObject();
    }

    public static class CrazyHead extends SCMHead implements CrazyMixin {
        private final boolean aBoolean;
        private final byte aByte;
        private final char aChar;
        private final double aDouble;
        private final float aFloat;
        private final int anInt;
        private final long aLong;
        private final short aShort;
        private final Object anObject;

        public CrazyHead(@NonNull String name, boolean aBoolean, byte aByte, char aChar, double aDouble, float aFloat,
                         int anInt, long aLong, short aShort, Object anObject) {
            super(name);
            this.aBoolean = aBoolean;
            this.aByte = aByte;
            this.aChar = aChar;
            this.aDouble = aDouble;
            this.aFloat = aFloat;
            this.anInt = anInt;
            this.aLong = aLong;
            this.aShort = aShort;
            this.anObject = anObject;
        }

        @Override
        public boolean isABoolean() {
            return aBoolean;
        }

        @Override
        public byte getAByte() {
            return aByte;
        }

        @Override
        public char getAChar() {
            return aChar;
        }

        @Override
        public double getADouble() {
            return aDouble;
        }

        @Override
        public float getAFloat() {
            return aFloat;
        }

        @Override
        public int getAnInt() {
            return anInt;
        }

        @Override
        public long getALong() {
            return aLong;
        }

        @Override
        public short getAShort() {
            return aShort;
        }

        @Override
        public Object getAnObject() {
            return anObject;
        }
    }

}
