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
package net.sourceforge.easyml.marshalling.java.util;

import java.util.Locale;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * LocaleStrategy class that implements the {@linkplain SimpleStrategy}
 * interface for the Java {@linkplain Locale}. This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.3
 */
public final class LocaleStrategy extends AbstractStrategy<Locale>
        implements SimpleStrategy<Locale> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "locale";
    /**
     * Constant defining the singleton instance.
     */
    public static final LocaleStrategy INSTANCE = new LocaleStrategy();
    private static final String SEPARATOR = "_";

    private LocaleStrategy() {
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
    public Class<Locale> target() {
        return Locale.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Locale> c) {
        return c == Locale.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return LocaleStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(Locale target, MarshalContext ctx) {
        return target.toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Locale unmarshal(String text, UnmarshalContext ctx) {
        final String[] components = text.split(LocaleStrategy.SEPARATOR);
        switch (components.length) {
            case 1:
                return new Locale(components[0]);
            case 2:
                return new Locale(components[0], components[1]);
            case 3:
                return new Locale(components[0], components[1], components[2]);
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "text: invalid number of locale components");
    }
}//class LocaleStrategy.
