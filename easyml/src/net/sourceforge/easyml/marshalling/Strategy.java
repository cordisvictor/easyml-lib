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
 * Strategy interface used to marshal a data type to EasyML and back again.
 *
 * @param <T> target class
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.0
 */
public interface Strategy<T> {

    /**
     * Returns true if this instance applies strictly to one
     * <code>Class</code>. False means that the implementation is applicable to
     * a class hierarchy.
     *
     * @return true if strict, false if inheritance applicable
     */
    boolean strict();

    /**
     * Returns the class on which this instance operates. The non-
     * <code>null</code> return class must be the exact target class if this
     * instance is strict or the root of the inheritance tree if this instance
     * is not strict.
     *
     * @return the target class
     */
    Class target();

    /**
     * Returns true if this instance is applicable to the given class, false
     * otherwise.
     *
     * @param c to test
     *
     * @return true if can be marshalled and un-marshalled using this strategy
     */
    boolean appliesTo(Class<T> c);

    /**
     * Returns the name of this instance which will be written and used at
     * reading. <b>Note:</b> the name must be a valid XML element name and
     * should not be in conflict with a DTD-defined element.
     *
     * @return the non-null and non-empty name
     */
    String name();
}//interface Strategy.
