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

import java.util.Optional;

/**
 * OptionalStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for the Java {@linkplain Optional} class.
 * This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.1
 * @since 1.5.1
 */
public final class OptionalStrategy extends AbstractStrategy implements CompositeStrategy<Optional> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "optional";
    /**
     * Constant defining the singleton instance.
     */
    public static final OptionalStrategy INSTANCE = new OptionalStrategy();
    private static final String ATTRIBUTE_EMPTY = "empty";

    private OptionalStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean strict() {
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<Optional> target() {
        return Optional.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Optional> c) {
        return c == Optional.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return OptionalStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Optional target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(OptionalStrategy.NAME);
        if (!target.isPresent()) {
            writer.setAttribute(ATTRIBUTE_EMPTY, String.valueOf(true));
        }
        target.ifPresent(writer);
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Optional unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        final boolean isEmpty = Boolean.parseBoolean(reader.elementAttribute(ATTRIBUTE_EMPTY));
        reader.next(); // consume optional start.
        if (isEmpty) {
            return Optional.empty();
        }
        return Optional.of(reader.read());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object unmarshalInit(Optional target, CompositeReader reader, UnmarshalContext ctx) {
        return target;
    }
}
