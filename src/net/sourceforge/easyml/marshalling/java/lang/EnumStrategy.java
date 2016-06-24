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
 * @since 1.0
 * @version 1.3.7
 */
public final class EnumStrategy extends AbstractStrategy<Enum>
        implements SimpleStrategy<Enum> {

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
        Class cls = target.getClass();
        if (!cls.isEnum()) {// handle polymorphic enums:
            cls = cls.getSuperclass();
        }
        // build enum name and constant name:
        final StringBuilder sb = new StringBuilder(ctx.aliasFor(cls, cls.getName()));
        sb.append(EnumStrategy.SEPARATOR);
        sb.append(target);
        return sb.toString();
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
        final String aliasOrName = text.substring(0, index);
        try {
            // find enum class and value:
            return Enum.valueOf(
                    ctx.classFor(aliasOrName),
                    text.substring(index + 1));
        } catch (ClassNotFoundException cnfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), cnfx);
        }
    }
}//class EnumStrategy.
