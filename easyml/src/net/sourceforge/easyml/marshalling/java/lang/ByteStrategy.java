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
 * ByteStrategy class that implements the {@linkplain SimpleStrategy} interface
 * for the Java {@linkplain Byte}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.2.2
 */
public final class ByteStrategy extends AbstractStrategy<Byte>
        implements SimpleStrategy<Byte> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "byte";
    /**
     * Constant defining the singleton instance.
     */
    public static final ByteStrategy INSTANCE = new ByteStrategy();

    private ByteStrategy() {
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
    public Class<Byte> target() {
        return Byte.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Byte> c) {
        return c == Byte.class || c == byte.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return ByteStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(Byte target, MarshalContext ctx) {
        return target.toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Byte unmarshal(String text, UnmarshalContext ctx) {
        try {
            return Byte.parseByte(text);
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
        }
    }
}//class ByteStrategy.