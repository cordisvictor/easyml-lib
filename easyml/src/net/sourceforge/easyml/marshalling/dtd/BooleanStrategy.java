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
package net.sourceforge.easyml.marshalling.dtd;

import net.sourceforge.easyml.DTD;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.UnmarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;

/**
 * BooleanStrategy class that implements the {@linkplain SimpleStrategy}
 * interface for the DTD boolean datatype. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.0
 * @since 1.0
 */
public final class BooleanStrategy extends AbstractStrategy<Boolean> implements SimpleStrategy<Boolean> {

    /**
     * Constant defining the singleton instance.
     */
    public static final BooleanStrategy INSTANCE = new BooleanStrategy();

    private BooleanStrategy() {
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
    public Class<Boolean> target() {
        return Boolean.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Boolean> c) {
        return c == Boolean.class || c == boolean.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return DTD.TYPE_BOOLEAN;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(Boolean target, MarshalContext ctx) {
        return target.toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Boolean unmarshal(String text, UnmarshalContext ctx) {
        return Boolean.valueOf(text);
    }
}
