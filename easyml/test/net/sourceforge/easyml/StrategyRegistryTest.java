package net.sourceforge.easyml;

import java.io.Serializable;
import javax.swing.JComponent;
import javax.swing.JLabel;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.CompositeStrategy;
import net.sourceforge.easyml.marshalling.CompositeWriter;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.UnmarshalContext;
import net.sourceforge.easyml.marshalling.dtd.IntStrategy;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author victor
 */
public class StrategyRegistryTest {

    @Test
    public void testAddContains() {
        final XMLWriter w = new XMLWriter(System.out);
        w.getSimpleStrategies().add(IntStrategy.INSTANCE);
        assertTrue(w.getSimpleStrategies().contains(IntStrategy.INSTANCE));
    }

    @Test
    public void testSimpleLookup() {
        final XMLWriter xw = new XMLWriter(System.out);
        assertEquals(IntStrategy.INSTANCE, xw.getSimpleStrategies().lookup(Integer.class));
    }

    @Test
    public void testRemoveContains() {
        final XMLWriter xw = new XMLWriter(System.out);
        xw.getSimpleStrategies().remove(IntStrategy.INSTANCE);
        assertTrue(xw.getSimpleStrategies().lookup(Integer.class) == null);
    }

    @Test
    public void testLookup() {
        final CompositeStrategy serial = new SerlialStrategy();
        final CompositeStrategy jcomp = new JCompStrategy();

        final XMLWriter xw = new XMLWriter(System.out);
        xw.getCompositeStrategies().add(serial);
        xw.getCompositeStrategies().add(jcomp);

        assertEquals(jcomp, xw.getCompositeStrategies().lookup(JLabel.class));
    }

    @Test
    public void testPriorize() {
        final CompositeStrategy serial = new SerlialStrategy();
        final CompositeStrategy jcomp = new JCompStrategy();

        final XMLWriter xw = new XMLWriter(System.out);
        xw.getCompositeStrategies().add(serial);
        xw.getCompositeStrategies().add(jcomp);
        xw.getCompositeStrategies().prioritize(serial, jcomp);

        assertEquals(serial, xw.getCompositeStrategies().lookup(JLabel.class));
    }

    private static class SerlialStrategy extends AbstractStrategy<Serializable> implements CompositeStrategy<Serializable> {

        @Override
        public boolean strict() {
            return false;
        }

        @Override
        public Class target() {
            return Serializable.class;
        }

        @Override
        public boolean appliesTo(Class<Serializable> c) {
            return Serializable.class.isAssignableFrom(c);
        }

        @Override
        public String name() {
            return "serial";
        }

        @Override
        public void marshal(Serializable target, CompositeWriter writer, MarshalContext ctx) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Serializable unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Serializable unmarshalInit(Serializable target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class JCompStrategy extends AbstractStrategy<JComponent> implements CompositeStrategy<JComponent> {

        @Override
        public boolean strict() {
            return false;
        }

        @Override
        public Class target() {
            return JComponent.class;
        }

        @Override
        public boolean appliesTo(Class<JComponent> c) {
            return JComponent.class.isAssignableFrom(c);
        }

        @Override
        public String name() {
            return "jcomp";
        }

        @Override
        public void marshal(JComponent target, CompositeWriter writer, MarshalContext ctx) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public JComponent unmarshalNew(CompositeReader reader, UnmarshalContext ctx) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public JComponent unmarshalInit(JComponent target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
