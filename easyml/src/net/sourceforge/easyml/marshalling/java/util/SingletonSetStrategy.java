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
import net.sourceforge.easyml.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;

/**
 * SingletonSetStrategy class that implements {@linkplain CompositeStrategy} for
 * the {@linkplain Collections#singleton(java.lang.Object) } set implementation.
 * This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.4.7
 * @since 1.0.2
 */
public final class SingletonSetStrategy extends AbstractStrategy implements CompositeStrategy<Set> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "singleton-set";
    /**
     * Constant defining the singleton instance.
     */
    public static final SingletonSetStrategy INSTANCE = new SingletonSetStrategy();
    private static final Class TARGET;
    private static final Field TARGET_ELEMENT;

    static {
        TARGET = Collections.singleton(null).getClass();
        Field singletonListElement;
        try {
            singletonListElement = TARGET.getDeclaredField("element");
            ReflectionUtil.setAccessible(singletonListElement);
        } catch (NoSuchFieldException | SecurityException ignored) {
            singletonListElement = null;
        }
        TARGET_ELEMENT = singletonListElement;
    }

    private SingletonSetStrategy() {
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
    public Class target() {
        return SingletonSetStrategy.TARGET;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Set> c) {
        return c == SingletonSetStrategy.TARGET;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return SingletonSetStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Set target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(SingletonSetStrategy.NAME);
        writer.write(target.iterator().next());
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        return Collections.singleton(null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set unmarshalInit(Set target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
        // consume root tag:
        reader.next();
        // read and set singleton element:
        TARGET_ELEMENT.set(target, reader.read());
        if (reader.atElementEnd() && reader.elementName().equals(SingletonSetStrategy.NAME)) {
            return target;
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "unexpected element end");
    }
}
