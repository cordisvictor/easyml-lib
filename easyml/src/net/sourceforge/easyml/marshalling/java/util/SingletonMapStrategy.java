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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

/**
 * SingletonMapStrategy class that implements {@linkplain CompositeStrategy} for
 * the {@linkplain Collections#singletonMap(java.lang.Object, java.lang.Object)
 * } map implementation. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.3.3
 * @since 1.0.2
 */
public final class SingletonMapStrategy extends AbstractStrategy<Map> implements CompositeStrategy<Map> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "singleton-map";
    /**
     * Constant defining the singleton instance.
     */
    public static final SingletonMapStrategy INSTANCE = new SingletonMapStrategy();
    private static final Class TARGET;
    private static final Field TARGET_SINGLE_K;
    private static final Field TARGET_SINGLE_V;

    static {
        TARGET = Collections.singletonMap(null, null).getClass();
        Field singleK, singleV;
        try {
            singleK = TARGET.getDeclaredField("k");
            singleK.setAccessible(true);
            singleV = TARGET.getDeclaredField("v");
            singleV.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException neverThrown) {
            singleK = null;
            singleV = null;
        }
        TARGET_SINGLE_K = singleK;
        TARGET_SINGLE_V = singleV;
    }

    private SingletonMapStrategy() {
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
        return SingletonMapStrategy.TARGET;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Map> c) {
        return c == SingletonMapStrategy.TARGET;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return SingletonMapStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Map target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(SingletonMapStrategy.NAME);
        final Map.Entry entry = (Map.Entry) target.entrySet().iterator().next();
        writer.write(entry.getKey());
        writer.write(entry.getValue());
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Map unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        return Collections.singletonMap(null, null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Map unmarshalInit(Map target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
        // consume root tag:
        reader.next();
        // read and set singleton element:
        TARGET_SINGLE_K.set(target, reader.read());
        TARGET_SINGLE_V.set(target, reader.read());
        if (reader.atElementEnd() && reader.elementName().equals(SingletonMapStrategy.NAME)) {
            return target;
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "unexpected element end");
    }
}
