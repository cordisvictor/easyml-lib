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
package net.sourceforge.easyml.marshalling.dtd;

import net.sourceforge.easyml.DTD;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

import java.text.ParseException;
import java.util.Date;

/**
 * DateStrategy class that implements the {@linkplain SimpleStrategy} interface
 * for the DTD date datatype. The default format used for representing dates is
 * ISO8601 without time zone information. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.3.5
 * @since 1.0
 */
public final class DateStrategy extends AbstractStrategy implements SimpleStrategy<Date> {

    /**
     * Constant defining the singleton instance.
     */
    public static final DateStrategy INSTANCE = new DateStrategy();

    private DateStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<Date> target() {
        return Date.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return DTD.TYPE_DATE;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(Date target, MarshalContext ctx) {
        return ctx.formatDate(target);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Date unmarshal(String text, UnmarshalContext ctx) {
        try {
            return ctx.parseDate(text);
        } catch (ParseException px) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), px);
        }
    }
}
