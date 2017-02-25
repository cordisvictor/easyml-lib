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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import net.sourceforge.easyml.DTD;
import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.CompositeAttributeWriter;
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.CompositeStrategy;
import net.sourceforge.easyml.marshalling.CompositeWriter;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

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
 * <br/>Pure Java reflection is used when un-marshalling objects, because the
 * Externalizable interface states that it's implementations must define "public
 * no-arg constructors".
 * <br/>
 * This implementation is thread-safe.
 * <br/>
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.4.3
 * @since 1.4.4
 */
public class ExternalizableStrategy extends AbstractStrategy<Externalizable> implements CompositeStrategy<Externalizable> {

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

    protected ExternalizableStrategy() {
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
        return Externalizable.class.isAssignableFrom(c);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return ExternalizableStrategy.NAME;
    }

    /**
     * Marshalling writing root attributes stage. Writes the <code>class</code>
     * attribute.
     *
     * @param target target to extract attribute values from
     * @param writer to write attributes with
     * @param ctx the context
     */
    protected void marshalDoAttributes(Externalizable target, CompositeAttributeWriter writer, MarshalContext ctx) {
        final Class c = target.getClass();
        writer.setAttribute(DTD.ATTRIBUTE_CLASS, ctx.aliasFor(c, c.getName()));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(Externalizable target, CompositeWriter writer, MarshalContext ctx) {
        Externalizable theTarget = target;
        // check for writeReplace():
        try {
            final Method writeReplaceM = target.getClass().getDeclaredMethod(METHOD_WRITEREPLACE);
            writeReplaceM.setAccessible(true); // method may be private. Hence must be set accessible true.
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
        this.marshalDoAttributes(theTarget, writer, ctx);
        // process composition:
        try {
            theTarget.writeExternal(new EOutputStream(writer));
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
    public Externalizable unmarshalNew(CompositeReader reader, UnmarshalContext ctx)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final String classAttrVal = reader.elementRequiredAttribute(DTD.ATTRIBUTE_CLASS);
        final Class cls = ctx.classFor(classAttrVal);
        if (Externalizable.class.isAssignableFrom(cls)) {
            try {
                return (Externalizable) ctx.defaultConstructorFor(cls).newInstance();
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
    public Object unmarshalInit(Externalizable target, CompositeReader reader, UnmarshalContext ctx)
            throws IllegalAccessException {
        // read object attributes: in exactly the same order as they were written:
        if (!reader.next() || !reader.atElementStart()) {
            throw new InvalidFormatException(ctx.readerPositionDescriptor(),
                    "expected: start element, found: " + reader.elementName());
        }
        final Class cls = target.getClass();
        try {
            target.readExternal(new EInputStream(reader));
        } catch (ClassNotFoundException | IOException ex) {
            throw new IllegalArgumentException(ex);
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
            } catch (NoSuchMethodException | IllegalAccessException readResolveNotFound) {
                // ignore.
            } catch (InvocationTargetException readResolveFailure) {
                throw new RuntimeException(readResolveFailure);
            }
            return target;
        }
        throw new InvalidFormatException(ctx.readerPositionDescriptor(), "missing element end: " + this.name());
    }

    private final class EOutputStream implements ObjectOutput {

        private final CompositeWriter writer;

        public EOutputStream(CompositeWriter writer) {
            this.writer = writer;
        }

        @Override
        public void close() throws IOException {
            // non-op.
        }

        @Override
        public void flush() throws IOException {
            // non-op.
        }

        @Override
        public void write(int val) throws IOException {
            this.writer.writeByte((byte) val);
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
            this.writer.writeBoolean(val);
        }

        @Override
        public void writeByte(int val) throws IOException {
            this.writer.writeByte((byte) val);
        }

        @Override
        public void writeBytes(String str) throws IOException {
            this.writer.write(str.getBytes());
        }

        @Override
        public void writeChar(int val) throws IOException {
            this.writer.writeChar((char) val);
        }

        @Override
        public void writeChars(String str) throws IOException {
            final char[] chars = new char[str.length()];
            str.getChars(0, chars.length, chars, 0);
            this.writer.write(chars);
        }

        @Override
        public void writeDouble(double val) throws IOException {
            this.writer.writeDouble(val);
        }

        @Override
        public void writeFloat(float val) throws IOException {
            this.writer.writeFloat(val);
        }

        @Override
        public void writeInt(int val) throws IOException {
            this.writer.writeInt(val);
        }

        @Override
        public void writeLong(long val) throws IOException {
            this.writer.writeLong(val);
        }

        @Override
        public void writeShort(int val) throws IOException {
            this.writer.writeShort((short) val);
        }

        @Override
        public void writeUTF(String str) throws IOException {
            this.writer.write(str);
        }

        @Override
        public void writeObject(Object obj) throws IOException {
            this.writer.write(obj);
        }
    }//(+)class EOutputStream.

    private final class EInputStream implements ObjectInput {

        private final CompositeReader reader;

        public EInputStream(CompositeReader reader) {
            this.reader = reader;
        }

        @Override
        public void close() throws IOException {
            // non-op.
        }

        @Override
        public int read() throws IOException {
            return (int) this.reader.readByte();
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            final byte[] read = (byte[]) this.reader.readArray(byte.class);
            System.arraycopy(read, 0, buf, off, len);
            return read.length;
        }

        @Override
        public boolean readBoolean() throws IOException {
            return this.reader.readBoolean();
        }

        @Override
        public byte readByte() throws IOException {
            return this.reader.readByte();
        }

        @Override
        public char readChar() throws IOException {
            return this.reader.readChar();
        }

        @Override
        public double readDouble() throws IOException {
            return this.reader.readDouble();
        }

        @Override
        public float readFloat() throws IOException {
            return this.reader.readFloat();
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
            return this.reader.readInt();
        }

        @Override
        public long readLong() throws IOException {
            return this.reader.readLong();
        }

        @Override
        public short readShort() throws IOException {
            return this.reader.readShort();
        }

        @Override
        public String readUTF() throws IOException {
            return (String) this.reader.read();
        }

        @Override
        public int readUnsignedByte() throws IOException {
            return this.reader.readByte();
        }

        @Override
        public int readUnsignedShort() throws IOException {
            return this.reader.readShort();
        }

        @Override
        public int skipBytes(int len) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read(byte[] b) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object readObject() throws ClassNotFoundException, IOException {
            return this.reader.read();
        }

        @Override
        public String readLine() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public long skip(long n) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int available() throws IOException {
            throw new UnsupportedOperationException();
        }
    }//(+)class EInputStream.
}//class ExternalizableStrategy.
