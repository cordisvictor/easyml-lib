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
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.CompositeWriter;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * EnumMapStrategy class that extends the {@linkplain MapStrategy} for
 * the {@linkplain EnumMap}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.8.2
 * @since 1.4.6
 */
public final class EnumMapStrategy extends MapStrategy<EnumMap> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "enummap";
    /**
     * Constant defining the singleton instance.
     */
    public static final EnumMapStrategy INSTANCE = new EnumMapStrategy();
    private static final String ATTRIBUTE_KEYTYPE = "keyType";

    private EnumMapStrategy() {
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
    public String name() {
        return EnumMapStrategy.NAME;
    }

    @Override
    protected void marshalAttrs(EnumMap target, CompositeWriter writer, MarshalContext ctx) {
        writer.setAttribute(ATTRIBUTE_KEYTYPE, ctx.aliasOrNameFor(maybeKeyType(target)));
    }

    @Override
    protected void marshalEntrySet(EnumMap target, CompositeWriter writer) {
        Set<Map.Entry> entrySet = target.entrySet();
        for (Map.Entry e : entrySet) {
            writer.writeString(((Enum) e.getKey()).name());
            writer.write(e.getValue());
        }
    }

    private static <K extends Enum<K>, V> Class<K> maybeKeyType(EnumMap<K, V> target) {
        if (target.isEmpty()) {
            throw new IllegalArgumentException("target EnumMap is empty");
        }
        return target.keySet().iterator().next().getDeclaringClass();
    }

    @Override
    public EnumMap unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException {
        return new EnumMap(ctx.classFor(reader.elementRequiredAttribute(ATTRIBUTE_KEYTYPE)));
    }

    @Override
    protected Function<CompositeReader, Object> unmarshalKey(EnumMap target, CompositeReader reader, UnmarshalContext ctx) {
        final Class keyTypeCls;
        try {
            keyTypeCls = ctx.classFor(reader.elementRequiredAttribute(ATTRIBUTE_KEYTYPE));
        } catch (ClassNotFoundException thrownAtUnmarshalNew) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), "invalid " + ATTRIBUTE_KEYTYPE, thrownAtUnmarshalNew);
        }
        return r -> Enum.valueOf(keyTypeCls, r.readString());
    }
}
