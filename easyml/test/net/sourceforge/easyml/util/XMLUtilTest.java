package net.sourceforge.easyml.util;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author victor
 */
public class XMLUtilTest {

    /**
     * Test of isIllegalXMLText method.
     */
    @Test
    public void testIsIllegalXMLText() {
        assertFalse(XMLUtil.isIllegalXMLText("legal xml text"));
        assertTrue(XMLUtil.isIllegalXMLText("illegal 3 < 4"));
        assertTrue(XMLUtil.isIllegalXMLText("illegal u&m"));
        assertTrue(XMLUtil.isIllegalXMLText("illegal 'text'"));
    }

    /**
     * Test of escapeXML method.
     */
    @Test
    public void testEscapeXML_char() {
        assertEquals("c", XMLUtil.escapeXML('c'));
        assertEquals(XMLUtil.XML_LEGAL_LT, XMLUtil.escapeXML('<'));
        assertEquals(XMLUtil.XML_LEGAL_AMP, XMLUtil.escapeXML('&'));
    }

    /**
     * Test of escapeXML method.
     */
    @Test
    public void testEscapeXML_String() {
        final String expected = "legal text";
        assertSame(expected, XMLUtil.escapeXML(expected));
        assertEquals("3 &lt; 4", XMLUtil.escapeXML("3 < 4"));
    }

    /**
     * Test of checkAlias method.
     */
    @Test
    public void testCheckAlias() {
        try {
            XMLUtil.validateAlias(null);
            fail("checkAlias: did not throw validation exception");
        } catch (IllegalArgumentException illegalAlias) {
        }
        try {
            XMLUtil.validateAlias("");
            fail("checkAlias: did not throw validation exception");
        } catch (IllegalArgumentException illegalAlias) {
        }
        try {
            XMLUtil.validateAlias("d&c");
            fail("checkAlias: did not throw validation exception");
        } catch (IllegalArgumentException illegalAlias) {
        }
        try {
            XMLUtil.validateAlias("my.alias");
        } catch (IllegalArgumentException legalAlias) {
            fail("checkAlias: threw validation exception for valid aliasa");
        }
    }

}
