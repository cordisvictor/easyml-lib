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

import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * StringBuilderStrategy class that implements the {@linkplain SimpleStrategy}
 * interface for the Java {@linkplain StringBuilder}.This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.0
 */
public final class StringBuilderStrategy extends AbstractStrategy<StringBuilder>
        implements SimpleStrategy<StringBuilder> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "strbldr";
    /**
     * Constant defining the singleton instance.
     */
    public static final StringBuilderStrategy INSTANCE = new StringBuilderStrategy();

    private StringBuilderStrategy() {
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
    public Class<StringBuilder> target() {
        return StringBuilder.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<StringBuilder> c) {
        return c == StringBuilder.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return StringBuilderStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(StringBuilder target, MarshalContext ctx) {
        return target.toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public StringBuilder unmarshal(String text, UnmarshalContext ctx) {
        return new StringBuilder(text);
    }
}//class StringBuilderStrategy.
