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

import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Victor Cordis ( cordis.victor at gmail.com)
 */
public class SecurityPolicyTest {

    @Test
    public void testAddContains() {
        final XMLReader r = new XMLReader(System.in);
        final XMLReader.SecurityPolicy sp = r.getSecurityPolicy();
        sp.add(ArrayList.class);
        assertFalse(sp.contains(LinkedList.class));
        assertTrue(sp.contains(ArrayList.class));
    }

    @Test
    public void testAddHierarchyContains() {
        final XMLReader r = new XMLReader(System.in);
        final XMLReader.SecurityPolicy sp = r.getSecurityPolicy();
        sp.addHierarchy(Map.class);
        assertTrue(sp.contains(HashMap.class));
        assertFalse(sp.contains(LinkedList.class));
    }

    @Test
    public void testAddHierarchySquash() {
        final XMLReader r = new XMLReader(System.in);
        final XMLReader.SecurityPolicy sp = r.getSecurityPolicy();
        sp.addHierarchy(List.class);
        sp.addHierarchy(Collection.class);
        final String policy = sp.toString();
        System.out.println(policy);
        assertFalse(policy.contains("List"));
    }

    @Test(expected = IllegalClassException.class)
    public void testAddHierarchyWhitelist() {
        final XMLReader r = new XMLReader(System.in);
        final XMLReader.SecurityPolicy sp = r.getSecurityPolicy();
        sp.setWhitelistMode();
        sp.addHierarchy(Map.class);
        sp.check(ArrayList.class);
    }

    @Test
    public void testAddHierarchyWhitelist1() {
        final XMLReader r = new XMLReader(System.in);
        final XMLReader.SecurityPolicy sp = r.getSecurityPolicy();
        sp.setWhitelistMode();
        sp.addHierarchy(Map.class);
        sp.check(HashMap.class);
    }

    @Test(expected = IllegalClassException.class)
    public void testAddHierarchyBlacklist() {
        final XMLReader r = new XMLReader(System.in);
        final XMLReader.SecurityPolicy sp = r.getSecurityPolicy();
        sp.setBlacklistMode();
        sp.addHierarchy(Map.class);
        sp.check(HashMap.class);
    }

    @Test
    public void testAddHierarchyBlacklist1() {
        final XMLReader r = new XMLReader(System.in);
        final XMLReader.SecurityPolicy sp = r.getSecurityPolicy();
        sp.setBlacklistMode();
        sp.addHierarchy(Map.class);
        sp.check(ArrayList.class);
    }

    @Test
    public void testWhitelist3() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final XMLWriter w = new XMLWriter(out);
        EasyML.defaultConfiguration(w);
        w.write(1);
        w.write(2.1);
        w.write(3);
        w.close();

        final XMLReader r = new XMLReader(new ByteArrayInputStream(out.toByteArray()));
        EasyML.defaultConfiguration(r);
        r.getSecurityPolicy().setWhitelistMode();
        r.getSecurityPolicy().add(Integer.class);
        assertEquals(1, r.read());
        try {
            r.read();
            fail("illegal double not thrown");
        } catch (IllegalClassException expectedEx) {
            assertEquals(3, r.read());
        } finally {
            r.close();
        }
    }

    @Test(expected = IllegalClassException.class)
    public void testWhitelist31() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final XMLWriter w = new XMLWriter(out);
        EasyML.defaultConfiguration(w);
        w.write(1);
        w.write(3);
        w.write(2.1);
        w.close();

        final XMLReader r = new XMLReader(new ByteArrayInputStream(out.toByteArray()));
        EasyML.defaultConfiguration(r);
        r.getSecurityPolicy().setWhitelistMode();
        r.getSecurityPolicy().add(Integer.class);
        assertEquals(1, r.read());
        assertEquals(3, r.read());
        try {
            r.read();
            fail("illegal double not thrown");
        } finally {
            r.close();
        }
    }

    @Test
    public void testWhitelist4() throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document dom = dBuilder.newDocument();

        final XMLWriter w = new XMLWriter(dom);
        EasyML.defaultConfiguration(w);
        w.write(1);
        w.write(2.1);
        w.write(3);
        w.close();

        final XMLReader r = new XMLReader(dom);
        EasyML.defaultConfiguration(r);
        r.getSecurityPolicy().setWhitelistMode();
        r.getSecurityPolicy().add(Integer.class);
        assertEquals(1, r.read());
        try {
            r.read();
            fail("illegal double not thrown");
        } catch (IllegalClassException expectedEx) {
            assertEquals(3, r.read());
        } finally {
            r.close();
        }
    }

    @Test(expected = IllegalClassException.class)
    public void testWhitelist41() throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document dom = dBuilder.newDocument();

        final XMLWriter w = new XMLWriter(dom);
        EasyML.defaultConfiguration(w);
        w.write(1);
        w.write(3);
        w.write(2.1);
        w.close();

        final XMLReader r = new XMLReader(dom);
        EasyML.defaultConfiguration(r);
        r.getSecurityPolicy().setWhitelistMode();
        r.getSecurityPolicy().add(Integer.class);
        assertEquals(1, r.read());
        assertEquals(3, r.read());
        try {
            r.read();
            fail("illegal double not thrown");
        } finally {
            r.close();
        }
    }

    @Test
    public void testBlacklist_consumeFully_text1() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final XMLWriter w = new XMLWriter(out);
        EasyML.defaultConfiguration(w);
        w.write(1);
        w.write(2.1);
        w.write(3);
        w.close();

        final XMLReader r = new XMLReader(new ByteArrayInputStream(out.toByteArray()));
        EasyML.defaultConfiguration(r);
        r.getSecurityPolicy().setBlacklistMode();
        r.getSecurityPolicy().add(Double.class);

        assertEquals(1, r.read());
        try {
            r.read();
            fail("illegal double not thrown");
        } catch (IllegalClassException icx) {
            assertEquals(Double.class, icx.getIllegalClass());
            assertEquals(3, r.read());
        } finally {
            r.close();
        }
    }

    @Test
    public void testBlacklist_consumeFully_text2() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final XMLWriter w = new XMLWriter(out);
        EasyML.defaultConfiguration(w);
        w.writeInt(1);
        w.write(new SecurityPolicyTest());
        w.writeInt(3);
        w.close();

        final XMLReader r = new XMLReader(new ByteArrayInputStream(out.toByteArray()));
        EasyML.defaultConfiguration(r);
        r.getSecurityPolicy().setBlacklistMode();
        r.getSecurityPolicy().add(SecurityPolicyTest.class);

        assertEquals(1, r.readInt());
        try {
            r.read();
            fail("illegal double not thrown");
        } catch (IllegalClassException icx) {
            assertEquals(SecurityPolicyTest.class, icx.getIllegalClass());
            assertEquals(3, r.readInt());
        } finally {
            r.close();
        }
    }
}
