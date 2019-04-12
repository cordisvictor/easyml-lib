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
import net.sourceforge.easyml.util.ReflectionUtil;
import net.sourceforge.easyml.util.ValueType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * ObjectStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for Java {@linkplain Object}s. This is a reflection-based
 * implementation used as a last resort if no other suitable strategy is found
 * for the target class.
 * <p>
 * <br/>This implementation acts as an override for the XML writer's
 * <code>writeObject</code> and XML reader's <code>readObject</code>, enabling
 * support for all-field serialization, inner classes, and encodes only the
 * non-default fields (i.e. fields with values different as the ones defined by
 * the default constructor, if any).
 * <p>
 * <br/> Non-pure Java reflection is used when un-marshalling objects of classes
 * which do not define a default constructor.
 * <p>
 * <br/>This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.1
 * @since 1.0
 */
public final class ObjectStrategy extends AbstractStrategy implements CompositeStrategy {
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

    private ObjectStrategy() {
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
     * {@inheritDoc }
     */
    @Override
    public void marshal(Object target, CompositeWriter writer, MarshalContext ctx) {
        // begin object encoding: class
        Class cls = target.getClass();
        writer.startElement(this.name());
        writer.setAttribute(DTD.ATTRIBUTE_CLASS, ctx.aliasOrNameFor(cls));
        // if inner class then write outer instance:
        final Field outerRef = ReflectionUtil.outerRefField(cls);
        Object defTarget = null;
        Object outer = null;
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
                defTarget = (outerRef != null ? ReflectionUtil.instantiateInner(cls, outer) : cls.newInstance());
            } catch (ReflectiveOperationException defaultConstructorX) {
                // cannot use defaults defined.
            }
        }
        // process inheritance:
        boolean notFirst = false;
        while (cls != Object.class) {
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
                    continue; // skip static, already encoded outer-ref object, or excluded field.
                }
                ReflectionUtil.setAccessible(f);
                // process field value:
                Object fieldValue = null;
                Object fieldDefaultValue = null;
                if (skipDefaults && defTarget != null) { // default defined:
                    try {
                        fieldValue = f.get(target);
                        fieldDefaultValue = f.get(defTarget);
                    } catch (IllegalAccessException neverThrown) {
                        // ignored.
                    }
                    // null-safe equality test:
                    if (Objects.equals(fieldValue, fieldDefaultValue)) {
                        continue; // skip default value.
                    }
                } else { // default not defined:
                    try {
                        fieldValue = f.get(target);
                    } catch (IllegalAccessException neverThrown) {
                        // ignored.
                    }
                }
                // write non-default attribute value:
                writer.startElement(ctx.aliasOrNameFor(f));
                if (fieldValue == null) {
                    writer.setAttribute(ATTRIBUTE_NIL, Boolean.toString(true));
                } else { // non-null:
                    if (ValueType.is(f.getType())) {
                        writer.writeValue(fieldValue.toString());
                    } else {
                        writer.write(fieldValue);
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
    public Object unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException {
        final Class cls = ctx.classFor(reader.elementRequiredAttribute(DTD.ATTRIBUTE_CLASS));
        Object ret;
        try {
            if (ReflectionUtil.isInnerClass(cls)) {
                if (!reader.next() || !reader.atElementStart() || !reader.elementName().equals(ObjectStrategy.ELEMENT_OUTER)) {
                    throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                            "expected element start: " + ObjectStrategy.ELEMENT_OUTER);
                }
                reader.next(); // consumed this.outer start.
                final Object outer = reader.read();
                // do not consume this.outer end: let the second step while do it.
                ret = ReflectionUtil.instantiateInner(cls, outer);
            } else {
                ret = cls.newInstance();
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
    public Object unmarshalInit(Object target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
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
                    Field f;
                    try {
                        f = ctx.fieldFor(cls, localPartName);
                    } catch (NoSuchFieldException invalidFieldName) {
                        throw new InvalidFormatException(ctx.readerPositionDescriptor(), invalidFieldName);
                    }
                    // check if field is indeed valid:
                    if (Modifier.isStatic(f.getModifiers())) {
                        throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                                "illegal field: " + cls.getName() + '.' + localPartName);
                    }
                    ReflectionUtil.setAccessible(f);
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
}
