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
import java.util.Map;
import java.util.Properties;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.*;

/**
 * PropertiesStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for the Java {@linkplain Properties} class. This implementation is
 * for treating the target class in a special way rather than as a regular
 * {@linkplain Map}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.3
 */
public final class PropertiesStrategy extends AbstractStrategy<Properties>
        implements CompositeStrategy<Properties> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "properties";
    /**
     * Constant defining the singleton instance.
     */
    public static final PropertiesStrategy INSTANCE = new PropertiesStrategy();
    private static final String ELEMENT_DEFAULTS = "defaults";
    private static final String ELEMENT_ENTRY = "entry";
    private static final String ATTRIBUTE_KEY = "key";
    private static final String ATTRIBUTE_VALUE = "value";
    private static Field propertiesDefaults;

    static {
        try {
            PropertiesStrategy.propertiesDefaults = Properties.class.getDeclaredField("defaults");
            PropertiesStrategy.propertiesDefaults.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            // will never happen.
        } catch (SecurityException sX) {
            throw new ExceptionInInitializerError(sX);
        }
    }

    private PropertiesStrategy() {
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
    public Class<Properties> target() {
        return Properties.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Properties> c) {
        return c == Properties.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return PropertiesStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Properties target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(PropertiesStrategy.NAME);
        Properties defaults = null;
        try {
            defaults = (Properties) PropertiesStrategy.propertiesDefaults.get(target);
        } catch (IllegalArgumentException ex) {
            // will never happen.
        } catch (IllegalAccessException ex) {
            // will never happen.
        }
        if (defaults != null) {
            writer.startElement(ELEMENT_DEFAULTS);
            writer.write(defaults);
            writer.endElement();
        }
        for (Map.Entry p : target.entrySet()) {
            writer.startElement(PropertiesStrategy.ELEMENT_ENTRY);
            writer.setAttribute(PropertiesStrategy.ATTRIBUTE_KEY, p.getKey().toString());
            writer.setAttribute(PropertiesStrategy.ATTRIBUTE_VALUE, p.getValue().toString());
            writer.endElement();
        }
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Properties unmarshalNew(CompositeReader reader, UnmarshalContext ctx)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return new Properties();// do not consume properties start: let the second step while do it.
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Properties unmarshalInit(Properties target, CompositeReader reader, UnmarshalContext ctx) {
        while (reader.next()) {// read Properties entries:
            if (reader.atElementStart()) {
                if (reader.elementName().equals(PropertiesStrategy.ELEMENT_ENTRY)) {
                    target.setProperty(
                            reader.elementRequiredAttribute(PropertiesStrategy.ATTRIBUTE_KEY),
                            reader.elementRequiredAttribute(PropertiesStrategy.ATTRIBUTE_VALUE));
                    reader.next(); // consumed entry element start.
                } else if (reader.elementName().equals(PropertiesStrategy.ELEMENT_DEFAULTS)) {
                    reader.next(); // consumed defaults element start.
                    try {
                        PropertiesStrategy.propertiesDefaults.set(target, reader.read());
                    } catch (IllegalAccessException neverThrown) {
                        // ignore it.
                    }
                    // do not consume defaults element end: let the next while do it.
                } else {
                    throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                            "unexpected element start");
                }
            } else if (reader.atElementEnd()
                    && reader.elementName().equals(PropertiesStrategy.NAME)) {
                return target;
            }
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                "missing element end");
    }
}//class PropertiesStrategy.
