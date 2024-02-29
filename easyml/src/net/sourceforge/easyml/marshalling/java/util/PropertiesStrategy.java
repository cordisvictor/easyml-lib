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

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * PropertiesStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for the Java {@linkplain Properties} class. This implementation is
 * for treating the target class in a special way rather than as a regular
 * {@linkplain Map}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.3
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

    private PropertiesStrategy() {
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
    public String name() {
        return PropertiesStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Properties target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(PropertiesStrategy.NAME);
        final Properties defaults = defaultsOf(target);
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
        return new Properties();// do not consume properties start: let unmarshalInit() while-loop do it.
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Properties unmarshalInit(Properties target, CompositeReader reader, UnmarshalContext ctx) {
        Properties result = target; // result might be reinited in order to set defaults.
        while (reader.next()) {// read Properties entries:
            if (reader.atElementStart()) {
                switch (reader.elementName()) {
                    case PropertiesStrategy.ELEMENT_ENTRY:
                        result.setProperty(
                                reader.elementRequiredAttribute(PropertiesStrategy.ATTRIBUTE_KEY),
                                reader.elementRequiredAttribute(PropertiesStrategy.ATTRIBUTE_VALUE));
                        reader.next(); // consumed entry element start.
                        break;
                    case PropertiesStrategy.ELEMENT_DEFAULTS:
                        if (!result.isEmpty()) {
                            throw new InvalidFormatException(ctx.readerPositionDescriptor(), "Properties defaults must be first element");
                        }
                        reader.next(); // consumed defaults element start.

                        final Object defaults = reader.read();
                        if (!(defaults instanceof Properties)) {
                            throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                                    "expected Properties defaults, found " + (defaults != null ? defaults.getClass() : null));
                        }
                        result = new Properties((Properties) defaults);
                        break;
                    default:
                        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "unexpected element start");
                }
            } else if (reader.atElementEnd()
                    && reader.elementName().equals(PropertiesStrategy.NAME)) {
                return result;
            }
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "missing element end");
    }

    private static Properties defaultsOf(Properties source) {
        final Set<String> allNames = source.stringPropertyNames();
        final Set<Object> propertyNames = source.keySet();

        if (allNames.size() == propertyNames.size()) {
            return null;
        }

        final Properties defaults = new Properties();
        for (String name : allNames) {
            if (!propertyNames.contains(name)) {
                defaults.setProperty(name, source.getProperty(name));
            }
        }
        return defaults;
    }

}
