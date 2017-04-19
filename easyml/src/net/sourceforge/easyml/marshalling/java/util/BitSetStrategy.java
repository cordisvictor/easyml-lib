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

import java.util.BitSet;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.CompositeStrategy;
import net.sourceforge.easyml.marshalling.CompositeWriter;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * BitSetStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for the Java {@linkplain BitSet}. This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.4.3
 * @version 1.4.3
 */
public final class BitSetStrategy extends AbstractStrategy<BitSet> implements CompositeStrategy<BitSet> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "bitset";
    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String ELEMENT_SET = "set";
    /**
     * Constant defining the singleton instance.
     */
    public static final BitSetStrategy INSTANCE = new BitSetStrategy();

    private BitSetStrategy() {
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
        return BitSet.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<BitSet> c) {
        return c == BitSet.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return BitSetStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(BitSet target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(this.name());
        for (int i = target.nextSetBit(0); i != -1; i = target.nextSetBit(i + 1)) {
            writer.startElement(ELEMENT_SET);
            writer.writeValue(String.valueOf(i));
            writer.endElement();
        }
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public BitSet unmarshalNew(CompositeReader reader, UnmarshalContext ctx)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return new BitSet();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public BitSet unmarshalInit(BitSet target, CompositeReader reader, UnmarshalContext ctx)
            throws IllegalAccessException {
        // consume root element start tag:
        reader.next();
        // read elements:
        while (true) {
            if (reader.atElementEnd() && reader.elementName().equals(this.name())) {
                return target;
            }
            try {
                final int setBitIdx = Integer.parseInt(reader.readValue());
                target.set(setBitIdx);
                // consume set element end tag:
                reader.next();
            } catch (NumberFormatException ex) {
                throw new InvalidFormatException(ctx.readerPositionDescriptor(), ex);
            }
        }
    }
}
