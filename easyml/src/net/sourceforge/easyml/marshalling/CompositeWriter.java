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
 * CompositeWriter interface is used by {@linkplain CompositeStrategy} instances
 * to write composite datatypes into XML.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.5
 */
public interface CompositeWriter extends CompositeAttributeWriter {

    /**
     * Writes the start of an element with the given name.
     * <br/>
     * <b>Note:</b> the start tag is left open for setting element attributes.
     * <br/>
     * <b>Note:</b> all start tag attributes must be set immediately after
     * calling this method, i.e. prior to any endXXX or writeXXX methods.
     *
     * @param name of the start tag element
     */
    void startElement(String name);

    /**
     * Writes the end tag of the current element.
     * <br/>
     * <b>Note:</b> this method can only be invoked after
     * {@linkplain #startElement(java.lang.String)} and optional
     * {@linkplain #setAttribute(java.lang.String, java.lang.String)}
     * invocations.
     */
    void endElement();

    /**
     * Writes the given boolean in XML format.
     *
     * @param b to write
     */
    void writeBoolean(boolean b);

    /**
     * Writes the given char in XML format.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @param c to write
     */
    void writeChar(char c);

    /**
     * Writes the given byte in XML format.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @param b to write
     */
    void writeByte(byte b);

    /**
     * Writes the given short in XML format.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @param s to write
     */
    void writeShort(short s);

    /**
     * Writes the given double in XML format.
     *
     * @param d to write
     */
    void writeDouble(double d);

    /**
     * Writes the given float in XML format.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @param f to write
     */
    void writeFloat(float f);

    /**
     * Writes the given int in XML format.
     *
     * @param i to write
     */
    void writeInt(int i);

    /**
     * Writes the given long in XML format.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @param l to write
     */
    void writeLong(long l);

    /**
     * Writes the given String in XML format.
     *
     * @param s to write
     */
    void writeString(String s);

    /**
     * Writes the given object in XML format. The input data can, in turn, be a
     * simple or composite object.
     *
     * @param o to write
     */
    void write(Object o);

    /**
     * Writes the given simple <code>value</code> type directly, not needing to
     * delegate the writing to a {@linkplain SimpleStrategy}.
     *
     * @param value the non-null string representation of the simple value type
     *
     * @throws IllegalArgumentException if value is null
     */
    void writeValue(String value);
}//interface CompositeWriter.
