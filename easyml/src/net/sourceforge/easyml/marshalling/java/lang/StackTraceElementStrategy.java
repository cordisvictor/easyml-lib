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
 * StackTraceElementStrategy class that implements the
 * {@linkplain SimpleStrategy} interface for the Java
 * {@linkplain StackTraceElement}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.2.2
 * @since 1.0
 */
public final class StackTraceElementStrategy extends AbstractStrategy implements SimpleStrategy<StackTraceElement> {

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
    public Class<StackTraceElement> target() {
        return StackTraceElement.class;
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
        return new StringBuilder(target.getClassName()).append(StackTraceElementStrategy.SEPARATOR)
                .append(target.getMethodName()).append(StackTraceElementStrategy.SEPARATOR)
                .append(target.getFileName()).append(StackTraceElementStrategy.SEPARATOR)
                .append(target.getLineNumber())
                .toString();
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
}
