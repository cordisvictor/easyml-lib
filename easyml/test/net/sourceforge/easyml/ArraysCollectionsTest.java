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

import net.sourceforge.easyml.marshalling.java.lang.CharsStrategy;
import net.sourceforge.easyml.marshalling.java.util.*;
import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * @author Victor Cordis ( cordis.victor at gmail.com)
 */
public class ArraysCollectionsTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream(256);

    @After
    public void tearDown() {
        this.out.reset();
    }

    @Test
    public void testEnumMapStrategy() {
        final EnumMap expected = new EnumMap(EasyML.Style.class);
        expected.put(EasyML.Style.PRETTY, 1);
        expected.put(EasyML.Style.DETAILED, 2);

        final XMLWriter xos = new XMLWriter(this.out);
        xos.alias(EasyML.Style.class, "emlStype");
        xos.getCompositeStrategies().add(EnumMapStrategy.INSTANCE);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.alias(EasyML.Style.class, "emlStype");
        xis.getCompositeStrategies().put(EnumMapStrategy.INSTANCE.name(), EnumMapStrategy.INSTANCE);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testEnumSetStrategy() {
        final EnumSet expected = EnumSet.allOf(EasyML.Style.class);

        final XMLWriter xos = new XMLWriter(this.out);
        xos.alias(EasyML.Style.class, "emlStype");
        xos.getCompositeStrategies().add(EnumSetStrategy.INSTANCE);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.alias(EasyML.Style.class, "emlStype");
        xis.getCompositeStrategies().put(EnumSetStrategy.INSTANCE.name(), EnumSetStrategy.INSTANCE);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testBitSetStrategy() {
        final BitSet expected = new BitSet();
        expected.set(1);
        expected.set(3);
        expected.set(64);

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(BitSetStrategy.INSTANCE);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(BitSetStrategy.INSTANCE.name(), BitSetStrategy.INSTANCE);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testGenericStringStringArray() {
        final String[][] expected = new String[][]{{"unu", "doi"}, {"eee", "nuu"}};

        final XMLWriter xos = new XMLWriter(this.out);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        final String[][] actual = (String[][]) xis.readArray(String[].class);
        xis.close();

        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
    }

    @Test
    public void testSingletonSet() {
        final Set<String> expected = Collections.singleton("single");
        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(SingletonSetStrategy.INSTANCE);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(SingletonSetStrategy.INSTANCE.name(), SingletonSetStrategy.INSTANCE);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testSingletonList() {
        final List<String> expected = Collections.singletonList("single");
        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(SingletonListStrategy.INSTANCE);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(SingletonListStrategy.INSTANCE.name(), SingletonListStrategy.INSTANCE);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testSingletonMap() {
        final Map<Integer, String> expected = Collections.singletonMap(1, "one");
        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(SingletonMapStrategy.INSTANCE);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(SingletonMapStrategy.INSTANCE.name(), SingletonMapStrategy.INSTANCE);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testCharArray() {
        final char[] expected = new char[]{'a', '&', 'c'};
        final XMLWriter xos = new XMLWriter(this.out);
        xos.getSimpleStrategies().add(CharsStrategy.INSTANCE);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getSimpleStrategies().put(CharsStrategy.INSTANCE.name(), CharsStrategy.INSTANCE);
        assertArrayEquals(expected, (char[]) xis.read());
        xis.close();
    }

    @Test
    public void testArrayListArray() throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document dom = dBuilder.newDocument();

        final List l0 = new ArrayList(20);
        l0.add(1);
        l0.add(2);
        final List l1 = new LinkedList();
        l1.add("one");
        l1.add("two");
        final List[] lists = new List[]{l0, l1};
        final XMLWriter xos = new XMLWriter(dom);
        EasyML.defaultConfiguration(xos);
        xos.write(lists);
        xos.close();
        System.out.println(dom);
        final XMLReader xis = new XMLReader(dom);
        EasyML.defaultConfiguration(xis);
        assertArrayEquals(lists, (Object[]) xis.read());
        xis.close();
    }

    @Test
    public void testArrayDeque() {
        final ArrayDeque expected = new ArrayDeque();
        expected.add(1);
        expected.add(2);
        expected.add(3);

        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.defaultConfiguration(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.defaultConfiguration(xis);
        assertArrayEquals(expected.toArray(), ((ArrayDeque) xis.read()).toArray());
        xis.close();
    }

    @Test
    public void testPriorityQueue() {
        final PriorityQueue expected = new PriorityQueue();
        expected.add(1);
        expected.add(2);
        expected.add(3);

        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.defaultConfiguration(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.defaultConfiguration(xis);
        assertArrayEquals(expected.toArray(), ((PriorityQueue) xis.read()).toArray());
        xis.close();
    }

    @Test
    public void testListListArray() {
        final List expected = new ArrayList();
        expected.add(1);
        List l2 = new LinkedList();
        l2.add(2);
        l2.add(3);
        l2.add(4);
        expected.add(l2);

        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.defaultConfiguration(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.defaultConfiguration(xis);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test(expected = IllegalClassException.class)
    public void testListListArrayInject1() {
        final List expected = new ArrayList();
        expected.add(1);
        List l2 = new LinkedList();
        l2.add(2);
        l2.add(3);
        l2.add(4);
        expected.add(l2);

        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.defaultConfiguration(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.defaultConfiguration(xis);
        xis.getSecurityPolicy().add(Integer.class);
        xis.read();
    }

    @Test
    public void testLinkedListCollection() {
        final Collection expected = new ArrayList(20);
        final List l = new LinkedList();
        l.add("one");
        l.add("two");
        expected.add(l);
        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.defaultConfiguration(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.defaultConfiguration(xis);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testTreeSet() {
        final Set<Integer> expected = new TreeSet<>(new IntegerComparator());
        expected.add(1);
        expected.add(2);

        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.defaultConfiguration(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.defaultConfiguration(xis);
        assertEquals(expected, xis.read());
        xis.close();
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

    @Test
    public void testMap() {
        final Map expected = new HashMap();
        expected.put(1, "unu");
        expected.put(2, "doi");
        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.defaultConfiguration(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.defaultConfiguration(xis);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testConcurrentMap() {
        final Map expected = new ConcurrentHashMap(2);
        expected.put(1, "unu");
        expected.put(2, "doi");
        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.defaultConfiguration(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.defaultConfiguration(xis);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testIdentityHashMapMap() {
        final Map expected = new IdentityHashMap();
        final Map m = new HashMap();
        m.put(1, "unu");
        m.put(2, "doi");
        expected.put(m, m);
        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.defaultConfiguration(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.defaultConfiguration(xis);
        assertEquals(expected.values().iterator().next(), ((Map) xis.read()).values().iterator().next());
        xis.close();
    }

    @Test
    public void testCollectionsEmptyMap() {
        final Map expected = Collections.EMPTY_MAP;
        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.defaultConfiguration(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.defaultConfiguration(xis);
        assertTrue(expected == xis.read());
        xis.close();
    }

    @Test
    public void testCollectionsSingletonMap() {
        final Map expected = Collections.singletonMap(3, "trei");
        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.defaultConfiguration(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.defaultConfiguration(xis);
        assertEquals(expected.toString(), xis.read().toString());
        xis.close();
    }

    @Test(expected = IllegalClassException.class)
    public void testCollectionsSingletonMapInjection1() {
        final Map expected = Collections.singletonMap(3, "trei");
        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.defaultConfiguration(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.defaultConfiguration(xis);
        xis.getSecurityPolicy().addHierarchy(Map.class);
        xis.read();
    }
}
