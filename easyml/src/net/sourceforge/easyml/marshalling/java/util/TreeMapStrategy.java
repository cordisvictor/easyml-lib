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

import java.util.Comparator;
import java.util.TreeMap;

/**
 * TreeMapStrategy class that extends the {@linkplain MapStrategy} for the
 * {@linkplain TreeMap}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.8.1
 * @since 1.0.3
 */
public final class TreeMapStrategy extends MapStrategy<TreeMap> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "treemap";
    /**
     * Constant defining comparator attribute name.
     */
    public static final String ATTRIBUTE_COMPARATOR = "comparator";
    private static final String ELEMENT_COMPARATOR = "comparator";
    /**
     * Constant defining the singleton instance.
     */
    public static final TreeMapStrategy INSTANCE = new TreeMapStrategy();

    private TreeMapStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class target() {
        return TreeMap.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return TreeMapStrategy.NAME;
    }

    /**
     * Override to prevent the marshalling of the {@linkplain #ATTRIBUTE_SIZE}.
     */
    @Override
    protected void marshalAttrs(TreeMap target, CompositeWriter writer, MarshalContext ctx) {
    }

    /**
     * Override which takes into account the tree map comparator.
     * <p>
     * {@inheritDoc }
     */
    @Override
    protected void marshalEntrySet(TreeMap target, CompositeWriter writer) {
        final Comparator comparator = target.comparator();
        if (comparator != null) {
            writer.setAttribute(ATTRIBUTE_COMPARATOR, Boolean.toString(true));
            writer.startElement(ELEMENT_COMPARATOR);
            writer.write(comparator);
            writer.endElement();
        }
        super.marshalEntrySet(target, writer);
    }

    /**
     * Override which takes into account the tree map comparator.
     * <p>
     * {@inheritDoc }
     */
    @Override
    public TreeMap unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        if (Boolean.parseBoolean(reader.elementAttribute(ATTRIBUTE_COMPARATOR))) {
            reader.next(); // consumed root start.
            reader.next(); // consumed comparator element start.
            // comparator element end will be consumed by unmarshalInit,
            // which invokes next on root start or comparator end regardless.
            final Object comparator = reader.read();
            if (!(comparator instanceof Comparator)) {
                throw new InvalidFormatException(ctx.readerPositionDescriptor(), "tree comparator invalid");
            }
            return new TreeMap((Comparator) comparator);
        }
        return new TreeMap();
    }
}
