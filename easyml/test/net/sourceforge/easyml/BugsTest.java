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

import net.sourceforge.easyml.marshalling.java.io.SerializableStrategy;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Victor Cordis ( cordis.victor at gmail.com)
 */
public class BugsTest {

    private EasyML easyml;

    @Before
    public void setup() {
        easyml = new EasyML();
    }

    @Test
    public void testEnumStrategyUsesNameNotToString() throws Exception {
        easyml = new EasyMLBuilder().build();

        final String xml = easyml.serialize(PolyEnum.VALUE1);
        final Object actual = easyml.deserialize(xml);

        assertEquals(PolyEnum.VALUE1, actual);
    }

    @Test
    public void testClassStrategyUsesAliasing() throws Exception {
        easyml = new EasyMLBuilder()
                .withAlias(Person.class, "Persona")
                .build();

        final String xml = easyml.serialize(Person.class);
        final Object actual = easyml.deserialize(xml);

        assertTrue(!xml.contains(Person.class.getName()));
        assertEquals(Person.class, actual);
    }

    @Test
    public void testBugObjectX_defValFieldHidesSuperDefValField() throws Exception {
        final NamedPerson expected = new NamedPerson("non-defP", "defNP");

        assertEquals(expected, easyml.deserialize(easyml.serialize(expected)));
    }

    @Test
    public void testBugSerial_PutGetFieldsCustomKeys() throws Exception {
        easyml = new EasyMLBuilder()
                //.withStyle(EasyML.Style.PRETTY)
                //.withProfile(EasyML.Profile.GENERIC)
                .withStrategy(new SerializableStrategy())
                .build();

        final ObjectUsingPutGetFields expected = new ObjectUsingPutGetFields("testText");

        String xml = easyml.serialize(expected);
        System.out.println(xml);

        assertEquals(expected, easyml.deserialize(xml));
    }

    private static class Person {

        private final String name;

        public Person() {
            name = "defP";
        }

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
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
            final Person other = (Person) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Person{" + "name=" + name + '}';
        }
    }

    private static final class NamedPerson extends Person {

        private final String name;

        public NamedPerson() {
            name = "defNP";
        }

        public NamedPerson(String superName, String name) {
            super(superName);
            this.name = name;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
            hash = 61 * hash + (this.name != null ? this.name.hashCode() : 0);
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
            final NamedPerson other = (NamedPerson) obj;
            if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
                return false;
            }
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "NamedPerson{superName=" + getName() + "name=" + name + '}';
        }
    }

    private enum PolyEnum {
        VALUE1 {
            @Override
            public String toString() {
                return "val1";
            }
        },
        VALUE2 {
            @Override
            public String toString() {
                return "val2";
            }
        }
    }

    private static final class ObjectUsingPutGetFields implements Serializable {

        private String text;

        public ObjectUsingPutGetFields() {
        }

        public ObjectUsingPutGetFields(String text) {
            this.text = text;
        }

        private void writeObject(ObjectOutputStream s) throws IOException {

            ObjectOutputStream.PutField fields = s.putFields();
            fields.put("data", text);
            s.writeFields();
        }

        private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {

            ObjectInputStream.GetField fields = s.readFields();
            text = (String) fields.get("data", null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ObjectUsingPutGetFields that = (ObjectUsingPutGetFields) o;
            return Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text);
        }

        @Override
        public String toString() {
            return "ObjectUsingPutGetFields{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }
}