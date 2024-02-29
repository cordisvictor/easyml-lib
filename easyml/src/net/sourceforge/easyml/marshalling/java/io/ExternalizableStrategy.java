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

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * ExternalizableStrategy class that implements the
 * {@linkplain CompositeStrategy} interface for Java
 * {@linkplain Externalizable}s. The implementation supports:
 * <ul>
 * <li><code>writeExternal(ObjectOutput)</code></li>
 * <li><code>readExternal(ObjectInput)</code></li>
 * <li><code>writeReplace()</code></li>
 * <li><code>readResolve()</code></li>
 * </ul>
 * <br/>This implementation depends on {@linkplain SerializableStrategy} to also be configured.
 * <br/>Pure Java reflection is used when un-marshalling objects, because the
 * Externalizable interface states that its implementations must define "public
 * no-arg constructors".
 * <br/>
 * This implementation is thread-safe.
 * <br/>
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.6.0
 * @since 1.4.4
 */
public final class ExternalizableStrategy extends AbstractStrategy implements CompositeStrategy<Externalizable> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "extern";
    /**
     * Constant defining the singleton instance.
     */
    public static final ExternalizableStrategy INSTANCE = new ExternalizableStrategy();
    private static final String METHOD_WRITEREPLACE = "writeReplace";
    private static final String METHOD_READRESOLVE = "readResolve";

    private ExternalizableStrategy() {
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
        return Externalizable.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<Externalizable> c) {
        return Externalizable.class.isAssignableFrom(c) && ReflectionUtil.isUnrestrictedOrOpen(c, ExternalizableStrategy.class);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return ExternalizableStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Externalizable target, CompositeWriter writer, MarshalContext ctx) {
        Externalizable theTarget = target;
        // check for writeReplace():
        try {
            final Method writeReplaceM = target.getClass().getDeclaredMethod(METHOD_WRITEREPLACE, ReflectionUtil.METHOD_NO_PARAMS);
            ReflectionUtil.setAccessible(writeReplaceM); // method may be private. Hence must be set accessible true.
            final Object replacement = writeReplaceM.invoke(target);
            if (replacement == null) {
                writer.write(null); // redirect to null.
                return;
            }
            if (!(replacement instanceof Serializable)) {
                throw new RuntimeException(new NotSerializableException(replacement.getClass().getName()));
            }
            if (!(replacement instanceof Externalizable)) {
                writer.write(replacement); // redirect to Serializable.
                return;
            }
            theTarget = (Externalizable) replacement;
        } catch (NoSuchMethodException | IllegalAccessException writeReplaceNotFound) {
            // proceed with standard serialization.
        } catch (InvocationTargetException writeReplaceFailure) {
            throw new RuntimeException(writeReplaceFailure);
        }
        // begin object encoding:
        writer.startElement(this.name());
        writer.setAttribute(DTD.ATTRIBUTE_CLASS, ctx.aliasOrNameFor(theTarget.getClass()));
        // process composition:
        try {
            theTarget.writeExternal(new ExtOutputStream(writer));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        // end object encoding:
        writer.endElement();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Externalizable unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException {
        final String classAttrVal = reader.elementRequiredAttribute(DTD.ATTRIBUTE_CLASS);
        final Class cls = ctx.classFor(classAttrVal);
        if (Externalizable.class.isAssignableFrom(cls)) {
            try {
                return (Externalizable) ReflectionUtil.instantiate(cls);
            } catch (ReflectiveOperationException noDefaultConstructorX) {
                throw new IllegalArgumentException("Externalizable class with invalid default constructor", noDefaultConstructorX);
            }
        }
        throw new IllegalArgumentException("class not externalizable: " + classAttrVal);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object unmarshalInit(Externalizable target, CompositeReader reader, UnmarshalContext ctx) {
        // read object attributes: in exactly the same order as they were written:
        if (!reader.next() || !reader.atElementStart()) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                    "expected: start element, found: " + reader.elementName());
        }
        final Class cls = target.getClass();
        try {
            target.readExternal(new ExtInputStream(reader));
        } catch (ClassNotFoundException | IOException ex) {
            throw new IllegalArgumentException(ex);
        }
        if (reader.atElementEnd() && reader.elementName().equals(this.name())) {
            // check for readResolve():
            try {
                final Method readResolveM = cls.getDeclaredMethod(METHOD_READRESOLVE, ReflectionUtil.METHOD_NO_PARAMS);
                ReflectionUtil.setAccessible(readResolveM); // method may be private. Hence, must be set accessible true.
                final Object resolved = readResolveM.invoke(target);
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

    private static final class ExtOutputStream implements ObjectOutput {

        private final CompositeWriter writer;

        public ExtOutputStream(CompositeWriter writer) {
            this.writer = writer;
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
        public void writeShort(int val) {
            this.writer.writeShort((short) val);
        }

        @Override
        public void writeUTF(String str) {
            this.writer.write(str);
        }

        @Override
        public void writeObject(Object obj) {
            this.writer.write(obj);
        }
    }

    private static final class ExtInputStream implements ObjectInput {

        private final CompositeReader reader;

        public ExtInputStream(CompositeReader reader) {
            this.reader = reader;
        }

        @Override
        public void close() {
            // non-op.
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
        public short readShort() {
            return this.reader.readShort();
        }

        @Override
        public String readUTF() {
            return (String) this.reader.read();
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
        public int read(byte[] b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object readObject() {
            return this.reader.read();
        }

        @Override
        public String readLine() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long skip(long n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int available() {
            throw new UnsupportedOperationException();
        }
    }
}
