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
package net.sourceforge.easyml.marshalling;

/**
 * Strategy interface is a {@linkplain Named} object used to marshal a data type to EasyML and back again.
 * By default, Strategy is strict.
 *
 * @param <T> target class
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.3
 * @since 1.0
 */
public interface Strategy<T> extends Named {

    /**
     * Returns true if this instance applies strictly to one
     * <code>Class</code>. False means that the implementation is applicable to
     * a class hierarchy.
     * Strict, by default.
     *
     * @return true if strict, false if inheritance applicable
     */
    default boolean strict() {
        return true;
    }

    /**
     * Returns true if this instance is applicable to the given class, false
     * otherwise.
     * Strict, by default.
     *
     * @param c to test
     * @return true if can be marshalled and un-marshalled using this strategy
     */
    default boolean appliesTo(Class<T> c) {
        return c == target();
    }

    /**
     * Returns the class on which this instance operates. The non-
     * <code>null</code> return class must be the exact target class if this
     * instance is strict or the root of the inheritance tree if this instance
     * is not strict.
     *
     * @return the target class
     */
    Class target();

}
