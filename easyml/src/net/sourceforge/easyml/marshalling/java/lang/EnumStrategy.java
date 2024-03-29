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
 * EnumStrategy class that implements the {@linkplain SimpleStrategy} interface
 * for the Java enums, including polymorphic ones. This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.3
 * @since 1.0
 */
public final class EnumStrategy extends AbstractStrategy implements SimpleStrategy<Enum> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "enum";
    /**
     * Constant defining the singleton instance.
     */
    public static final EnumStrategy INSTANCE = new EnumStrategy();
    private static final char SEPARATOR = '.';

    private EnumStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean strict() {
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<Enum> target() {
        return Enum.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Enum> c) {
        return Enum.class.isAssignableFrom(c); // not isEnum() because of polymorphic-enums.
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return EnumStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(Enum target, MarshalContext ctx) {
        final Class cls = target.getDeclaringClass(); // handle polymorphic enums.
        // build enum name and constant name:
        return new StringBuilder(ctx.aliasOrNameFor(cls))
                .append(EnumStrategy.SEPARATOR)
                .append(target.name())
                .toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Enum unmarshal(String text, UnmarshalContext ctx) {
        // find separator:
        final int index = text.lastIndexOf(EnumStrategy.SEPARATOR);
        if (index < 0 || index == text.length() - 1) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), "text: missing separator");
        }
        try {
            // find enum class and value:
            return Enum.valueOf(ctx.classFor(text.substring(0, index)), text.substring(index + 1));
        } catch (ClassNotFoundException cnfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), cnfx);
        }
    }
}
