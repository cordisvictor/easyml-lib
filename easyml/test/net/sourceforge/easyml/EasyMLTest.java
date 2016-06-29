package net.sourceforge.easyml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.easyml.marshalling.dtd.StringStrategy;
import net.sourceforge.easyml.testmodel.AbstractDTO;
import net.sourceforge.easyml.testmodel.PersonDTO;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author victor
 */
public class EasyMLTest {

    private EasyML easyml;

    @Before
    public void setup() {
        easyml = new EasyML();
    }

    @Test
    public void testLookupClass() {
        assertEquals(StringStrategy.INSTANCE, easyml.lookupSimpleStrategyBy(String.class));
    }

    @Test
    public void testLookupName() {
        assertEquals(StringStrategy.INSTANCE, easyml.lookupSimpleStrategyBy("string"));
    }

    @Test
    public void testClearCache() throws Exception {
        easyml.alias(PersonDTO.class, "Person");
        easyml.alias(PersonDTO.class, "lastName", "Person");

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
    public void testAliasClassAndFields() throws Exception {
        easyml.alias(PersonDTO.class, "Person");
        easyml.alias(PersonDTO.class, "lastName", "Person");
        easyml.alias(AbstractDTO.class, "id", "ID");

        final PersonDTO expected = new PersonDTO(1, "fn", "ln");

        assertEquals(expected, easyml.deserialize(easyml.serialize(expected)));
    }

    @Test
    public void testWhitelist1() {
        easyml.deserializationSecurityPolicy().setWhitelistMode();
        easyml.deserializationSecurityPolicy().addHierarchy(List.class);
        easyml.deserializationSecurityPolicy().add(Integer.class);
        easyml.deserialize(easyml.serialize(new ArrayList(Arrays.asList(1, 2, 3))));
    }

    @Test(expected = IllegalClassException.class)
    public void testWhitelist2() {
        easyml.deserializationSecurityPolicy().setWhitelistMode();
        easyml.deserializationSecurityPolicy().addHierarchy(Number[].class);
        easyml.deserializationSecurityPolicy().add(Integer.class);
        easyml.deserialize(easyml.serialize(new Number[]{1, 2.1, 2}));
    }
}
