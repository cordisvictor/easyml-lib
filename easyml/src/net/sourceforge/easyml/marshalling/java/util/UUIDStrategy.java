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
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

import java.util.UUID;

/**
 * UUIDStrategy class that implements the {@linkplain SimpleStrategy} interface
 * for the Java {@linkplain UUID}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.2.2
 * @since 1.0
 */
public final class UUIDStrategy extends AbstractStrategy implements SimpleStrategy<UUID> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "uuid";
    /**
     * Constant defining the singleton instance.
     */
    public static final UUIDStrategy INSTANCE = new UUIDStrategy();

    private UUIDStrategy() {
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
    public Class<UUID> target() {
        return UUID.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<UUID> c) {
        return c == UUID.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return UUIDStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(UUID target, MarshalContext ctx) {
        return target.toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public UUID unmarshal(String text, UnmarshalContext ctx) {
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException iax) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), iax);
        }
    }
}
