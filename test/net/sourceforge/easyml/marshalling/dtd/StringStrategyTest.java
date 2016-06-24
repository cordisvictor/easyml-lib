package net.sourceforge.easyml.marshalling.dtd;

import net.sourceforge.easyml.EasyML;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author victor
 */
public class StringStrategyTest {

    private final EasyML easyml = new EasyML();

    /**
     * Test of unmarshal method, of class Base64Strategy.
     */
    @Test
    public void testMarshalUnmarshal0() {
        final String expected = null;

        final String xml = easyml.serialize(expected);

        assertEquals(expected, easyml.deserialize(xml));
    }

    /**
     * Test of unmarshal method, of class Base64Strategy.
     */
    @Test
    public void testMarshalUnmarshal1() {
        final String expected = "";

        final String xml = easyml.serialize(expected);

        assertEquals(expected, easyml.deserialize(xml));
    }

    /**
     * Test of unmarshal method, of class Base64Strategy.
     */
    @Test
    public void testMarshalUnmarshal2() {
        final String expected = "Hello world!";

        final String xml = easyml.serialize(expected);

        assertEquals(expected, easyml.deserialize(xml));
    }

    /**
     * Test of unmarshal method, of class Base64Strategy.
     */
    @Test
    public void testMarshalUnmarshal3() {
        final String expected = "3 < 4 & 2 == 2";

        final String xml = easyml.serialize(expected);

        assertEquals(expected, easyml.deserialize(xml));
    }
}
