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
package net.sourceforge.easyml;

/**
 * DTD class contains the DTD constants.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.0
 */
public final class DTD {

    /**
     * The constant defining the <code>easyml</code> XML element.
     */
    public static final String ELEMENT_EASYML = "easyml";
    /**
     * The constant defining the <code>nil</code> XML element.
     */
    public static final String ELEMENT_NIL = "nil";
    /**
     * The constant defining the <code>object</code> XML element.
     */
    public static final String ELEMENT_OBJECT = "object";
    /**
     * The constant defining the <code>array</code> XML element.
     */
    public static final String ELEMENT_ARRAY = "array";
    /**
     * The constant defining the <code>version</code> XML element attribute.
     */
    public static final String ATTRIBUTE_VERSION = "version";
    /**
     * The constant defining the <code>id</code> XML element attribute.
     */
    public static final String ATTRIBUTE_ID = "id";
    /**
     * The constant defining the <code>idref</code> XML element attribute.
     */
    public static final String ATTRIBUTE_IDREF = "idref";
    /**
     * The constant defining the <code>class</code> XML element attribute.
     */
    public static final String ATTRIBUTE_CLASS = "class";
    /**
     * The constant defining the <code>length</code> XML element attribute.
     */
    public static final String ATTRIBUTE_LENGTH = "length";
    /**
     * The constant defining the <code>base64</code> type XML element.
     */
    public static final String TYPE_BASE64 = "base64";
    /**
     * The constant defining the <code>boolean</code> type XML element.
     */
    public static final String TYPE_BOOLEAN = "boolean";
    /**
     * The constant defining the <code>double</code> type XML element.
     */
    public static final String TYPE_DOUBLE = "double";
    /**
     * The constant defining the <code>int</code> type XML element.
     */
    public static final String TYPE_INT = "int";
    /**
     * The constant defining the <code>string</code> type XML element.
     */
    public static final String TYPE_STRING = "string";
    /**
     * The constant defining the <code>date</code> type XML element.
     */
    public static final String TYPE_DATE = "date";
    /**
     * The constant defining the <code>date</code> format, which is ISO-8601.
     */
    public static final String FORMAT_DATE = "yyyy-MM-dd'T'HH:mm:ss:SSS";

    private DTD() {
    }
}//class DTD.
