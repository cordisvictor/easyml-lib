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

import net.sourceforge.easyml.marshalling.dtd.StringStrategy;
import net.sourceforge.easyml.testmodel.AbstractDTO;
import net.sourceforge.easyml.testmodel.PersonDTO;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Victor Cordis ( cordis.victor at gmail.com)
 */
public class EasyMLTest {

    private EasyML easyml;

    @Test
    public void testLookupClass() {
        easyml = new EasyML();
        assertEquals(StringStrategy.INSTANCE, easyml.lookupSimpleStrategyBy(String.class));
    }

    @Test
    public void testLookupName() {
        easyml = new EasyML();
        assertEquals(StringStrategy.INSTANCE, easyml.lookupSimpleStrategyBy("string"));
    }

    @Test
    public void testClearCache() throws Exception {
        easyml = new EasyMLBuilder()
                .withAlias(PersonDTO.class, "Person")
                .withAlias(PersonDTO.class, "lastName", "Person")
                .build();

        final PersonDTO expected = new PersonDTO(1, "fn", "ln");
        final Object actual = easyml.deserialize(easyml.serialize(expected));

        assertEquals(4, easyml.readerPrototype.cachedAliasingReflection.size());

        easyml.clearCache();

        assertEquals(expected, actual);
        assertEquals(4 - 2/* 2 aliases */, easyml.readerPrototype.cachedAliasingReflection.size());
    }

    @Test
    public void testCustomRootTag() throws Exception {
        easyml = new EasyMLBuilder()
                .withCustomRootTag("thePersons")
                .build();

        final PersonDTO expected = new PersonDTO(1, "fn", "ln");

        final String xml = easyml.serialize(expected);
        assertTrue(xml.startsWith("<thePersons>"));
        assertEquals(expected, easyml.deserialize(xml));
    }

    @Test
    public void testAliasClassAndFields() throws Exception {
        easyml = new EasyMLBuilder()
                .withAlias(PersonDTO.class, "Person")
                .withAlias(PersonDTO.class, "lastName", "Person")
                .withAlias(AbstractDTO.class, "id", "ID")
                .build();

        final PersonDTO expected = new PersonDTO(1, "fn", "ln");

        assertEquals(expected, easyml.deserialize(easyml.serialize(expected)));
    }

    @Test
    public void testWhitelist1() {
        easyml = new EasyMLBuilder()
                .withSecurityPolicy(true, new Class[]{Integer.class}, new Class[]{List.class})
                .build();
        easyml.deserialize(easyml.serialize(new ArrayList(Arrays.asList(1, 2, 3))));
    }

    @Test(expected = IllegalClassException.class)
    public void testWhitelist2() {
        easyml = new EasyMLBuilder()
                .withSecurityPolicy(true, new Class[]{Integer.class}, new Class[]{Number[].class})
                .build();
        easyml.deserialize(easyml.serialize(new Number[]{1, 2.1, 2}));
    }

    @Test(expected = InvalidFormatException.class)
    public void testContinuousIOStreamWriteRead() {
        ByteArrayOutputStream continuousIO = new ByteArrayOutputStream();

        easyml = new EasyMLBuilder()
                .build();
        XMLWriter writer = easyml.newWriter(continuousIO);
        writer.write("unu");
        writer.write("doi");
        writer.write("trei");
        writer.flush();
        writer.writeInt(1);
        writer.writeInt(2);
        writer.writeInt(3);
        writer.close();

        System.out.println(continuousIO);

        XMLReader reader = easyml.newReader(new ByteArrayInputStream(continuousIO.toByteArray()));

        assertEquals("unu", reader.read());
        assertEquals("doi", reader.read());
        assertEquals("trei", reader.read());
        assertEquals(1, reader.readInt());
        assertEquals(2, reader.readInt());
        assertEquals(3, reader.readInt());

        try {
            reader.read();
        } finally {
            reader.close();
        }
    }

    @Test
    public void testPrettyCollectionsArrayList() {
        Object expected = new ArrayList(List.of(1, 2));

        easyml = new EasyMLBuilder()
                .withPrettyCollections(true)
                .build();

        final String xml = easyml.serialize(expected);
        System.out.println(xml);

        assertEquals(expected, easyml.deserialize(xml));
    }

    @Test
    public void testPrettyCollectionsArrayDeque() {
        ArrayDeque expected = new ArrayDeque(List.of(1, 2));

        easyml = new EasyMLBuilder()
                .withPrettyCollections(true)
                .build();

        final String xml = easyml.serialize(expected);
        System.out.println(xml);

        assertArrayEquals(expected.toArray(), ((ArrayDeque) easyml.deserialize(xml)).toArray());
    }

    @Test
    public void testPrettyCollectionsEnumSet() {
        Object expected = EnumSet.allOf(EasyML.Style.class);

        easyml = new EasyMLBuilder()
                .withAlias(EasyML.Style.class, "estyle")
                .withPrettyCollections(true)
                .build();

        final String xml = easyml.serialize(expected);
        System.out.println(xml);

        assertEquals(expected, easyml.deserialize(xml));
    }

    @Test
    public void testPrettyCollectionsEnumMap() {
        Object expected = new EnumMap<>(Map.of(EasyML.Style.PRETTY, "yes"));

        easyml = new EasyMLBuilder()
                .withAlias(EasyML.Style.class, "estyle")
                .withPrettyCollections(true)
                .build();

        final String xml = easyml.serialize(expected);
        System.out.println(xml);

        assertEquals(expected, easyml.deserialize(xml));
    }

    @Test
    public void testPrettyCollectionsTreeMap() {
        TreeMap expected = new TreeMap(new IntegerComparator());
        expected.putAll(Map.of(1, "one", 2, "two"));

        easyml = new EasyMLBuilder()
                .withPrettyCollections(true)
                .build();

        final String xml = easyml.serialize(expected);
        System.out.println(xml);

        assertEquals(expected, easyml.deserialize(xml));
    }

    @Test
    public void testPrettyCollectionsPriorityQueue() {
        PriorityQueue expected = new PriorityQueue(new IntegerComparator());
        expected.addAll(List.of(1, 3, 2));

        easyml = new EasyMLBuilder()
                .withPrettyCollections(true)
                .build();

        final String xml = easyml.serialize(expected);
        System.out.println(xml);

        assertArrayEquals(expected.toArray(), ((PriorityQueue) easyml.deserialize(xml)).toArray());
    }

    private static final class IntegerComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer i1, Integer i2) {
            return i1 - i2;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object o) {
            return o != null && o.getClass() == this.getClass();
        }
    }
}
