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

import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

import java.util.concurrent.atomic.AtomicLong;

/**
 * AtomicLongStrategy class that implements the {@linkplain SimpleStrategy} interface
 * for the {@linkplain AtomicLong} datatype. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.0
 * @since 1.5.3
 */
public final class AtomicLongStrategy extends AbstractStrategy implements SimpleStrategy<AtomicLong> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "atomic-long";
    /**
     * Constant defining the singleton instance.
     */
    public static final AtomicLongStrategy INSTANCE = new AtomicLongStrategy();

    private AtomicLongStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<AtomicLong> target() {
        return AtomicLong.class;
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
    public String marshal(AtomicLong target, MarshalContext ctx) {
        return String.valueOf(target.get());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AtomicLong unmarshal(String text, UnmarshalContext ctx) {
        try {
            return new AtomicLong(Long.parseLong(text));
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
        }
    }
}
