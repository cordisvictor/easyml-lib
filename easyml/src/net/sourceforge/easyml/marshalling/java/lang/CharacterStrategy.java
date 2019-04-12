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
 * CharacterStrategy class that implements the {@linkplain SimpleStrategy}
 * interface for the Java {@linkplain Character}. This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.3.3
 * @since 1.0
 */
public final class CharacterStrategy extends AbstractStrategy implements SimpleStrategy<Character> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "char";
    /**
     * Constant defining the singleton instance.
     */
    public static final CharacterStrategy INSTANCE = new CharacterStrategy();

    private CharacterStrategy() {
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
    public Class<Character> target() {
        return Character.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Character> c) {
        return c == Character.class || c == char.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return CharacterStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(Character target, MarshalContext ctx) {
        return target.toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Character unmarshal(String text, UnmarshalContext ctx) {
        if (text.length() == 1) {
            return text.charAt(0);
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "text length not 1");
    }
}//class CharacterStrategy.
