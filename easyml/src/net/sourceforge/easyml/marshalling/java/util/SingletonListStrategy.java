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
import java.util.List;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.CompositeStrategy;
import net.sourceforge.easyml.marshalling.CompositeWriter;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * SingletonListStrategy class that implements {@linkplain CompositeStrategy}
 * for the {@linkplain Collections#singletonList(java.lang.Object) } list
 * implementation. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0.2
 * @version 1.3.3
 */
public final class SingletonListStrategy extends AbstractStrategy<List> implements CompositeStrategy<List> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "singleton-lst";
    /**
     * Constant defining the singleton instance.
     */
    public static final SingletonListStrategy INSTANCE = new SingletonListStrategy();
    private static final Class TARGET;
    private static final Field TARGET_ELEMENT;

    static {
        TARGET = Collections.singletonList(null).getClass();
        Field singletonListElement;
        try {
            singletonListElement = TARGET.getDeclaredField("element");
            singletonListElement.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException neverThrown) {
            singletonListElement = null;
        }
        TARGET_ELEMENT = singletonListElement;
    }

    private SingletonListStrategy() {
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
        return SingletonListStrategy.TARGET;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<List> c) {
        return c == SingletonListStrategy.TARGET;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return SingletonListStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(List target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(SingletonListStrategy.NAME);
        writer.write(target.get(0));
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return Collections.singletonList(null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List unmarshalInit(List target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
        // consume root tag:
        reader.next();
        // read and set singleton element:
        TARGET_ELEMENT.set(target, reader.read());
        if (reader.atElementEnd() && reader.elementName().equals(SingletonListStrategy.NAME)) {
            return target;
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "unexpected element end");
    }
}
