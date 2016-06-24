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
