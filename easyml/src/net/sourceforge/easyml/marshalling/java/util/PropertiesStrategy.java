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
import java.util.Map;
import java.util.Properties;

/**
 * PropertiesStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for the Java {@linkplain Properties} class. This implementation is
 * for treating the target class in a special way rather than as a regular
 * {@linkplain Map}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.4.7
 * @since 1.0
 */
public final class PropertiesStrategy extends AbstractStrategy implements CompositeStrategy<Properties> {

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
    private static final Field propertiesDefaults;

    static {
        Field propertiesDefs;
        try {
            propertiesDefs = Properties.class.getDeclaredField("defaults");
            ReflectionUtil.setAccessible(propertiesDefs);
        } catch (NoSuchFieldException | SecurityException ignored) {
            propertiesDefs = null;
        }
        propertiesDefaults = propertiesDefs;
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
        } catch (IllegalArgumentException | IllegalAccessException ex) {
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
    public Properties unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        return new Properties();// do not consume properties start: let the second step while do it.
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Properties unmarshalInit(Properties target, CompositeReader reader, UnmarshalContext ctx) {
        while (reader.next()) {// read Properties entries:
            if (reader.atElementStart()) {
                switch (reader.elementName()) {
                    case PropertiesStrategy.ELEMENT_ENTRY:
                        target.setProperty(
                                reader.elementRequiredAttribute(PropertiesStrategy.ATTRIBUTE_KEY),
                                reader.elementRequiredAttribute(PropertiesStrategy.ATTRIBUTE_VALUE));
                        reader.next(); // consumed entry element start.
                        break;
                    case PropertiesStrategy.ELEMENT_DEFAULTS:
                        reader.next(); // consumed defaults element start.
                        try {
                            PropertiesStrategy.propertiesDefaults.set(target, reader.read());
                        } catch (IllegalAccessException neverThrown) {
                            // ignore it.
                        }
                        // do not consume defaults element end: let the next while do it.
                        break;
                    default:
                        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "unexpected element start");
                }
            } else if (reader.atElementEnd()
                    && reader.elementName().equals(PropertiesStrategy.NAME)) {
                return target;
            }
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "missing element end");
    }
}
