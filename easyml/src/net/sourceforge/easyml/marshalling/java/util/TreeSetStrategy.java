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
import java.util.TreeSet;

/**
 * TreeSetStrategy class that extends the {@linkplain CollectionStrategy} for the
 * {@linkplain TreeSet}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.4.6
 * @since 1.4.6
 */
public final class TreeSetStrategy extends CollectionStrategy<TreeSet> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "treeset";
    /**
     * Constant defining comparator attribute name.
     */
    public static final String ATTRIBUTE_COMPARATOR = "comparator";
    private static final String ELEMENT_COMPARATOR = "comparator";
    /**
     * Constant defining the singleton instance.
     */
    public static final TreeSetStrategy INSTANCE = new TreeSetStrategy();

    private TreeSetStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class target() {
        return TreeSet.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return TreeSetStrategy.NAME;
    }

    /**
     * Override which takes into account the tree set comparator.
     * <p>
     * {@inheritDoc }
     */
    @Override
    public void marshal(TreeSet target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(TreeSetStrategy.NAME);
        final Comparator comparator = target.comparator();
        if (comparator != null) {
            writer.setAttribute(ATTRIBUTE_COMPARATOR, Boolean.toString(true));
            writer.startElement(ELEMENT_COMPARATOR);
            writer.write(comparator);
            writer.endElement();
        }
        this.marshalElements(target, writer);
        writer.endElement();
    }

    /**
     * Override which takes into account the tree set comparator.
     * <p>
     * {@inheritDoc }
     */
    @Override
    public TreeSet unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        if (Boolean.parseBoolean(reader.elementAttribute(ATTRIBUTE_COMPARATOR))) {
            reader.next(); // consumed root start.
            reader.next(); // consumed comparator element start.
            // comparator element end will be consumed by unmarshalInit,
            // which invokes next on root start or comparator end regardless.
            final Object comparator = reader.read();
            if (!(comparator instanceof Comparator)) {
                throw new InvalidFormatException(ctx.readerPositionDescriptor(), "tree comparator invalid");
            }
            return new TreeSet((Comparator) comparator);
        }
        return new TreeSet();
    }
}
