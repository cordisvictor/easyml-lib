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
package net.sourceforge.easyml.marshalling.java.util;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.CompositeStrategy;
import net.sourceforge.easyml.marshalling.CompositeWriter;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * SingletonMapStrategy class that implements {@linkplain CompositeStrategy} for
 * the {@linkplain Collections#singletonMap(java.lang.Object, java.lang.Object)
 * } map implementation. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0.2
 * @version 1.3.3
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
    private static Field target_k;
    private static Field target_v;

    static {
        TARGET = Collections.singletonMap(null, null).getClass();
        try {
            target_k = TARGET.getDeclaredField("k");
            target_k.setAccessible(true);
            target_v = TARGET.getDeclaredField("v");
            target_v.setAccessible(true);
        } catch (NoSuchFieldException neverThrown) {
        } catch (SecurityException neverThrown) {
        }
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
    public Map unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
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
        target_k.set(target, reader.read());
        target_v.set(target, reader.read());
        if (reader.atElementEnd() && reader.elementName().equals(SingletonMapStrategy.NAME)) {
            return target;
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "unexpected element end");
    }
}
