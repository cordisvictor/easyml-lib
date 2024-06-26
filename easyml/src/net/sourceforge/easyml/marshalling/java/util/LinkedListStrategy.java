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
package net.sourceforge.easyml.marshalling.java.util;

import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.CompositeWriter;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

import java.util.LinkedList;

/**
 * LinkedListStrategy class that extends the {@linkplain CollectionStrategy} for
 * the {@linkplain LinkedList}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.8.1
 * @since 1.0
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
    public String name() {
        return LinkedListStrategy.NAME;
    }

    /**
     * Override to prevent the marshalling of the {@linkplain #ATTRIBUTE_SIZE}.
     */
    @Override
    protected void marshalAttrs(LinkedList target, CompositeWriter writer, MarshalContext ctx) {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LinkedList unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        return new LinkedList();
    }
}
