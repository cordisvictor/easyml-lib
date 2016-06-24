package net.sourceforge.easyml.util;

import net.sourceforge.easyml.testmodel.PersonDTO;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author victor
 */
public class ReflectionUtilTest {

    /**
     * Test of qualifiedNameFor method.
     */
    @Test
    public void testQualifiedNameFor_Field() throws Exception {
        final String qn1 = ReflectionUtil.qualifiedNameFor(TestC1.class.getDeclaredField("id"));
        final String qn2 = ReflectionUtil.qualifiedNameFor(TestC2.class.getDeclaredField("id"));
        assertFalse(qn1.equals(qn2));
    }

    /**
     * Test of qualifiedNameFor method.
     */
    @Test
    public void testQualifiedNameFor_Class_String() throws Exception {
        final String qn1 = ReflectionUtil.qualifiedNameFor(TestC1.class, "id");
        final String qn2 = ReflectionUtil.qualifiedNameFor(TestC2.class, "id");
        assertFalse(qn1.equals(qn2));
    }

    /**
     * Test of forName method.
     */
    @Test
    public void testForName() throws Exception {
        assertEquals(Void.TYPE, ReflectionUtil.classForName(Void.TYPE.getName()));
        assertEquals(Integer.TYPE, ReflectionUtil.classForName(Integer.TYPE.getName()));
        assertEquals(String.class, ReflectionUtil.classForName(String.class.getName()));
    }

    /**
     * Test of instantiate method.
     */
    @Test
    public void testInstantiate() throws Exception {
        final Object instantiated = ReflectionUtil.instantiate(PersonDTO.class);
        assertEquals(PersonDTO.class, instantiated.getClass());
        assertEquals(new PersonDTO(), instantiated);
    }

    /**
     * Test of instantiate method.
     */
    @Test
    public void testInstantiateUnsafely() throws Exception {
        final Object instantiated = ReflectionUtil.instantiateUnsafely(NoDefCtorObject.class);
        assertNotNull(instantiated);
        assertEquals(NoDefCtorObject.class, instantiated.getClass());
    }

    /**
     * Test of hasClassFieldProperty method.
     */
    @Test
    public void testHasClassFieldProperty() throws Exception {
        assertTrue(ReflectionUtil.hasClassFieldProperty(TestBean.class, TestBean.class.getDeclaredField("i")));
        assertTrue(ReflectionUtil.hasClassFieldProperty(TestBean.class, TestBean.class.getDeclaredField("data")));
        assertFalse(ReflectionUtil.hasClassFieldProperty(TestBean.class, TestBean.class.getDeclaredField("nonProp")));
    }

    private static class TestC1 {

        private int id;
    }

    private static class TestC2 {

        private int id;
    }

    private static class TestBean {

        private int i;
        private Object[] data;
        private boolean nonProp;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public Object getData(int index) {
            return data[index];
        }
    }

    private static class NoDefCtorObject {

        private int id;

        public NoDefCtorObject(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 41 * hash + this.id;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NoDefCtorObject other = (NoDefCtorObject) obj;
            if (this.id != other.id) {
                return false;
            }
            return true;
        }
    }

}
