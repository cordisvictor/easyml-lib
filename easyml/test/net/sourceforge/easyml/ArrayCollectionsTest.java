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

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Test;

import net.sourceforge.easyml.marshalling.java.lang.CharsStrategy;
import net.sourceforge.easyml.marshalling.java.io.SerializableStrategy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sourceforge.easyml.marshalling.java.util.BitSetStrategy;
import net.sourceforge.easyml.marshalling.java.util.SingletonListStrategy;
import net.sourceforge.easyml.marshalling.java.util.SingletonMapStrategy;
import net.sourceforge.easyml.marshalling.java.util.SingletonSetStrategy;
import org.w3c.dom.Document;

/**
 * @author Victor Cordis ( cordis.victor at gmail.com)
 */
public class ArrayCollectionsTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream(256);

    @After
    public void tearDown() {
        this.out.reset();
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
        assertEquals(expected, (BitSet) xis.read());
        xis.close();
    }

    @Test
    public void testGenericStringStringArray() {
        final String[][] expected = new String[][]{{"unu", "doi"}, {"eee", "nuu"}};

        final XMLWriter xos = new XMLWriter(this.out);
//        ExtendedEasyML.Profile.SPECIFIC.configure(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
//        ExtendedEasyML.Profile.SPECIFIC.configure(xis);
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
        assertEquals(expected, (Set<String>) xis.read());
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
        assertEquals(expected, (List<String>) xis.read());
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
        assertEquals(expected, (Map<Integer, String>) xis.read());
        xis.close();
    }

    @Test
    public void testStack() {
        final Stack<String> expected = new Stack();
        expected.push("call1");
        expected.push("call2");
        expected.push("call3");
        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(SerializableStrategy.INSTANCE);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(SerializableStrategy.INSTANCE.name(), SerializableStrategy.INSTANCE);
        assertEquals(expected, (Stack<String>) xis.read());
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
//        final XMLWriter xos = new XMLWriter(this.out);
        final XMLWriter xos = new XMLWriter(dom);
        EasyML.Profile.SPECIFIC.configure(xos);
        xos.write(lists);
        xos.close();
        System.out.println(dom);
//        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        final XMLReader xis = new XMLReader(dom);
        EasyML.Profile.SPECIFIC.configure(xis);
        assertArrayEquals(lists, (Object[]) xis.read());
        xis.close();
    }

    @Test
    public void testListListArray() throws Exception {
        final List expected = new ArrayList();
        expected.add(1);
        List l2 = new LinkedList();
        l2.add(2);
        l2.add(3);
        l2.add(4);
        expected.add(l2);

        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.Profile.SPECIFIC.configure(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.Profile.SPECIFIC.configure(xis);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test(expected = IllegalClassException.class)
    public void testListListArrayInject1() throws Exception {
        final List expected = new ArrayList();
        expected.add(1);
        List l2 = new LinkedList();
        l2.add(2);
        l2.add(3);
        l2.add(4);
        expected.add(l2);

        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.Profile.SPECIFIC.configure(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.Profile.SPECIFIC.configure(xis);
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
        EasyML.Profile.SPECIFIC.configure(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.Profile.SPECIFIC.configure(xis);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testMap() {
        final Map expected = new HashMap();
        expected.put(1, "unu");
        expected.put(2, "doi");
        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.Profile.SPECIFIC.configure(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.Profile.SPECIFIC.configure(xis);
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
        EasyML.Profile.SPECIFIC.configure(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.Profile.SPECIFIC.configure(xis);
        assertEquals(expected.values().iterator().next(), ((Map) xis.read()).values().iterator().next());
        xis.close();
    }

    @Test
    public void testCollectionsEmptyMap() {
        final Map expected = Collections.EMPTY_MAP;
        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.Profile.SPECIFIC.configure(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.Profile.SPECIFIC.configure(xis);
        assertTrue(expected == xis.read());
        xis.close();
    }

    @Test
    public void testCollectionsSingletonMap() {
        final Map expected = Collections.singletonMap(3, "trei");
        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.Profile.SPECIFIC.configure(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.Profile.SPECIFIC.configure(xis);
        assertEquals(expected.toString(), xis.read().toString());
        xis.close();
    }

    @Test(expected = IllegalClassException.class)
    public void testCollectionsSingletonMapInjection1() {
        final Map expected = Collections.singletonMap(3, "trei");
        final XMLWriter xos = new XMLWriter(this.out);
        EasyML.Profile.SPECIFIC.configure(xos);
        xos.write(expected);
        xos.close();
        System.out.println(this.out);
        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        EasyML.Profile.SPECIFIC.configure(xis);
        xis.getSecurityPolicy().addHierarchy(Map.class);
        xis.read();
    }
}
