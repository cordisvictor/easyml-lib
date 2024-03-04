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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

/**
 * RecordStrategy class that implements {@linkplain CompositeStrategy} for
 * the Java records.
 * This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.7.1
 * @since 1.7.0
 */
public final class RecordStrategy extends AbstractStrategy implements CompositeStrategy<Record> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "record";
    /**
     * Constant defining the singleton instance.
     */
    public static final RecordStrategy INSTANCE = new RecordStrategy();
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    private RecordStrategy() {
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
        return Record.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Record> c) {
        return c.isRecord();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return RecordStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Record target, CompositeWriter writer, MarshalContext ctx) {
        Class cls = target.getClass();
        writer.startElement(RecordStrategy.NAME);
        writer.setAttribute(DTD.ATTRIBUTE_CLASS, ctx.aliasOrNameFor(cls));
        for (RecordComponent rc : cls.getRecordComponents()) {
            writer.write(getValue(target, rc));
        }
        writer.endElement();
    }

    private static Object getValue(Record source, RecordComponent component) {
        try {
            final MethodHandle getterMH = lookup.findVirtual(
                    source.getClass(),
                    component.getName(),
                    MethodType.methodType(component.getType()));
            return getterMH.invoke(source);
        } catch (Throwable e) {
            throw new IllegalArgumentException("component: cannot marshal record component: " + component, e);
        }
    }

    /**
     * Also inits records because constructor requires all values and does not allow escaping this reference.
     * <p>
     * {@inheritDoc }
     */
    @Override
    public Record unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException {
        final Class cls = ctx.classFor(reader.elementRequiredAttribute(DTD.ATTRIBUTE_CLASS));
        if (!cls.isRecord()) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), "element class not a record: " + cls);
        }
        // consume root tag:
        reader.next();

        // read record components: in exactly the same order as they were written:
        final RecordComponent[] rcs = cls.getRecordComponents();
        final Object[] values = new Object[rcs.length];

        int component = 0;
        while (component < rcs.length && reader.atElementStart()) {
            values[component] = reader.read();
            component++;
        }

        if (component == rcs.length && reader.atElementEnd() && reader.elementName().equals(RecordStrategy.NAME)) {
            try {
                return newRecord(cls, rcs, values);
            } catch (Throwable e) {
                throw new InvalidFormatException(ctx.readerPositionDescriptor(), "could not instantiate record: " + cls);
            }
        }
        final String message = component != rcs.length ?
                "unexpected record components: " + component + ", expected " + rcs.length :
                "unexpected element end";
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), message);
    }

    private static Record newRecord(Class recordClass, RecordComponent[] recordComponents, Object[] args) throws Throwable {
        Class<?>[] paramTypes = Arrays.stream(recordComponents)
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);
        final MethodHandle constructorMH = lookup.findConstructor(
                        recordClass,
                        MethodType.methodType(void.class, paramTypes))
                .asType(MethodType.methodType(Object.class, paramTypes));
        return (Record) constructorMH.invokeWithArguments(args);
    }
}
