/*
 * Copyright (c) 2011, Victor Cordis. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of EasyML library.
 *
 * EasyML library is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License (LGPL) as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * EasyML library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with EasyML library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Please contact the author ( cordis.victor@gmail.com ) if you need additional
 * information or have any questions.
 */
package net.sourceforge.easyml.util;

import java.util.regex.Pattern;

/**
 * XMLUtil utility class used to format the XML element and element attribute
 * values by escaping and un-escaping illegal XML characters.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.5
 */
public final class XMLUtil {

    /**
     * Constant holding the <code>&amp;#13;</code> string.
     */
    public static final String XML_LEGAL_CR = "&#13;";
    /**
     * Constant holding the <code>&amp;lt;</code> string.
     */
    public static final String XML_LEGAL_LT = "&lt;";
    /**
     * Constant holding the <code>&amp;gt;</code> string.
     */
    public static final String XML_LEGAL_GT = "&gt;";
    /**
     * Constant holding the <code>&amp;amp;</code> string.
     */
    public static final String XML_LEGAL_AMP = "&amp;";
    /**
     * Constant holding the <code>&amp;quot;</code> string.
     */
    public static final String XML_LEGAL_QUOT = "&quot;";
    /**
     * Constant holding the <code>&amp;apos;</code> string.
     */
    public static final String XML_LEGAL_APOS = "&apos;";
    /**
     * Constant holding the <code>\r</code> character.
     */
    public static final char XML_ILLEGAL_CR = '\r';
    /**
     * Constant holding the <code>&lt;</code> character.
     */
    public static final char XML_ILLEGAL_LT = '<';
    /**
     * Constant holding the <code>&gt;</code> character.
     */
    public static final char XML_ILLEGAL_GT = '>';
    /**
     * Constant holding the <code>&amp;</code> character.
     */
    public static final char XML_ILLEGAL_AMP = '&';
    /**
     * Constant holding the <code>&quot;</code> character.
     */
    public static final char XML_ILLEGAL_QUOT = '"';
    /**
     * Constant holding the <code>&apos;</code> character.
     */
    public static final char XML_ILLEGAL_APOS = '\'';
    /**
     * Pattern used to match text against illegal XML characters.
     */
    private static final Pattern illegalXMLText = Pattern.compile("[\r<>&\"']");

    /**
     * Returns true if the input text contains the one or more illegal XML
     * characters, false otherwise.
     *
     * @param text the text to test
     *
     * @return true if text contains at least one illegal XML character, false
     * otherwise
     */
    public static boolean isIllegalXMLText(String text) {
        return XMLUtil.illegalXMLText.matcher(text).find();
    }

    /**
     * Returns true if the input text is a legal XML tag.
     *
     * @param tag to test if legal
     *
     * @return true if tag is a legal XML tag, false otherwise
     */
    public static boolean isLegalXMLTag(String tag) {
        if (tag == null || tag.isEmpty()) {
            return false;
        }
        if (!Character.isLetter(tag.charAt(0))) {
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
     * Escapes the input character if illegal or returns it's string value if
     * legal.
     *
     * @param c the char to escape
     *
     * @return the string value of <code>c</code> or it's corresponding escape
     */
    public static String escapeXML(char c) {
        if (c == XMLUtil.XML_ILLEGAL_CR) {
            return XMLUtil.XML_LEGAL_CR;
        }
        if (c == XMLUtil.XML_ILLEGAL_LT) {
            return XMLUtil.XML_LEGAL_LT;
        }
        if (c == XMLUtil.XML_ILLEGAL_GT) {
            return XMLUtil.XML_LEGAL_GT;
        }
        if (c == XMLUtil.XML_ILLEGAL_AMP) {
            return XMLUtil.XML_LEGAL_AMP;
        }
        if (c == XMLUtil.XML_ILLEGAL_QUOT) {
            return XMLUtil.XML_LEGAL_QUOT;
        }
        if (c == XMLUtil.XML_ILLEGAL_APOS) {
            return XMLUtil.XML_LEGAL_APOS;
        }
        return String.valueOf(c);
    }

    /**
     * Escapes the illegal chars in the input string, if any, and returns the
     * escaped string.
     *
     * @param text the string to escape
     *
     * @return the escaped string
     */
    public static String escapeXML(String text) {
        final int initLength = text.length();
        for (int index = 0; index < initLength; index++) {
            char crt = text.charAt(index);
            if (crt == XMLUtil.XML_ILLEGAL_CR
                    || crt == XMLUtil.XML_ILLEGAL_LT
                    || crt == XMLUtil.XML_ILLEGAL_GT
                    || crt == XMLUtil.XML_ILLEGAL_AMP
                    || crt == XMLUtil.XML_ILLEGAL_QUOT
                    || crt == XMLUtil.XML_ILLEGAL_APOS) {
                final StringBuilder sb = new StringBuilder(text);
                do {
                    if (crt == XMLUtil.XML_ILLEGAL_CR) {
                        sb.replace(index, index + 1, XMLUtil.XML_LEGAL_CR);
                        index += 5;
                    } else if (crt == XMLUtil.XML_ILLEGAL_LT) {
                        sb.replace(index, index + 1, XMLUtil.XML_LEGAL_LT);
                        index += 4;
                    } else if (crt == XMLUtil.XML_ILLEGAL_GT) {
                        sb.replace(index, index + 1, XMLUtil.XML_LEGAL_GT);
                        index += 4;
                    } else if (crt == XMLUtil.XML_ILLEGAL_AMP) {
                        sb.replace(index, index + 1, XMLUtil.XML_LEGAL_AMP);
                        index += 5;
                    } else if (crt == XMLUtil.XML_ILLEGAL_QUOT) {
                        sb.replace(index, index + 1, XMLUtil.XML_LEGAL_QUOT);
                        index += 6;
                    } else if (crt == XMLUtil.XML_ILLEGAL_APOS) {
                        sb.replace(index, index + 1, XMLUtil.XML_LEGAL_APOS);
                        index += 6;
                    } else {
                        index++;
                    }
                    if (index < sb.length()) {
                        crt = sb.charAt(index);
                    } else {
                        return sb.toString();
                    }
                } while (true);
            }
        }
        return text;
    }

    /**
     * Validates the given <code>alias</code>.
     *
     * @param alias to validate
     *
     * @throws IllegalArgumentException if alias is invalid
     */
    public static void validateAlias(String alias) {
        if (alias == null || alias.isEmpty() || XMLUtil.isIllegalXMLText(alias)) {
            throw new IllegalArgumentException("alias: null, empty, or contains illegal XML chars: " + alias);
        }
    }

    private XMLUtil() {
    }
}
