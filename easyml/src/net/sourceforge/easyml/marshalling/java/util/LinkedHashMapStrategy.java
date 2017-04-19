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

import java.util.LinkedHashMap;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * LinkedHashMapStrategy class that extends the {@linkplain MapStrategy} for the
 * {@linkplain LinkedHashMap}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.2.2
 */
public final class LinkedHashMapStrategy extends MapStrategy<LinkedHashMap> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "lnkhashmap";
    /**
     * Constant defining the singleton instance.
     */
    public static final LinkedHashMapStrategy INSTANCE = new LinkedHashMapStrategy();

    private LinkedHashMapStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class target() {
        return LinkedHashMap.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<LinkedHashMap> c) {
        return c == LinkedHashMap.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return LinkedHashMapStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LinkedHashMap unmarshalNew(CompositeReader reader, UnmarshalContext ctx)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        try {
            return new LinkedHashMap(Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_SIZE)));
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
        }
    }
}
