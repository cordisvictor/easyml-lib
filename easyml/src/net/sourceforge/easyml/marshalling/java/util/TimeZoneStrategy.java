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

import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

import java.util.TimeZone;

/**
 * TimeZoneStrategy class that implements the {@linkplain SimpleStrategy}
 * interface for the {@linkplain TimeZone}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.3
 * @since 1.5.3
 */
public final class TimeZoneStrategy extends AbstractStrategy implements SimpleStrategy<TimeZone> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "timezone";
    /**
     * Constant defining the singleton instance.
     */
    public static final TimeZoneStrategy INSTANCE = new TimeZoneStrategy();

    private TimeZoneStrategy() {
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
    public Class<TimeZone> target() {
        return TimeZone.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<TimeZone> c) {
        return TimeZone.class.isAssignableFrom(c);
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
    public String marshal(TimeZone target, MarshalContext ctx) {
        return target.getID();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public TimeZone unmarshal(String text, UnmarshalContext ctx) {
        return TimeZone.getTimeZone(text);
    }
}
