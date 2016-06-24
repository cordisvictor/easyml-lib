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

import java.util.LinkedList;
import net.sourceforge.easyml.marshalling.CompositeAttributeWriter;
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * LinkedListStrategy class that extends the {@linkplain CollectionStrategy} for
 * the {@linkplain LinkedList}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.0.3
 */
public final class LinkedListStrategy extends CollectionStrategy<LinkedList> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "lnklst";
    /**
     * Constant defining the singleton instance.
     */
    public static final LinkedListStrategy INSTANCE = new LinkedListStrategy();

    private LinkedListStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class target() {
        return LinkedList.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<LinkedList> c) {
        return c == LinkedList.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return LinkedListStrategy.NAME;
    }

    /**
     * Override to prevent the marshalling of the {@linkplain #ATTRIBUTE_SIZE}.
     */
    @Override
    public void marshalAttr(LinkedList target, CompositeAttributeWriter writer) {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LinkedList unmarshalNew(CompositeReader reader, UnmarshalContext ctx)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return new LinkedList();
    }
}
