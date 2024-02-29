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

import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.CompositeWriter;

import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * ReflectionUtil utility class contains reflection helper methods.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.6.0
 * @since 1.0
 */
public final class ReflectionUtil {

    /**
     * Constant used to cache the zero-arg signature for reflecting methods.
     */
    public static final Class[] METHOD_NO_PARAMS = new Class[]{};
    /**
     * Constant used to cache the zero-arg reflective invocations for methods.
     */
    public static final Object[] METHOD_NO_PARAM_VALUES = new Object[]{};
    private static final String PREFIX_IS = "is";
    private static final String PREFIX_GET = "get";
    private static final String PREFIX_SET = "set";
    private static final Map<String, Class> typesForName = new HashMap<>();

    static {
        typesForName.put(Void.TYPE.getName(), Void.TYPE);
        typesForName.put(Boolean.TYPE.getName(), Boolean.TYPE);
        typesForName.put(Byte.TYPE.getName(), Byte.TYPE);
        typesForName.put(Character.TYPE.getName(), Character.TYPE);
        typesForName.put(Short.TYPE.getName(), Short.TYPE);
        typesForName.put(Integer.TYPE.getName(), Integer.TYPE);
        typesForName.put(Long.TYPE.getName(), Long.TYPE);
        typesForName.put(Float.TYPE.getName(), Float.TYPE);
        typesForName.put(Double.TYPE.getName(), Double.TYPE);
    }

    /**
     * Returns field info used to read properties.
     * Contains the field getter accessor, if defined by the field's class.
     *
     * @param f to reflect
     * @return field info
     */
    public static FieldInfo fieldInfoForRead(Field f) {
        return fieldInfoFor(f, AccessorType.GETTER);
    }

    /**
     * Returns field info used to write properties.
     * Contains the field setter accessor, if defined by the field's class.
     *
     * @param f to reflect
     * @return field info
     */
    public static FieldInfo fieldInfoForWrite(Field f) {
        return fieldInfoFor(f, AccessorType.SETTER);
    }

    public static final class FieldInfo {

        /**
         * True if the underlying field is a property, false otherwise.
         */
        public final boolean isProperty;
        /**
         * Field accessor if {@linkplain #isProperty} and defined by field's class, else null.
         */
        public final Method accessor;

        private FieldInfo(boolean isProperty, Method accessor) {
            this.isProperty = isProperty;
            this.accessor = accessor;
        }
    }

    private static FieldInfo fieldInfoFor(Field f, AccessorType accessorType) {
        if (Modifier.isStatic(f.getModifiers())) {
            return new FieldInfo(false, null);
        }
        final Class fDeclaring = f.getDeclaringClass();
        final Class fType = f.getType();
        final String fProperty = propertyNameFor(f);
        // search for getter or setter:
        boolean isProperty = false;
        for (Method m : fDeclaring.getMethods()) {
            final int mMod = m.getModifiers();
            if (Modifier.isStatic(mMod) || !Modifier.isPublic(mMod)) {
                continue; // skip static or non-public.
            }
            final Class mRet = m.getReturnType();
            final String mName = m.getName();
            final int mParamCount = m.getParameterCount();
            final Class[] mParams = mParamCount <= 2 ? m.getParameterTypes() : null;
            if (mParamCount == 0 && mRet == fType) {
                if (fType == boolean.class && mName.equals(PREFIX_IS + fProperty)
                        || mName.equals(PREFIX_GET + fProperty)) {
                    if (accessorType == AccessorType.GETTER) {
                        return new FieldInfo(true, m);
                    }
                    isProperty = true;
                }
            } else if (mParamCount == 1) {
                if (mRet != void.class && mParams[0] == int.class && mName.equals(PREFIX_GET + fProperty)) {
                    isProperty = true;
                } else if (mRet == void.class && mParams[0] == fType && mName.equals(PREFIX_SET + fProperty)) {
                    if (accessorType == AccessorType.SETTER) {
                        return new FieldInfo(true, m);
                    }
                    isProperty = true;
                }
            } else if (mParamCount == 2) {
                if (mRet == void.class && mParams[0] == int.class && mName.equals(PREFIX_SET + fProperty)) {
                    isProperty = true;
                }
            }
        }
        return new FieldInfo(isProperty, null);
    }

