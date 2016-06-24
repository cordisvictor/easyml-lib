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
package net.sourceforge.easyml.marshalling;

/**
 * SimpleStrategy interface extends the {@linkplain Strategy} interface with the
 * methods used to marshal a data type to XML text and back again. The
 * responsibility of XML special characters encoding and decoding is reserved to
 * the XML output and input stream classes.
 *
 * @param <T> target class
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.0
 */
public interface SimpleStrategy<T> extends Strategy<T> {

    /**
     * Marshals the given non-null object to plain text. The responsibility of
     * encoding XML special characters is left to the encoder.
     *
     * @param target to marshal
     * @param ctx the marshalling context
     *
     * @return the text
     */
    String marshal(T target, MarshalContext ctx);

    /**
     * Un-marshals the given non-null text to it's equivalent object. The
     * responsibility of decoding XML special characters is left to the decoder.
     * Hence, the
     * <code>text</code> parameter contains the un-escaped XML text.
     *
     * @param text the input text
     * @param ctx the un-marshalling context
     *
     * @return the un-marshaled object
     */
    T unmarshal(String text, UnmarshalContext ctx);
}
