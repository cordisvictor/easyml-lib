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

import net.sourceforge.easyml.DTD;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.*;
import net.sourceforge.easyml.util.ReflectionUtil.ValueType;

import java.lang.reflect.Array;

/**
 * ArrayStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for Java arrays. This is a reflection-based implementation used for
 * arrays only.
 * <p>
 * <br/>This implementation acts as an override for the XML writer's
 * <code>writeArray</code> and XML reader's <code>readArray</code> deprecating
 * the {@linkplain net.sourceforge.easyml.XMLReader#readArray(java.lang.Class)
 * }
 * method and encodes only the non-default array elements (i.e. skips the
 * default element values w.r.t. the target array class).
 * <p>
 * <br/>This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.3.7
 * @since 1.0
 */
public final class ArrayStrategy extends AbstractStrategy implements CompositeStrategy {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "arrayx";
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
        writer.setAttribute(DTD.ATTRIBUTE_CLASS, ctx.aliasOrNameFor(cls));
        writer.setAttribute(DTD.ATTRIBUTE_LENGTH, Integer.toString(length));
        // if skipDefaults then compute array default element value:
        final boolean skipDefaults = ctx.skipDefaults();
        final Class arrayItemCls = cls.getComponentType();
        // write non-default array elements:
        if (arrayItemCls.isPrimitive()) {
            final ValueType vt = ValueType.of(arrayItemCls);
            int i = 0;
            while (i < length) {
                if (vt.getWriteArrayItem(writer, target, i, skipDefaults)) { // write element:
                    i++;
                } else { // write skip section:
                    int skip = 1; // at least 1 default element value.
                    i++; // move on the next element, if any.
                    while (i < length && vt.isDefaultArrayItem(target, i)) {
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
        } else {
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
    public Object unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException {
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
    public Object unmarshalInit(Object target, CompositeReader reader, UnmarshalContext ctx) {
        // consume root element start:
        reader.next();
        // read elements:
        final Class arrayItemCls = target.getClass().getComponentType();
        try {
            if (arrayItemCls.isPrimitive()) {
                final ValueType vt = ValueType.of(arrayItemCls);
                final int length = Array.getLength(target);
                int i = 0;
                while (i < length) {
                    if (reader.atElementStart() && reader.elementName().equals(ArrayStrategy.ELEMENT_SKIP)) { // skip section:
                        final String sizeAttr = reader.elementAttribute(ArrayStrategy.ATTRIBUTE_SIZE);
                        reader.next(); // consumed skip start.
                        reader.next(); // consumed skip end.
                        i += sizeAttr != null ? Integer.parseInt(sizeAttr) : 1;
                    } else { // element to read:
                        vt.setReadArrayItem(reader, target, i);
                        i++;
                    }
                }
            } else {
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
}