    private enum AccessorType {
        GETTER,
        SETTER
    }

    private static String propertyNameFor(Field f) {
        final String name = f.getName();
        final char upper0 = Character.toUpperCase(name.charAt(0));
        if (name.length() == 1) {
            return String.valueOf(upper0);
        }
        char[] nameChars = name.toCharArray();
        nameChars[0] = upper0;
        return new String(nameChars);
    }

    /**
     * Reads the object property using the <code>getter</code>.
     *
     * @param o      the source
     * @param f      fallback to be read directly
     * @param getter to invoke
     * @return property value
     * @throws IllegalAccessException if no getter and field can not be accessed
     */
    public static Object readProperty(Object o, Field f, Method getter) throws IllegalAccessException {
        if (getter != null) {
            try {
                if (!Modifier.isPublic(getter.getModifiers())) {
                    throw new IllegalArgumentException("non-public getter: " + getter);
                }
                return getter.invoke(o, ReflectionUtil.METHOD_NO_PARAMS);
            } catch (IllegalAccessException neverThrown) {
            } catch (InvocationTargetException e) {
                return readField(o, f);
            }
        }
        return readField(o, f);
    }

    /**
     * Reads the object field directly.
     *
     * @param o the source
     * @param f to be read directly
     * @return field value
     * @throws IllegalAccessException if field can not be accessed
     */
    public static Object readField(Object o, Field f) throws IllegalAccessException {
        setAccessible(f);
        return f.get(o);
    }

    /**
     * Writes the object property using the <code>setter</code>.
     *
     * @param o      the source
     * @param value  to write
     * @param f      fallback to be written directly
     * @param setter to invoke
     * @throws IllegalAccessException if no setter and field can not be accessed
     */
    public static void writeProperty(Object o, Object value, Field f, Method setter) throws IllegalAccessException {
        if (setter != null) {
            try {
                if (!Modifier.isPublic(setter.getModifiers())) {
                    throw new IllegalArgumentException("non-public setter: " + setter);
                }
                setter.invoke(o, value);
            } catch (IllegalAccessException neverThrown) {
            } catch (InvocationTargetException e) {
                writeField(o, value, f);
            }
        }
        writeField(o, value, f);
    }

    /**
     * Writes the object field directly.
     *
     * @param o     the source
     * @param value to write
     * @param f     to be written directly
     * @throws IllegalAccessException if field can not be accessed
     */
    public static void writeField(Object o, Object value, Field f) throws IllegalAccessException {
        setAccessible(f);
        f.set(o, value);
    }

    /**
     * Returns true if the given <code>cls</code> module is open to the <code>other</code> class' module.
     *
     * @param cls   to test
     * @param other to test against
     * @return true if cls module is open to other
     */
    public static boolean isOpen(Class cls, Class other) {
        return cls.getModule()
                .isOpen(cls.getPackageName(), other.getModule());
    }

    /**
     * Returns true if Java Modules not yet restricted or
     * if the given <code>cls</code> module is open to the <code>other</code> class' module.
     *
     * @param cls   to test
     * @param other to test against
     * @return true if unrestricted modules or cls module is open to other
     */
    public static boolean isUnrestrictedOrOpen(Class cls, Class other) {
        return JVMUtil.isUnrestricted() || isOpen(cls, other);
    }

