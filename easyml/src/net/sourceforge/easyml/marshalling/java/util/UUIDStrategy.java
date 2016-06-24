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

import java.util.UUID;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * UUIDStrategy class that implements the {@linkplain SimpleStrategy} interface
 * for the Java {@linkplain UUID}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.2.2
 */
public final class UUIDStrategy extends AbstractStrategy<UUID>
        implements SimpleStrategy<UUID> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "uuid";
    /**
     * Constant defining the singleton instance.
     */
    public static final UUIDStrategy INSTANCE = new UUIDStrategy();

    private UUIDStrategy() {
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
    public Class<UUID> target() {
        return UUID.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<UUID> c) {
        return c == UUID.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return UUIDStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(UUID target, MarshalContext ctx) {
        return target.toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public UUID unmarshal(String text, UnmarshalContext ctx) {
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException iax) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), iax);
        }
    }
}//class UUIDStrategy.
