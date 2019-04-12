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

import net.sourceforge.easyml.EasyML;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author victor
 */
public class StringStrategyTest {

    private final EasyML easyml = new EasyML();

    @Test
    public void testMarshalUnmarshal0() {
        final String expected = null;

        final String xml = easyml.serialize(expected);

        assertEquals(expected, easyml.deserialize(xml));
    }

    @Test
    public void testMarshalUnmarshal1() {
        final String expected = "";

        final String xml = easyml.serialize(expected);

        assertEquals(expected, easyml.deserialize(xml));
    }

    @Test
    public void testMarshalUnmarshal2() {
        final String expected = "Hello world!";

        final String xml = easyml.serialize(expected);

        assertEquals(expected, easyml.deserialize(xml));
    }

    @Test
    public void testMarshalUnmarshal3() {
        final String expected = "3 < 4 & 2 == 2";

        final String xml = easyml.serialize(expected);

        assertEquals(expected, easyml.deserialize(xml));
    }
}
