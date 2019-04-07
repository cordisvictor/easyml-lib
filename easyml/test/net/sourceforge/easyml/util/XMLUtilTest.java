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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author victor
 */
public class XMLUtilTest {

    @Test
    public void testIsLegalXMLText() {
        assertTrue(XMLUtil.isLegalXMLText("legal xml text"));
        assertFalse(XMLUtil.isLegalXMLText("illegal 3 < 4"));
        assertFalse(XMLUtil.isLegalXMLText("illegal u&m"));
        assertFalse(XMLUtil.isLegalXMLText("illegal 'text'"));
    }

    @Test
    public void testIsLegalXMLTag() {
        assertTrue(XMLUtil.isLegalXMLTag("easyml"));
        assertTrue(XMLUtil.isLegalXMLTag("easyml-3"));
        assertFalse(XMLUtil.isLegalXMLTag("3easyml"));
        assertFalse(XMLUtil.isLegalXMLTag("_easyml"));
    }

    @Test
    public void testEscapeXML_String_legal() {
        final String expected = "legal text";
        assertSame(expected, XMLUtil.escapeXML(expected));
    }

    @Test
    public void testEscapeXML_String_LT() {
        assertEquals("3 &lt; 4", XMLUtil.escapeXML("3 < 4"));
    }

    @Test
    public void testEscapeXML_String_multiple1() {
        assertEquals("3 &lt; 4 &gt; 2", XMLUtil.escapeXML("3 < 4 > 2"));
    }

    @Test
    public void testEscapeXML_String_multiple2() {
        assertEquals("3 &lt; 4 &amp;&amp; true", XMLUtil.escapeXML("3 < 4 && true"));
    }
}
