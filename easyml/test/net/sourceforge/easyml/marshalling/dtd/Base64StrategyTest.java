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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.JLabel;
import net.sourceforge.easyml.EasyML;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author victor
 */
public class Base64StrategyTest {

    private final EasyML easyml = new EasyML();

    /**
     * Test of marshal method, of class Base64Strategy.d
     */
    @Test
    public void testMarshalUnmarshal() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ObjectOutputStream(out).writeObject(new JLabel("TXT"));

        final String xml = easyml.serialize(out.toByteArray());

        final byte[] base64 = (byte[]) easyml.deserialize(xml);
        assertEquals("TXT", ((JLabel) new ObjectInputStream(new ByteArrayInputStream(base64)).readObject()).getText());
    }
}
