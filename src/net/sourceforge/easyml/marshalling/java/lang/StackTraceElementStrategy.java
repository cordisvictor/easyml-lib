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
 * StackTraceElementStrategy class that implements the
 * {@linkplain SimpleStrategy} interface for the Java
 * {@linkplain StackTraceElement}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.2.2
 */
public final class StackTraceElementStrategy extends AbstractStrategy<StackTraceElement>
        implements SimpleStrategy<StackTraceElement> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "stacktrace-e";
    /**
     * Constant defining the singleton instance.
     */
    public static final StackTraceElementStrategy INSTANCE = new StackTraceElementStrategy();
    private static final String SEPARATOR = ":";

    private StackTraceElementStrategy() {
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
    public Class<StackTraceElement> target() {
        return StackTraceElement.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<StackTraceElement> c) {
        return c == StackTraceElement.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return StackTraceElementStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(StackTraceElement target, MarshalContext ctx) {
        return new StringBuilder(target.getClassName()).append(StackTraceElementStrategy.SEPARATOR).append(target.getMethodName()).append(StackTraceElementStrategy.SEPARATOR).append(target.getFileName()).append(StackTraceElementStrategy.SEPARATOR).append(target.getLineNumber()).toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public StackTraceElement unmarshal(String text, UnmarshalContext ctx) {
        final String[] fields = text.split(StackTraceElementStrategy.SEPARATOR);
        if (fields.length == 4) {
            try {
                return new StackTraceElement(fields[0], fields[1], fields[2], Integer.parseInt(fields[3]));
            } catch (NumberFormatException nfx) {
                throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
            }
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "text: requires 4 fields");
    }
}//class StackTraceElementStrategy.
