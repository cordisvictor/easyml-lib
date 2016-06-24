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

import java.lang.reflect.Array;
import net.sourceforge.easyml.DTD;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.*;
import net.sourceforge.easyml.util.ValueType;

/**
 * ArrayStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for Java arrays. This is a reflection-based implementation used for
 * arrays only.
 *
 * <br/>This implementation acts as an override for the XML writer's
 * <code>writeArray</code> and XML reader's <code>readArray</code> deprecating
 * the {@linkplain net.sourceforge.easyml.XMLReader#readArray(java.lang.Class)
 * }
 * method and encodes only the non-default array elements (i.e. skips the
 * default element values w.r.t. the target array class).
 *
 * <br/>This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.7
 */
public final class ArrayStrategy extends AbstractStrategy implements CompositeStrategy {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "arrayx";
    /**
     * <b>Backward compatibility:</b> Constant defining the value used for the
     * strategy name prior to EasyML 1.3.5.
     *
     * @deprecated use {@linkplain #NAME} instead.
     */
    public static final String NAME_1_3_4 = "array-o";
    /**
     * Constant defining the singleton instance.
     */
    public static final ArrayStrategy INSTANCE = new ArrayStrategy();
    private static final String ELEMENT_SKIP = "this.skip";
    private static final String ATTRIBUTE_SIZE = "size";

    private ArrayStrategy() {
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
    public Class target() {
        return Object[].class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class c) {
        return c.isArray();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return ArrayStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Object target, CompositeWriter writer, MarshalContext ctx) {
        final Class cls = target.getClass();
        final int length = Array.getLength(target);
        writer.startElement(ArrayStrategy.NAME);
        writer.setAttribute(DTD.ATTRIBUTE_CLASS, ctx.aliasFor(cls, cls.getName()));
        writer.setAttribute(DTD.ATTRIBUTE_LENGTH, Integer.toString(length));
        // if skipDefaults then compute array default element value:
        final boolean skipDefaults = ctx.skipDefaults();
        final Class componentType = cls.getComponentType();
        final ValueType pvt = ValueType.ofPrimitive(componentType);
        // write non-default array elements:
        if (pvt != null) { // primitives array:
            int i = 0;
            while (i < length) {
                if (pvt.getWriteArrayItem(writer, target, i, skipDefaults)) { // write element:
                    i++;
                } else { // write skip section:
                    int skip = 1; // at least 1 default element value.
                    i++; // move on the next element, if any.
                    while (i < length && pvt.isDefaultArrayItem(target, i)) {
                        skip++;
                        i++;
                    }
                    writer.startElement(ArrayStrategy.ELEMENT_SKIP);
                    if (skip > 1) {
                        writer.setAttribute(ATTRIBUTE_SIZE, Integer.toString(skip));
                    }
                    writer.endElement();
                }
            }
        } else { // objects array:
            final Object[] arrayTarget = (Object[]) target;
            int i = 0;
            while (i < length) {
                if (skipDefaults && arrayTarget[i] == null) { // write skip section:
                    int skip = 1; // at least 1 default element value.
                    i++; // move on the next element, if any.
                    while (i < length && arrayTarget[i] == null) {
                        skip++;
                        i++;
                    }
                    writer.startElement(ArrayStrategy.ELEMENT_SKIP);
                    if (skip > 1) {
                        writer.setAttribute(ATTRIBUTE_SIZE, Integer.toString(skip));
                    }
                    writer.endElement();
                } else { // write element:
                    writer.write(arrayTarget[i]);
                    i++;
                }
            }
        }
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object unmarshalNew(CompositeReader reader, UnmarshalContext ctx)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final Class cls = ctx.classFor(reader.elementRequiredAttribute(DTD.ATTRIBUTE_CLASS));
        if (cls.isArray()) {
            try {
                return Array.newInstance(cls.getComponentType(),
                        Integer.parseInt(reader.elementRequiredAttribute(DTD.ATTRIBUTE_LENGTH)));
            } catch (NumberFormatException nfx) {
                throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
            }
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "class attribute value does not resolve to an array");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object unmarshalInit(Object target, CompositeReader reader, UnmarshalContext ctx)
            throws IllegalAccessException {
        // consume root element start:
        reader.next();
        // read elements:
        final ValueType pvt = ValueType.ofPrimitive(target.getClass().getComponentType());
        try {
            if (pvt != null) { // primitives array:
                final int length = Array.getLength(target);
                int i = 0;
                while (i < length) {
                    if (reader.atElementStart() && reader.elementName().equals(ArrayStrategy.ELEMENT_SKIP)) { // skip section:
                        final String sizeAttr = reader.elementAttribute(ArrayStrategy.ATTRIBUTE_SIZE);
                        reader.next(); // consumed skip start.
                        reader.next(); // consumed skip end.
                        i += sizeAttr != null ? Integer.parseInt(sizeAttr) : 1;
                    } else { // element to read:
                        pvt.setReadArrayItem(reader, target, i);
                        i++;
                    }
                }
            } else { // objects array:
                final Object[] arrayTarget = (Object[]) target;
                int i = 0;
                while (i < arrayTarget.length) {
                    if (reader.atElementStart() && reader.elementName().equals(ArrayStrategy.ELEMENT_SKIP)) { // skip section:
                        final String sizeAttr = reader.elementAttribute(ArrayStrategy.ATTRIBUTE_SIZE);
                        reader.next(); // consumed skip start.
                        reader.next(); // consumed skip end.
                        i += sizeAttr != null ? Integer.parseInt(sizeAttr) : 1;
                    } else { // element to read:
                        arrayTarget[i] = reader.read();
                        i++;
                    }
                }
            }
            return target;
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
        }
    }
}//class ArrayStrategy.
