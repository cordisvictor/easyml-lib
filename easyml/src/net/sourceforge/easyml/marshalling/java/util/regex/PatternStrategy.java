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
package net.sourceforge.easyml.marshalling.java.util.regex;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.*;

/**
 * PatternStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for the Java {@linkplain Pattern}. This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.2.4
 */
public final class PatternStrategy extends AbstractStrategy<Pattern>
        implements CompositeStrategy<Pattern> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "pattern";
    /**
     * Constant defining the singleton instance.
     */
    public static final PatternStrategy INSTANCE = new PatternStrategy();
    private static final String ATTRIBUTE_REGEX = "regex";
    private static final String ATTRIBUTE_FLAGS = "flags";

    private PatternStrategy() {
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
    public Class<Pattern> target() {
        return Pattern.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Pattern> c) {
        return c == Pattern.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return PatternStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Pattern target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(PatternStrategy.NAME);
        writer.setAttribute(PatternStrategy.ATTRIBUTE_REGEX, target.pattern());
        final int flags = target.flags();
        if (flags != 0) {
            writer.setAttribute(PatternStrategy.ATTRIBUTE_FLAGS, Integer.toString(flags));
        }
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Pattern unmarshalNew(CompositeReader reader, UnmarshalContext ctx)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        // read the attributes set at marshalling:
        final String regex = reader.elementRequiredAttribute(PatternStrategy.ATTRIBUTE_REGEX);
        final String flagsStr = reader.elementAttribute(PatternStrategy.ATTRIBUTE_FLAGS);
        try {
            return (flagsStr != null) ? Pattern.compile(regex, Integer.parseInt(flagsStr))
                    : Pattern.compile(regex);
        } catch (NumberFormatException | PatternSyntaxException nfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Pattern unmarshalInit(Pattern target, CompositeReader reader, UnmarshalContext ctx)
            throws IllegalAccessException {
        reader.next(); // moved the reader on the root element end.
        return target;
    }
}//class PatternStrategy.
