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
package net.sourceforge.easyml.marshalling.java.math;

import java.math.BigInteger;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * BigIntegerStrategy class that implements the {@linkplain SimpleStrategy}
 * interface for the Java {@linkplain BigInteger}. This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.2.2
 */
public final class BigIntegerStrategy extends AbstractStrategy<BigInteger>
        implements SimpleStrategy<BigInteger> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "big-int";
    /**
     * Constant defining the singleton instance.
     */
    public static final BigIntegerStrategy INSTANCE = new BigIntegerStrategy();

    private BigIntegerStrategy() {
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
    public Class<BigInteger> target() {
        return BigInteger.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<BigInteger> c) {
        return c == BigInteger.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return BigIntegerStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(BigInteger target, MarshalContext ctx) {
        return target.toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public BigInteger unmarshal(String text, UnmarshalContext ctx) {
        try {
            return new BigInteger(text);
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
        }
    }
}//class BigIntegerStrategy.
