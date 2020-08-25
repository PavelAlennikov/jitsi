/**
 *
 * Copyright 2003-2007 Jive Software, 2016-2020 Florian Schmaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.java.sip.communicator.impl.protocol.jabber.httpupload;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

/**
 * A collection of utility methods for String objects.
 */
public class StringUtils {

    public static final String MD5 = "MD5";
    public static final String SHA1 = "SHA-1";

    /**
     * Deprecated, do not use.
     *
     * @deprecated use StandardCharsets.UTF_8 instead.
     */
    // TODO: Remove in Smack 4.5.
    @Deprecated
    public static final String UTF8 = "UTF-8";

    /**
     * Deprecated, do not use.
     *
     * @deprecated use StandardCharsets.US_ASCII instead.
     */
    // TODO: Remove in Smack 4.5.
    @Deprecated
    public static final String USASCII = "US-ASCII";

    public static final String QUOTE_ENCODE = "&quot;";
    public static final String APOS_ENCODE = "&apos;";
    public static final String AMP_ENCODE = "&amp;";
    public static final String LT_ENCODE = "&lt;";
    public static final String GT_ENCODE = "&gt;";

    public static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    /**
     * Escape <code>input</code> for XML.
     *
     * @param input the input to escape.
     * @return the XML escaped variant of <code>input</code>.
     */
    public static CharSequence escapeForXml(CharSequence input) {
        return escapeForXml(input, XmlEscapeMode.safe);
    }

    /**
     * Escape <code>input</code> for XML.
     *
     * @param input the input to escape.
     * @return the XML escaped variant of <code>input</code>.
     * @since 4.2
     */
    public static CharSequence escapeForXmlAttribute(CharSequence input) {
        return escapeForXml(input, XmlEscapeMode.forAttribute);
    }

    /**
     * Escape <code>input</code> for XML.
     * <p>
     * This is an optimized variant of {@link #escapeForXmlAttribute(CharSequence)} for XML where the
     * XML attribute is quoted using ''' (Apos).
     * </p>
     *
     * @param input the input to escape.
     * @return the XML escaped variant of <code>input</code>.
     * @since 4.2
     */
    public static CharSequence escapeForXmlAttributeApos(CharSequence input) {
        return escapeForXml(input, XmlEscapeMode.forAttributeApos);
    }

    /**
     * Escape <code>input</code> for XML.
     *
     * @param input the input to escape.
     * @return the XML escaped variant of <code>input</code>.
     * @since 4.2
     */
    public static CharSequence escapeForXmlText(CharSequence input) {
        return escapeForXml(input, XmlEscapeMode.forText);
    }

    private enum XmlEscapeMode {
        safe,
        forAttribute,
        forAttributeApos,
        forText,
    }

    /**
     * Escapes all necessary characters in the CharSequence so that it can be used
     * in an XML doc.
     *
     * @param input the CharSequence to escape.
     * @return the string with appropriate characters escaped.
     */
    private static CharSequence escapeForXml(final CharSequence input, final XmlEscapeMode xmlEscapeMode) {
        if (input == null) {
            return null;
        }
        final int len = input.length();
        final StringBuilder out = new StringBuilder((int) (len * 1.3));
        CharSequence toAppend;
        char ch;
        int last = 0;
        int i = 0;
        while (i < len) {
            toAppend = null;
            ch = input.charAt(i);
            switch (xmlEscapeMode) {
            case safe:
                switch (ch) {
                case '<':
                    toAppend = LT_ENCODE;
                    break;
                case '>':
                    toAppend = GT_ENCODE;
                    break;
                case '&':
                    toAppend = AMP_ENCODE;
                    break;
                case '"':
                    toAppend = QUOTE_ENCODE;
                    break;
                case '\'':
                    toAppend = APOS_ENCODE;
                    break;
                default:
                    break;
                }
                break;
            case forAttribute:
                // No need to escape '>' for attributes.
                switch (ch) {
                case '<':
                    toAppend = LT_ENCODE;
                    break;
                case '&':
                    toAppend = AMP_ENCODE;
                    break;
                case '"':
                    toAppend = QUOTE_ENCODE;
                    break;
                case '\'':
                    toAppend = APOS_ENCODE;
                    break;
                default:
                    break;
                }
                break;
            case forAttributeApos:
                // No need to escape '>' and '"' for attributes using '\'' as quote.
                switch (ch) {
                case '<':
                    toAppend = LT_ENCODE;
                    break;
                case '&':
                    toAppend = AMP_ENCODE;
                    break;
                case '\'':
                    toAppend = APOS_ENCODE;
                    break;
                default:
                    break;
                }
                break;
            case forText:
                // No need to escape '"', '\'', and '>' for text.
                switch (ch) {
                case '<':
                    toAppend = LT_ENCODE;
                    break;
                case '&':
                    toAppend = AMP_ENCODE;
                    break;
                default:
                    break;
                }
                break;
            }
            if (toAppend != null) {
                if (i > last) {
                    out.append(input, last, i);
                }
                out.append(toAppend);
                last = ++i;
            } else {
                i++;
            }
        }
        if (last == 0) {
            return input;
        }
        if (i > last) {
            out.append(input, last, i);
        }
        return out;
    }

