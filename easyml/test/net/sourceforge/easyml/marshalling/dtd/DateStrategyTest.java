package net.sourceforge.easyml.marshalling.dtd;

import java.util.Date;
import net.sourceforge.easyml.EasyML;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author victor
 */
public class DateStrategyTest {

    private final EasyML easyml = new EasyML();

    /**
     * Test of unmarshal method, of class Base64Strategy.
     */
    @Test
    public void testMarshalUnmarshal() {
        final Date expected = new Date();

        final String xml = easyml.serialize(expected);

        assertEquals(expected, easyml.deserialize(xml));
    }
}
