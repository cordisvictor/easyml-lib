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

/**
 * CompositeReader interface is used by {@linkplain CompositeStrategy} instances
 * to read a composite datatype from XML format.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.2.2
 * @since 1.0
 */
public interface CompositeReader extends CompositeAttributeReader {

    /**
     * Moves this instance to the next element tag returning true if any, false
     * otherwise.
     *
     * @return true if at next tag, false if at document end
     */
    boolean next();

    /**
     * Returns true if this instance is at an element start tag.
     *
     * @return true if at start, false otherwise
     */
    boolean atElementStart();

    /**
     * Returns true if this instance is at an element end tag.
     *
     * @return true if at end, false otherwise
     */
    boolean atElementEnd();

    /**
     * Returns the name of the element this instance is at. This method can be
     * invoked only if {@linkplain #atElementStart()} or
     * {@linkplain #atElementEnd()}.
     *
     * @return the element name
     */
    String elementName();

    /**
     * Reads a boolean from XML.
     *
     * @return boolean read
     */
    boolean readBoolean();

    /**
     * Reads a char from XML.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @return char read
     */
    char readChar();

    /**
     * Reads a byte from XML.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @return byte read
     */
    byte readByte();

    /**
     * Reads a short from XML.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @return short read
     */
    short readShort();

    /**
     * Reads a double from XML.
     *
     * @return double read
     */
    double readDouble();

    /**
     * Reads a float from XML.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @return float read
     */
    float readFloat();

    /**
     * Reads an int from XML.
     *
     * @return int read
     */
    int readInt();

    /**
     * Reads a long from XML.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @return long read
     */
    long readLong();

    /**
     * Reads a String from XML.
     *
     * @return String read
     */
    String readString();

    /**
     * Reads the object starting from the element start this instance is at and
     * returns it.
     * <br/>
     * <b>Note: This method can only be invoked if this instance is at an
     * element start.</b>
     *
     * @return the read sub-object
     */
    Object read();

    /**
     * Reads the array-object starting from the element start this instance is
     * at and returns an array of <code>componentType</code>, if and only if the
     * current start element is an array. This method is differs from
     * {@linkplain #read()} in that it allows the exact array class to be
     * specified.
     * <br/>
     * <b>Note: This method can only be invoked <b>if</b> this instance is at an
     * element start.</b>
     *
     * @param componentType the resulting array component type
     * @return the read array-object
     */
    Object readArray(Class componentType);

    /**
     * Reads the string value of the element this instance is at directly, not
     * needing to delegate the reading to a {@linkplain SimpleStrategy}.
     * <br/>
     * <b>Note: This method can be invoked only once, if this instance is at an
     * element start.</b>
     *
     * @return the read string value
     */
    String readValue();
}
