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
