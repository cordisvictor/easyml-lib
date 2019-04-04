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
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.UnmarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;

/**
 * IntStrategy class that implements the {@linkplain SimpleStrategy} interface
 * for the DTD integer datatype. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.2.2
 * @since 1.0
 */
public final class IntStrategy extends AbstractStrategy<Integer> implements SimpleStrategy<Integer> {

    /**
     * Constant defining the singleton instance.
     */
    public static final IntStrategy INSTANCE = new IntStrategy();

    private IntStrategy() {
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
    public Class<Integer> target() {
        return Integer.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Integer> c) {
        return c == Integer.class || c == int.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return DTD.TYPE_INT;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(Integer target, MarshalContext ctx) {
        return target.toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Integer unmarshal(String text, UnmarshalContext ctx) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
        }
    }
}
