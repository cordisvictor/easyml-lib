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
import java.util.Set;

/**
 * MapStrategy abstract class that implements the {@linkplain CompositeStrategy}
 * interface for the Java Util {@linkplain Map} implementations. This
 * implementation is thread-safe.
 *
 * @param <T> target map class
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.4.6
 * @since 1.0
 */
public abstract class MapStrategy<T extends Map> extends AbstractStrategy implements CompositeStrategy<T> {

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
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<T> c) {
        return c == target();
    }

    /**
     * Writes the {@linkplain #ATTRIBUTE_SIZE} for the given target. This method
     * can be overridden to write used-defined attributes for the root element.
     *
     * @param target target to be marshalled
     * @param writer to write attributes with
     */
    protected void marshalAttr(T target, CompositeWriter writer) {
        writer.setAttribute(ATTRIBUTE_SIZE, Integer.toString(target.size()));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(T target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(this.name());
        this.marshalAttr(target, writer);
        this.marshalEntrySet(target, writer);
        writer.endElement();
    }

    protected void marshalEntrySet(T target, CompositeWriter writer) {
        Set<Map.Entry> entrySet = target.entrySet();
        for (Map.Entry e : entrySet) {
            writer.write(e.getKey());
            writer.write(e.getValue());
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public T unmarshalInit(T target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
        // consume root element:
        reader.next();
        // read entries:
        final String endElementName = this.name();
        while (true) {
            if (reader.atElementEnd() && reader.elementName().equals(endElementName)) {
                return target;
            }
            final Object key = reader.read();
            if (target.containsKey(key)) {
                throw new InvalidFormatException(ctx.readerPositionDescriptor(), "duplicate key: " + key);
            }
            target.put(key, reader.read());
        }
    }
}
