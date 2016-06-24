package net.sourceforge.easyml.marshalling.dtd;

import net.sourceforge.easyml.EasyML;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author victor
 */
public class BooleanStrategyTest {

    private final EasyML easyml = new EasyML();

    /**
     * Test of unmarshal method, of class boolean.
     */
    @Test
    public void testMarshalUnmarshal() {
        final String xml = easyml.serialize(true);

        assertEquals(true, easyml.deserialize(xml));
    }
}
