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

import java.util.BitSet;
import net.sourceforge.easyml.marshalling.java.io.SerializableStrategy;
import net.sourceforge.easyml.marshalling.java.lang.ObjectStrategy;
import net.sourceforge.easyml.marshalling.java.lang.ObjectStrategyV1_3_4;
import net.sourceforge.easyml.marshalling.java.util.BitSetStrategy;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author victor
 */
public class BugsTest {

    private EasyML easyml;

    @Before
    public void setup() {
        easyml = new EasyML();
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
    public void testBackwardsCompatibility_v1_3_5_with_v1_3_4() throws Exception {
        final EasyML easyml134 = new EasyML();
        easyml134.writerPrototype.getCompositeStrategies().remove(ObjectStrategy.INSTANCE);
        easyml134.writerPrototype.getCompositeStrategies().add(ObjectStrategyV1_3_4.INSTANCE);

        final Person expected = new Person("fn");

        assertEquals(expected, easyml.deserialize(easyml134.serialize(expected)));
    }

    @Test
    public void testBugObjectX_defValFieldHidesSuperDefValField() throws Exception {
        final NamedPerson expected = new NamedPerson("non-defP", "defNP");

        assertEquals(expected, easyml.deserialize(easyml.serialize(expected)));
    }

    @Test
    public void testBugSerial_PutGetFieldsCustomKeys() throws Exception {
        // we want to test the serialization protocol on the bitset as usecase
        // so we make sure that SerializableStrategy will be used:
        easyml = new EasyMLBuilder()
                //.withStyle(EasyML.Style.PRETTY)
                //.withProfile(EasyML.Profile.GENERIC)
                .withStrategy(SerializableStrategy.INSTANCE)
                .withoutStrategy(BitSetStrategy.INSTANCE)
                .build();

        final BitSet expected = new BitSet();
        expected.set(1);
        expected.set(3);
        expected.set(8);
        expected.set(11);
        expected.set(0);
        expected.set(64);

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
}
