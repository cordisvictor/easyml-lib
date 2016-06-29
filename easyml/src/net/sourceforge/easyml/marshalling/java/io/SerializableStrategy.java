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
package net.sourceforge.easyml.marshalling.java.io;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.easyml.DTD;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.*;
import net.sourceforge.easyml.util.*;

/**
 * SerializableStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for Java {@linkplain Serializable}s. The implementation supports:
 * <ul> <li><code>transient</code> keyword</li> <li>default read and write
 * object with redundancy checking</li>
 * <li><code>readObject(ObjectInputStream)</code></li>
 * <li><code>writeObject(ObjectOutputStream)</code></li>
 * <li><code>writeReplace()</code></li> <li><code>readResolve()</code></li>
 * </ul> The <code>writeObject(ObjectOutputStream)</code> and
 * <code>readObject(ObjectInputStream)</code> do not support the following: <ul> <li>{@linkplain ObjectOutputStream#writeUnshared(java.lang.Object)
 * }</li> <li>{@linkplain ObjectInputStream#readUnshared() }</li>
 * <li>{@linkplain ObjectInputStream#registerValidation(java.io.ObjectInputValidation,int)}</li>
 * </ul>
 *
 * <br/> Non-pure Java reflection is used when un-marshalling objects of classes
 * which do not define a default constructor.<br/>This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.8
 */
