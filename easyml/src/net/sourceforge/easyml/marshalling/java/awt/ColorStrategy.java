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
package net.sourceforge.easyml.marshalling.java.awt;

import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.*;

import java.awt.*;

/**
 * ColorStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for the Java {@linkplain Color}. This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.2.4
 * @since 1.0
 */
public final class ColorStrategy extends AbstractStrategy implements CompositeStrategy<Color> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "color";
    /**
     * Constant defining the singleton instance.
     */
    public static final ColorStrategy INSTANCE = new ColorStrategy();
    private static final String ATTRIBUTE_RED = "r";
    private static final String ATTRIBUTE_GREEN = "g";
    private static final String ATTRIBUTE_BLUE = "b";
    private static final String ATTRIBUTE_ALPHA = "a";
    private static final Class TARGET = ColorStrategy.targetClass();

    private static Class targetClass() {
        try {
            // init Color.class by using Class.forName with initialize=false:
            return Class.forName("java.awt.Color", false, ColorStrategy.class.getClassLoader());
        } catch (ClassNotFoundException neverThrown) {
            // java.awt.Color should be a part of the JDK.
            return null;
        }
    }

    private ColorStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Color> c) {
        return c == TARGET;
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
    public Class<Color> target() {
        return TARGET;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return ColorStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Color target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(ColorStrategy.NAME);
        writer.setAttribute(ATTRIBUTE_RED, Integer.toString(target.getRed()));
        writer.setAttribute(ATTRIBUTE_GREEN, Integer.toString(target.getGreen()));
        writer.setAttribute(ATTRIBUTE_BLUE, Integer.toString(target.getBlue()));
        writer.setAttribute(ATTRIBUTE_ALPHA, Integer.toString(target.getAlpha()));
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Color unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        // read the attributes set at marshalling:
        final String alphaAttr = reader.elementAttribute(ATTRIBUTE_ALPHA);
        try {
            if (alphaAttr != null) { // let's say alpha is not mandatory:
                return new Color(Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_RED)),
                        Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_GREEN)),
                        Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_BLUE)),
                        Integer.parseInt(alphaAttr));
            }
            return new Color(Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_RED)),
                    Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_GREEN)),
                    Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_BLUE)));
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Color unmarshalInit(Color target, CompositeReader reader, UnmarshalContext ctx) {
        reader.next(); // moved the reader from the root element start to end.
        return target;
    }
}
