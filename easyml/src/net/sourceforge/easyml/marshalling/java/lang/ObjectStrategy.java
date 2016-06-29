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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.sourceforge.easyml.DTD;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.*;
import net.sourceforge.easyml.util.*;

/**
 * ObjectStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for Java {@linkplain Object}s. This is a reflection-based
 * implementation used as a last resort if no other suitable strategy is found
 * for the target class.
 *
 * <br/>This implementation acts as an override for the XML writer's
 * <code>writeObject</code> and XML reader's <code>readObject</code>, enabling
 * support for all-field serialization, inner classes, and encodes only the
 * non-default fields (i.e. fields with values different as the ones defined by
 * the default constructor, if any).
 *
 * <br/> Non-pure Java reflection is used when un-marshalling objects of classes
 * which do not define a default constructor.
 *
 * <br/>This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.8
 */
public class ObjectStrategy extends AbstractStrategy
        implements CompositeStrategy {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "objectx";
    /**
     * Constant defining the singleton instance.
     */
    public static final ObjectStrategy INSTANCE = new ObjectStrategy();
    private static final String ELEMENT_OUTER = "this.out";
    private static final String ELEMENT_SUPER = "this.sup";
    private static final String ATTRIBUTE_NIL = "nil";

    protected ObjectStrategy() {
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
        return Object.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class c) {
        return !c.isArray(); // do not override array strategy.
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return ObjectStrategy.NAME;
    }

    /**
     * Returns true if the given inheritance <code>level</code> will be included
     * in the processing (marshalling or unmarshalling), false if processing
     * will end at the given level.
     *
     * @param level the inheritance level
     *
     * @return true if continue, false otherwise
     */
    protected boolean continueProcessFor(Class level) {
        return level != Object.class;
    }

    /**
     * Marshalling writing root attributes stage. Writes the <code>class</code>
     * attribute.
     *
     * @param target target to extract attribute values from
     * @param writer to write attributes with
     * @param ctx the context
     */
    protected void marshalDoAttributes(Object target, CompositeAttributeWriter writer, MarshalContext ctx) {
        final Class c = target.getClass();
        writer.setAttribute(DTD.ATTRIBUTE_CLASS, ctx.aliasFor(c, c.getName()));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Object target, CompositeWriter writer, MarshalContext ctx) {
        // begin object encoding: class
        writer.startElement(this.name());
        this.marshalDoAttributes(target, writer, ctx);
        // begin object encoding: if non-static inner class then write outer instance:
        Class cls = target.getClass();
        Object defTarget = null;
        Object outer = null;
        final Field outerRef = ReflectionUtil.outerRefField(cls);
        if (outerRef != null) {
            try {
                outer = outerRef.get(target);
            } catch (IllegalAccessException neverThrown) {
                // ignored.
            }
            writer.startElement(ObjectStrategy.ELEMENT_OUTER);
            writer.write(outer);
            writer.endElement();
        }
        // if skipDefaults and defTarget not inited then init for comparison usage:
        final boolean skipDefaults = ctx.skipDefaults();
        if (skipDefaults) {
            try {
                defTarget = (outerRef != null ? ReflectionUtil.instantiateInner(cls, outer) : ctx.defaultInstanceFor(cls));
            } catch (ReflectiveOperationException defaultConstructorX) {
                // cannot use defaults defined.
            }
        }
        // process inheritance:
        boolean notFirst = false;
        while (this.continueProcessFor(cls)) {
            if (notFirst) {
                writer.startElement(ELEMENT_SUPER);
                writer.endElement();
            }
            notFirst = true;
            // process composition:
            for (Field f : cls.getDeclaredFields()) {
                // process field:
                if (Modifier.isStatic(f.getModifiers())
                        || (outerRef != null && f.getName().equals(outerRef.getName()))
                        || ctx.excluded(f)) {
                    continue; // skip static, already encoded outer-refed object, or excluded field.
                }
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                // process field value:
                Object attributeValue = null;
                Object defaultValue = null;
                if (skipDefaults && defTarget != null) { // default defined:
                    try {
                        attributeValue = f.get(target);
                        defaultValue = f.get(defTarget);
                    } catch (IllegalAccessException neverThrown) {
                        // ignored.
                    }
                    // null-safe equality test:
                    if (attributeValue == null) {
                        if (defaultValue == null) {
                            continue; // skip default value.
                        }
                    } else {
                        if (attributeValue.equals(defaultValue)) {
                            continue; // skip default value.
                        }
                    }
                } else { // default not defined:
                    try {
                        attributeValue = f.get(target);
                    } catch (IllegalAccessException neverThrown) {
                        // ignored.
                    }
                }
                // write non-default attribute value:
                writer.startElement(ctx.aliasFor(f, f.getName()));
                if (attributeValue == null) {
                    writer.setAttribute(ATTRIBUTE_NIL, Boolean.toString(true));
                } else { // non-null:
                    if (ValueType.is(f.getType())) {
                        writer.writeValue(attributeValue.toString());
                    } else {
                        writer.write(attributeValue);
                    }
                }
                writer.endElement();
            }
            cls = cls.getSuperclass();
        }
        // end object encoding:
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object unmarshalNew(CompositeReader reader, UnmarshalContext ctx)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final Class cls = ctx.classFor(reader.elementRequiredAttribute(DTD.ATTRIBUTE_CLASS));
        Object ret;
        try {
            if (ReflectionUtil.hasOuterRefField(cls)) {
                if (!reader.next() || !reader.atElementStart() || !reader.elementName().equals(ObjectStrategy.ELEMENT_OUTER)) {
                    throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                            "expected element start: " + ObjectStrategy.ELEMENT_OUTER);
                }
                reader.next(); // consumed this.outer start.
                final Object outer = reader.read();
                // do not consume this.outer end: let the second step while do it.
                ret = ReflectionUtil.instantiateInner(cls, outer);
            } else {
                ret = ctx.defaultConstructorFor(cls).newInstance();
            }
        } catch (ReflectiveOperationException defaultConstructorX) {
            ret = ReflectionUtil.instantiateUnsafely(cls);
        }
        return ret;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object unmarshalInit(Object target, CompositeReader reader, UnmarshalContext ctx)
            throws IllegalAccessException {
        // read object fields: in exactly the same order as they were written:
        Class cls = target.getClass();
        while (reader.next()) {
            if (reader.atElementStart()) {
                if (reader.elementName().equals(ELEMENT_SUPER)) {
                    reader.next(); // consume start, leaving end tag to be consumed by the next while.
                    cls = cls.getSuperclass();
                } else {
                    // field: search the class for it:
                    final String localPartName = reader.elementName();
                    Field f = null;
                    try {
                        f = ctx.fieldFor(cls, localPartName);
                    } catch (NoSuchFieldException invalidFieldName) {
                        throw new InvalidFormatException(ctx.readerPositionDescriptor(), invalidFieldName);
                    } catch (SecurityException ex) {
                        throw new InvalidFormatException(ctx.readerPositionDescriptor(), ex);
                    }
                    // check if field is indeed valid:
                    if (Modifier.isStatic(f.getModifiers())) {
                        throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                                "illegal field: " + cls.getName() + '.' + localPartName);
                    }
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    // read and set it to field:
                    final String nilAttr = reader.elementAttribute(ATTRIBUTE_NIL);
                    if (nilAttr != null && Boolean.parseBoolean(nilAttr)) {
                        f.set(target, null);
                    } else {
                        final ValueType vt = ValueType.of(f.getType());
                        if (vt != null) {
                            try {
                                f.set(target, vt.parseValue(reader.readValue()));
                            } catch (NumberFormatException nfx) {
                                throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
                            } catch (IllegalArgumentException iax) {
                                throw new InvalidFormatException(ctx.readerPositionDescriptor(), iax);
                            }
                        } else { // move down and read object:
                            if (!reader.next() || !reader.atElementStart()) {
                                throw new InvalidFormatException(ctx.readerPositionDescriptor(), "expected element start");
                            }
                            f.set(target, reader.read());
                        }
                    }
                }
            } else if (reader.atElementEnd() && reader.elementName().equals(this.name())) {
                return target;
            }
        }// while.
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "missing element end: " + this.name());
    }
}// class ObjectStrategy.
