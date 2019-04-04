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
import java.util.EnumSet;

/**
 * EnumSetStrategy class that extends the {@linkplain AbstractStrategy} for
 * the {@linkplain EnumSet}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.4.6
 * @since 1.4.6
 */
public final class EnumSetStrategy extends AbstractStrategy<EnumSet> implements CompositeStrategy<EnumSet> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "enumset";
    /**
     * Constant defining the singleton instance.
     */
    public static final EnumSetStrategy INSTANCE = new EnumSetStrategy();
    private static final String ATTRIBUTE_ELEMENTTYPE = "elementType";
    private static Field elementType;

    static {
        try {
            elementType = EnumSet.class.getDeclaredField("elementType");
            elementType.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            // will never happen.
        } catch (SecurityException sX) {
            throw new ExceptionInInitializerError(sX);
        }
    }

    private EnumSetStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean strict() {
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class target() {
        return EnumSet.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<EnumSet> c) {
        return EnumSet.class.isAssignableFrom(c);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return EnumSetStrategy.NAME;
    }

    @Override
    public void marshal(EnumSet target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(EnumSetStrategy.NAME);
        writer.setAttribute(ATTRIBUTE_ELEMENTTYPE, ctx.aliasOrNameFor(reflectElementType(target)));
        for (Object e : target) {
            writer.writeString(((Enum) e).name());
        }
        writer.endElement();
    }

    private static Class reflectElementType(EnumSet target) {
        try {
            return (Class) elementType.get(target);
        } catch (IllegalArgumentException | IllegalAccessException ignored) {
            return null;
        }
    }

    @Override
    public EnumSet unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException {
        return EnumSet.noneOf(ctx.classFor(reader.elementRequiredAttribute(ATTRIBUTE_ELEMENTTYPE)));
    }

    @Override
    public Object unmarshalInit(EnumSet target, CompositeReader reader, UnmarshalContext ctx) {
        // consume root element:
        reader.next();
        // read elements:
        final Class elementTypeCls = reflectElementType(target);
        while (true) {
            if (reader.atElementEnd() && reader.elementName().equals(EnumSetStrategy.NAME)) {
                return target;
            }
            final String elementName = reader.readString();
            if (!target.add(Enum.valueOf(elementTypeCls, elementName))) {
                throw new InvalidFormatException(ctx.readerPositionDescriptor(), "adding: " + elementName);
            }
        }
    }
}
