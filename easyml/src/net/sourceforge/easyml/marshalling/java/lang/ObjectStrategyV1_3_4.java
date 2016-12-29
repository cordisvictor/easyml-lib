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
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.easyml.DTD;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.*;
import net.sourceforge.easyml.util.*;

/**
 * <b>Backward compatibility:</b> ObjectStrategyV1_3_4 class that implements the
 * {@linkplain CompositeStrategy} interface for Java {@linkplain Object}s, prior
 * to EasyML 1.3.5.
 *
 * @deprecated use {@linkplain ObjectStrategy} instead.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.3.5
 * @version 1.3.8
 */
@Deprecated
public class ObjectStrategyV1_3_4 extends AbstractStrategy
        implements CompositeStrategy {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "object-o";
    /**
     * Constant defining the singleton instance.
     */
    public static final ObjectStrategyV1_3_4 INSTANCE = new ObjectStrategyV1_3_4();
    private static final String ELEMENT_OUTER = "this.out";
    private static final String ATTRIBUTE_NIL = "nil";

    protected ObjectStrategyV1_3_4() {
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
    @Deprecated
    public String name() {
        return ObjectStrategyV1_3_4.NAME;
    }

    protected boolean continueProcessFor(Class level) {
        return level != Object.class;
    }

    protected void marshalDoAttributes(Object target, CompositeAttributeWriter writer, MarshalContext ctx) {
        final Class c = target.getClass();
        writer.setAttribute(DTD.ATTRIBUTE_CLASS, ctx.aliasFor(c, c.getName()));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    @Deprecated
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
            writer.startElement(ObjectStrategyV1_3_4.ELEMENT_OUTER);
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
        while (this.continueProcessFor(cls)) { // process inheritance:
            for (Field f : cls.getDeclaredFields()) { // process composition:
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
                if (!reader.next() || !reader.atElementStart() || !reader.elementName().equals(ObjectStrategyV1_3_4.ELEMENT_OUTER)) {
                    throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                            "expected element start: " + ObjectStrategyV1_3_4.ELEMENT_OUTER);
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
    @Deprecated
    public Object unmarshalInit(Object target, CompositeReader reader, UnmarshalContext ctx)
            throws IllegalAccessException {
        // read object fields: in exactly the same order as they were written:
        Class cls = target.getClass();
        final Set<String> fields = new HashSet<>();
        while (reader.next()) {
            if (reader.atElementStart()) {
                final String localPartName = reader.elementName();
                // search the class for the specified field:
                if (!fields.add(localPartName)) {
                    cls = cls.getSuperclass(); // duplicate private field => move to super class.
                    fields.clear();
                }
                Field f = null;
                while (this.continueProcessFor(cls)) {
                    try {
                        f = ctx.fieldFor(cls, localPartName);
                        if (!Modifier.isStatic(f.getModifiers())) {
                            break; // field found.
                        }
                    } catch (NoSuchFieldException searchInSuperclass) {
                    } catch (SecurityException ex) {
                        throw new InvalidFormatException(ctx.readerPositionDescriptor(), ex);
                    }
                    cls = cls.getSuperclass();
                    fields.clear();
                }
                // check if field is indeed valid:
                if (f == null || Modifier.isStatic(f.getModifiers())) {
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
            } else if (reader.atElementEnd() && reader.elementName().equals(this.name())) {
                return target;
            }
        }// while.
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "missing element end: " + this.name());
    }
}// class ObjectStrategy.
