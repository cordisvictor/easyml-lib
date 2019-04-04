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
package net.sourceforge.easyml.marshalling.java.util.concurrent;

import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.UnmarshalContext;
import net.sourceforge.easyml.marshalling.java.util.MapStrategy;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ConcurrentHashMapStrategy class that extends the {@linkplain MapStrategy} for the
 * {@linkplain ConcurrentHashMap}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.4.6
 * @since 1.4.6
 */
public final class ConcurrentHashMapStrategy extends MapStrategy<ConcurrentHashMap> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "concurrenthashmap";
    /**
     * Constant defining the singleton instance.
     */
    public static final ConcurrentHashMapStrategy INSTANCE = new ConcurrentHashMapStrategy();

    private ConcurrentHashMapStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class target() {
        return ConcurrentHashMap.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return ConcurrentHashMapStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ConcurrentHashMap unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        try {
            return new ConcurrentHashMap(Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_SIZE)));
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
        }
    }
}
