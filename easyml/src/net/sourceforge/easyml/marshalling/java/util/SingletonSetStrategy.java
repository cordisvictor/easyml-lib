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
import java.util.Set;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.CompositeStrategy;
import net.sourceforge.easyml.marshalling.CompositeWriter;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * SingletonSetStrategy class that implements {@linkplain CompositeStrategy} for
 * the {@linkplain Collections#singleton(java.lang.Object) } set implementation.
 * This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0.2
 * @version 1.3.3
 */
public final class SingletonSetStrategy extends AbstractStrategy<Set> implements CompositeStrategy<Set> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "singleton-set";
    /**
     * Constant defining the singleton instance.
     */
    public static final SingletonSetStrategy INSTANCE = new SingletonSetStrategy();
    private static final Class TARGET;
    private static Field target_element;

    static {
        TARGET = Collections.singleton(null).getClass();
        try {
            target_element = TARGET.getDeclaredField("element");
            target_element.setAccessible(true);
        } catch (NoSuchFieldException neverThrown) {
        } catch (SecurityException neverThrown) {
        }
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
    public Set unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
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
        target_element.set(target, reader.read());
        if (reader.atElementEnd() && reader.elementName().equals(SingletonSetStrategy.NAME)) {
            return target;
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "unexpected element end");
    }
}
