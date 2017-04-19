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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sourceforge.easyml.marshalling.java.io.SerializableStrategy;
import net.sourceforge.easyml.marshalling.java.lang.ObjectStrategy;
import net.sourceforge.easyml.testmodel.DefaultCompositeObject;
import net.sourceforge.easyml.testmodel.FacultyDTO;
import net.sourceforge.easyml.testmodel.PersonDTO;
import net.sourceforge.easyml.testmodel.StudentPersonDTO;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 *
 * @author victor
 */
public class XMLWriterTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @After
    public void afterTest() {
        this.out.reset();
    }

    @Test
    public void testWriteObject() {
        final String fn = "victor";
        final String ln = "cordis";
        final XMLWriter xw = new XMLWriter(this.out);
        xw.write(new PersonDTO(3, fn, ln));
        xw.flush();

        final String xml = new String(this.out.toByteArray());
        assertTrue(xml.contains("3"));
        assertTrue(xml.contains(fn));
        assertTrue(xml.contains(ln));
    }

    @Test
    public void testWriteArray() {
        final XMLWriter xw = new XMLWriter(this.out);
        xw.write(new int[]{0, 1, 2});
        xw.flush();

        final String xml = new String(this.out.toByteArray());
        assertTrue(xml.contains(" length=\"3\""));
        assertTrue(xml.contains("<int>0</int>"));
        assertTrue(xml.contains("<int>1</int>"));
        assertTrue(xml.contains("<int>2</int>"));
    }

    @Test
    public void testWriteMatrix() {
        final Object[][] matrix = new Object[2][];
        matrix[0] = new Integer[]{1, 2, 3};
        matrix[1] = new String[]{"5230", "3<4"};

        final XMLWriter xw = new XMLWriter(this.out);
        xw.write(matrix);
        xw.flush();

        final XMLReader xr = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        final Object[][] read = (Object[][]) xr.readArray(Object[].class);
        assertEquals(matrix.length, read.length);
        assertArrayEquals(matrix[0], read[0]);
        assertArrayEquals(matrix[1], read[1]);
    }

    @Test
    public void testWriteSkipDefaults1() {
        final DefaultCompositeObject dco = new DefaultCompositeObject();

        final XMLWriter xw = new XMLWriter(this.out);
        xw.getCompositeStrategies().add(ObjectStrategy.INSTANCE);
        xw.write(dco);
        xw.flush();

        System.out.println(this.out);

        final XMLReader xr = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xr.getCompositeStrategies().put(ObjectStrategy.NAME, ObjectStrategy.INSTANCE);
        assertEquals(dco, xr.read());
    }

    @Test
    public void testWriteSkipDefaults2() {
        final DefaultCompositeObject dco = new DefaultCompositeObject();
        dco.getDefObject().setName("Changed");

        final XMLWriter xw = new XMLWriter(this.out);
        xw.getCompositeStrategies().add(ObjectStrategy.INSTANCE);
        xw.setPrettyPrint(true);
        xw.write(dco);
        xw.flush();

        System.out.println(this.out);

        final XMLReader xr = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xr.getCompositeStrategies().put(ObjectStrategy.NAME, ObjectStrategy.INSTANCE);
        assertEquals(dco, xr.read());
    }

    @Test
    public void testWriteSkipDefaults3() {
        final DefaultCompositeObject dco = new DefaultCompositeObject();

        final XMLWriter xw = new XMLWriter(this.out);
        xw.getCompositeStrategies().add(ObjectStrategy.INSTANCE);
        xw.write(dco);
        xw.flush();

        System.out.println(this.out);

        final XMLReader xr = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xr.getCompositeStrategies().put(ObjectStrategy.NAME, ObjectStrategy.INSTANCE);
        assertEquals(dco, xr.read());
    }

    @Test
    public void testWriteSkipDefaults4() {
        final DefaultCompositeObject dco = new DefaultCompositeObject();
        dco.getDefObject().setName("Changed");

        final XMLWriter xw = new XMLWriter(this.out);
        xw.getCompositeStrategies().add(SerializableStrategy.INSTANCE);
        xw.setPrettyPrint(true);
        xw.write(dco);
        xw.flush();

        System.out.println(this.out);

        final XMLReader xr = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xr.getCompositeStrategies().put(SerializableStrategy.NAME, SerializableStrategy.INSTANCE);
        assertEquals(dco, xr.read());
    }

    @Test
    public void testWriteObjectGraph0() {
        final FacultyDTO f = new FacultyDTO(-1, "UBB");
        f.setStudents(new StudentPersonDTO[]{new StudentPersonDTO(1, "F", "L", true, f), new StudentPersonDTO()});

        final XMLWriter xw = new XMLWriter(this.out);
        xw.write(f);
        xw.flush();

        System.out.println(this.out);

        final XMLReader xr = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        assertEquals(f, xr.read());
    }

    @Test
    public void testWriteObjectGraph0DOM() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        final FacultyDTO f = new FacultyDTO(-1, "UBB");
        f.setStudents(new StudentPersonDTO[]{new StudentPersonDTO(1, "F", "L", true, f), new StudentPersonDTO()});

        final XMLWriter xw = new XMLWriter(dom);
        xw.write(f);
        xw.flush();

        final XMLReader xr = new XMLReader(dom);
        assertEquals(f, xr.read());
    }

    @Test
    public void testWriteObjectGraph1() {
        final Node n0 = new Node(1, new Node(2, null));
        n0.next.next = n0;

        final XMLWriter xw = new XMLWriter(this.out);
        xw.setPrettyPrint(true);
        try {
            xw.write(n0);
            xw.write(new NoFields());
            xw.flush();

            System.out.println(this.out);

            assertTrue(new String(this.out.toByteArray()).contains("<object idref=\"1\"/>"));
        } catch (StackOverflowError soe) {
            fail("graph cycle detection failed");
        }
    }

    @Test
    public void testWriteObjectGraphDOM() throws Exception {
        final Node n0 = new Node(1, new Node(2, null));
        n0.next.next = n0;

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document dom = dBuilder.newDocument();

        final XMLWriter xw = new XMLWriter(dom);
        xw.setPrettyPrint(true);
        try {
            xw.write(n0);
            xw.flush();

            final XMLReader xr = new XMLReader(dom);
            xr.read();
        } catch (StackOverflowError soe) {
            fail("graph cycle detection failed");
        }
    }

    private static class Node {

        public int id;
        public Node next;

        public Node() {
        }

        public Node(int id, Node next) {
            this.id = id;
            this.next = next;
        }

        public int getId() {
            return id;
        }

        public Node getNext() {
            return next;
        }
    }

    private static class NoFields {
    }
}