public class SerializableStrategy extends AbstractStrategy<Serializable>
        implements CompositeStrategy<Serializable> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "serial";
    /**
     * Constant defining the singleton instance.
     */
    public static final SerializableStrategy INSTANCE = new SerializableStrategy();
    private static final String ELEMENT_OUTER = "this.out";
    private static final String ELEMENT_FIELDS = "this.fields";
    private static final String ATTRIBUTE_NIL = "nil";
    private static final String METHOD_WRITEOBJECT = "writeObject";
    private static final String METHOD_READOBJECT = "readObject";
    private static final String METHOD_WRITEREPLACE = "writeReplace";
    private static final String METHOD_READRESOLVE = "readResolve";
    private static final Class[] PARAMS_OOS = new Class[]{ObjectOutputStream.class};
    private static final Class[] PARAMS_OIS = new Class[]{ObjectInputStream.class};

    private static ValueType valueTypeFor(Class declaring, String field) {
        try {
            return valueTypeFor(declaring.getDeclaredField(field));
        } catch (NoSuchFieldException nsfX) {
            return null;
        }
    }

    private static ValueType valueTypeFor(Field f) {
        return Modifier.isStatic(f.getModifiers()) ? null : ValueType.of(f.getType());
    }

    protected SerializableStrategy() {
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
        return Serializable.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Serializable> c) {
        return Serializable.class.isAssignableFrom(c) && !c.isArray(); // do not override array strategy.
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return SerializableStrategy.NAME;
    }

    /**
     * Returns true if the given inheritance <code>level</code> will be included
     * in the processing (marshalling or unmarshalling), false if processing
     * will end at the given level.
     *
     * @param level of inheritance to check if process should continue for
     *
     * @return true if continue, false otherwise
     */
    protected boolean continueProcessFor(Class level) {
        return Serializable.class.isAssignableFrom(level);
    }

    private void defaultMarshalObject(Serializable target, Serializable defTarget, CompositeWriter writer, MarshalContext ctx, Class level, Field outerRef) {
        writer.startElement(ELEMENT_FIELDS);
        for (Field f : level.getDeclaredFields()) { // process composition:
            // process field:
            final int fMod = f.getModifiers();
            if (Modifier.isStatic(fMod)
                    || Modifier.isTransient(fMod)
                    || (outerRef != null && f.getName().equals(outerRef.getName()))
                    || ctx.excluded(f)) {
                continue; // skip static, transient, already encoded outer-refed object, or excluded field.
            }
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            // process field value:
            Object attributeValue = null;
            Object defaultValue = null;
            if (ctx.skipDefaults() && defTarget != null) { // default target defined:
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
            } else { // comparison default value undefined:
                try {
                    attributeValue = f.get(target);
                } catch (IllegalAccessException neverThrown) {
                    // ignored.
                }
            }
            // encode non-default attribute value:
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
        writer.endElement();
    }

    /**
     * Marshalling writing root attributes stage. Writes the <code>class</code>
     * attribute.
     *
     * @param target target to extract attribute values from
     * @param writer to write attributes with
     * @param ctx the context
     */
    protected void marshalDoAttributes(Serializable target, CompositeAttributeWriter writer, MarshalContext ctx) {
        final Class c = target.getClass();
        writer.setAttribute(DTD.ATTRIBUTE_CLASS, ctx.aliasFor(c, c.getName()));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Serializable target, CompositeWriter writer, MarshalContext ctx) {
        Serializable theTarget = target;
        Serializable theDef = null;
        // check for writeReplace():
        try {
            final Method writeReplaceM = theTarget.getClass().getDeclaredMethod(METHOD_WRITEREPLACE);
            writeReplaceM.setAccessible(true); // method may be private. Hence must be set accessible true.
            final Object replacement = writeReplaceM.invoke(theTarget);
            if (replacement == null) {
                writer.write(null);
                return;
            }
            if (!(replacement instanceof Serializable)) {
                throw new RuntimeException(new NotSerializableException(replacement.getClass().getName()));
            }
            theTarget = (Serializable) replacement;
        } catch (NoSuchMethodException writeReplaceNotFound) {
            // ignore.
        } catch (InvocationTargetException writeReplaceFailure) {
            throw new RuntimeException(writeReplaceFailure);
        } catch (IllegalAccessException neverThrown) {
            // ignore.
        }
        // begin object encoding:
        writer.startElement(this.name());
        this.marshalDoAttributes(theTarget, writer, ctx);
        // begin object encoding: if non-static inner class then write outer instance:
        final Class<Serializable> cls = (Class<Serializable>) theTarget.getClass();
        Object outer = null;
        final Field outerRef = ReflectionUtil.outerRefField(cls);
        if (outerRef != null) {
            try {
                outer = outerRef.get(theTarget);
            } catch (IllegalAccessException neverThrown) {
                // ignored.
            }
            writer.startElement(SerializableStrategy.ELEMENT_OUTER);
            writer.write(outer);
            writer.endElement();
        }
        // if skipDefaults then init default for comparison usage:
        if (ctx.skipDefaults()) {
            try {
                theDef = outerRef != null ? ReflectionUtil.instantiateInner(cls, outer) : ctx.defaultInstanceFor(cls);
            } catch (ReflectiveOperationException defaultConstructorX) {
                // cannot use defaults defined.
            }
        }
        try {
            final SOutputStream sos = new SOutputStream(theTarget, theDef, writer, ctx, cls, outerRef);
            do {// process inheritance:
                try { // process composition:
                    // check for writeObject():
                    final Method writeObjectM = sos.level.getDeclaredMethod(METHOD_WRITEOBJECT, PARAMS_OOS);
                    writeObjectM.setAccessible(true); // method should be private. Hence must be set accessible true.
                    writeObjectM.invoke(theTarget, sos);
                } catch (NoSuchMethodException nsmX) {
                    this.defaultMarshalObject(theTarget, theDef, writer, ctx, sos.level, outerRef);
                } catch (InvocationTargetException itX) {
                    throw new IllegalArgumentException(itX);
                } catch (IllegalAccessException neverThrown) {
                    // ignored.
                }
                sos.level = sos.level.getSuperclass();
            } while (this.continueProcessFor(sos.level));
        } catch (IOException neverThrown) {
            // ignored.
        }
        // end object encoding:
        writer.endElement();
    }

    private void defaultUnmarshalObject(Object instance, CompositeReader reader, UnmarshalContext ctx, Class level) {
        if (!reader.atElementStart() || !reader.elementName().equals(SerializableStrategy.ELEMENT_FIELDS)) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                    "expected: " + SerializableStrategy.ELEMENT_FIELDS);
        }
        while (reader.next()) {
            if (reader.atElementStart()) {
                final String localPartName = reader.elementName();
                // search the class-level for the specified field:
                try {
                    Field f = ctx.fieldFor(level, localPartName);
                    // check if field is indeed valid:
                    if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) {
                        throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                                "illegal field: " + level.getName() + '.' + localPartName);
                    }
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }

                    // read and set it to field:
                    final String nilAttr = reader.elementAttribute(ATTRIBUTE_NIL);
                    if (nilAttr != null && Boolean.parseBoolean(nilAttr)) {
                        f.set(instance, null);
                    } else {
                        final ValueType vt = ValueType.of(f.getType());
                        if (vt != null) {
                            try {
                                f.set(instance, vt.parseValue(reader.readValue()));
                            } catch (NumberFormatException nfx) {
                                throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
                            } catch (IllegalArgumentException iax) {
                                throw new InvalidFormatException(ctx.readerPositionDescriptor(), iax);
                            }
                        } else { // move down and read object:
                            if (!reader.next() || !reader.atElementStart()) {
                                throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                                        "expected element start");
                            }
                            f.set(instance, reader.read());
                        }
                    }
                } catch (IllegalAccessException neverThrown) {
                    // field is set to accessible. Hence ignore.
                } catch (NoSuchFieldException nsfX) {
                    throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                            "undefined field: " + level.getName() + '.' + localPartName);
                } catch (SecurityException sX) {
                    throw new InvalidFormatException(ctx.readerPositionDescriptor(), sX);
                }
            } else if (reader.atElementEnd() && reader.elementName().equals(SerializableStrategy.ELEMENT_FIELDS)) {
                reader.next();
                return;
            }
        }// while.
        throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                "missing element end: " + SerializableStrategy.ELEMENT_FIELDS);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Serializable unmarshalNew(CompositeReader reader, UnmarshalContext ctx)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final String classAttrVal = reader.elementRequiredAttribute(DTD.ATTRIBUTE_CLASS);
        final Class cls = ctx.classFor(classAttrVal);
        if (Serializable.class.isAssignableFrom(cls)) {
            Object ret;
            try {
                if (ReflectionUtil.hasOuterRefField(cls)) {
                    if (!reader.next() || !reader.atElementStart() || !reader.elementName().equals(SerializableStrategy.ELEMENT_OUTER)) {
                        throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                                "expected element start: " + SerializableStrategy.ELEMENT_OUTER);
                    }
                    reader.next(); // consumed start this.outer.
                    final Object outer = reader.read();
                    // do not consume this.outer end: let the second step while do it.
                    ret = ReflectionUtil.instantiateInner(cls, outer);
                } else {
                    ret = ctx.defaultConstructorFor(cls).newInstance();
                }
            } catch (ReflectiveOperationException defaultConstructorX) {
                ret = ReflectionUtil.instantiateUnsafely(cls);
            }
            return (Serializable) ret;
        }
        throw new IllegalArgumentException("class not serializable: " + classAttrVal);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Serializable unmarshalInit(Serializable target, CompositeReader reader, UnmarshalContext ctx)
            throws IllegalAccessException {
        // read object attributes: in exactly the same order as they were written:
        if (!reader.next() || !reader.atElementStart()) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                    "expected: start element, found: " + reader.elementName());
        }
        final Class cls = target.getClass();
        try {
            final SInputStream sis = new SInputStream(target, reader, ctx, cls);
            do {
                try {
                    // check for readObject():
                    final Method readObject = sis.level.getDeclaredMethod(METHOD_READOBJECT, PARAMS_OIS);
                    readObject.setAccessible(true); // method should be private. Hence must be set accessible true.
                    readObject.invoke(target, sis);
                } catch (NoSuchMethodException nsmX) {
                    this.defaultUnmarshalObject(target, reader, ctx, sis.level);
                } catch (InvocationTargetException itX) {
                    throw new IllegalArgumentException(itX);
                } catch (IllegalAccessException neverThrown) {
                    // ignored.
                }
                sis.level = sis.level.getSuperclass();
            } while (this.continueProcessFor(sis.level));
        } catch (IOException neverThrown) {
            // ignored.
        }
        if (reader.atElementEnd() && reader.elementName().equals(this.name())) {
            // check for readResolve():
            try {
                final Method readResolveM = cls.getDeclaredMethod(METHOD_READRESOLVE);
                readResolveM.setAccessible(true); // method may be private. Hence must be set accessible true.
                final Object resolved = readResolveM.invoke(target);
                if (resolved == null) {
                    return null;
                }
                if (!(resolved instanceof Serializable)) {
                    throw new RuntimeException(new NotSerializableException(resolved.getClass().getName()));
                }
                return (Serializable) resolved;
            } catch (NoSuchMethodException readResolveNotFound) {
                // ignore.
            } catch (InvocationTargetException readResolveFailure) {
                throw new RuntimeException(readResolveFailure);
            } catch (IllegalAccessException neverThrown) {
                // ignore.
            }
            return target;
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "missing element end: " + this.name());
    }

    private final class SOutputStream extends ObjectOutputStream {

        private final Serializable target;
        private final Serializable defs;
        private final CompositeWriter writer;
        private final MarshalContext context;
        private Class level;
        private final Field outerRef;
        private PutFieldImpl lazyPutFieldImpl;

        public SOutputStream(Serializable target, Serializable defs, CompositeWriter writer, MarshalContext ctx, Class level, Field outerRef)
                throws IOException {
            this.target = target;
            this.defs = defs;
            this.writer = writer;
            this.context = ctx;
            this.level = level;
            this.outerRef = outerRef;
            this.lazyPutFieldImpl = null; // lazy.
        }

        @Override
        public void close() throws IOException {
            // non-op.
        }

        @Override
        public void defaultWriteObject() throws IOException {
            defaultMarshalObject(this.target, this.defs, this.writer, this.context, this.level, this.outerRef);
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public PutField putFields() throws IOException {
            if (this.lazyPutFieldImpl == null) {
                this.lazyPutFieldImpl = new PutFieldImpl();
            }
            return this.lazyPutFieldImpl;
        }

        @Override
        public void reset() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void useProtocolVersion(int version) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(int val) throws IOException {
            this.writer.write((byte) val);
        }

        @Override
        public void write(byte[] buf) throws IOException {
            this.writer.write(buf);
        }

        @Override
        public void write(byte[] buf, int off, int len) throws IOException {
            this.writer.write(Arrays.copyOfRange(buf, off, len - off));
        }

        @Override
        public void writeBoolean(boolean val) throws IOException {
            this.writer.write(val);
        }

        @Override
        public void writeByte(int val) throws IOException {
            this.writer.write((byte) val);
        }

        @Override
        public void writeBytes(String str) throws IOException {
            this.writer.write(str.getBytes());
        }

        @Override
        public void writeChar(int val) throws IOException {
            this.writer.write((char) val);
        }

        @Override
        public void writeChars(String str) throws IOException {
            final char[] chars = new char[str.length()];
            str.getChars(0, chars.length, chars, 0);
            this.writer.write(chars);
        }

        @Override
        public void writeDouble(double val) throws IOException {
            this.writer.write(val);
        }

        @Override
        public void writeFields() throws IOException {
            this.writer.startElement(SerializableStrategy.ELEMENT_FIELDS);
            if (this.lazyPutFieldImpl != null) {
                for (Map.Entry<String, Object> field : this.lazyPutFieldImpl.content.entrySet()) {
                    final String key = field.getKey();
                    final Object val = field.getValue();
                    this.writer.startElement(key);
                    if (val == null) {
                        this.writer.setAttribute(SerializableStrategy.ATTRIBUTE_NIL, Boolean.toString(true));
                    } else { // non-null:
                        if (SerializableStrategy.valueTypeFor(this.level, key) != null) {
                            this.writer.writeValue(val.toString());
                        } else {
                            this.writer.write(val);
                        }
                    }
                    this.writer.endElement();
                }
            }
            this.writer.endElement();
        }

        @Override
        public void writeFloat(float val) throws IOException {
            this.writer.write(val);
        }

        @Override
        public void writeInt(int val) throws IOException {
            this.writer.write(val);
        }

        @Override
        public void writeLong(long val) throws IOException {
            this.writer.write(val);
        }

        @Override
        protected void writeObjectOverride(Object obj) throws IOException {
            this.writer.write(obj);
        }

        @Override
        public void writeShort(int val) throws IOException {
            this.writer.write((short) val);
        }

        @Override
        public void writeUTF(String str) throws IOException {
            this.writer.write(str);
        }

        @Override
        public void writeUnshared(Object obj) throws IOException {
            throw new UnsupportedOperationException();
        }
    }//(+)class SOutputStream.

    private static final class PutFieldImpl extends ObjectOutputStream.PutField {

        private final Map<String, Object> content = new HashMap<String, Object>();

        @Override
        public void put(String name, boolean val) {
            this.content.put(name, val);
        }

        @Override
        public void put(String name, byte val) {
            this.content.put(name, val);
        }

        @Override
        public void put(String name, char val) {
            this.content.put(name, val);
        }

        @Override
        public void put(String name, short val) {
            this.content.put(name, val);
        }

        @Override
        public void put(String name, int val) {
            this.content.put(name, val);
        }

        @Override
        public void put(String name, long val) {
            this.content.put(name, val);
        }

        @Override
        public void put(String name, float val) {
            this.content.put(name, val);
        }

        @Override
        public void put(String name, double val) {
            this.content.put(name, val);
        }

        @Override
        public void put(String name, Object val) {
            this.content.put(name, val);
        }

        @Deprecated
        @Override
        public void write(ObjectOutput out) throws IOException {
            throw new UnsupportedOperationException("Deprecated");
        }
    }//(+)class PutFieldImpl.

    private final class SInputStream extends ObjectInputStream {

        private Object instance;
        private CompositeReader reader;
        private final UnmarshalContext context;
        private Class level;

        public SInputStream(Object instance, CompositeReader reader, UnmarshalContext ctx, Class level) throws IOException {
            this.instance = instance;
            this.reader = reader;
            this.context = ctx;
            this.level = level;
        }

        @Override
        public void registerValidation(ObjectInputValidation obj, int prio) throws NotActiveException, InvalidObjectException {
        }

        @Override
        public void close() throws IOException {
            this.reader = null;
            this.instance = null;
            this.level = null;
        }

        @Override
        public void defaultReadObject() throws IOException, ClassNotFoundException {
            defaultUnmarshalObject(this.instance, this.reader, this.context, this.level);
        }

        @Override
        public int read() throws IOException {
            return (Integer) this.reader.read();
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            final byte[] read = (byte[]) this.reader.readArray(byte.class);
            System.arraycopy(read, 0, buf, off, len);
            return read.length;
        }

        @Override
        public boolean readBoolean() throws IOException {
            return (Boolean) this.reader.read();
        }

        @Override
        public byte readByte() throws IOException {
            return (Byte) this.reader.read();
        }

        @Override
        public char readChar() throws IOException {
            return (Character) this.reader.read();
        }

        @Override
        public double readDouble() throws IOException {
            return (Double) this.reader.read();
        }

        @Override
        public GetField readFields() throws IOException, ClassNotFoundException {
            if (!this.reader.atElementStart() || !this.reader.elementName().equals(SerializableStrategy.ELEMENT_FIELDS)) {
                throw new InvalidFormatException(this.context.readerPositionDescriptor(),
                        "expected: " + SerializableStrategy.ELEMENT_FIELDS + ", found: " + this.reader.elementName());
            }
            final Map<String, Object> fields = new HashMap<String, Object>();
            while (this.reader.next()) {
                if (this.reader.atElementStart()) {
                    final String localPartName = this.reader.elementName();
                    final String nilAttr = this.reader.elementAttribute(ATTRIBUTE_NIL);
                    if (nilAttr != null && Boolean.parseBoolean(nilAttr)) {
                        fields.put(localPartName, null);
                    } else {
                        // check for an alias in case readFields reads XML written by defaultMarshalObject:
                        try {
                            final Field aliasedF = this.context.fieldFor(this.level, localPartName);
                            final ValueType keyVT = SerializableStrategy.valueTypeFor(aliasedF);
                            if (keyVT != null) {
                                try {
                                    fields.put(localPartName, keyVT.parseValue(this.reader.readValue()));
                                } catch (NumberFormatException nfx) {
                                    throw new InvalidFormatException(this.context.readerPositionDescriptor(), nfx);
                                } catch (IllegalArgumentException iax) {
                                    throw new InvalidFormatException(this.context.readerPositionDescriptor(), iax);
                                }
                            } else { // move down and read object:
                                if (!this.reader.next() || !this.reader.atElementStart()) {
                                    throw new InvalidFormatException(this.context.readerPositionDescriptor(), "expected element start");
                                }
                                fields.put(localPartName, this.reader.read());
                            }
                        } catch (NoSuchFieldException invalidLocalPartNameX) {
                            throw new IOException(invalidLocalPartNameX);
                        }
                    }
                } else if (this.reader.atElementEnd() && this.reader.elementName().equals(SerializableStrategy.ELEMENT_FIELDS)) {
                    this.reader.next(); // consume end tag.
                    return new GetFieldImpl(fields);
                }
            }// while.
            throw new InvalidFormatException(this.context.readerPositionDescriptor(),
                    "missing element end: " + SerializableStrategy.ELEMENT_FIELDS);
        }

        @Override
        public float readFloat() throws IOException {
            return (Float) this.reader.read();
        }

        @Override
        public void readFully(byte[] buf) throws IOException {
            this.read(buf, 0, buf.length);
        }

        @Override
        public void readFully(byte[] buf, int off, int len) throws IOException {
            this.read(buf, off, len);
        }

        @Override
        public int readInt() throws IOException {
            return (Integer) this.reader.read();
        }

        @Override
        public long readLong() throws IOException {
            return (Long) this.reader.read();
        }

        @Override
        protected Object readObjectOverride() throws IOException, ClassNotFoundException {
            return this.reader.read();
        }

        @Override
        public short readShort() throws IOException {
            return (Short) this.reader.read();
        }

        @Override
        public String readUTF() throws IOException {
            return (String) this.reader.read();
        }

        @Override
        public Object readUnshared() throws IOException, ClassNotFoundException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int readUnsignedByte() throws IOException {
            return (Integer) this.reader.read();
        }

        @Override
        public int readUnsignedShort() throws IOException {
            return (Integer) this.reader.read();
        }

        @Override
        public int skipBytes(int len) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized void mark(int readlimit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public int read(byte[] b) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized void reset() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public long skip(long n) throws IOException {
            throw new UnsupportedOperationException();
        }
    }//(+)class SInputStream.

    private static final class GetFieldImpl extends ObjectInputStream.GetField {

        private final Map content;

        public GetFieldImpl(Map content) {
            this.content = content;
        }

        @Override
        public ObjectStreamClass getObjectStreamClass() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean defaulted(String name) throws IOException {
            return false;
        }

        public <T> T get0(String name, Class<T> c) {
            final Object value = this.content.get(name);
            if (value != null) {
                if (value.getClass() == c) {
                    return (T) value;
                }
                throw new IllegalArgumentException("field name does not map to required type: " + name + ", " + c.getName());
            }
            return null;
        }

        @Override
        public boolean get(String name, boolean val) throws IOException {
            final Boolean value = this.get0(name, Boolean.class);
            return value != null ? value : val;
        }

        @Override
        public byte get(String name, byte val) throws IOException {
            final Byte value = this.get0(name, Byte.class);
            return value != null ? value : val;
        }

        @Override
        public char get(String name, char val) throws IOException {
            final Character value = this.get0(name, Character.class);
            return value != null ? value : val;
        }

        @Override
        public short get(String name, short val) throws IOException {
            final Short value = this.get0(name, Short.class);
            return value != null ? value : val;
        }

        @Override
        public int get(String name, int val) throws IOException {
            final Integer value = this.get0(name, Integer.class);
            return value != null ? value : val;
        }

        @Override
        public long get(String name, long val) throws IOException {
            final Long value = this.get0(name, Long.class);
            return value != null ? value : val;
        }

        @Override
        public float get(String name, float val) throws IOException {
            final Float value = this.get0(name, Float.class);
            return value != null ? value : val;
        }

        @Override
        public double get(String name, double val) throws IOException {
            final Double value = this.get0(name, Double.class);
            return value != null ? value : val;
        }

        @Override
        public Object get(String name, Object val) throws IOException {
            final Object value = this.content.get(name);
            return value != null ? value : val;
        }
    }//(+)class GetFieldImpl.
}//class SerializableStrategy.
