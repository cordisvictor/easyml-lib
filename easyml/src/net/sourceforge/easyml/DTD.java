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
package net.sourceforge.easyml;

/**
 * DTD class contains the DTD constants.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.3.0
 * @since 1.0
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
}
