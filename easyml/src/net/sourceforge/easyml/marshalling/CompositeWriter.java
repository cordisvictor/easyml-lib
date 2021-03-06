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
package net.sourceforge.easyml.marshalling;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * CompositeWriter interface is used by {@linkplain CompositeStrategy} instances
 * to write composite datatypes into XML.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.1
 * @since 1.0
 */
public interface CompositeWriter extends Consumer, IntConsumer, LongConsumer, DoubleConsumer {

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
     * Writes an attribute-equals-value pair to the current start tag attribute
     * list.
     * <br/>
     * <b>Note:</b> this writer must be at an element start tag.
     *
     * @param attribute the attribute name
     * @param value     the attribute value
     */
    void setAttribute(String attribute, String value);

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
     * @throws IllegalArgumentException if value is null
     */
    void writeValue(String value);

    /**
     * Consumes the given object by writing it.
     */
    @Override
    default void accept(Object t) {
        this.write(t);
    }

    /**
     * Consumes the given int by writing it as int.
     */
    @Override
    default void accept(int value) {
        this.writeInt(value);
    }

    /**
     * Consumes the given long by writing it as long.
     */
    @Override
    default void accept(long value) {
        this.writeLong(value);
    }

    /**
     * Consumes the given double by writing it as double.
     */
    @Override
    default void accept(double value) {
        this.writeDouble(value);
    }
}
