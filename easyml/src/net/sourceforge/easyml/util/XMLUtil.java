/*
 * Copyright 2012 Victor Cordis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Please contact the author ( cordis.victor@gmail.com ) if you need additional
 * information or have any questions.
 */
package net.sourceforge.easyml.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XMLUtil utility class used to format the XML element and element attribute
 * values by escaping and un-escaping illegal XML characters.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.7.4
 * @since 1.0
 */
public final class XMLUtil {

    /**
     * Constant holding the illegal <code>$</code>.
     */
    public static final char XML_TAG_ILLEGAL_$ = '$';
    /**
     * Constant holding the escaped form of <code>$</code>.
     */
    public static final String XML_TAG_LEGAL_$ = "_-_";
    private static final String XML_TAG_ILLEGAL_$_QUOTED = Matcher.quoteReplacement(Character.toString(XML_TAG_ILLEGAL_$));
    private static final Pattern XML_TAG_LEGAL_$_PATTERN = Pattern.compile(Pattern.quote(XML_TAG_LEGAL_$));
    private static final char XML_ILLEGAL_LT = '<';
    private static final char XML_ILLEGAL_GT = '>';
    private static final char XML_ILLEGAL_AMP = '&';
    private static final char XML_ILLEGAL_QUOT = '"';
    private static final char XML_ILLEGAL_APOS = '\'';
    private static final char XML_ILLEGAL_CR = '\r';
    private static final String XML_LEGAL_LT = "&lt;";
    private static final String XML_LEGAL_GT = "&gt;";
    private static final String XML_LEGAL_AMP = "&amp;";
    private static final String XML_LEGAL_QUOT = "&quot;";
    private static final String XML_LEGAL_APOS = "&apos;";
    private static final String XML_LEGAL_CR = "&#13;";

    /**
     * Returns true if the input text is free from illegal XML characters, false otherwise.
     *
     * @param text the text to test
     * @return true if text doesn't contain illegal XML characters, false otherwise
     */
    public static boolean isLegalXMLText(String text) {
        return findIllegalXMLCharIn(text) == -1;
    }

    private static int findIllegalXMLCharIn(String text) {
        final int len = text.length();
        for (int i = 0; i < len; i++) {
            final char crt = text.charAt(i);
            if (crt == XML_ILLEGAL_LT
                    || crt == XML_ILLEGAL_GT
                    || crt == XML_ILLEGAL_AMP
                    || crt == XML_ILLEGAL_QUOT
                    || crt == XML_ILLEGAL_APOS
                    || crt == XML_ILLEGAL_CR) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns true if the input text is a legal XML tag.
     *
     * @param tag to test if legal
     * @return true if tag is a legal XML tag, false otherwise
     */
    public static boolean isLegalXMLTag(String tag) {
        if (tag == null || tag.isEmpty() || !Character.isLetter(tag.charAt(0))) {
            return false;
        }
        final int len = tag.length();
        for (int i = 1; i < len; i++) {
            final char crt = tag.charAt(i);
            if (!Character.isLetterOrDigit(crt) && crt != '-' && crt != '_') {
                return false;
            }
        }
        return true;
    }

    /**
     * Escapes the illegal chars in the input string, if any, and returns the
     * escaped string.
     *
     * @param text the string to escape
     * @return the escaped string
     */
    public static String escapeXML(String text) {
        final int illegalStartIdx = findIllegalXMLCharIn(text);
        if (illegalStartIdx == -1) {
            return text; // all legal.
        }
        final int len = text.length();
        final StringBuilder sb = new StringBuilder(len + 10);
        // copy initial legal part:
        for (int i = 0; i < illegalStartIdx; i++) {
            sb.append(text.charAt(i));
        }
        // escape the remaining part:
        for (int i = illegalStartIdx; i < len; i++) {
            appendEscaped(sb, text.charAt(i));
        }
        return sb.toString();
    }

    private static void appendEscaped(StringBuilder destination, char c) {
        if (c == XML_ILLEGAL_LT) {
            destination.append(XML_LEGAL_LT);
        } else if (c == XML_ILLEGAL_GT) {
            destination.append(XML_LEGAL_GT);
        } else if (c == XML_ILLEGAL_AMP) {
            destination.append(XML_LEGAL_AMP);
        } else if (c == XML_ILLEGAL_QUOT) {
            destination.append(XML_LEGAL_QUOT);
        } else if (c == XML_ILLEGAL_APOS) {
            destination.append(XML_LEGAL_APOS);
        } else if (c == XML_ILLEGAL_CR) {
            destination.append(XML_LEGAL_CR);
        } else {
            destination.append(c);
        }
    }

    /**
     * Escapes <code>$</code> in the given tag.
     *
     * @param tag to escape
     * @return the escaped tag
     */
    public static String escapeXMLTag(String tag) {
        final int symbolIdx = tag.indexOf(XML_TAG_ILLEGAL_$);
        if (symbolIdx == -1) {
            return tag; // all legal.
        }
        final int len = tag.length();
        final StringBuilder sb = new StringBuilder(len + 10);
        // copy initial legal part:
        for (int i = 0; i < symbolIdx; i++) {
            sb.append(tag.charAt(i));
        }
        // escape the remaining part:
        for (int i = symbolIdx; i < len; i++) {
            final char c = tag.charAt(i);
            if (c == XML_TAG_ILLEGAL_$) {
                sb.append(XML_TAG_LEGAL_$);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Unescapes <code>$</code> in the given tag.
     *
     * @param tag to unescape
     * @return the unescaped tag
     */
    public static String unescapeXMLTag(String tag) {
        final int escapedIdx = tag.indexOf(XML_TAG_LEGAL_$);
        if (escapedIdx == -1) {
            return tag; // all legal.
        }
        return XML_TAG_LEGAL_$_PATTERN.matcher(tag).replaceAll(XML_TAG_ILLEGAL_$_QUOTED);
    }

    private XMLUtil() {
    }
}
