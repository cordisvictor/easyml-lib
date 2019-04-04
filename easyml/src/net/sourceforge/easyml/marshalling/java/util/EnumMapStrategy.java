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
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * EnumMapStrategy class that extends the {@linkplain AbstractStrategy} for
 * the {@linkplain EnumMap}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.4.6
 * @since 1.4.6
 */
public final class EnumMapStrategy extends AbstractStrategy<EnumMap> implements CompositeStrategy<EnumMap> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "enummap";
    /**
     * Constant defining the singleton instance.
     */
    public static final EnumMapStrategy INSTANCE = new EnumMapStrategy();
    private static final String ATTRIBUTE_KEYTYPE = "keyType";
    private static Field keyType;

    static {
        try {
            keyType = EnumMap.class.getDeclaredField("keyType");
            keyType.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            // will never happen.
        } catch (SecurityException sX) {
            throw new ExceptionInInitializerError(sX);
        }
    }

    private EnumMapStrategy() {
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
        return EnumMap.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<EnumMap> c) {
        return c == EnumMap.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return EnumMapStrategy.NAME;
    }

    @Override
    public void marshal(EnumMap target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(EnumMapStrategy.NAME);
        writer.setAttribute(ATTRIBUTE_KEYTYPE, ctx.aliasOrNameFor(reflectKeyType(target)));

        Set<Map.Entry> entrySet = target.entrySet();
        for (Map.Entry e : entrySet) {
            writer.writeString(((Enum) e.getKey()).name());
            writer.write(e.getValue());
        }
        writer.endElement();
    }

    private static Class reflectKeyType(EnumMap target) {
        try {
            return (Class) keyType.get(target);
        } catch (IllegalArgumentException | IllegalAccessException ignored) {
            return null;
        }
    }

    @Override
    public EnumMap unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException {
        return new EnumMap(ctx.classFor(reader.elementRequiredAttribute(ATTRIBUTE_KEYTYPE)));
    }

    @Override
    public Object unmarshalInit(EnumMap target, CompositeReader reader, UnmarshalContext ctx) {
        // consume root element:
        reader.next();
        // read elements:
        final Class keyTypeCls = reflectKeyType(target);
        while (true) {
            if (reader.atElementEnd() && reader.elementName().equals(EnumMapStrategy.NAME)) {
                return target;
            }
            final Enum key = Enum.valueOf(keyTypeCls, reader.readString());
            final Object value = reader.read();
            if (target.put(key, value) != null) {
                throw new InvalidFormatException(ctx.readerPositionDescriptor(), "duplicate key: " + key);
            }
        }
    }
}
