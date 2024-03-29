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
package net.sourceforge.easyml.marshalling.java.util;

import net.sourceforge.easyml.marshalling.*;

import java.util.OptionalDouble;

/**
 * OptionalDoubleStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for the Java {@linkplain OptionalDouble} class.
 * This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.7.3
 * @since 1.7.3
 */
public final class OptionalDoubleStrategy extends AbstractStrategy implements CompositeStrategy<OptionalDouble> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "optional-double";
    /**
     * Constant defining the singleton instance.
     */
    public static final OptionalDoubleStrategy INSTANCE = new OptionalDoubleStrategy();
    private static final String ATTRIBUTE_EMPTY = "empty";

    private OptionalDoubleStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<OptionalDouble> target() {
        return OptionalDouble.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return OptionalDoubleStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(OptionalDouble target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(OptionalDoubleStrategy.NAME);
        if (target.isEmpty()) {
            writer.setAttribute(ATTRIBUTE_EMPTY, String.valueOf(true));
        }
        target.ifPresent(writer);
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public OptionalDouble unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        final boolean isEmpty = Boolean.parseBoolean(reader.elementAttribute(ATTRIBUTE_EMPTY));
        reader.next(); // consume optional start.
        if (isEmpty) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(reader.readDouble());
    }
}
