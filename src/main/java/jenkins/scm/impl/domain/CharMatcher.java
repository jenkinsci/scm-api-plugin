/*
 * Copyright (C) 2008 The Guava Authors
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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;
import java.util.function.Predicate;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Determines a true or false value for any Java {@code char} value, just as {@link Predicate} does
 * for any {@link Object}. Also offers basic text processing methods based on this function.
 * Implementations are strongly encouraged to be side-effect-free and immutable.
 *
 * <p>Throughout the documentation of this class, the phrase "matching character" is used to mean
 * "any {@code char} value {@code c} for which {@code this.matches(c)} returns {@code true}".
 *
 * <p><b>Warning:</b> This class deals only with {@code char} values, that is, <a
 * href="http://www.unicode.org/glossary/#BMP_character">BMP characters</a>. It does not understand
 * <a href="http://www.unicode.org/glossary/#supplementary_code_point">supplementary Unicode code
 * points</a> in the range {@code 0x10000} to {@code 0x10FFFF} which includes the majority of
 * assigned characters, including important CJK characters and emoji.
 *
 * <p>Supplementary characters are <a
 * href="https://docs.oracle.com/javase/8/docs/api/java/lang/Character.html#supplementary">encoded
 * into a {@code String} using surrogate pairs</a>, and a {@code CharMatcher} treats these just as
 * two separate characters. {@link #countIn} counts each supplementary character as 2 {@code char}s.
 *
 * <p>For up-to-date Unicode character properties (digit, letter, etc.) and support for
 * supplementary code points, use ICU4J UCharacter and UnicodeSet (freeze() after building). For
 * basic text processing based on UnicodeSet use the ICU4J UnicodeSetSpanner.
 *
 * <p>See the Guava User Guide article on <a
 * href="https://github.com/google/guava/wiki/StringsExplained#charmatcher">{@code CharMatcher}
 * </a>.
 *
 * <p>Adapted from Guava 31.0.1. Usage only internal to this plugin. Marked as restricted to prevent
 * any further usages. This should be switched to upstream when it is no longer beta.
 *
 * @author Kevin Bourrillion
 */
@Restricted(NoExternalUse.class)
public abstract class CharMatcher implements Predicate<Character> {
  /*
   *           N777777777NO
   *         N7777777777777N
   *        M777777777777777N
   *        $N877777777D77777M
   *       N M77777777ONND777M
   *       MN777777777NN  D777
   *     N7ZN777777777NN ~M7778
   *    N777777777777MMNN88777N
   *    N777777777777MNZZZ7777O
   *    DZN7777O77777777777777
   *     N7OONND7777777D77777N
   *      8$M++++?N???$77777$
   *       M7++++N+M77777777N
   *        N77O777777777777$                              M
   *          DNNM$$$$777777N                              D
   *         N$N:=N$777N7777M                             NZ
   *        77Z::::N777777777                          ODZZZ
   *       77N::::::N77777777M                         NNZZZ$
   *     $777:::::::77777777MN                        ZM8ZZZZZ
   *     777M::::::Z7777777Z77                        N++ZZZZNN
   *    7777M:::::M7777777$777M                       $++IZZZZM
   *   M777$:::::N777777$M7777M                       +++++ZZZDN
   *     NN$::::::7777$$M777777N                      N+++ZZZZNZ
   *       N::::::N:7$O:77777777                      N++++ZZZZN
   *       M::::::::::::N77777777+                   +?+++++ZZZM
   *       8::::::::::::D77777777M                    O+++++ZZ
   *        ::::::::::::M777777777N                      O+?D
   *        M:::::::::::M77777777778                     77=
   *        D=::::::::::N7777777777N                    777
   *       INN===::::::=77777777777N                  I777N
   *      ?777N========N7777777777787M               N7777
   *      77777$D======N77777777777N777N?         N777777
   *     I77777$$$N7===M$$77777777$77777777$MMZ77777777N
   *      $$$$$$$$$$$NIZN$$$$$$$$$M$$7777777777777777ON
   *       M$$$$$$$$M    M$$$$$$$$N=N$$$$7777777$$$ND
   *      O77Z$$$$$$$     M$$$$$$$$MNI==$DNNNNM=~N
   *   7 :N MNN$$$$M$      $$$777$8      8D8I
   *     NMM.:7O           777777778
   *                       7777777MN
   *                       M NO .7:
   *                       M   :   M
   *                            8
   */

  // Constant matcher factory methods

  /** Matches any character. */
  public static CharMatcher any() {
    return Any.INSTANCE;
  }

