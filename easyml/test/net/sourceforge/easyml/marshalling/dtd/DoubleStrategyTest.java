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
package net.sourceforge.easyml.marshalling.dtd;

import net.sourceforge.easyml.EasyML;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author victor
 */
public class DoubleStrategyTest {

    private final EasyML easyml = new EasyML();

    /**
     * Test of unmarshal method, of class Base64Strategy.
     */
    @Test
    public void testMarshalUnmarshal() {
        final double expected = -1.2;

        final String xml = easyml.serialize(expected);

        assertEquals(expected, easyml.deserialize(xml));
    }
}
