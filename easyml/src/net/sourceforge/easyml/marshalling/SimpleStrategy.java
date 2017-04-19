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