    /**
     * Encodes an array of bytes as String representation of hexadecimal.
     *
     * @param bytes an array of bytes to convert to a hex string.
     * @return generated hex string.
     */
    public static String encodeHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_CHARS[v >>> 4];
            hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] toUtf8Bytes(String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 24 upper case characters from the latin alphabet and numbers without '0' and 'O'.
     */
    public static final String UNAMBIGUOUS_NUMBERS_AND_LETTERS_STRING = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZ";

    /**
     * 24 upper case characters from the latin alphabet and numbers without '0' and 'O'.
     */
    private static final char[] UNAMBIGUOUS_NUMBERS_AND_LETTERS = UNAMBIGUOUS_NUMBERS_AND_LETTERS_STRING.toCharArray();

    public static String randomString(final int length, Random random) {
        if (length == 0) {
            return "";
        }

        char[] randomChars = new char[length];
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(UNAMBIGUOUS_NUMBERS_AND_LETTERS.length);
            randomChars[i] = UNAMBIGUOUS_NUMBERS_AND_LETTERS[index];
        }
        return new String(randomChars);
    }

    /**
     * Returns true if CharSequence is not null and is not empty, false otherwise.
     * Examples:
     *    isNotEmpty(null) - false
     *    isNotEmpty("") - false
     *    isNotEmpty(" ") - true
     *    isNotEmpty("empty") - true
     *
     * @param cs checked CharSequence
     * @return true if string is not null and is not empty, false otherwise
     */
    public static boolean isNotEmpty(CharSequence cs) {
        return !isNullOrEmpty(cs);
    }

    /**
     * Returns true if the given CharSequence is null or empty.
     *
     * @param cs TODO javadoc me please
     * @return true if the given CharSequence is null or empty
     */
    public static boolean isNullOrEmpty(CharSequence cs) {
        return cs == null || isEmpty(cs);
    }

    /**
     * Returns true if all given CharSequences are not empty.
     *
     * @param css the CharSequences to test.
     * @return true if all given CharSequences are not empty.
     */
    public static boolean isNotEmpty(CharSequence... css) {
        for (CharSequence cs : css) {
            if (StringUtils.isNullOrEmpty(cs)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if all given CharSequences are either null or empty.
     *
     * @param css the CharSequences to test.
     * @return true if all given CharSequences are null or empty.
     */
    public static boolean isNullOrEmpty(CharSequence... css) {
        for (CharSequence cs : css) {
            if (StringUtils.isNotEmpty(cs)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNullOrNotEmpty(CharSequence cs) {
        if (cs == null) {
            return true;
        }
        return !cs.toString().isEmpty();
    }

    /**
     * Returns true if the given CharSequence is empty.
     *
     * @param cs TODO javadoc me please
     * @return true if the given CharSequence is empty
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs.length() == 0;
    }



    public static String returnIfNotEmptyTrimmed(String string) {
        if (string == null)
            return null;
        String trimmedString = string.trim();
        if (trimmedString.length() > 0) {
            return trimmedString;
        } else {
            return null;
        }
    }

    public static boolean nullSafeCharSequenceEquals(CharSequence csOne, CharSequence csTwo) {
        return nullSafeCharSequenceComparator(csOne, csTwo) == 0;
    }

    public static int nullSafeCharSequenceComparator(CharSequence csOne, CharSequence csTwo) {
        if (csOne == null ^ csTwo == null) {
            return (csOne == null) ? -1 : 1;
        }
        if (csOne == null && csTwo == null) {
            return 0;
        }
        return csOne.toString().compareTo(csTwo.toString());
    }

    /**
     * Require a {@link CharSequence} to be neither null, nor empty.
     *
     * @deprecated use {@link #requireNotNullNorEmpty(CharSequence, String)} instead.
     * @param cs CharSequence
     * @param message error message
     * @param <CS> CharSequence type
     * @return cs TODO javadoc me please
     */
    @Deprecated
    public static <CS extends CharSequence> CS requireNotNullOrEmpty(CS cs, String message) {
        return requireNotNullNorEmpty(cs, message);
    }

    /**
     * Require a {@link CharSequence} to be neither null, nor empty.
     *
     * @param cs CharSequence
     * @param message error message
     * @param <CS> CharSequence type
     * @return cs TODO javadoc me please
     */
    public static <CS extends CharSequence> CS requireNotNullNorEmpty(CS cs, String message) {
        if (isNullOrEmpty(cs)) {
            throw new IllegalArgumentException(message);
        }
        return cs;
    }

    public static <CS extends CharSequence> CS requireNullOrNotEmpty(CS cs, String message) {
        if (cs == null) {
            return null;
        }
        if (isEmpty(cs)) {
            throw new IllegalArgumentException(message);
        }
        return cs;
    }

    /**
     * Return the String representation of the given char sequence if it is not null.
     *
     * @param cs the char sequence or null.
     * @return the String representation of <code>cs</code> or null.
     */
    public static String maybeToString(CharSequence cs) {
        if (cs == null) {
            return null;
        }
        return cs.toString();
    }

    /**
     * Defined by XML 1.0 § 2.3 as:
     *  S      ::=      (#x20 | #x9 | #xD | #xA)+
     *
     * @see <a href="https://www.w3.org/TR/xml/#sec-white-space">XML 1.0 § 2.3</a>
     */
    private static final Pattern XML_WHITESPACE = Pattern.compile("[\t\n\r ]");

    public static String deleteXmlWhitespace(String string) {
        return XML_WHITESPACE.matcher(string).replaceAll("");
    }

    public static Appendable appendHeading(Appendable appendable, String heading) throws IOException {
        return appendHeading(appendable, heading, '-');
    }

    public static Appendable appendHeading(Appendable appendable, String heading, char underlineChar) throws IOException {
        appendable.append(heading).append('\n');
        for (int i = 0; i < heading.length(); i++) {
            appendable.append(underlineChar);
        }
        return appendable.append('\n');
    }

    public static final String PORTABLE_NEWLINE_REGEX = "\\r?\\n";

    public static List<String> splitLinesPortable(String input) {
        String[] lines = input.split(PORTABLE_NEWLINE_REGEX);
        return Arrays.asList(lines);
    }
}
