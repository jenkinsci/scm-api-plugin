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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Parser for a map of reversed domain names stored as a serialized radix tree.
 *
 * <p>Adapted from Guava 31.0.1. Usage only internal to this plugin. Marked as restricted to prevent
 * any further usages. This should be switched to upstream when it is no longer beta.
 */
@Restricted(NoExternalUse.class)
final class TrieParser {
  /**
   * Parses a serialized trie representation of a map of reversed public suffixes into an immutable
   * map of public suffixes.
   */
  static Map<String, PublicSuffixType> parseTrie(CharSequence encoded) {
    Map<String, PublicSuffixType> builder = new HashMap<>();
    int encodedLen = encoded.length();
    int idx = 0;
    while (idx < encodedLen) {
      idx += doParseTrieToBuilder(new ArrayDeque<>(), encoded, idx, builder);
    }
    return Collections.unmodifiableMap(builder);
  }

  /**
   * Parses a trie node and returns the number of characters consumed.
   *
   * @param stack The prefixes that precede the characters represented by this node. Each entry of
   *     the stack is in reverse order.
   * @param encoded The serialized trie.
   * @param start An index in the encoded serialized trie to begin reading characters from.
   * @param builder A map builder to which all entries will be added.
   * @return The number of characters consumed from {@code encoded}.
   */
  private static int doParseTrieToBuilder(
      Deque<CharSequence> stack,
      CharSequence encoded,
      int start,
      Map<String, PublicSuffixType> builder) {

    int encodedLen = encoded.length();
    int idx = start;
    char c = '\0';

    // Read all of the characters for this node.
    for (; idx < encodedLen; idx++) {
      c = encoded.charAt(idx);
      if (c == '&' || c == '?' || c == '!' || c == ':' || c == ',') {
        break;
      }
    }

    stack.push(reverse(encoded.subSequence(start, idx)));

    if (c == '!' || c == '?' || c == ':' || c == ',') {
      // '!' represents an interior node that represents a REGISTRY entry in the map.
      // '?' represents a leaf node, which represents a REGISTRY entry in map.
      // ':' represents an interior node that represents a private entry in the map
      // ',' represents a leaf node, which represents a private entry in the map.
      String domain = String.join("", stack);
      if (domain.length() > 0) {
        builder.put(domain, PublicSuffixType.fromCode(c));
      }
    }
    idx++;

    if (c != '?' && c != ',') {
      while (idx < encodedLen) {
        // Read all the children
        idx += doParseTrieToBuilder(stack, encoded, idx, builder);
        if (encoded.charAt(idx) == '?' || encoded.charAt(idx) == ',') {
          // An extra '?' or ',' after a child node indicates the end of all children of this node.
          idx++;
          break;
        }
      }
    }
    stack.pop();
    return idx - start;
  }

  private static CharSequence reverse(CharSequence s) {
    return new StringBuilder(s).reverse();
  }
}
