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
package net.sourceforge.easyml.marshalling.java.math;

import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

import java.math.BigInteger;

/**
 * BigIntegerStrategy class that implements the {@linkplain SimpleStrategy}
 * interface for the Java {@linkplain BigInteger}. This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.2.2
 * @since 1.0
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
}
