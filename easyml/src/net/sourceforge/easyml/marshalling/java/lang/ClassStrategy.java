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
package net.sourceforge.easyml.marshalling.java.lang;

import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * ClassStrategy class that implements the {@linkplain SimpleStrategy} interface
 * for the Java {@linkplain Class}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.3.7
 * @since 1.0
 */
public final class ClassStrategy extends AbstractStrategy implements SimpleStrategy<Class> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "class";
    /**
     * Constant defining the singleton instance.
     */
    public static final ClassStrategy INSTANCE = new ClassStrategy();

    private ClassStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<Class> target() {
        return Class.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return ClassStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(Class target, MarshalContext ctx) {
        return ctx.aliasOrNameFor(target);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class unmarshal(String text, UnmarshalContext ctx) {
        try {
            return ctx.classFor(text);
        } catch (ClassNotFoundException cnfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), cnfx);
        }
    }
}
