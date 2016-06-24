/*
 * Copyright (c) 2011, Victor Cordis. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of EasyML library.
 *
 * EasyML library is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License (LGPL) as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * EasyML library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with EasyML library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Please contact the author ( cordis.victor@gmail.com ) if you need additional
 * information or have any questions.
 */
package net.sourceforge.easyml.marshalling.dtd;

import java.text.ParseException;
import java.util.Date;
import net.sourceforge.easyml.DTD;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.UnmarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;

/**
 * DateStrategy class that implements the {@linkplain SimpleStrategy} interface
 * for the DTD date datatype. The default format used for representing dates is
 * ISO8601 without time zone information. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.5
 */
public final class DateStrategy extends AbstractStrategy<Date> implements SimpleStrategy<Date> {

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
    public boolean strict() {
        return true;
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
    public boolean appliesTo(Class<Date> c) {
        return c == Date.class;
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
}//class DateStrategy.
