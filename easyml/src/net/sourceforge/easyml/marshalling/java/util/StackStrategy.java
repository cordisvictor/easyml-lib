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
import net.sourceforge.easyml.marshalling.UnmarshalContext;

import java.util.Stack;

/**
 * StackStrategy class that extends the {@linkplain CollectionStrategy} for the
 * {@linkplain Stack}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.0.3
 * @since 1.0.3
 */
public final class StackStrategy extends CollectionStrategy<Stack> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "stack";
    /**
     * Constant defining the singleton instance.
     */
    public static final StackStrategy INSTANCE = new StackStrategy();

    private StackStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class target() {
        return Stack.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return StackStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Stack unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        return new Stack();
    }
}
