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
package net.sourceforge.easyml.marshalling.java.util.concurrent.atomic;


import net.sourceforge.easyml.marshalling.*;

import java.util.concurrent.atomic.AtomicReference;

/**
 * AtomicReferenceStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for the Java {@linkplain AtomicReference} class.
 * This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.0
 * @since 1.5.3
 */
public final class AtomicReferenceStrategy extends AbstractStrategy implements CompositeStrategy<AtomicReference> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "atomic-ref";
    /**
     * Constant defining the singleton instance.
     */
    public static final AtomicReferenceStrategy INSTANCE = new AtomicReferenceStrategy();

    private AtomicReferenceStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<AtomicReference> target() {
        return AtomicReference.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(AtomicReference target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(NAME);
        writer.write(target.get());
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AtomicReference unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        reader.next(); // consume atomic-ref start.
        return new AtomicReference(reader.read());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AtomicReference unmarshalInit(AtomicReference target, CompositeReader reader, UnmarshalContext ctx) {
        return target;
    }
}
