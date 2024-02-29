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

import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.*;

import java.util.Collections;
import java.util.Set;

/**
 * EmptySetStrategy class that implements {@linkplain CompositeStrategy} for
 * the {@linkplain Collections#emptySet()} implementation.
 * This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.3
 * @since 1.5.3
 */
public final class EmptySetStrategy extends AbstractStrategy implements CompositeStrategy<Set> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "empty-set";
    /**
     * Constant defining the singleton instance.
     */
    public static final EmptySetStrategy INSTANCE = new EmptySetStrategy();
    private static final Class TARGET = Collections.emptySet().getClass();

    private EmptySetStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class target() {
        return EmptySetStrategy.TARGET;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return EmptySetStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Set target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(EmptySetStrategy.NAME);
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set unmarshalInit(Set target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
        // consume root tag:
        reader.next();
        if (reader.atElementEnd() && reader.elementName().equals(EmptySetStrategy.NAME)) {
            return target;
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "unexpected element end");
    }
}
