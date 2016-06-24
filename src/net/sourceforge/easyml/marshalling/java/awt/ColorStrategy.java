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
package net.sourceforge.easyml.marshalling.java.awt;

import java.awt.Color;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.*;

/**
 * ColorStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for the Java {@linkplain Color}. This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.2.4
 */
public final class ColorStrategy extends AbstractStrategy<Color>
        implements CompositeStrategy<Color> {

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
    public Color unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
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
    public Color unmarshalInit(Color target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
        reader.next(); // moved the reader from the root element start to end.
        return target;
    }
}//class ColorStrategy.
