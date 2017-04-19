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
