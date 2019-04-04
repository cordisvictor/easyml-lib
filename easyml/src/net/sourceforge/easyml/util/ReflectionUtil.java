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

import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ReflectionUtil utility class contains reflection helper methods.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.4.5
 * @since 1.0
 */
public final class ReflectionUtil {

    private static final String PREFIX_IS = "is";
    private static final String PREFIX_GET = "get";
    private static final String PREFIX_SET = "set";
    private static final char SEPARATOR_CLASS_FIELD = '#';

    /**
     * Returns the fully-qualified name of the given field. The return value can
     * then be used as an alias key.
     *
     * @param f to compute the name for
     * @return the aliasing name
     */
    public static String qualifiedNameFor(Field f) {
        return qualifiedNameFor(f.getDeclaringClass(), f.getName());
    }

    /**
     * Returns the fully-qualified name of the given field. The return value can
     * then be used as an alias key.
     *
     * @param declaring the class declaring the field
     * @param field     the field name, within the declaring class
     * @return the aliasing name
     */
    public static String qualifiedNameFor(Class declaring, String field) {
        return declaring.getName() + SEPARATOR_CLASS_FIELD + field;
    }

    /**
     * Returns the field name from a fully-qualified name of the given field.
     *
     * @param fieldFQN the FQN of the field
     * @return the field name
     */
    public static String fieldNameFor(String fieldFQN) {
        return fieldFQN.substring(fieldFQN.indexOf(SEPARATOR_CLASS_FIELD) + 1);
    }

