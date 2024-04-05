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

import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.CompositeWriter;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

import java.util.EnumSet;
import java.util.function.Function;

/**
 * EnumSetStrategy class that extends the {@linkplain CollectionStrategy} for
 * the {@linkplain EnumSet}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.8.2
 * @since 1.4.6
 */
public final class EnumSetStrategy extends CollectionStrategy<EnumSet> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "enumset";
    /**
     * Constant defining the singleton instance.
     */
    public static final EnumSetStrategy INSTANCE = new EnumSetStrategy();
    private static final String ATTRIBUTE_ELEMENTTYPE = "elementType";

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
    protected void marshalAttrs(EnumSet target, CompositeWriter writer, MarshalContext ctx) {
        writer.setAttribute(ATTRIBUTE_ELEMENTTYPE, ctx.aliasOrNameFor(elementTypeOf(target)));
    }

    @Override
    protected void marshalElements(EnumSet target, CompositeWriter writer) {
        for (Object e : target) {
            writer.writeString(((Enum) e).name());
        }
    }

    private static <E extends Enum<E>> Class<E> elementTypeOf(EnumSet<E> source) {
        return (source.isEmpty() ? EnumSet.complementOf(source) : source).iterator()
                .next()
                .getDeclaringClass();
    }

    @Override
    public EnumSet unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException {
        return EnumSet.noneOf(ctx.classFor(reader.elementRequiredAttribute(ATTRIBUTE_ELEMENTTYPE)));
    }

    @Override
    protected Function<CompositeReader, Object> unmarshalElement(EnumSet target, CompositeReader reader, UnmarshalContext ctx) {
        final Class elementTypeCls = elementTypeOf(target);
        return r -> Enum.valueOf(elementTypeCls, r.readString());
    }
}
