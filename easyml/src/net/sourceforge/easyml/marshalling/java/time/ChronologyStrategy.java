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
package net.sourceforge.easyml.marshalling.java.time;

import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

import java.time.DateTimeException;
import java.time.chrono.Chronology;

/**
 * ChronologyStrategy class that implements the {@linkplain SimpleStrategy}
 * interface for the {@linkplain Chronology}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.1
 * @since 1.5.1
 */
public final class ChronologyStrategy extends AbstractStrategy implements SimpleStrategy<Chronology> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "chronology";
    /**
     * Constant defining the singleton instance.
     */
    public static final ChronologyStrategy INSTANCE = new ChronologyStrategy();

    private ChronologyStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean strict() {
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<Chronology> target() {
        return Chronology.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Chronology> c) {
        return Chronology.class.isAssignableFrom(c);
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
    public String marshal(Chronology target, MarshalContext ctx) {
        return target.getId();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Chronology unmarshal(String text, UnmarshalContext ctx) {
        try {
            return Chronology.of(text);
        } catch (DateTimeException dtX) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), dtX);
        }
    }
}
