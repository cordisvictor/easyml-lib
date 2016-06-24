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

import java.util.Collection;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.*;

/**
 * CollectionStrategy abstract class that implements the
 * {@linkplain CompositeStrategy} interface for the Java Util
 * {@linkplain Collection} implementations. This implementation is thread-safe.
 *
 * @param <T> target collection class
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.3
 */
public abstract class CollectionStrategy<T extends Collection> extends AbstractStrategy<T>
        implements CompositeStrategy<T> {

    /**
     * Constant defining collection size attribute name.
     */
    protected static final String ATTRIBUTE_SIZE = "size";

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean strict() {
        return true;
    }

    /**
     * Writes the {@linkplain #ATTRIBUTE_SIZE} for the given target. This method
     * can be overridden to write used-defined attributes for the root element.
     *
     * @param target target to be marshalled
     * @param writer to write attributes with
     */
    public void marshalAttr(T target, CompositeAttributeWriter writer) {
        writer.setAttribute(ATTRIBUTE_SIZE, Integer.toString(target.size()));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(T target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(this.name());
        this.marshalAttr(target, writer);
        for (Object o : target) {
            writer.write(o);
        }
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public T unmarshalInit(T target, CompositeReader reader, UnmarshalContext ctx)
            throws IllegalAccessException {
        // consume root element:
        reader.next();
        // read elements:
        while (true) {
            if (reader.atElementEnd() && reader.elementName().equals(this.name())) {
                return target;
            }
            final Object element = reader.read();
            if (!target.add(element)) {
                throw new InvalidFormatException(ctx.readerPositionDescriptor(), "adding: " + element);
            }
        }
    }
}//class CollectionStrategy.
