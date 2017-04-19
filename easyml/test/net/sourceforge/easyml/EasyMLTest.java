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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.easyml.marshalling.dtd.StringStrategy;
import net.sourceforge.easyml.testmodel.AbstractDTO;
import net.sourceforge.easyml.testmodel.PersonDTO;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author victor
 */
public class EasyMLTest {

    private EasyML easyml;

    @Test
    public void testLookupClass() {
        easyml = new EasyML();
        assertEquals(StringStrategy.INSTANCE, easyml.lookupSimpleStrategyBy(String.class));
    }

    @Test
    public void testLookupName() {
        easyml = new EasyML();
        assertEquals(StringStrategy.INSTANCE, easyml.lookupSimpleStrategyBy("string"));
    }

    @Test
    public void testClearCache() throws Exception {
        easyml = new EasyMLBuilder()
                .withAlias(PersonDTO.class, "Person")
                .withAlias(PersonDTO.class, "lastName", "Person")
                .build();

        final PersonDTO expected = new PersonDTO(1, "fn", "ln");
        final Object actual = easyml.deserialize(easyml.serialize(expected));

        assertEquals(4, easyml.readerPrototype.cachedAliasingReflection.size());
        assertEquals(1, easyml.readerPrototype.cachedDefCtors.size());
        assertEquals(1, easyml.writerPrototype.cachedDefCtors.size());
        assertSame(easyml.readerPrototype.cachedDefCtors, easyml.writerPrototype.cachedDefCtors);

        easyml.clearCache();

        assertEquals(expected, actual);
        assertEquals(4 - 2/* 2 aliases */, easyml.readerPrototype.cachedAliasingReflection.size());
        assertTrue(easyml.readerPrototype.cachedDefCtors.isEmpty());
        assertTrue(easyml.writerPrototype.cachedDefCtors.isEmpty());
    }

    @Test
    public void testCustomRootTag() throws Exception {
        easyml = new EasyMLBuilder()
                .withCustomRootTag("thePersons")
                .build();

        final PersonDTO expected = new PersonDTO(1, "fn", "ln");

        final String xml = easyml.serialize(expected);
        assertTrue(xml.startsWith("<thePersons>"));
        assertEquals(expected, easyml.deserialize(xml));
    }

    @Test
    public void testAliasClassAndFields() throws Exception {
        easyml = new EasyMLBuilder()
                .withAlias(PersonDTO.class, "Person")
                .withAlias(PersonDTO.class, "lastName", "Person")
                .withAlias(AbstractDTO.class, "id", "ID")
                .build();

        final PersonDTO expected = new PersonDTO(1, "fn", "ln");

        assertEquals(expected, easyml.deserialize(easyml.serialize(expected)));
    }

    @Test
    public void testWhitelist1() {
        easyml = new EasyMLBuilder()
                .withSecurityPolicy(true, new Class[]{Integer.class}, new Class[]{List.class})
                .build();
        easyml.deserialize(easyml.serialize(new ArrayList(Arrays.asList(1, 2, 3))));
    }

    @Test(expected = IllegalClassException.class)
    public void testWhitelist2() {
        easyml = new EasyMLBuilder()
                .withSecurityPolicy(true, new Class[]{Integer.class}, new Class[]{Number[].class})
                .build();
        easyml.deserialize(easyml.serialize(new Number[]{1, 2.1, 2}));
    }
}
