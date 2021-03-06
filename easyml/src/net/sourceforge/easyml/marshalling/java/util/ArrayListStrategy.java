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
import net.sourceforge.easyml.marshalling.UnmarshalContext;

import java.util.ArrayList;

/**
 * ArrayListStrategy class that extends the {@linkplain CollectionStrategy} for
 * the {@linkplain ArrayList}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.2.2
 * @since 1.0
 */
public final class ArrayListStrategy extends CollectionStrategy<ArrayList> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "arraylst";
    /**
     * Constant defining the singleton instance.
     */
    public static final ArrayListStrategy INSTANCE = new ArrayListStrategy();

    private ArrayListStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class target() {
        return ArrayList.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return ArrayListStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ArrayList unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        try {
            return new ArrayList(Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_SIZE)));
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
        }
    }
}
