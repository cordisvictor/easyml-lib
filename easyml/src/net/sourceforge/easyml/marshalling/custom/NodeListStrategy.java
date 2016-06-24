/*
 * Copyright (c) 2015, Victor Cordis. All rights reserved.
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
package net.sourceforge.easyml.marshalling.custom;

import java.lang.reflect.Array;
import net.sourceforge.easyml.DTD;
import net.sourceforge.easyml.marshalling.*;
import net.sourceforge.easyml.util.XMLUtil;

/**
 * NodeListStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for custom-tagged XML node lists. This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0.4
 * @version 1.2.4
 */
public final class NodeListStrategy extends AbstractStrategy implements CompositeStrategy {

    private final String name;
    private final Class target;

    /**
     * Creates a new instance for the given name and target-array class.
     *
     * @param name the name of the XML element
     * @param target the array class of the target arrays
     */
    public NodeListStrategy(String name, Class target) {
        if (!XMLUtil.isLegalXMLTag(name)) {
            throw new IllegalArgumentException("name: " + name);
        }
        if (!target.isArray()) {
            throw new IllegalArgumentException("target: not an array class: " + target);
        }
        this.name = name;
        this.target = target;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class c) {
        return c == this.target;
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
        return this.target;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Object target, CompositeWriter writer, MarshalContext ctx) {
        final int length = Array.getLength(target);
        writer.startElement(this.name);
        writer.setAttribute(DTD.ATTRIBUTE_LENGTH, Integer.toString(length));
        // write array elements:
        int i = 0;
        while (i < length) {
            writer.write(Array.get(target, i));
            i++;
        }
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object unmarshalNew(CompositeReader reader, UnmarshalContext ctx)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return Array.newInstance(this.target.getComponentType(),
                Integer.parseInt(reader.elementRequiredAttribute(DTD.ATTRIBUTE_LENGTH)));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object unmarshalInit(Object target, CompositeReader reader, UnmarshalContext ctx)
            throws IllegalAccessException {
        reader.next(); // consumed root element start:
        // read elements:
        final int length = Array.getLength(target);
        int i = 0;
        while (i < length) {
            Array.set(target, i, reader.read());
            i++;
        }
        return target;
    }
}
