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
package net.sourceforge.easyml.marshalling.java.util;

import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

import java.util.Locale;

/**
 * LocaleStrategy class that implements the {@linkplain SimpleStrategy}
 * interface for the Java {@linkplain Locale}. This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.3.3
 * @since 1.0
 */
public final class LocaleStrategy extends AbstractStrategy implements SimpleStrategy<Locale> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "locale";
    /**
     * Constant defining the singleton instance.
     */
    public static final LocaleStrategy INSTANCE = new LocaleStrategy();
    private static final String SEPARATOR = "_";

    private LocaleStrategy() {
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
    public Class<Locale> target() {
        return Locale.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Locale> c) {
        return c == Locale.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return LocaleStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(Locale target, MarshalContext ctx) {
        return target.toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Locale unmarshal(String text, UnmarshalContext ctx) {
        final String[] components = text.split(LocaleStrategy.SEPARATOR);
        switch (components.length) {
            case 1:
                return new Locale(components[0]);
            case 2:
                return new Locale(components[0], components[1]);
            case 3:
                return new Locale(components[0], components[1], components[2]);
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "text: invalid number of locale components");
    }
}
