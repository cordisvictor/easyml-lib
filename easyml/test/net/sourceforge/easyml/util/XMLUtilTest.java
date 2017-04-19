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
