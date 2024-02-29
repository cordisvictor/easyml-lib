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
package net.sourceforge.easyml.marshalling.java.io;

import net.sourceforge.easyml.DTD;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.*;
import net.sourceforge.easyml.util.ReflectionUtil;
import net.sourceforge.easyml.util.ReflectionUtil.ValueType;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
 * <li>{@linkplain ObjectInputStream#registerValidation(java.io.ObjectInputValidation, int)}</li>
 * </ul>
 * <p>
 * <br/> Non-pure Java reflection is used when un-marshalling objects of classes
 * which do not define a default constructor.<br/>This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.6.0
 * @since 1.0
 */
public final class SerializableStrategy extends AbstractStrategy implements CompositeStrategy<Serializable>, Caching {
    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "serial";
    private static final String ELEMENT_OUTER = "this.out";
    private static final String ELEMENT_FIELDS = "this.fields";
    private static final String ATTRIBUTE_NIL = "nil";
    private static final String FIELD_PERSISTENTFIELDS = "serialPersistentFields";
    private static final int MODIFIERS_PERSISTENTFIELDS = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;
    private static final String METHOD_WRITEREPLACE = "writeReplace";
    private static final String METHOD_WRITEOBJECT = "writeObject";
    private static final String METHOD_READRESOLVE = "readResolve";
    private static final String METHOD_READOBJECT = "readObject";
    private static final Class[] PARAMS_OOS = new Class[]{ObjectOutputStream.class};
    private static final Class[] PARAMS_OIS = new Class[]{ObjectInputStream.class};
    private static final NoSuchMethodException EXCEPTION_FLOW_NOSUCHMETHOD = new NoSuchMethodException();
    private static final Method NO_METHOD = emptyMethod();

    private static Method emptyMethod() {
        try {
            return Object.class.getMethod("toString");
        } catch (NoSuchMethodException somethingIsVeryWrong) {
            throw new ExceptionInInitializerError(somethingIsVeryWrong);
        }
    }

    private final ConcurrentHashMap<Class, Method> writeReplaceCache;
    private final ConcurrentHashMap<Class, Method> writeObjectCache;
    private final ConcurrentHashMap<Class, Method> readResolveCache;
    private final ConcurrentHashMap<Class, Method> readObjectCache;

    public SerializableStrategy() {
        writeReplaceCache = new ConcurrentHashMap<>();
        writeObjectCache = new ConcurrentHashMap<>();
        readResolveCache = new ConcurrentHashMap<>();
        readObjectCache = new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearCache() {
        writeReplaceCache.clear();
        writeObjectCache.clear();
        readResolveCache.clear();
        readObjectCache.clear();
    }

    private Method cachedWriteReplace(Class source) throws NoSuchMethodException {
        return methodOrThrow(writeReplaceCache.computeIfAbsent(source, SerializableStrategy::reflectWriteReplace));
    }

    private static Method reflectWriteReplace(Class source) {
        return reflectMethod(source, c -> accessHierarchicalMethod(c, METHOD_WRITEREPLACE, ReflectionUtil.METHOD_NO_PARAMS));
    }

    private static Method accessHierarchicalMethod(Class classHierarchy, String methodName, Class[] methodParams) throws NoSuchMethodException {
        Class crt = classHierarchy;
        do {
            try {
                return accessDeclaredMethod(crt, methodName, methodParams);
            } catch (NoSuchMethodException e) {
                crt = crt.getSuperclass();
            }
        } while (Serializable.class.isAssignableFrom(crt));
        throw EXCEPTION_FLOW_NOSUCHMETHOD;
    }

    private static Method accessDeclaredMethod(Class cls, String methodName, Class[] methodParams) throws NoSuchMethodException {
        final Method m = cls.getDeclaredMethod(methodName, methodParams);
        ReflectionUtil.setAccessible(m);
        return m;
    }

    private static Method reflectMethod(Class source, MethodReflector reflector) {
        try {
            return reflector.reflect(source);
        } catch (NoSuchMethodException methodNotFound) {
            return NO_METHOD;
        }
    }

    private static Method methodOrThrow(Method m) throws NoSuchMethodException {
        if (m == NO_METHOD) {
            throw EXCEPTION_FLOW_NOSUCHMETHOD;
        }
        return m;
    }

    private Method cachedWriteObject(Class source) throws NoSuchMethodException {
        return methodOrThrow(writeObjectCache.computeIfAbsent(source, SerializableStrategy::reflectWriteObject));
    }

    private static Method reflectWriteObject(Class source) {
        return reflectMethod(source, c -> accessDeclaredMethod(c, METHOD_WRITEOBJECT, PARAMS_OOS));
    }

    private Method cachedReadResolve(Class source) throws NoSuchMethodException {
        return methodOrThrow(readResolveCache.computeIfAbsent(source, SerializableStrategy::reflectReadResolve));
    }

    private static Method reflectReadResolve(Class source) {
        return reflectMethod(source, c -> accessHierarchicalMethod(c, METHOD_READRESOLVE, ReflectionUtil.METHOD_NO_PARAMS));
    }

    private Method cachedReadObject(Class source) throws NoSuchMethodException {
        return methodOrThrow(readObjectCache.computeIfAbsent(source, SerializableStrategy::reflectReadObject));
    }

    private static Method reflectReadObject(Class source) {
        return reflectMethod(source, c -> accessDeclaredMethod(c, METHOD_READOBJECT, PARAMS_OIS));
    }

    @FunctionalInterface
    private interface MethodReflector {
        Method reflect(Class source) throws NoSuchMethodException;
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
        return Serializable.class.isAssignableFrom(c)
                && !c.isArray() // do not override array strategy.
                && ReflectionUtil.isUnrestrictedOrOpen(c, SerializableStrategy.class);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return SerializableStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Serializable target, CompositeWriter writer, MarshalContext ctx) {
        Object theTarget = target;
        Object theDef = null;
        // check for writeReplace():
        try {
            final Object replacement = cachedWriteReplace(theTarget.getClass())
                    .invoke(theTarget);
            if (replacement == null || replacement instanceof Externalizable) {
                writer.write(replacement); // redirect to null or Externalizable.
                return;
            }
            if (!(replacement instanceof Serializable)) {
                throw new RuntimeException(new NotSerializableException(replacement.getClass().getName()));
            }
            theTarget = replacement; // continue here but with replacement as target.
        } catch (NoSuchMethodException | IllegalAccessException writeReplaceNotFound) {
            // writeReplace not found.
        } catch (InvocationTargetException writeReplaceFailure) {
            throw new RuntimeException(writeReplaceFailure);
        }
        // begin object encoding:
        final Class cls = theTarget.getClass();
        writer.startElement(this.name());
        writer.setAttribute(DTD.ATTRIBUTE_CLASS, ctx.aliasOrNameFor(cls));
        // if inner class then write outer instance:
        final Field clsOuter = ReflectionUtil.outerRefField(cls);
        Object outer = null;
        if (clsOuter != null) {
            try {
                outer = ReflectionUtil.readField(theTarget, clsOuter);
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
                theDef = clsOuter != null ? ReflectionUtil.instantiateInner(cls, outer) : ReflectionUtil.instantiate(cls);
            } catch (ReflectiveOperationException defaultConstructorX) {
                // cannot use defaults defined.
            }
        }
        final SerOutputStream outStream = SerOutputStream.tryCreate(theTarget, theDef, writer, ctx, cls, clsOuter);
        do {// process inheritance:
            try { // process composition:
                // check for writeObject():
                cachedWriteObject(outStream.level)
                        .invoke(theTarget, outStream);
            } catch (NoSuchMethodException writeObjectNotFound) {
                defaultMarshalObject(theTarget, theDef, writer, ctx, outStream.level, clsOuter);
            } catch (InvocationTargetException writeObjectFailure) {
                throw new RuntimeException(writeObjectFailure);
            } catch (IllegalAccessException neverThrown) {
                // ignored.
            }
            outStream.level = outStream.level.getSuperclass();
        } while (Serializable.class.isAssignableFrom(outStream.level));
        // end object encoding:
        writer.endElement();
    }

    private static void defaultMarshalObject(Object target, Object defTarget, CompositeWriter writer, MarshalContext ctx, Class level, Field outerRef) {
        writer.startElement(ELEMENT_FIELDS);
        for (Field f : level.getDeclaredFields()) { // process composition:
            // process field:
            final int fMod = f.getModifiers();
            if (Modifier.isStatic(fMod)
                    || Modifier.isTransient(fMod)
                    || (outerRef != null && f.getName().equals(outerRef.getName()))
                    || ctx.excluded(f)) {
                continue; // skip static, transient, already encoded outer-ref object, or EasyML excluded field.
            }
            ReflectionUtil.setAccessible(f);
            // process field value:
            Object fieldValue = null;
            Object fieldDefaultValue = null;
            if (ctx.skipDefaults() && defTarget != null) {
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
            } else { // comparison default value undefined:
                try {
                    fieldValue = f.get(target);
                } catch (IllegalAccessException neverThrown) {
                    // ignored.
                }
            }
            // encode non-default field value:
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
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Serializable unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException {
        final String classAttr = reader.elementRequiredAttribute(DTD.ATTRIBUTE_CLASS);
        final Class cls = ctx.classFor(classAttr);
        if (!Serializable.class.isAssignableFrom(cls)) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(), "class not serializable: " + classAttr);
        }
        Object ret;
        try {
            if (ReflectionUtil.isInnerClass(cls)) {
                if (!reader.next() || !reader.atElementStart() || !reader.elementName().equals(SerializableStrategy.ELEMENT_OUTER)) {
                    throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                            "expected element start: " + SerializableStrategy.ELEMENT_OUTER);
                }
                reader.next(); // consumed start this.outer.
                final Object outer = reader.read();
                // do not consume this.outer end: let unmarshalInit() while-loop do it.
                ret = ReflectionUtil.instantiateInner(cls, outer);
            } else {
                ret = ReflectionUtil.instantiate(cls);
            }
        } catch (ReflectiveOperationException noDefaultConstructor) {
            ret = ReflectionUtil.instantiateUnsafely(cls);
        }
        return (Serializable) ret;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object unmarshalInit(Serializable target, CompositeReader reader, UnmarshalContext ctx) {
        // read object fields: in exactly the same order as they were written:
        if (!reader.next() || !reader.atElementStart()) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                    "expected: start element, found: " + reader.elementName());
        }
        final Class cls = target.getClass();
        final SerInputStream inStream = SerInputStream.tryCreate(target, reader, ctx, cls);
        do {
            try {
                // check for readObject():
                cachedReadObject(inStream.level)
                        .invoke(target, inStream);
            } catch (NoSuchMethodException readObjectNotFound) {
                defaultUnmarshalObject(target, reader, ctx, inStream.level, inStream);
            } catch (InvocationTargetException readObjectFailure) {
                throw new RuntimeException(readObjectFailure);
            } catch (IllegalAccessException neverThrown) {
                // ignored.
            }
            inStream.level = inStream.level.getSuperclass();
        } while (Serializable.class.isAssignableFrom(inStream.level));
        if (reader.atElementEnd() && reader.elementName().equals(this.name())) {
            // check for readResolve():
            try {
                final Object resolved = cachedReadResolve(cls)
                        .invoke(target);
                if (resolved == null) {
                    return null;
                }
                if (!(resolved instanceof Serializable)) {
                    throw new RuntimeException(new NotSerializableException(resolved.getClass().getName()));
                }
                return resolved;
            } catch (NoSuchMethodException | IllegalAccessException readResolveNotFound) {
                // ignore.
            } catch (InvocationTargetException readResolveFailure) {
                throw new RuntimeException(readResolveFailure);
            }
            return target;
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "missing element end: " + this.name());
    }

    private static void defaultUnmarshalObject(Object instance, CompositeReader reader, UnmarshalContext ctx, Class level, SerInputStream inputStream) {
        if (hasSerializablePersistentFields(level)) {
            inputStream.readFields();
            return;
        }
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
                    ReflectionUtil.setAccessible(f);
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
                }
            } else if (reader.atElementEnd() && reader.elementName().equals(SerializableStrategy.ELEMENT_FIELDS)) {
                reader.next();
                return;
            }
        }// while.
        throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                "missing element end: " + SerializableStrategy.ELEMENT_FIELDS);
    }

    private static boolean hasSerializablePersistentFields(Class cls) {
        try {
            final Field serialPersistentFields = cls.getDeclaredField(FIELD_PERSISTENTFIELDS);
            return (serialPersistentFields.getModifiers() & MODIFIERS_PERSISTENTFIELDS) == MODIFIERS_PERSISTENTFIELDS
                    && serialPersistentFields.getType() == ObjectStreamField[].class;
        } catch (NoSuchFieldException noSerialPersistentFields) {
            return false;
        }
    }

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

    private static final class SerOutputStream extends ObjectOutputStream {

        private final Object target;
        private final Object defs;
        private final CompositeWriter writer;
        private final MarshalContext context;
        private Class level;
        private final Field outerRef;
        private PutFieldImpl lazyPutFieldImpl;

        public static SerOutputStream tryCreate(Object target, Object defs, CompositeWriter writer, MarshalContext ctx, Class level, Field outerRef) {
            try {
                return new SerOutputStream(target, defs, writer, ctx, level, outerRef);
            } catch (IOException ioX) {
                throw new RuntimeException(ioX);
            }
        }

        private SerOutputStream(Object target, Object defs, CompositeWriter writer, MarshalContext ctx, Class level, Field outerRef) throws IOException {
            this.target = target;
            this.defs = defs;
            this.writer = writer;
            this.context = ctx;
            this.level = level;
            this.outerRef = outerRef;
            this.lazyPutFieldImpl = null; // lazy.
        }

        @Override
        public void close() {
            // non-op.
        }

        @Override
        public void flush() {
            // non-op.
        }

        @Override
        public void defaultWriteObject() {
            defaultMarshalObject(this.target, this.defs, this.writer, this.context, this.level, this.outerRef);
        }

        @Override
        public PutField putFields() {
            if (this.lazyPutFieldImpl == null) {
                this.lazyPutFieldImpl = new PutFieldImpl();
            }
            return this.lazyPutFieldImpl;
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void useProtocolVersion(int version) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(int val) {
            this.writer.writeByte((byte) val);
        }

        @Override
        public void write(byte[] buf) {
            this.writer.write(buf);
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            this.writer.write(Arrays.copyOfRange(buf, off, len - off));
        }

        @Override
        public void writeBoolean(boolean val) {
            this.writer.writeBoolean(val);
        }

        @Override
        public void writeByte(int val) {
            this.writer.writeByte((byte) val);
        }

        @Override
        public void writeBytes(String str) {
            this.writer.write(str.getBytes());
        }

        @Override
        public void writeChar(int val) {
            this.writer.writeChar((char) val);
        }

        @Override
        public void writeChars(String str) {
            final char[] chars = new char[str.length()];
            str.getChars(0, chars.length, chars, 0);
            this.writer.write(chars);
        }

        @Override
        public void writeDouble(double val) {
            this.writer.writeDouble(val);
        }

        @Override
        public void writeFields() {
            this.writer.startElement(SerializableStrategy.ELEMENT_FIELDS);
            if (this.lazyPutFieldImpl != null) {
                for (Map.Entry<String, Object> field : this.lazyPutFieldImpl.fields.entrySet()) {
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
        public void writeFloat(float val) {
            this.writer.writeFloat(val);
        }

        @Override
        public void writeInt(int val) {
            this.writer.writeInt(val);
        }

        @Override
        public void writeLong(long val) {
            this.writer.writeLong(val);
        }

        @Override
        protected void writeObjectOverride(Object obj) {
            this.writer.write(obj);
        }

        @Override
        public void writeShort(int val) {
            this.writer.writeShort((short) val);
        }

        @Override
        public void writeUTF(String str) {
            this.writer.write(str);
        }

        @Override
        public void writeUnshared(Object obj) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class PutFieldImpl extends ObjectOutputStream.PutField {

        private final Map<String, Object> fields = new HashMap<>();

        @Override
        public void put(String name, boolean val) {
            this.fields.put(name, val);
        }

        @Override
        public void put(String name, byte val) {
            this.fields.put(name, val);
        }

        @Override
        public void put(String name, char val) {
            this.fields.put(name, val);
        }

        @Override
        public void put(String name, short val) {
            this.fields.put(name, val);
        }

        @Override
        public void put(String name, int val) {
            this.fields.put(name, val);
        }

        @Override
        public void put(String name, long val) {
            this.fields.put(name, val);
        }

        @Override
        public void put(String name, float val) {
            this.fields.put(name, val);
        }

        @Override
        public void put(String name, double val) {
            this.fields.put(name, val);
        }

        @Override
        public void put(String name, Object val) {
            this.fields.put(name, val);
        }

        @Deprecated
        @Override
        public void write(ObjectOutput out) {
            throw new UnsupportedOperationException("Deprecated");
        }
    }

    private static final class SerInputStream extends ObjectInputStream {

        private Object instance;
        private CompositeReader reader;
        private final UnmarshalContext context;
        private Class level;

        public static SerInputStream tryCreate(Object instance, CompositeReader reader, UnmarshalContext ctx, Class level) {
            try {
                return new SerInputStream(instance, reader, ctx, level);
            } catch (IOException ioX) {
                throw new RuntimeException(ioX);
            }
        }

        private SerInputStream(Object instance, CompositeReader reader, UnmarshalContext ctx, Class level) throws IOException {
            this.instance = instance;
            this.reader = reader;
            this.context = ctx;
            this.level = level;
        }

        @Override
        public void registerValidation(ObjectInputValidation obj, int prio) {
        }

        @Override
        public void close() {
            this.reader = null;
            this.instance = null;
            this.level = null;
        }

        @Override
        public void defaultReadObject() {
            defaultUnmarshalObject(this.instance, this.reader, this.context, this.level, this);
        }

        @Override
        public int read() {
            return (int) this.reader.readByte();
        }

        @Override
        public int read(byte[] buf, int off, int len) {
            final byte[] read = (byte[]) this.reader.readArray(byte.class);
            System.arraycopy(read, 0, buf, off, len);
            return read.length;
        }

        @Override
        public boolean readBoolean() {
            return this.reader.readBoolean();
        }

        @Override
        public byte readByte() {
            return this.reader.readByte();
        }

        @Override
        public char readChar() {
            return this.reader.readChar();
        }

        @Override
        public double readDouble() {
            return this.reader.readDouble();
        }

        @Override
        public GetField readFields() {
            if (!this.reader.atElementStart() || !this.reader.elementName().equals(SerializableStrategy.ELEMENT_FIELDS)) {
                throw new InvalidFormatException(this.context.readerPositionDescriptor(),
                        "expected: " + SerializableStrategy.ELEMENT_FIELDS + ", found: " + this.reader.elementName());
            }
            final Map<String, Object> fields = new HashMap<>();
            while (this.reader.next()) {
                if (this.reader.atElementStart()) {
                    final String localPartName = this.reader.elementName();
                    final String nilAttr = this.reader.elementAttribute(ATTRIBUTE_NIL);
                    if (nilAttr != null && Boolean.parseBoolean(nilAttr)) {
                        fields.put(localPartName, null);
                    } else {
                        // check for an alias in case readFields reads XML written by defaultMarshalObject:
                        ValueType keyVT;
                        try {
                            final Field aliasedF = this.context.fieldFor(this.level, localPartName);
                            keyVT = SerializableStrategy.valueTypeFor(aliasedF);
                        } catch (NoSuchFieldException customFieldKey) {
                            // must be a non-source field name.
                            // we will read type from XML.
                            keyVT = null;
                        }
                        if (keyVT != null) {
                            try {
                                fields.put(localPartName, keyVT.parseValue(this.reader.readValue()));
                            } catch (IllegalArgumentException iax) {
                                throw new InvalidFormatException(this.context.readerPositionDescriptor(), iax);
                            }
                        } else { // move down and read object:
                            if (!this.reader.next() || !this.reader.atElementStart()) {
                                throw new InvalidFormatException(this.context.readerPositionDescriptor(), "expected element start");
                            }
                            fields.put(localPartName, this.reader.read());
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
        public float readFloat() {
            return this.reader.readFloat();
        }

        @Override
        public void readFully(byte[] buf) {
            this.read(buf, 0, buf.length);
        }

        @Override
        public void readFully(byte[] buf, int off, int len) {
            this.read(buf, off, len);
        }

        @Override
        public int readInt() {
            return this.reader.readInt();
        }

        @Override
        public long readLong() {
            return this.reader.readLong();
        }

        @Override
        protected Object readObjectOverride() {
            return this.reader.read();
        }

        @Override
        public short readShort() {
            return this.reader.readShort();
        }

        @Override
        public String readUTF() {
            return (String) this.reader.read();
        }

        @Override
        public Object readUnshared() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int readUnsignedByte() {
            return this.reader.readByte();
        }

        @Override
        public int readUnsignedShort() {
            return this.reader.readShort();
        }

        @Override
        public int skipBytes(int len) {
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
        public int read(byte[] b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized void reset() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long skip(long n) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class GetFieldImpl extends ObjectInputStream.GetField {

        private final Map fields;

        public GetFieldImpl(Map fields) {
            this.fields = fields;
        }

        @Override
        public ObjectStreamClass getObjectStreamClass() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean defaulted(String name) {
            return false;
        }

        private <T> T get0(String name, Class<T> c, T defValue) {
            final Object value = this.fields.get(name);
            if (value != null) {
                if (value.getClass() == c) {
                    return (T) value;
                }
                throw new IllegalArgumentException("field name does not map to required type: " + name + ", " + c.getName());
            }
            return defValue;
        }

        @Override
        public boolean get(String name, boolean val) {
            return this.get0(name, Boolean.class, val);
        }

        @Override
        public byte get(String name, byte val) {
            return this.get0(name, Byte.class, val);
        }

        @Override
        public char get(String name, char val) {
            return this.get0(name, Character.class, val);
        }

        @Override
        public short get(String name, short val) {
            return this.get0(name, Short.class, val);
        }

        @Override
        public int get(String name, int val) {
            return this.get0(name, Integer.class, val);
        }

        @Override
        public long get(String name, long val) {
            return this.get0(name, Long.class, val);
        }

        @Override
        public float get(String name, float val) {
            return this.get0(name, Float.class, val);
        }

        @Override
        public double get(String name, double val) {
            return this.get0(name, Double.class, val);
        }

        @Override
        public Object get(String name, Object val) {
            final Object value = this.fields.get(name);
            return value != null ? value : val;
        }
    }
}
