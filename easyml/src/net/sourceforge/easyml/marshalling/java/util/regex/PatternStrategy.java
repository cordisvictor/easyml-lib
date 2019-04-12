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
package net.sourceforge.easyml.marshalling.java.util.regex;

import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.*;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * PatternStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for the Java {@linkplain Pattern}. This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.2.4
 * @since 1.0
 */
public final class PatternStrategy extends AbstractStrategy implements CompositeStrategy<Pattern> {

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
    public Pattern unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
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
    public Pattern unmarshalInit(Pattern target, CompositeReader reader, UnmarshalContext ctx) {
        reader.next(); // moved the reader on the root element end.
        return target;
    }
}
