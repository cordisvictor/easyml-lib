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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * ReflectionUtil utility class contains reflection helper methods.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.0
 * @since 1.0
 */
public final class ReflectionUtil {

    /**
     * Constant used to cache the zero-arg signature for reflecting methods.
     */
    public static final Class[] METHOD_NO_PARAMS = new Class[]{};
    private static final String PREFIX_IS = "is";
    private static final String PREFIX_GET = "get";
    private static final String PREFIX_SET = "set";

    /**
     * Returns true if the input field is a property, i.e. has a corresponding
     * getter, setter, indexed getter or indexed setter.
     *
     * @param f field to test if is property
     * @return true if the field is a property, false otherwise
     */
    public static boolean isFieldProperty(Field f) {
        if (Modifier.isStatic(f.getModifiers())) {
            return false;
        }
        final Class fDeclaring = f.getDeclaringClass();
        final Class fType = f.getType();
        final String propertyName = propertyNameFor(f);
        // search for getter or setter:
        final Method[] methods = fDeclaring.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            final int iMod = methods[i].getModifiers();
            if ((iMod & Modifier.PUBLIC) == 0 || (iMod & Modifier.STATIC) != 0) {
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
        if (fieldName.length() == 1) {
            return String.valueOf(upperFirst);
        }
        return upperFirst + fieldName.substring(1);
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
     * Returns true if the given class is an inner class and so has a synthetic
     * outer-reference field containing the reference to the outer object.
     *
     * @param c to test if has outer reference field
     * @return true if has field, false otherwise
     */
    public static boolean isInnerClass(Class c) {
        return c.isAnonymousClass() || (c.isMemberClass() && !Modifier.isStatic(c.getModifiers())) || c.isLocalClass();
    }

    /**
     * Returns the field reflecting the reference to the outer class of <code>c</code>.
     *
     * @param c the inner class to search for outer ref field
     * @return the outer class reference field or null if c isn't an inner class
     */
    public static Field outerRefField(Class c) {
        if (isInnerClass(c)) {
            final Field[] declared = c.getDeclaredFields();
            for (int i = 0; i < declared.length; i++) {
                final Field f = declared[i];
                if (f.isSynthetic() && f.getName().startsWith("this$")) {
                    setAccessible(f);
                    return f;
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
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final Constructor nonArg = c.getDeclaredConstructor(c.getEnclosingClass());
        setAccessible(nonArg);
        return (T) nonArg.newInstance(outer);
    }

    /**
     * Instantiates the given class unsafely, bypassing the class-defined constructors.
     *
     * @param c to instantiate
     * @return new instance of c
     */
    public static Object instantiateUnsafely(Class c) {
        final int modifiers = c.getModifiers();
        if (Modifier.isInterface(modifiers) || Modifier.isAbstract(modifiers)) {
            throw new IllegalArgumentException("Forbidden to instantiate interface or abstract class: " + c.getName());
        }
        return UnsafeInstantiatorHolder.instantiator.apply(c);
    }

    /**
     * Sets accessible objects as accessible if not already.
     *
     * @param ao to check and set accessible
     */
    public static void setAccessible(AccessibleObject ao) {
        UnsafeAccessibilityHolder.accessibilitySetter.accept(ao);
    }

    private static final class UnsafeInstantiatorHolder {

        private static final Function<Class, Object> instantiator = create();

        private static Function<Class, Object> create() {
            // if available, use sun.misc.Unsafe:
            try {
                final Class unsafeC = Class.forName("sun.misc.Unsafe");
                final Field theUnsafeF = unsafeC.getDeclaredField("theUnsafe");
                theUnsafeF.setAccessible(true);
                final Object theUnsafe = theUnsafeF.get(null);
                final Method allocateInstanceM = unsafeC.getMethod("allocateInstance", Class.class);
                return c -> {
                    try {
                        return allocateInstanceM.invoke(theUnsafe, c);
                    } catch (IllegalAccessException | InvocationTargetException neverThrown) {
                        return null;
                    }
                };
            } catch (ClassNotFoundException | NoSuchFieldException | SecurityException |
                    IllegalArgumentException | IllegalAccessException | NoSuchMethodException sunUnsafeNotAvailable) {
            }
            // if available, use java.io.ObjectInputStream:
            try {
                final Method newInstanceM = ObjectInputStream.class.getDeclaredMethod("newInstance", Class.class, Class.class);
                newInstanceM.setAccessible(true);
                return c -> {
                    try {
                        return newInstanceM.invoke(null, c, Object.class);
                    } catch (IllegalAccessException | InvocationTargetException neverThrown) {
                        return null;
                    }
                };
            } catch (NoSuchMethodException | SecurityException notAvailable) {
            }
            // if available, use java.io.ObjectStreamClass:
            try {
                final Method getConstructorIdM = ObjectStreamClass.class.getDeclaredMethod("getConstructorId", Class.class);
                setAccessible(getConstructorIdM);
                final int constructorId = (Integer) getConstructorIdM.invoke(null, Object.class);
                final Method newInstanceM = ObjectStreamClass.class.getDeclaredMethod("newInstance", Class.class, int.class);
                newInstanceM.setAccessible(true);
                return c -> {
                    try {
                        return newInstanceM.invoke(null, c, constructorId);
                    } catch (IllegalAccessException | InvocationTargetException neverThrown) {
                        return null;
                    }
                };
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                    IllegalArgumentException | InvocationTargetException notAvailable) {
            }
            // else unsafe allocation will not work, if used:
            return c -> {
                throw new UnsupportedOperationException("unsafe instantiation not supported on this JVM");
            };
        }
    }

    private static final class UnsafeAccessibilityHolder {

        private static final Consumer<AccessibleObject> accessibilitySetter = create();

        private static Consumer<AccessibleObject> create() {
            return JVMUtil.getJavaMajorVersion() < 9 ? java8AccessibilitySetter() : java9PlusAccessibilitySetter();
        }

        private static Consumer<AccessibleObject> java8AccessibilitySetter() {
            return ao -> {
                ao.setAccessible(true);
            };
        }

        private static Consumer<AccessibleObject> java9PlusAccessibilitySetter() {
            try {
                final Class unsafeC = Class.forName("sun.misc.Unsafe");
                final Field theUnsafeF = unsafeC.getDeclaredField("theUnsafe");
                theUnsafeF.setAccessible(true);
                final Method objectFieldOffsetM = unsafeC.getMethod("objectFieldOffset", Field.class);
                final Method putBooleanM = unsafeC.getMethod("putBoolean", Object.class, long.class, boolean.class);
                final Object theUnsafe = theUnsafeF.get(null);

                final Field overrideField = AccessibleObject.class.getDeclaredField("override");
                final long overrideOffset = (Long) objectFieldOffsetM.invoke(theUnsafe, overrideField);

                return ao -> {
                    try {
                        putBooleanM.invoke(theUnsafe, ao, overrideOffset, true);
                    } catch (ReflectiveOperationException e) {
                        java9PlusSetAccessible(ao);
                    }
                };
            } catch (Exception unsafeMissing) {
                return ao -> java9PlusSetAccessible(ao);
            }
        }

        private static void java9PlusSetAccessible(AccessibleObject ao) {
            try {
                ao.setAccessible(true);
            } catch (SecurityException sX) {
                throw new UnsupportedOperationException("setAccessibleTrue on " + ao, sX);
            }
        }
    }

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
    }

    private ReflectionUtil() {
    }
}