  /** Matches no characters. */
  public static CharMatcher none() {
    return None.INSTANCE;
  }

  /** Determines whether a character is ASCII, meaning that its code point is less than 128. */
  public static CharMatcher ascii() {
    return Ascii.INSTANCE;
  }

  // Static factories

  /** Returns a {@code char} matcher that matches only one specified BMP character. */
  public static CharMatcher is(final char match) {
    return new Is(match);
  }

  /**
   * Returns a {@code char} matcher that matches any character except the BMP character specified.
   *
   * <p>To negate another {@code CharMatcher}, use {@link #negate()}.
   */
  public static CharMatcher isNot(final char match) {
    return new IsNot(match);
  }

  /**
   * Returns a {@code char} matcher that matches any BMP character present in the given character
   * sequence. Returns a bogus matcher if the sequence contains supplementary characters.
   */
  public static CharMatcher anyOf(final CharSequence sequence) {
    switch (sequence.length()) {
      case 0:
        return none();
      case 1:
        return is(sequence.charAt(0));
      case 2:
        return isEither(sequence.charAt(0), sequence.charAt(1));
      default:
        // TODO(lowasser): is it potentially worth just going ahead and building a precomputed
        // matcher?
        return new AnyOf(sequence);
    }
  }

  /**
   * Returns a {@code char} matcher that matches any character in a given BMP range (both endpoints
   * are inclusive). For example, to match any lowercase letter of the English alphabet, use {@code
   * CharMatcher.inRange('a', 'z')}.
   *
   * @throws IllegalArgumentException if {@code endInclusive < startInclusive}
   */
  public static CharMatcher inRange(final char startInclusive, final char endInclusive) {
    return new InRange(startInclusive, endInclusive);
  }

  // Constructors

  /**
   * Constructor for use by subclasses. When subclassing, you may want to override {@code
   * toString()} to provide a useful description.
   */
  protected CharMatcher() {}

  // Abstract methods

  /** Determines a true or false value for the given character. */
  public abstract boolean matches(char c);

  // Non-static factories

  /** Returns a matcher that matches any character not matched by this matcher. */
  // @Override under Java 8 but not under Java 7
  @Override
  public CharMatcher negate() {
    return new Negated(this);
  }

  /**
   * Returns a matcher that matches any character matched by both this matcher and {@code other}.
   */
  public CharMatcher and(CharMatcher other) {
    return new And(this, other);
  }

  /**
   * Returns a matcher that matches any character matched by either this matcher or {@code other}.
   */
  public CharMatcher or(CharMatcher other) {
    return new Or(this, other);
  }

  /**
   * Returns a {@code char} matcher functionally equivalent to this one, but which may be faster to
   * query than the original; your mileage may vary. Precomputation takes time and is likely to be
   * worthwhile only if the precomputed matcher is queried many thousands of times.
   *
   * <p>This method has no effect (returns {@code this}) when called in GWT: it's unclear whether a
   * precomputed matcher is faster, but it certainly consumes more memory, which doesn't seem like a
   * worthwhile tradeoff in a browser.
   */
  public CharMatcher precomputed() {
    return precomputedInternal();
  }

  private static final int DISTINCT_CHARS = Character.MAX_VALUE - Character.MIN_VALUE + 1;

  /**
   * This is the actual implementation of {@link #precomputed}.
   *
   * <p>This implementation tries to be smart in a number of ways. It recognizes cases where the
   * negation is cheaper to precompute than the matcher itself; it tries to build small hash tables
   * for matchers that only match a few characters, and so on. In the worst-case scenario, it
   * constructs an eight-kilobyte bit array and queries that. In many situations this produces a
   * matcher which is faster to query than the original.
   */
  CharMatcher precomputedInternal() {
    final BitSet table = new BitSet();
    setBits(table);
    int totalCharacters = table.cardinality();
    if (totalCharacters * 2 <= DISTINCT_CHARS) {
      return precomputedPositive(totalCharacters, table, toString());
    } else {
      // TODO(lowasser): is it worth it to worry about the last character of large matchers?
      table.flip(Character.MIN_VALUE, Character.MAX_VALUE + 1);
      int negatedCharacters = DISTINCT_CHARS - totalCharacters;
      String suffix = ".negate()";
      final String description = toString();
      String negatedDescription =
          description.endsWith(suffix)
              ? description.substring(0, description.length() - suffix.length())
              : description + suffix;
      return new NegatedFastMatcher(
          precomputedPositive(negatedCharacters, table, negatedDescription)) {
        @Override
        public String toString() {
          return description;
        }
      };
    }
  }

