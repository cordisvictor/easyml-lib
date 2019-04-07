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

/**
 * XMLUtil utility class used to format the XML element and element attribute
 * values by escaping and un-escaping illegal XML characters.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.0
 * @since 1.0
 */
public final class XMLUtil {

    static final char XML_ILLEGAL_LT = '<';
    static final char XML_ILLEGAL_GT = '>';
    static final char XML_ILLEGAL_AMP = '&';
    static final char XML_ILLEGAL_QUOT = '"';
    static final char XML_ILLEGAL_APOS = '\'';
    static final char XML_ILLEGAL_CR = '\r';
    static final String XML_LEGAL_LT = "&lt;";
    static final String XML_LEGAL_GT = "&gt;";
    static final String XML_LEGAL_AMP = "&amp;";
    static final String XML_LEGAL_QUOT = "&quot;";
    static final String XML_LEGAL_APOS = "&apos;";
    static final String XML_LEGAL_CR = "&#13;";

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

    private XMLUtil() {
    }
}