    /**
     * Sets accessible objects as accessible if not already.
     *
     * @param ao to check and set accessible
     */
    public static void setAccessible(AccessibleObject ao) {
        UnsafeAccessibilityHolder.accessibilitySetter.accept(ao);
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
        final Class type = typesForName.get(typeName);
        return type != null ? type : Class.forName(typeName);
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
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * Instantiates the given class safely, using declared constructor.
     *
     * @param c to instantiate
     * @return new instance of c
     */
    public static Object instantiate(Class c)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return c.getDeclaredConstructor(METHOD_NO_PARAMS).newInstance(METHOD_NO_PARAM_VALUES);
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
            throw new IllegalArgumentException("Cannot instantiate interface or abstract class: " + c.getName());
        }
        return UnsafeInstantiatorHolder.instantiator.apply(c);
    }

    private static final class UnsafeAccessibilityHolder {

        private static final Consumer<AccessibleObject> accessibilitySetter = create();

        private static Consumer<AccessibleObject> create() {
            final int javaMajor = JVMUtil.getJavaMajorVersion();
            return JVMUtil.JAVA_MAJOR_VERSION_MODULES <= javaMajor && javaMajor <= JVMUtil.JAVA_MAJOR_VERSION_LAST_UNRESTRICTED ?
                    java9AccessibilitySetter() :
                    javaAccessibilitySetter();
        }

        private static Consumer<AccessibleObject> java9AccessibilitySetter() {
            try {
                final Class unsafeC = Class.forName("sun.misc.Unsafe");
                final Field theUnsafeF = unsafeC.getDeclaredField("theUnsafe");
                final Method objectFieldOffsetM = unsafeC.getMethod("objectFieldOffset", Field.class);
                final Method putBooleanM = unsafeC.getMethod("putBoolean", Object.class, long.class, boolean.class);

                final Field overrideField = AccessibleObject.class.getDeclaredField("override");

                theUnsafeF.setAccessible(true);
                final Object theUnsafe = theUnsafeF.get(null);
                final long overrideOffset = (Long) objectFieldOffsetM.invoke(theUnsafe, overrideField);

                return ao -> {
                    try {
                        putBooleanM.invoke(theUnsafe, ao, overrideOffset, true);
                    } catch (ReflectiveOperationException e) {
                        ao.setAccessible(true);
                    }
                };
            } catch (Exception unsafeMissing) {
                return ao -> ao.setAccessible(true);
            }
        }

        private static Consumer<AccessibleObject> javaAccessibilitySetter() {
            return ao -> ao.setAccessible(true);
        }

    }

    private static final class UnsafeInstantiatorHolder {

        private static final Function<Class, Object> instantiator = create();

        private static Function<Class, Object> create() {
            // if available, use sun.misc.Unsafe:
            try {
                final Class unsafeC = Class.forName("sun.misc.Unsafe");
                final Field theUnsafeF = unsafeC.getDeclaredField("theUnsafe");
                setAccessible(theUnsafeF);
                final Object theUnsafe = theUnsafeF.get(null);
                final Method allocateInstanceM = unsafeC.getMethod("allocateInstance", Class.class);
                return c -> {
                    try {
                        return allocateInstanceM.invoke(theUnsafe, c);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                };
            } catch (Exception sunUnsafeNotAvailable) {
            }
            // if available, use java.io.ObjectInputStream:
            try {
                final Method newInstanceM = ObjectInputStream.class.getDeclaredMethod("newInstance", Class.class, Class.class);
                setAccessible(newInstanceM);
                return c -> {
                    try {
                        return newInstanceM.invoke(null, c, Object.class);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                };
            } catch (Exception notAvailable) {
            }
            // if available, use java.io.ObjectStreamClass:
            try {
                final Method getConstructorIdM = ObjectStreamClass.class.getDeclaredMethod("getConstructorId", Class.class);
                setAccessible(getConstructorIdM);
                final int constructorId = (Integer) getConstructorIdM.invoke(null, Object.class);
                final Method newInstanceM = ObjectStreamClass.class.getDeclaredMethod("newInstance", Class.class, int.class);
                setAccessible(newInstanceM);
                return c -> {
                    try {
                        return newInstanceM.invoke(null, c, constructorId);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                };
            } catch (Exception notAvailable) {
            }
            // else unsafe allocation will not work, if used:
            return c -> {
                throw new UnsupportedOperationException("unsafe instantiation not supported on this JVM");
            };
        }
    }

    /**
     * ValueType enumerates all value-type classes.
     * Used to reflect value types in objects and arrays.
     *
     * @author Victor Cordis ( cordis.victor at gmail.com)
     * @version 1.6.0
     * @since 1.0
     */
    public enum ValueType {

        BOOLEAN {
            @Override
            public Object parseValue(String value) {
                return Boolean.parseBoolean(value);
            }

            @Override
            public boolean getWriteArrayItem(CompositeWriter writer, Object array, int itemIdx, boolean skipDefs) {
                final boolean item = Array.getBoolean(array, itemIdx);
                if (skipDefs && item == false) {
                    return false;
                }
                writer.writeBoolean(item);
                return true;
            }

            @Override
            public boolean isDefaultArrayItem(Object array, int itemIdx) {
                return Array.getBoolean(array, itemIdx) == false;
            }

            @Override
            public boolean setReadArrayItem(CompositeReader reader, Object array, int itemIdx) {
                final boolean item = reader.readBoolean();
                if (item == false) {
                    return false;
                }
                Array.setBoolean(array, itemIdx, item);
                return true;
            }
        },
        BOOLEAN_WRAPPER {
            @Override
            public Object parseValue(String value) {
                return Boolean.parseBoolean(value);
            }

        },
        BYTE {
            @Override
            public Object parseValue(String value) {
                return Byte.parseByte(value);
            }

            @Override
            public boolean getWriteArrayItem(CompositeWriter writer, Object array, int itemIdx, boolean skipDefs) {
                final byte item = Array.getByte(array, itemIdx);
                if (skipDefs && item == 0) {
                    return false;
                }
                writer.writeByte(item);
                return true;
            }

            @Override
            public boolean isDefaultArrayItem(Object array, int itemIdx) {
                return Array.getByte(array, itemIdx) == 0;
            }

            @Override
            public boolean setReadArrayItem(CompositeReader reader, Object array, int itemIdx) {
                final byte item = reader.readByte();
                if (item == 0) {
                    return false;
                }
                Array.setByte(array, itemIdx, item);
                return true;
            }
        },
        BYTE_WRAPPER {
            @Override
            public Object parseValue(String value) {
                return Byte.parseByte(value);
            }

        },
        SHORT {
            @Override
            public Object parseValue(String value) {
                return Short.parseShort(value);
            }

            @Override
            public boolean getWriteArrayItem(CompositeWriter writer, Object array, int itemIdx, boolean skipDefs) {
                final short item = Array.getShort(array, itemIdx);
                if (skipDefs && item == 0) {
                    return false;
                }
                writer.writeShort(item);
                return true;
            }

            @Override
            public boolean isDefaultArrayItem(Object array, int itemIdx) {
                return Array.getShort(array, itemIdx) == 0;
            }

            @Override
            public boolean setReadArrayItem(CompositeReader reader, Object array, int itemIdx) {
                final short item = reader.readShort();
                if (item == 0) {
                    return false;
                }
                Array.setShort(array, itemIdx, item);
                return true;
            }
        },
        SHORT_WRAPPER {
            @Override
            public Object parseValue(String value) {
                return Short.parseShort(value);
            }

        },
        INT {
            @Override
            public Object parseValue(String value) {
                return Integer.parseInt(value);
            }

            @Override
            public boolean getWriteArrayItem(CompositeWriter writer, Object array, int itemIdx, boolean skipDefs) {
                final int item = Array.getInt(array, itemIdx);
                if (skipDefs && item == 0) {
                    return false;
                }
                writer.writeInt(item);
                return true;
            }

            @Override
            public boolean isDefaultArrayItem(Object array, int itemIdx) {
                return Array.getInt(array, itemIdx) == 0;
            }

            @Override
            public boolean setReadArrayItem(CompositeReader reader, Object array, int itemIdx) {
                final int item = reader.readInt();
                if (item == 0) {
                    return false;
                }
                Array.setInt(array, itemIdx, item);
                return true;
            }
        },
        INT_WRAPPER {
            @Override
            public Object parseValue(String value) {
                return Integer.parseInt(value);
            }

        },
        LONG {
            @Override
            public Object parseValue(String value) {
                return Long.parseLong(value);
            }

            @Override
            public boolean getWriteArrayItem(CompositeWriter writer, Object array, int itemIdx, boolean skipDefs) {
                final long item = Array.getLong(array, itemIdx);
                if (skipDefs && item == 0) {
                    return false;
                }
                writer.writeLong(item);
                return true;
            }

            @Override
            public boolean isDefaultArrayItem(Object array, int itemIdx) {
                return Array.getLong(array, itemIdx) == 0;
            }

            @Override
            public boolean setReadArrayItem(CompositeReader reader, Object array, int itemIdx) {
                final long item = reader.readLong();
                if (item == 0) {
                    return false;
                }
                Array.setLong(array, itemIdx, item);
                return true;
            }
        },
        LONG_WRAPPER {
            @Override
            public Object parseValue(String value) {
                return Long.parseLong(value);
            }

        },
        FLOAT {
            @Override
            public Object parseValue(String value) {
                return Float.parseFloat(value);
            }

            @Override
            public boolean getWriteArrayItem(CompositeWriter writer, Object array, int itemIdx, boolean skipDefs) {
                final float item = Array.getFloat(array, itemIdx);
                if (skipDefs && item == 0f) {
                    return false;
                }
                writer.writeFloat(item);
                return true;
            }

            @Override
            public boolean isDefaultArrayItem(Object array, int itemIdx) {
                return Array.getFloat(array, itemIdx) == 0f;
            }

            @Override
            public boolean setReadArrayItem(CompositeReader reader, Object array, int itemIdx) {
                final float item = reader.readFloat();
                if (item == 0f) {
                    return false;
                }
                Array.setFloat(array, itemIdx, item);
                return true;
            }
        },
        FLOAT_WRAPPER {
            @Override
            public Object parseValue(String value) {
                return Float.parseFloat(value);
            }
        },
        DOUBLE {
            @Override
            public Object parseValue(String value) {
                return Double.parseDouble(value);
            }

            @Override
            public boolean getWriteArrayItem(CompositeWriter writer, Object array, int itemIdx, boolean skipDefs) {
                final double item = Array.getDouble(array, itemIdx);
                if (skipDefs && item == 0.0) {
                    return false;
                }
                writer.writeDouble(item);
                return true;
            }

            @Override
            public boolean isDefaultArrayItem(Object array, int itemIdx) {
                return Array.getDouble(array, itemIdx) == 0.0;
            }

            @Override
            public boolean setReadArrayItem(CompositeReader reader, Object array, int itemIdx) {
                final double item = reader.readDouble();
                if (item == 0.0) {
                    return false;
                }
                Array.setDouble(array, itemIdx, item);
                return true;
            }
        },
        DOUBLE_WRAPPER {
            @Override
            public Object parseValue(String value) {
                return Double.parseDouble(value);
            }
        },
        CHAR {
            @Override
            public Object parseValue(String value) {
                if (value.length() != 1) {
                    throw new IllegalArgumentException("value: length not 1: " + value);
                }
                return value.charAt(0);
            }

            @Override
            public boolean getWriteArrayItem(CompositeWriter writer, Object array, int itemIdx, boolean skipDefs) {
                final char item = Array.getChar(array, itemIdx);
                if (skipDefs && item == 0) {
                    return false;
                }
                writer.writeChar(item);
                return true;
            }

            @Override
            public boolean isDefaultArrayItem(Object array, int itemIdx) {
                return Array.getChar(array, itemIdx) == 0;
            }

            @Override
            public boolean setReadArrayItem(CompositeReader reader, Object array, int itemIdx) {
                final char item = reader.readChar();
                if (item == 0) {
                    return false;
                }
                Array.setChar(array, itemIdx, item);
                return true;
            }
        },
        CHAR_WRAPPER {
            @Override
            public Object parseValue(String value) {
                if (value.length() != 1) {
                    throw new IllegalArgumentException("value: length not 1: " + value);
                }
                return value.charAt(0);
            }
        },
        STRING {
            @Override
            public Object parseValue(String value) {
                return value;
            }
        };

        private static final Map<Class, ValueType> types = new IdentityHashMap<>();

        static {
            types.put(Boolean.TYPE, BOOLEAN);
            types.put(Boolean.class, BOOLEAN_WRAPPER);
            types.put(Byte.TYPE, BYTE);
            types.put(Byte.class, BYTE_WRAPPER);
            types.put(Short.TYPE, SHORT);
            types.put(Short.class, SHORT_WRAPPER);
            types.put(Integer.TYPE, INT);
            types.put(Integer.class, INT_WRAPPER);
            types.put(Long.TYPE, LONG);
            types.put(Long.class, LONG_WRAPPER);
            types.put(Float.TYPE, FLOAT);
            types.put(Float.class, FLOAT_WRAPPER);
            types.put(Double.TYPE, DOUBLE);
            types.put(Double.class, DOUBLE_WRAPPER);
            types.put(Character.TYPE, CHAR);
            types.put(Character.class, CHAR_WRAPPER);
            types.put(String.class, STRING);
        }

        /**
         * Returns <code>true</code> if the given type class represents a value
         * type.
         *
         * @param type to test
         * @return true if value type, false otherwise
         */
        public static boolean is(Class type) {
            return types.containsKey(type);
        }

        /**
         * Returns the value type constant corresponding to the given type class. If
         * the given class does not represent a value-type then <code>null</code> is
         * returned.
         *
         * @param type to get constant for
         * @return the constant, if any, or null
         */
        public static ValueType of(Class type) {
            return types.get(type);
        }

        /**
         * Parses the given string representation and returns the value.
         *
         * @param value to parse
         * @return the value
         */
        public abstract Object parseValue(String value);

        /**
         * Reflection method used to check if the item at itemIdx of the given array
         * has a default value.
         *
         * @param array   container
         * @param itemIdx index of item
         * @return true if default, false otherwise
         */
        public boolean isDefaultArrayItem(Object array, int itemIdx) {
            return Array.get(array, itemIdx) == null;
        }

        /**
         * Reflection method used to get and write an array value, if not
         * <code>skipDefs</code> and <code>valueIsDefault</code>, to prevent auto-boxing.
         *
         * @param writer   to write with
         * @param array    the array
         * @param itemIdx  the array item index
         * @param skipDefs true if skip defaults
         * @return true if written, false if skipDefs and default
         */
        public boolean getWriteArrayItem(CompositeWriter writer, Object array, int itemIdx, boolean skipDefs) {
            final Object item = Array.get(array, itemIdx);
            if (skipDefs && item == null) {
                return false;
            }
            writer.write(item);
            return true;
        }

        /**
         * Reflection method used to read and set an array value, to prevent
         * auto-boxing. The value is not set at the array indexIdem if it is the
         * array-type default value.
         *
         * @param reader  to read with
         * @param array   the array
         * @param itemIdx the array item index
         * @return true if set, false otherwise
         */
        public boolean setReadArrayItem(CompositeReader reader, Object array, int itemIdx) {
            final Object item = reader.read();
            if (item == null) {
                return false;
            }
            Array.set(array, itemIdx, item);
            return true;
        }
    }

    private ReflectionUtil() {
    }
}