  /**
   * Helper method for {@link #precomputedInternal} that doesn't test if the negation is cheaper.
   */
  private static CharMatcher precomputedPositive(
      int totalCharacters, BitSet table, String description) {
    switch (totalCharacters) {
      case 0:
        return none();
      case 1:
        return is((char) table.nextSetBit(0));
      case 2:
        char c1 = (char) table.nextSetBit(0);
        char c2 = (char) table.nextSetBit(c1 + 1);
        return isEither(c1, c2);
      default:
        return isSmall(totalCharacters, table.length())
            ? SmallCharMatcher.from(table, description)
            : new BitSetMatcher(table, description);
    }
  }

  private static boolean isSmall(int totalCharacters, int tableLength) {
    return totalCharacters <= SmallCharMatcher.MAX_SIZE
        && tableLength > (totalCharacters * 4 * Character.SIZE);
    // err on the side of BitSetMatcher
  }

  /** Sets bits in {@code table} matched by this matcher. */
  void setBits(BitSet table) {
    for (int c = Character.MAX_VALUE; c >= Character.MIN_VALUE; c--) {
      if (matches((char) c)) {
        table.set(c);
      }
    }
  }

  // Text processing routines

  /**
   * Returns {@code true} if a character sequence contains only matching BMP characters.
   *
   * <p>The default implementation iterates over the sequence, invoking {@link #matches} for each
   * character, until this returns {@code false} or the end is reached.
   *
   * @param sequence the character sequence to examine, possibly empty
   * @return {@code true} if this matcher matches every character in the sequence, including when
   *     the sequence is empty
   */
  public boolean matchesAllOf(CharSequence sequence) {
    for (int i = sequence.length() - 1; i >= 0; i--) {
      if (!matches(sequence.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns {@code true} if a character sequence contains no matching BMP characters. Equivalent to
   * {@code !matchesAnyOf(sequence)}.
   *
   * <p>The default implementation iterates over the sequence, invoking {@link #matches} for each
   * character, until this returns {@code true} or the end is reached.
   *
   * @param sequence the character sequence to examine, possibly empty
   * @return {@code true} if this matcher matches no characters in the sequence, including when the
   *     sequence is empty
   */
  public boolean matchesNoneOf(CharSequence sequence) {
    return indexIn(sequence) == -1;
  }

  /**
   * Returns the index of the first matching BMP character in a character sequence, or {@code -1} if
   * no matching character is present.
   *
   * <p>The default implementation iterates over the sequence in forward order calling {@link
   * #matches} for each character.
   *
   * @param sequence the character sequence to examine from the beginning
   * @return an index, or {@code -1} if no character matches
   */
  public int indexIn(CharSequence sequence) {
    return indexIn(sequence, 0);
  }

  /**
   * Returns the index of the first matching BMP character in a character sequence, starting from a
   * given position, or {@code -1} if no character matches after that position.
   *
   * <p>The default implementation iterates over the sequence in forward order, beginning at {@code
   * start}, calling {@link #matches} for each character.
   *
   * @param sequence the character sequence to examine
   * @param start the first index to examine; must be nonnegative and no greater than {@code
   *     sequence.length()}
   * @return the index of the first matching character, guaranteed to be no less than {@code start},
   *     or {@code -1} if no character matches
   * @throws IndexOutOfBoundsException if start is negative or greater than {@code
   *     sequence.length()}
   */
  public int indexIn(CharSequence sequence, int start) {
    int length = sequence.length();
    Preconditions.checkPositionIndex(start, length);
    for (int i = start; i < length; i++) {
      if (matches(sequence.charAt(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the index of the last matching BMP character in a character sequence, or {@code -1} if
   * no matching character is present.
   *
   * <p>The default implementation iterates over the sequence in reverse order calling {@link
   * #matches} for each character.
   *
   * @param sequence the character sequence to examine from the end
   * @return an index, or {@code -1} if no character matches
   */
  public int lastIndexIn(CharSequence sequence) {
    for (int i = sequence.length() - 1; i >= 0; i--) {
      if (matches(sequence.charAt(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the number of matching {@code char}s found in a character sequence.
   *
   * <p>Counts 2 per supplementary character.
   */
  public int countIn(CharSequence sequence) {
    int count = 0;
    for (int i = 0; i < sequence.length(); i++) {
      if (matches(sequence.charAt(i))) {
        count++;
      }
    }
    return count;
  }

  /**
   * Returns a string containing all non-matching characters of a character sequence, in order. For
   * example:
   *
   * <pre>{@code
   * CharMatcher.is('a').removeFrom("bazaar")
   * }</pre>
   *
   * ... returns {@code "bzr"}.
   */
  public String removeFrom(CharSequence sequence) {
    String string = sequence.toString();
    int pos = indexIn(string);
    if (pos == -1) {
      return string;
    }

    char[] chars = string.toCharArray();
    int spread = 1;

    // This unusual loop comes from extensive benchmarking
    OUT:
    while (true) {
      pos++;
      while (true) {
        if (pos == chars.length) {
          break OUT;
        }
        if (matches(chars[pos])) {
          break;
        }
        chars[pos - spread] = chars[pos];
        pos++;
      }
      spread++;
    }
    return new String(chars, 0, pos - spread);
  }

  /**
   * Returns a string containing all matching BMP characters of a character sequence, in order. For
   * example:
   *
   * <pre>{@code
   * CharMatcher.is('a').retainFrom("bazaar")
   * }</pre>
   *
   * ... returns {@code "aaa"}.
   */
  public String retainFrom(CharSequence sequence) {
    return negate().removeFrom(sequence);
  }

  /**
   * Returns a string copy of the input character sequence, with each matching BMP character
   * replaced by a given replacement character. For example:
   *
   * <pre>{@code
   * CharMatcher.is('a').replaceFrom("radar", 'o')
   * }</pre>
   *
   * ... returns {@code "rodor"}.
   *
   * <p>The default implementation uses {@link #indexIn(CharSequence)} to find the first matching
   * character, then iterates the remainder of the sequence calling {@link #matches(char)} for each
   * character.
   *
   * @param sequence the character sequence to replace matching characters in
   * @param replacement the character to append to the result string in place of each matching
   *     character in {@code sequence}
   * @return the new string
   */
  public String replaceFrom(CharSequence sequence, char replacement) {
    String string = sequence.toString();
    int pos = indexIn(string);
    if (pos == -1) {
      return string;
    }
    char[] chars = string.toCharArray();
    chars[pos] = replacement;
    for (int i = pos + 1; i < chars.length; i++) {
      if (matches(chars[i])) {
        chars[i] = replacement;
      }
    }
    return new String(chars);
  }

  /**
   * Returns a string copy of the input character sequence, with each matching BMP character
   * replaced by a given replacement sequence. For example:
   *
   * <pre>{@code
   * CharMatcher.is('a').replaceFrom("yaha", "oo")
   * }</pre>
   *
   * ... returns {@code "yoohoo"}.
   *
   * <p><b>Note:</b> If the replacement is a fixed string with only one character, you are better
   * off calling {@link #replaceFrom(CharSequence, char)} directly.
   *
   * @param sequence the character sequence to replace matching characters in
   * @param replacement the characters to append to the result string in place of each matching
   *     character in {@code sequence}
   * @return the new string
   */
  public String replaceFrom(CharSequence sequence, CharSequence replacement) {
    int replacementLen = replacement.length();
    if (replacementLen == 0) {
      return removeFrom(sequence);
    }
    if (replacementLen == 1) {
      return replaceFrom(sequence, replacement.charAt(0));
    }

    String string = sequence.toString();
    int pos = indexIn(string);
    if (pos == -1) {
      return string;
    }

    int len = string.length();
    StringBuilder buf = new StringBuilder((len * 3 / 2) + 16);

    int oldpos = 0;
    do {
      buf.append(string, oldpos, pos);
      buf.append(replacement);
      oldpos = pos + 1;
      pos = indexIn(string, oldpos);
    } while (pos != -1);

    buf.append(string, oldpos, len);
    return buf.toString();
  }

  /**
   * Returns a substring of the input character sequence that omits all matching BMP characters from
   * the beginning and from the end of the string. For example:
   *
   * <pre>{@code
   * CharMatcher.anyOf("ab").trimFrom("abacatbab")
   * }</pre>
   *
   * ... returns {@code "cat"}.
   *
   * <p>Note that:
   *
   * <pre>{@code
   * CharMatcher.inRange('\0', ' ').trimFrom(str)
   * }</pre>
   *
   * ... is equivalent to {@link String#trim()}.
   */
  public String trimFrom(CharSequence sequence) {
    int len = sequence.length();
    int first;
    int last;

    for (first = 0; first < len; first++) {
      if (!matches(sequence.charAt(first))) {
        break;
      }
    }
    for (last = len - 1; last > first; last--) {
      if (!matches(sequence.charAt(last))) {
        break;
      }
    }

    return sequence.subSequence(first, last + 1).toString();
  }

  /**
   * Returns a substring of the input character sequence that omits all matching BMP characters from
   * the beginning of the string. For example:
   *
   * <pre>{@code
   * CharMatcher.anyOf("ab").trimLeadingFrom("abacatbab")
   * }</pre>
   *
   * ... returns {@code "catbab"}.
   */
  public String trimLeadingFrom(CharSequence sequence) {
    int len = sequence.length();
    for (int first = 0; first < len; first++) {
      if (!matches(sequence.charAt(first))) {
        return sequence.subSequence(first, len).toString();
      }
    }
    return "";
  }

  /**
   * Returns a substring of the input character sequence that omits all matching BMP characters from
   * the end of the string. For example:
   *
   * <pre>{@code
   * CharMatcher.anyOf("ab").trimTrailingFrom("abacatbab")
   * }</pre>
   *
   * ... returns {@code "abacat"}.
   */
  public String trimTrailingFrom(CharSequence sequence) {
    int len = sequence.length();
    for (int last = len - 1; last >= 0; last--) {
      if (!matches(sequence.charAt(last))) {
        return sequence.subSequence(0, last + 1).toString();
      }
    }
    return "";
  }

  /**
   * Returns a string copy of the input character sequence, with each group of consecutive matching
   * BMP characters replaced by a single replacement character. For example:
   *
   * <pre>{@code
   * CharMatcher.anyOf("eko").collapseFrom("bookkeeper", '-')
   * }</pre>
   *
   * ... returns {@code "b-p-r"}.
   *
   * <p>The default implementation uses {@link #indexIn(CharSequence)} to find the first matching
   * character, then iterates the remainder of the sequence calling {@link #matches(char)} for each
   * character.
   *
   * @param sequence the character sequence to replace matching groups of characters in
   * @param replacement the character to append to the result string in place of each group of
   *     matching characters in {@code sequence}
   * @return the new string
   */
  public String collapseFrom(CharSequence sequence, char replacement) {
    // This implementation avoids unnecessary allocation.
    int len = sequence.length();
    for (int i = 0; i < len; i++) {
      char c = sequence.charAt(i);
      if (matches(c)) {
        if (c == replacement && (i == len - 1 || !matches(sequence.charAt(i + 1)))) {
          // a no-op replacement
          i++;
        } else {
          StringBuilder builder = new StringBuilder(len).append(sequence, 0, i).append(replacement);
          return finishCollapseFrom(sequence, i + 1, len, replacement, builder, true);
        }
      }
    }
    // no replacement needed
    return sequence.toString();
  }

  private String finishCollapseFrom(
      CharSequence sequence,
      int start,
      int end,
      char replacement,
      StringBuilder builder,
      boolean inMatchingGroup) {
    for (int i = start; i < end; i++) {
      char c = sequence.charAt(i);
      if (matches(c)) {
        if (!inMatchingGroup) {
          builder.append(replacement);
          inMatchingGroup = true;
        }
      } else {
        builder.append(c);
        inMatchingGroup = false;
      }
    }
    return builder.toString();
  }

  /**
   * @deprecated Provided only to satisfy the {@link Predicate} interface; use {@link #matches}
   *     instead.
   */
  @Deprecated
  @Override
  public boolean test(Character character) {
    return matches(character);
  }

  /**
   * Returns a string representation of this {@code CharMatcher}, such as {@code
   * CharMatcher.or(WHITESPACE, JAVA_DIGIT)}.
   */
  @Override
  public String toString() {
    return super.toString();
  }

  /**
   * Returns the Java Unicode escape sequence for the given {@code char}, in the form "\u12AB" where
   * "12AB" is the four hexadecimal digits representing the 16-bit code unit.
   */
  private static String showCharacter(char c) {
    String hex = "0123456789ABCDEF";
    char[] tmp = {'\\', 'u', '\0', '\0', '\0', '\0'};
    for (int i = 0; i < 4; i++) {
      tmp[5 - i] = hex.charAt(c & 0xF);
      c = (char) (c >> 4);
    }
    return String.copyValueOf(tmp);
  }

  // Fast matchers

  /** A matcher for which precomputation will not yield any significant benefit. */
  abstract static class FastMatcher extends CharMatcher {

    @Override
    public final CharMatcher precomputed() {
      return this;
    }

    @Override
    public CharMatcher negate() {
      return new NegatedFastMatcher(this);
    }
  }

  /** {@link FastMatcher} which overrides {@code toString()} with a custom name. */
  abstract static class NamedFastMatcher extends FastMatcher {

    private final String description;

    NamedFastMatcher(String description) {
      this.description = Objects.requireNonNull(description);
    }

    @Override
    public final String toString() {
      return description;
    }
  }

  /** Negation of a {@link FastMatcher}. */
  static class NegatedFastMatcher extends Negated {

    NegatedFastMatcher(CharMatcher original) {
      super(original);
    }

    @Override
    public final CharMatcher precomputed() {
      return this;
    }
  }

  /** Fast matcher using a {@link BitSet} table of matching characters. */
  private static final class BitSetMatcher extends NamedFastMatcher {

    private final BitSet table;

    private BitSetMatcher(BitSet table, String description) {
      super(description);
      if (table.length() + Long.SIZE < table.size()) {
        table = (BitSet) table.clone();
        // If only we could actually call BitSet.trimToSize() ourselves...
      }
      this.table = table;
    }

    @Override
    public boolean matches(char c) {
      return table.get(c);
    }

    @Override
    void setBits(BitSet bitSet) {
      bitSet.or(table);
    }
  }

  // Static constant implementation classes

  /** Implementation of {@link #any()}. */
  private static final class Any extends NamedFastMatcher {

    static final Any INSTANCE = new Any();

    private Any() {
      super("CharMatcher.any()");
    }

    @Override
    public boolean matches(char c) {
      return true;
    }

    @Override
    public int indexIn(CharSequence sequence) {
      return (sequence.length() == 0) ? -1 : 0;
    }

    @Override
    public int indexIn(CharSequence sequence, int start) {
      int length = sequence.length();
      Preconditions.checkPositionIndex(start, length);
      return (start == length) ? -1 : start;
    }

    @Override
    public int lastIndexIn(CharSequence sequence) {
      return sequence.length() - 1;
    }

    @Override
    public boolean matchesAllOf(CharSequence sequence) {
      Objects.requireNonNull(sequence);
      return true;
    }

    @Override
    public boolean matchesNoneOf(CharSequence sequence) {
      return sequence.length() == 0;
    }

    @Override
    public String removeFrom(CharSequence sequence) {
      Objects.requireNonNull(sequence);
      return "";
    }

    @Override
    public String replaceFrom(CharSequence sequence, char replacement) {
      char[] array = new char[sequence.length()];
      Arrays.fill(array, replacement);
      return new String(array);
    }

    @Override
    public String replaceFrom(CharSequence sequence, CharSequence replacement) {
      StringBuilder result = new StringBuilder(sequence.length() * replacement.length());
      for (int i = 0; i < sequence.length(); i++) {
        result.append(replacement);
      }
      return result.toString();
    }

    @Override
    public String collapseFrom(CharSequence sequence, char replacement) {
      return (sequence.length() == 0) ? "" : String.valueOf(replacement);
    }

    @Override
    public String trimFrom(CharSequence sequence) {
      Objects.requireNonNull(sequence);
      return "";
    }

    @Override
    public int countIn(CharSequence sequence) {
      return sequence.length();
    }

    @Override
    public CharMatcher and(CharMatcher other) {
      return Objects.requireNonNull(other);
    }

    @Override
    public CharMatcher or(CharMatcher other) {
      Objects.requireNonNull(other);
      return this;
    }

    @Override
    public CharMatcher negate() {
      return none();
    }
  }

  /** Implementation of {@link #none()}. */
  private static final class None extends NamedFastMatcher {

    static final None INSTANCE = new None();

    private None() {
      super("CharMatcher.none()");
    }

    @Override
    public boolean matches(char c) {
      return false;
    }

    @Override
    public int indexIn(CharSequence sequence) {
      Objects.requireNonNull(sequence);
      return -1;
    }

    @Override
    public int indexIn(CharSequence sequence, int start) {
      int length = sequence.length();
      Preconditions.checkPositionIndex(start, length);
      return -1;
    }

    @Override
    public int lastIndexIn(CharSequence sequence) {
      Objects.requireNonNull(sequence);
      return -1;
    }

    @Override
    public boolean matchesAllOf(CharSequence sequence) {
      return sequence.length() == 0;
    }

    @Override
    public boolean matchesNoneOf(CharSequence sequence) {
      Objects.requireNonNull(sequence);
      return true;
    }

    @Override
    public String removeFrom(CharSequence sequence) {
      return sequence.toString();
    }

    @Override
    public String replaceFrom(CharSequence sequence, char replacement) {
      return sequence.toString();
    }

    @Override
    public String replaceFrom(CharSequence sequence, CharSequence replacement) {
      Objects.requireNonNull(replacement);
      return sequence.toString();
    }

    @Override
    public String collapseFrom(CharSequence sequence, char replacement) {
      return sequence.toString();
    }

    @Override
    public String trimFrom(CharSequence sequence) {
      return sequence.toString();
    }

    @Override
    public String trimLeadingFrom(CharSequence sequence) {
      return sequence.toString();
    }

    @Override
    public String trimTrailingFrom(CharSequence sequence) {
      return sequence.toString();
    }

    @Override
    public int countIn(CharSequence sequence) {
      Objects.requireNonNull(sequence);
      return 0;
    }

    @Override
    public CharMatcher and(CharMatcher other) {
      Objects.requireNonNull(other);
      return this;
    }

    @Override
    public CharMatcher or(CharMatcher other) {
      return Objects.requireNonNull(other);
    }

    @Override
    public CharMatcher negate() {
      return any();
    }
  }

  /** Implementation of {@link #ascii()}. */
  private static final class Ascii extends NamedFastMatcher {

    static final Ascii INSTANCE = new Ascii();

    Ascii() {
      super("CharMatcher.ascii()");
    }

    @Override
    public boolean matches(char c) {
      return c <= '\u007f';
    }
  }

  /** Implementation that matches characters that fall within multiple ranges. */
  private static class RangesMatcher extends CharMatcher {

    private final String description;
    private final char[] rangeStarts;
    private final char[] rangeEnds;

    RangesMatcher(String description, char[] rangeStarts, char[] rangeEnds) {
      this.description = description;
      this.rangeStarts = rangeStarts;
      this.rangeEnds = rangeEnds;
      if (rangeStarts.length != rangeEnds.length) {
        throw new IllegalArgumentException();
      }
      for (int i = 0; i < rangeStarts.length; i++) {
        if (rangeStarts[i] > rangeEnds[i]) {
          throw new IllegalArgumentException();
        }
        if (i + 1 < rangeStarts.length) {
          if (rangeEnds[i] >= rangeStarts[i + 1]) {
            throw new IllegalArgumentException();
          }
        }
      }
    }

    @Override
    public boolean matches(char c) {
      int index = Arrays.binarySearch(rangeStarts, c);
      if (index >= 0) {
        return true;
      } else {
        index = ~index - 1;
        return index >= 0 && c <= rangeEnds[index];
      }
    }

    @Override
    public String toString() {
      return description;
    }
  }

  // Non-static factory implementation classes

  /** Implementation of {@link #negate()}. */
  private static class Negated extends CharMatcher {

    final CharMatcher original;

    Negated(CharMatcher original) {
      this.original = Objects.requireNonNull(original);
    }

    @Override
    public boolean matches(char c) {
      return !original.matches(c);
    }

    @Override
    public boolean matchesAllOf(CharSequence sequence) {
      return original.matchesNoneOf(sequence);
    }

    @Override
    public boolean matchesNoneOf(CharSequence sequence) {
      return original.matchesAllOf(sequence);
    }

    @Override
    public int countIn(CharSequence sequence) {
      return sequence.length() - original.countIn(sequence);
    }

    @Override
    void setBits(BitSet table) {
      BitSet tmp = new BitSet();
      original.setBits(tmp);
      tmp.flip(Character.MIN_VALUE, Character.MAX_VALUE + 1);
      table.or(tmp);
    }

    @Override
    public CharMatcher negate() {
      return original;
    }

    @Override
    public String toString() {
      return original + ".negate()";
    }
  }

  /** Implementation of {@link #and(CharMatcher)}. */
  private static final class And extends CharMatcher {

    final CharMatcher first;
    final CharMatcher second;

    And(CharMatcher a, CharMatcher b) {
      first = Objects.requireNonNull(a);
      second = Objects.requireNonNull(b);
    }

    @Override
    public boolean matches(char c) {
      return first.matches(c) && second.matches(c);
    }

    @Override
    void setBits(BitSet table) {
      BitSet tmp1 = new BitSet();
      first.setBits(tmp1);
      BitSet tmp2 = new BitSet();
      second.setBits(tmp2);
      tmp1.and(tmp2);
      table.or(tmp1);
    }

    @Override
    public String toString() {
      return "CharMatcher.and(" + first + ", " + second + ")";
    }
  }

  /** Implementation of {@link #or(CharMatcher)}. */
  private static final class Or extends CharMatcher {

    final CharMatcher first;
    final CharMatcher second;

    Or(CharMatcher a, CharMatcher b) {
      first = Objects.requireNonNull(a);
      second = Objects.requireNonNull(b);
    }

    @Override
    void setBits(BitSet table) {
      first.setBits(table);
      second.setBits(table);
    }

    @Override
    public boolean matches(char c) {
      return first.matches(c) || second.matches(c);
    }

    @Override
    public String toString() {
      return "CharMatcher.or(" + first + ", " + second + ")";
    }
  }

  // Static factory implementations

  /** Implementation of {@link #is(char)}. */
  private static final class Is extends FastMatcher {

    private final char match;

    Is(char match) {
      this.match = match;
    }

    @Override
    public boolean matches(char c) {
      return c == match;
    }

    @Override
    public String replaceFrom(CharSequence sequence, char replacement) {
      return sequence.toString().replace(match, replacement);
    }

    @Override
    public CharMatcher and(CharMatcher other) {
      return other.matches(match) ? this : none();
    }

    @Override
    public CharMatcher or(CharMatcher other) {
      return other.matches(match) ? other : super.or(other);
    }

    @Override
    public CharMatcher negate() {
      return isNot(match);
    }

    @Override
    void setBits(BitSet table) {
      table.set(match);
    }

    @Override
    public String toString() {
      return "CharMatcher.is('" + showCharacter(match) + "')";
    }
  }

  /** Implementation of {@link #isNot(char)}. */
  private static final class IsNot extends FastMatcher {

    private final char match;

    IsNot(char match) {
      this.match = match;
    }

    @Override
    public boolean matches(char c) {
      return c != match;
    }

    @Override
    public CharMatcher and(CharMatcher other) {
      return other.matches(match) ? super.and(other) : other;
    }

    @Override
    public CharMatcher or(CharMatcher other) {
      return other.matches(match) ? any() : this;
    }

    @Override
    void setBits(BitSet table) {
      table.set(0, match);
      table.set(match + 1, Character.MAX_VALUE + 1);
    }

    @Override
    public CharMatcher negate() {
      return is(match);
    }

    @Override
    public String toString() {
      return "CharMatcher.isNot('" + showCharacter(match) + "')";
    }
  }

  private static CharMatcher.IsEither isEither(char c1, char c2) {
    return new CharMatcher.IsEither(c1, c2);
  }

  /** Implementation of {@link #anyOf(CharSequence)} for exactly two characters. */
  private static final class IsEither extends FastMatcher {

    private final char match1;
    private final char match2;

    IsEither(char match1, char match2) {
      this.match1 = match1;
      this.match2 = match2;
    }

    @Override
    public boolean matches(char c) {
      return c == match1 || c == match2;
    }

    @Override
    void setBits(BitSet table) {
      table.set(match1);
      table.set(match2);
    }

    @Override
    public String toString() {
      return "CharMatcher.anyOf(\"" + showCharacter(match1) + showCharacter(match2) + "\")";
    }
  }

  /** Implementation of {@link #anyOf(CharSequence)} for three or more characters. */
  private static final class AnyOf extends CharMatcher {

    private final char[] chars;

    public AnyOf(CharSequence chars) {
      this.chars = chars.toString().toCharArray();
      Arrays.sort(this.chars);
    }

    @Override
    public boolean matches(char c) {
      return Arrays.binarySearch(chars, c) >= 0;
    }

    @Override
    void setBits(BitSet table) {
      for (char c : chars) {
        table.set(c);
      }
    }

    @Override
    public String toString() {
      StringBuilder description = new StringBuilder("CharMatcher.anyOf(\"");
      for (char c : chars) {
        description.append(showCharacter(c));
      }
      description.append("\")");
      return description.toString();
    }
  }

  /** Implementation of {@link #inRange(char, char)}. */
  private static final class InRange extends FastMatcher {

    private final char startInclusive;
    private final char endInclusive;

    InRange(char startInclusive, char endInclusive) {
      if (endInclusive < startInclusive) {
        throw new IllegalArgumentException();
      }
      this.startInclusive = startInclusive;
      this.endInclusive = endInclusive;
    }

    @Override
    public boolean matches(char c) {
      return startInclusive <= c && c <= endInclusive;
    }

    @Override
    void setBits(BitSet table) {
      table.set(startInclusive, endInclusive + 1);
    }

    @Override
    public String toString() {
      return "CharMatcher.inRange('"
          + showCharacter(startInclusive)
          + "', '"
          + showCharacter(endInclusive)
          + "')";
    }
  }
}
