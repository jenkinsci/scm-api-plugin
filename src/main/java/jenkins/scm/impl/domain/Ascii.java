/*
 * Copyright (C) 2010 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package jenkins.scm.impl.domain;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Static methods pertaining to ASCII characters (those in the range of values {@code 0x00} through
 * {@code 0x7F}), and to strings containing such characters.
 *
 * <p>Adapted from Guava 31.0.1. Usage only internal to this plugin. Marked as restricted to prevent
 * any further usages. This should be switched to upstream when it is no longer beta.
 *
 * @author Catherine Berry
 * @author Gregory Kick
 */
@Restricted(NoExternalUse.class)
public final class Ascii {

  private Ascii() {}

  /** A bit mask which selects the bit encoding ASCII character case. */
  private static final char CASE_MASK = 0x20;

  /**
   * Returns a copy of the input string in which all {@linkplain #isUpperCase(char) uppercase ASCII
   * characters} have been converted to lowercase. All other characters are copied without
   * modification.
   */
  public static String toLowerCase(String string) {
    int length = string.length();
    for (int i = 0; i < length; i++) {
      if (isUpperCase(string.charAt(i))) {
        char[] chars = string.toCharArray();
        for (; i < length; i++) {
          char c = chars[i];
          if (isUpperCase(c)) {
            chars[i] = (char) (c ^ CASE_MASK);
          }
        }
        return String.valueOf(chars);
      }
    }
    return string;
  }

  /**
   * Indicates whether {@code c} is one of the twenty-six uppercase ASCII alphabetic characters
   * between {@code 'A'} and {@code 'Z'} inclusive. All others (including non-ASCII characters)
   * return {@code false}.
   */
  public static boolean isUpperCase(char c) {
    return (c >= 'A') && (c <= 'Z');
  }
}
