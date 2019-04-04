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
package net.sourceforge.easyml;

import net.sourceforge.easyml.marshalling.java.io.ExternalizableStrategy;
import net.sourceforge.easyml.marshalling.java.io.SerializableStrategy;
import net.sourceforge.easyml.marshalling.java.lang.ArrayStrategy;
import net.sourceforge.easyml.marshalling.java.lang.ClassStrategy;
import net.sourceforge.easyml.marshalling.java.lang.EnumStrategy;
import org.junit.After;
import org.junit.Test;

import java.io.*;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;

/**
 * @author Victor Cordis ( cordis.victor at gmail.com)
 */
public class ExternalizableSerializableTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream(256);

    @After
    public void tearDown() {
        this.out.reset();
    }

    @Test
    public void testEnumSetStrategy() {
        final EnumSet expected = EnumSet.allOf(EasyML.Style.class);

        final XMLWriter xos = new XMLWriter(this.out);
        xos.alias(EasyML.Style.class, "emlStype");
        xos.getSimpleStrategies().add(EnumStrategy.INSTANCE);
        xos.getSimpleStrategies().add(ClassStrategy.INSTANCE);
        xos.getCompositeStrategies().add(ArrayStrategy.INSTANCE);
        xos.getCompositeStrategies().add(SerializableStrategy.INSTANCE);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.alias(EasyML.Style.class, "emlStype");
        xis.getSimpleStrategies().put(EnumStrategy.INSTANCE.name(), EnumStrategy.INSTANCE);
        xis.getSimpleStrategies().put(ClassStrategy.INSTANCE.name(), ClassStrategy.INSTANCE);
        xis.getCompositeStrategies().put(ArrayStrategy.INSTANCE.name(), ArrayStrategy.INSTANCE);
        xis.getCompositeStrategies().put(SerializableStrategy.INSTANCE.name(), SerializableStrategy.INSTANCE);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testSerialPersistentFields() {
        final Map expected = new ConcurrentHashMap(2);
        expected.put(1, "unu");
        expected.put(2, "doi");
        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(ArrayStrategy.INSTANCE);
        xos.getCompositeStrategies().add(SerializableStrategy.INSTANCE);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(ArrayStrategy.NAME, ArrayStrategy.INSTANCE);
        xis.getCompositeStrategies().put(SerializableStrategy.NAME, SerializableStrategy.INSTANCE);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testExternToSerial() {
        final Duration expected = new Duration(3, 52);

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(ExternalizableStrategy.INSTANCE);
        xos.getCompositeStrategies().add(SerializableStrategy.INSTANCE);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(ExternalizableStrategy.NAME, ExternalizableStrategy.INSTANCE);
        xis.getCompositeStrategies().put(SerializableStrategy.NAME, SerializableStrategy.INSTANCE);
        assertEquals(expected, (Duration) xis.read());
        xis.close();
    }

    // mock Java 8 java.time api:
    private static final class Duration implements Serializable {

        private final long seconds;
        private final int nanos;

        public Duration(long seconds, int nanos) {
            this.seconds = seconds;
            this.nanos = nanos;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + (int) (this.seconds ^ (this.seconds >>> 32));
            hash = 83 * hash + this.nanos;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Duration other = (Duration) obj;
            if (this.seconds != other.seconds) {
                return false;
            }
            if (this.nanos != other.nanos) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Duration{" + "seconds=" + seconds + ", nanos=" + nanos + '}';
        }

        private Object writeReplace() {
            return new Ser(Ser.DURATION_TYPE, this);
        }

        private void readObject(ObjectInputStream s) throws InvalidObjectException {
            throw new InvalidObjectException("Deserialization via serialization delegate");
        }

        void writeExternal(DataOutput out) throws IOException {
            out.writeLong(seconds);
            out.writeInt(nanos);

        }

        static Duration readExternal(DataInput in) throws IOException {
            long seconds = in.readLong();
            int nanos = in.readInt();
            return new Duration(seconds, nanos);

        }
    }

    private static final class Ser implements Externalizable {

        public static final byte DURATION_TYPE = 1;
        private byte type;
        private Object object;

        public Ser() {
        }

        Ser(byte type, Object obj) {
            this.type = type;
            this.object = obj;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeByte(type);
            ((Duration) object).writeExternal(out);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            type = in.readByte();
            object = Duration.readExternal(in);
        }

        private Object readResolve() {
            return object;
        }
    }
}
