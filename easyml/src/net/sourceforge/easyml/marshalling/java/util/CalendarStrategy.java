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
import net.sourceforge.easyml.marshalling.*;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * CalendarStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for the Java {@linkplain Calendar} class.
 * This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.1
 * @since 1.5.1
 */
public final class CalendarStrategy extends AbstractStrategy implements CompositeStrategy<Calendar> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "calendar";
    /**
     * Constant defining the singleton instance.
     */
    public static final CalendarStrategy INSTANCE = new CalendarStrategy();
    private static final String ATTRIBUTE_TIME = "time";
    private static final String ATTRIBUTE_TZ = "tz";

    private CalendarStrategy() {
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
    public Class<Calendar> target() {
        return Calendar.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Calendar> c) {
        return Calendar.class.isAssignableFrom(c);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return CalendarStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Calendar target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(CalendarStrategy.NAME);
        writer.setAttribute(ATTRIBUTE_TIME, Long.toString(target.getTimeInMillis()));
        writer.setAttribute(ATTRIBUTE_TZ, target.getTimeZone().getID());
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Calendar unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        try {
            final long time = Long.parseLong(reader.elementRequiredAttribute(ATTRIBUTE_TIME));
            final TimeZone tz = TimeZone.getTimeZone(reader.elementRequiredAttribute(ATTRIBUTE_TZ));
            reader.next(); // consume optional start.
            Calendar result = Calendar.getInstance(tz);
            result.setTimeInMillis(time);
            return result;
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object unmarshalInit(Calendar target, CompositeReader reader, UnmarshalContext ctx) {
        return target;
    }
}
