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

import java.util.IdentityHashMap;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * IdentityHashMapStrategy class that extends the {@linkplain MapStrategy} for
 * the {@linkplain IdentityHashMap}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.2.2
 */
public final class IdentityHashMapStrategy extends MapStrategy<IdentityHashMap> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "idhashmap";
    /**
     * Constant defining the singleton instance.
     */
    public static final IdentityHashMapStrategy INSTANCE = new IdentityHashMapStrategy();

    private IdentityHashMapStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class target() {
        return IdentityHashMap.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<IdentityHashMap> c) {
        return c == IdentityHashMap.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return IdentityHashMapStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public IdentityHashMap unmarshalNew(CompositeReader reader, UnmarshalContext ctx)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        try {
            return new IdentityHashMap(Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_SIZE)));
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
        }
    }
}
