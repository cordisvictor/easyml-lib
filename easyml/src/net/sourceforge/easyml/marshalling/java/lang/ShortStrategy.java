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
package net.sourceforge.easyml.marshalling.java.lang;

import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * ShortStrategy class that implements the {@linkplain SimpleStrategy} interface
 * for the Java {@linkplain Short}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.2.2
 */
public final class ShortStrategy extends AbstractStrategy<Short>
        implements SimpleStrategy<Short> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "short";
    /**
     * Constant defining the singleton instance.
     */
    public static final ShortStrategy INSTANCE = new ShortStrategy();

    private ShortStrategy() {
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
    public Class<Short> target() {
        return Short.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Short> c) {
        return c == Short.class || c == short.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return ShortStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(Short target, MarshalContext ctx) {
        return target.toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Short unmarshal(String text, UnmarshalContext ctx) {
        try {
            return Short.parseShort(text);
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
        }
    }
}//class ShortStrategy.