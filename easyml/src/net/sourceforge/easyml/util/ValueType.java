/*
 * Copyright (c) 2011, Victor Cordis. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of EasyML library.
 *
 * EasyML library is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License (LGPL) as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * EasyML library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with EasyML library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Please contact the author ( cordis.victor@gmail.com ) if you need additional
 * information or have any questions.
 */
package net.sourceforge.easyml.util;

import java.lang.reflect.Array;
import java.util.IdentityHashMap;
import java.util.Map;
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.CompositeWriter;

/**
 * ValueType enum containing all value-type classes.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.2.0
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
        types.put(boolean.class, BOOLEAN);
        types.put(Boolean.class, BOOLEAN_WRAPPER);
        types.put(byte.class, BYTE);
        types.put(Byte.class, BYTE_WRAPPER);
        types.put(short.class, SHORT);
        types.put(Short.class, SHORT_WRAPPER);
        types.put(int.class, INT);
        types.put(Integer.class, INT_WRAPPER);
        types.put(long.class, LONG);
        types.put(Long.class, LONG_WRAPPER);
        types.put(float.class, FLOAT);
        types.put(Float.class, FLOAT_WRAPPER);
        types.put(double.class, DOUBLE);
        types.put(Double.class, DOUBLE_WRAPPER);
        types.put(char.class, CHAR);
        types.put(Character.class, CHAR_WRAPPER);
        types.put(String.class, STRING);
    }

    /**
     * Returns <code>true</code> if the given type class represents a value
     * type.
     *
     * @param type to test
     *
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
     *
     * @return the constant, if any, or null
     */
    public static ValueType of(Class type) {
        return types.get(type);
    }

    /**
     * Returns the value type constant corresponding to the given primitive type
     * class. If the given class does not represent a primitive value-type then
     * <code>null</code> is returned.
     *
     * @param type primitive type to get constant for
     *
     * @return the constant, if any, or null
     */
    public static ValueType ofPrimitive(Class type) {
        return type.isPrimitive() ? types.get(type) : null;
    }

    /**
     * Parses the given string representation and returns the value.
     *
     * @param value to parse
     *
     * @return the value
     */
    public abstract Object parseValue(String value);

    /**
     * Reflection method used to get and write an array value, if not
     * <code>skipDefs && valueIsDefault</code>, to prevent auto-boxing.
     *
     * @param writer to write with
     * @param array the array
     * @param itemIdx the array item index
     * @param skipDefs true if skip defaults
     *
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
     * Reflection method used to check if the item at itemIdx of the given array
     * has a default value.
     *
     * @param array container
     * @param itemIdx index of item
     *
     * @return true if default, false otherwise
     */
    public boolean isDefaultArrayItem(Object array, int itemIdx) {
        return Array.get(array, itemIdx) == null;
    }

    /**
     * Reflection method used to read and set an array value, to prevent
     * auto-boxing. The value is not set at the array indexIdem if it is the
     * array-type default value.
     *
     * @param reader to read with
     * @param array the array
     * @param itemIdx the array item index
     *
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
