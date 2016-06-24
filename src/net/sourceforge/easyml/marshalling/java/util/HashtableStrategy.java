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

import java.util.Hashtable;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * HashtableStrategy class that extends the {@linkplain MapStrategy} for the
 * {@linkplain Hashtable}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.2.2
 */
public final class HashtableStrategy extends MapStrategy<Hashtable> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "hashtbl";
    /**
     * Constant defining the singleton instance.
     */
    public static final HashtableStrategy INSTANCE = new HashtableStrategy();

    private HashtableStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class target() {
        return Hashtable.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Hashtable> c) {
        return c == Hashtable.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return HashtableStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Hashtable unmarshalNew(CompositeReader reader, UnmarshalContext ctx)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        try {
            return new Hashtable(Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_SIZE)));
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
        }
    }
}
