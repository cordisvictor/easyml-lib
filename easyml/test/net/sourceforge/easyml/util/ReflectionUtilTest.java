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
package net.sourceforge.easyml.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author victor
 */
public class ReflectionUtilTest {

    @Test
    public void testForName() throws Exception {
        assertEquals(Void.TYPE, ReflectionUtil.classForName(Void.TYPE.getName()));
        assertEquals(Integer.TYPE, ReflectionUtil.classForName(Integer.TYPE.getName()));
        assertEquals(String.class, ReflectionUtil.classForName(String.class.getName()));
    }

    @Test
    public void testInstantiateUnsafely() throws Exception {
        final Object instantiated = ReflectionUtil.instantiateUnsafely(NoDefCtorObject.class);
        assertNotNull(instantiated);
        assertEquals(NoDefCtorObject.class, instantiated.getClass());
    }

    @Test
    public void testIsFieldProperty() throws Exception {
        assertTrue(ReflectionUtil.isFieldProperty(TestBean.class.getDeclaredField("i")));
        assertTrue(ReflectionUtil.isFieldProperty(TestBean.class.getDeclaredField("data")));
        assertFalse(ReflectionUtil.isFieldProperty(TestBean.class.getDeclaredField("nonProp")));
    }

    private static class TestC1 {

        private int id;
    }

    private static class TestC2 {

        private int id;
    }

    public static class TestBean {

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