    /**
     * Returns true if the input field is a property, i.e. has a corresponding
     * getter, setter, indexed getter or indexed setter within the given class.
     *
     * @param c class in which to search
     * @param f field which to search
     * @return true if the field is a property, false otherwise
     * @throws SecurityException
     */
    public static boolean hasClassFieldProperty(Class c, Field f) {
        final Class fType = f.getType();
        final String propertyName = ReflectionUtil.propertyNameFor(f);
        // search for getter or setter:
        final Method[] methods = c.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            final int iModifs = methods[i].getModifiers();
            if ((iModifs & Modifier.PUBLIC) == 0 || (iModifs & Modifier.STATIC) != 0) {
                continue; // skip non-public or static.
            }
            final Class iRet = methods[i].getReturnType();
            final String iName = methods[i].getName();
            final Class[] iParams = methods[i].getParameterTypes();
            if (iParams.length == 0 && iRet == fType) {
                if (iRet == boolean.class && iName.equals(PREFIX_IS + propertyName)
                        || iName.equals(PREFIX_GET + propertyName)) {
                    return true;
                }
            } else if (iParams.length == 1) {
                if (iRet != void.class && iParams[0] == int.class && iName.equals(PREFIX_GET + propertyName)) {
                    return true;
                }
                if (iRet == void.class && iParams[0] == fType && iName.equals(PREFIX_SET + propertyName)) {
                    return true;
                }
            } else if (iParams.length == 2) {
                if (iRet == void.class && iParams[0] == int.class && iName.equals(PREFIX_SET + propertyName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String propertyNameFor(Field f) {
        final String fieldName = f.getName();
        final char upperFirst = Character.toUpperCase(fieldName.charAt(0));
        if (fieldName.length() > 1) {
            return upperFirst + fieldName.substring(1);
        }
        return String.valueOf(upperFirst);
    }

    /**
     * Returns the given class' default constructor, if any. If found but not
     * accessible, it will be set to be accessible.
     *
     * @param <T> underlying type for the given class c
     * @param c   the class to reflect the default constructor for
     * @return class' default constructor
     */
    public static <T> Constructor<T> defaultConstructor(Class<T> c)
            throws NoSuchMethodException {
        final Constructor nonArg = c.getDeclaredConstructor();
        if (!nonArg.isAccessible()) {
            nonArg.setAccessible(true);
        }
        return nonArg;
    }

    /**
     * Instantiates the given class using the default constructor. The modifier
     * needs not to be public.
     *
     * @param <T> underlying type for the given class c
     * @param c   the class to instantiate
     * @return a new class instance
     */
    public static <T> T instantiate(Class<T> c)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return ReflectionUtil.defaultConstructor(c).newInstance();
    }

    /**
     * Returns true if the given class is an inner class and so has a synthetic
     * outer-reference field containing the reference to the outer object.
     *
     * @param c to test if has outer reference field
     * @return true if has field, false otherwise
     */
    public static boolean hasOuterRefField(Class c) {
        return ReflectionUtil.outerRefField(c) != null;
    }

    /**
     * If <code>cls</code> is a non-static inner class then the field reflecting
     * the outer class reference is returned, else <code>null</code> is
     * returned. The field accessibility is set to true.
     *
     * @param cls the class to search for outer ref field
     * @return the field or null if cls is not an inner class
     */
    public static Field outerRefField(Class cls) {
        if (cls.isAnonymousClass() || (cls.isMemberClass() && !Modifier.isStatic(cls.getModifiers())) || cls.isLocalClass()) {
            final Field[] declared = cls.getDeclaredFields();
            for (int i = 0; i < declared.length; i++) {
                if (declared[i].isSynthetic() && declared[i].getName().startsWith("this$")) {
                    declared[i].setAccessible(true);
                    return declared[i];
                }
            }
        }
        return null;
    }

    /**
     * Instantiates the given non-static inner class using the default
     * constructor. The modifier needs not to be public.
     *
     * @param c     the class to instantiate
     * @param outer the class outer instance
     * @return a new inner class instance
     */
    public static <T> T instantiateInner(Class<T> c, Object outer)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final Constructor nonArg = c.getDeclaredConstructor(c.getEnclosingClass());
        if (!nonArg.isAccessible()) {
            nonArg.setAccessible(true);
        }
        return (T) nonArg.newInstance(outer);
    }

    /**
     * Instantiates the given class unsafely, bypassing the class-defined constructors.
     *
     * @param <T> the to-instantiate class type
     * @param c   to instantiate
     * @return new instance of c
     */
    public static <T> T instantiateUnsafely(Class<T> c) {
        final int modifiers = c.getModifiers();
        if (Modifier.isInterface(modifiers) || Modifier.isAbstract(modifiers)) {
            throw new IllegalArgumentException("Forbidden to instantiate interface or abstract class: " + c.getName());
        }
        return UnsafeInstantiaton.unsafeInstantiator.instantiate(c);
    }

    /**
     * Returns the class for the given name, like
     * {@linkplain Class#forName(java.lang.String)}, except this works including
     * for the primitive types.
     *
     * @param typeName the type name, for the primitives, or class name
     * @return the found class
     * @throws ClassNotFoundException
     */
    public static Class classForName(String typeName) throws ClassNotFoundException {
        final Class type = Primitives.map.get(typeName);
        return (type != null) ? type : Class.forName(typeName);
    }

    /**
     * UnsafeInstantiator lazy inits the JVM-dependent unsafe instantiator and
     * provides the API for instantiating classes with no default (zero-arg)
     * constructors.
     */
    private interface UnsafeInstantiator {

        <T> T instantiate(Class<T> c);
    }

    private static final class UnsafeInstantiaton {

        private static final UnsafeInstantiator unsafeInstantiator = create();

        private static UnsafeInstantiator create() {
            // if available, use sun.misc.Unsafe:
            try {
                final Class unsafeC = Class.forName("sun.misc.Unsafe");
                final Field theUnsafeF = unsafeC.getDeclaredField("theUnsafe");
                theUnsafeF.setAccessible(true);
                final Object theUnsafe = theUnsafeF.get(null);
                final Method allocateInstanceM = unsafeC.getMethod("allocateInstance", Class.class);
                return new UnsafeInstantiator() {
                    @Override
                    public Object instantiate(Class c) {
                        try {
                            return allocateInstanceM.invoke(theUnsafe, c);
                        } catch (IllegalAccessException | InvocationTargetException neverThrown) {
                            return null;
                        }
                    }
                };
            } catch (ClassNotFoundException | NoSuchFieldException | SecurityException |
                    IllegalArgumentException | IllegalAccessException | NoSuchMethodException sunUnsafeNotAvailable) {
            }
            // if available, use java.io.ObjectInputStream:
            try {
                final Method newInstanceM = ObjectInputStream.class.getDeclaredMethod("newInstance", Class.class, Class.class);
                newInstanceM.setAccessible(true);
                return new UnsafeInstantiator() {
                    @Override
                    public Object instantiate(Class c) {
                        try {
                            return newInstanceM.invoke(null, c, Object.class);
                        } catch (IllegalAccessException | InvocationTargetException neverThrown) {
                            return null;
                        }
                    }
                };
            } catch (NoSuchMethodException | SecurityException notAvailable) {
            }
            // if available, use java.io.ObjectStreamClass:
            try {
                final Method getConstructorIdM = ObjectStreamClass.class.getDeclaredMethod("getConstructorId", Class.class);
                getConstructorIdM.setAccessible(true);
                final int constructorId = (Integer) getConstructorIdM.invoke(null, Object.class);
                final Method newInstanceM = ObjectStreamClass.class.getDeclaredMethod("newInstance", Class.class, int.class);
                newInstanceM.setAccessible(true);
                return new UnsafeInstantiator() {
                    @Override
                    public Object instantiate(Class c) {
                        try {
                            return newInstanceM.invoke(null, c, constructorId);
                        } catch (IllegalAccessException | InvocationTargetException neverThrown) {
                            return null;
                        }
                    }
                };
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                    IllegalArgumentException | InvocationTargetException notAvailable) {
            }
            // else unsafe allocation will not work, if used:
            return new UnsafeInstantiator() {
                @Override
                public <T> T instantiate(Class<T> c) {
                    throw new UnsupportedOperationException("unsafe instantiation not supported on this JVM");
                }
            };
        }

        private UnsafeInstantiaton() {
        }
    }

    /**
     * Primitives class lazy inits the type-name to primitive-class mapping.
     */
    private static final class Primitives {

        private static final Map<String, Class> map = new HashMap<>(9);

        static {
            map.put(Void.TYPE.getName(), Void.TYPE);
            map.put(Boolean.TYPE.getName(), Boolean.TYPE);
            map.put(Byte.TYPE.getName(), Byte.TYPE);
            map.put(Character.TYPE.getName(), Character.TYPE);
            map.put(Short.TYPE.getName(), Short.TYPE);
            map.put(Integer.TYPE.getName(), Integer.TYPE);
            map.put(Long.TYPE.getName(), Long.TYPE);
            map.put(Float.TYPE.getName(), Float.TYPE);
            map.put(Double.TYPE.getName(), Double.TYPE);
        }

        private Primitives() {
        }
    }

    private ReflectionUtil() {
    }
}
