package net.sourceforge.easyml.marshalling.dtd;

import net.sourceforge.easyml.EasyML;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author victor
 */
public class IntStrategyTest {

    private final EasyML easyml = new EasyML();

    /**
     * Test of unmarshal method, of class Base64Strategy.
     */
    @Test
    public void testMarshalUnmarshal() {
        final int expected = -1;

        final String xml = easyml.serialize(expected);

        assertEquals(expected, easyml.deserialize(xml));
    }
}
