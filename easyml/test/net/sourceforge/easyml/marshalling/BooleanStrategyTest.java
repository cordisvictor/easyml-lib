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

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

/**
 * @author Victor Cordis ( cordis.victor at gmail.com)
 */
public class BooleanStrategyTest {

    private final EasyML easyml = new EasyML();

    @Test
    public void testMarshalUnmarshal() {
        final String xml = easyml.serialize(true);

        assertEquals(true, easyml.deserialize(xml));
    }

    @Test
    public void testAtomicMarshalUnmarshal() {
        final String xml = easyml.serialize(new AtomicBoolean(true));

        assertEquals(true, ((AtomicBoolean) easyml.deserialize(xml)).get());
    }
}
