package net.sourceforge.easyml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 *
 * @author victor
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
        EasyML.Profile.SPECIFIC.configure(w);
        w.write(1);
        w.write(2.1);
        w.write(3);
        w.close();

        final XMLReader r = new XMLReader(new ByteArrayInputStream(out.toByteArray()));
        EasyML.Profile.SPECIFIC.configure(r);
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
        EasyML.Profile.SPECIFIC.configure(w);
        w.write(1);
        w.write(3);
        w.write(2.1);
        w.close();

        final XMLReader r = new XMLReader(new ByteArrayInputStream(out.toByteArray()));
        EasyML.Profile.SPECIFIC.configure(r);
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
        EasyML.Profile.SPECIFIC.configure(w);
        w.write(1);
        w.write(2.1);
        w.write(3);
        w.close();

        final XMLReader r = new XMLReader(dom);
        EasyML.Profile.SPECIFIC.configure(r);
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
        EasyML.Profile.SPECIFIC.configure(w);
        w.write(1);
        w.write(3);
        w.write(2.1);
        w.close();

        final XMLReader r = new XMLReader(dom);
        EasyML.Profile.SPECIFIC.configure(r);
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
        EasyML.Profile.SPECIFIC.configure(w);
        w.write(1);
        w.write(2.1);
        w.write(3);
        w.close();

        final XMLReader r = new XMLReader(new ByteArrayInputStream(out.toByteArray()));
        EasyML.Profile.SPECIFIC.configure(r);
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
        EasyML.Profile.SPECIFIC.configure(w);
        w.writeInt(1);
        w.write(new SecurityPolicyTest());
        w.writeInt(3);
        w.close();

        final XMLReader r = new XMLReader(new ByteArrayInputStream(out.toByteArray()));
        EasyML.Profile.SPECIFIC.configure(r);
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
