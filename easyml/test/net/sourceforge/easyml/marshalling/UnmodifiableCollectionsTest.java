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
package net.sourceforge.easyml.marshalling;

import net.sourceforge.easyml.XMLReader;
import net.sourceforge.easyml.XMLWriter;
import net.sourceforge.easyml.marshalling.java.util.CollectionsStrategies;
import net.sourceforge.easyml.marshalling.java.util.ImmutableCollectionsStrategies;
import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Victor Cordis ( cordis.victor at gmail.com)
 */
public class UnmodifiableCollectionsTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream(256);

    @After
    public void tearDown() {
        this.out.reset();
    }

    @Test
    public void testListOf12() {
        final List expected = List.of(1, 2);

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(ImmutableCollectionsStrategies.INSTANCE_LIST12);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(ImmutableCollectionsStrategies.NAME_LIST12, ImmutableCollectionsStrategies.INSTANCE_LIST12);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testListOfN() {
        final List expected = List.of(0, 1, 2, 3, 4);

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(ImmutableCollectionsStrategies.INSTANCE_LISTN);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(ImmutableCollectionsStrategies.NAME_LISTN, ImmutableCollectionsStrategies.INSTANCE_LISTN);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testUnmodifList() {
        final List expected = Collections.unmodifiableList(new LinkedList<>(asList(1, 2, 3)));

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(CollectionsStrategies.INSTANCE_UNMODIFIABLE_LIST);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(CollectionsStrategies.NAME_UNMODIFIABLE_LIST, CollectionsStrategies.INSTANCE_UNMODIFIABLE_LIST);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testUnmodifListRA() {
        final List expected = Collections.unmodifiableList(asList(1, 2, 3));

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(CollectionsStrategies.INSTANCE_UNMODIFIABLE_LIST_RA);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(CollectionsStrategies.NAME_UNMODIFIABLE_LIST_RA, CollectionsStrategies.INSTANCE_UNMODIFIABLE_LIST_RA);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testMapOf12() {
        final Map expected = Map.of(0, "0");

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(ImmutableCollectionsStrategies.INSTANCE_MAP12);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(ImmutableCollectionsStrategies.NAME_MAP12, ImmutableCollectionsStrategies.INSTANCE_MAP12);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testMapOfN() {
        final Map expected = Map.of(0, "0", 1, "1", 2, "2", 3, "3", 4, "4");

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(ImmutableCollectionsStrategies.INSTANCE_MAPN);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(ImmutableCollectionsStrategies.NAME_MAPN, ImmutableCollectionsStrategies.INSTANCE_MAPN);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testUnmodifMap() {
        final Map expected = Collections.unmodifiableMap(Map.of(0, "0", 1, "1"));

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(CollectionsStrategies.INSTANCE_UNMODIFIABLE_MAP);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(CollectionsStrategies.NAME_UNMODIFIABLE_MAP, CollectionsStrategies.INSTANCE_UNMODIFIABLE_MAP);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testSetOf12() {
        final Set expected = Set.of(1, 2);

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(ImmutableCollectionsStrategies.INSTANCE_SET12);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(ImmutableCollectionsStrategies.NAME_SET12, ImmutableCollectionsStrategies.INSTANCE_SET12);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testSetOfN() {
        final Set expected = Set.of(0, 1, 2, 3, 4);

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(ImmutableCollectionsStrategies.INSTANCE_SETN);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(ImmutableCollectionsStrategies.NAME_SETN, ImmutableCollectionsStrategies.INSTANCE_SETN);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testUnmodifSet() {
        final Set expected = Collections.unmodifiableSet(Set.of(0, 1, 2, 3, 4));

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(CollectionsStrategies.INSTANCE_UNMODIFIABLE_SET);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(CollectionsStrategies.NAME_UNMODIFIABLE_SET, CollectionsStrategies.INSTANCE_UNMODIFIABLE_SET);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testUnmodifSeq() {
        final SequencedCollection expected = Collections.unmodifiableSequencedCollection(asList(1, 2, 3));

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(CollectionsStrategies.INSTANCE_UNMODIFIABLE_SEQ);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(CollectionsStrategies.NAME_UNMODIFIABLE_SEQ, CollectionsStrategies.INSTANCE_UNMODIFIABLE_SEQ);
        assertArrayEquals(expected.toArray(), ((SequencedCollection) xis.read()).toArray());
        xis.close();
    }

}
