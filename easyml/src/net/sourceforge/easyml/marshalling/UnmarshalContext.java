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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Date;

/**
 * UnmarshalContext interface defines the contextual data given to
 * {@linkplain Strategy} instances at un-marshalling stage.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.8
 */
public interface UnmarshalContext {

    /**
     * Returns the class for the given alias, or <code>null</code> if the given
     * alias is not configured.
     *
     * @param alias of the class
     *
     * @return the class or null
     */
    Class aliasedClassFor(String alias);

    /**
     * Returns the field for the given class-alias pair, or <code>null</code> if
     * the given class-alias is not configured.
     *
     * @param declaring class declaring the field
     * @param alias of the field
     *
     * @return the field or null
     */
    Field aliasedFieldFor(Class declaring, String alias);

    /**
     * Returns the given class' default constructor, if any. If found but not
     * accessible, it will be set to be accessible.
     *
     * @param <T> underlying type for the given class c
     * @param c the class to reflect the default constructor for
     *
     * @return class' default constructor
     * @throws NoSuchMethodException if no default constructor defined for class
     */
    <T> Constructor<T> defaultConstructorFor(Class<T> c) throws NoSuchMethodException;

    /**
     * Returns the class for the given <code>aliasOrName</code>, or throws
     * ClassNotFoundException if the given <code>aliasOrName</code> is not a
     * known class alias or class name.
     *
     * @param aliasOrName a configured class alias or a class name
     *
     * @return the class
     * @throws ClassNotFoundException
     */
    Class classFor(String aliasOrName) throws ClassNotFoundException;

    /**
     * Returns the field for the given <code>declaring</code> class and
     * <code>aliasOrName</code> pair, or throws NoSuchFieldException if the
     * given <code>aliasOrName</code> is not a known field alias or field name,
     * within the given declaring class.
     *
     * @param declaring class declaring the field
     * @param aliasOrName a configured field alias or a field name
     *
     * @return the field
     * @throws NoSuchFieldException
     */
    Field fieldFor(Class declaring, String aliasOrName) throws NoSuchFieldException;

    /**
     * Calculates a descriptor detailing the current reader position inside the
     * XML. This information should be used as exception message for detailing
     * read exceptions.
     *
     * @return the reader position descriptor
     */
    String readerPositionDescriptor();

    /**
     * Parses the given date using the date format context configuration.
     *
     * @param date to parse
     *
     * @return the parsed date
     * @throws ParseException when date is invalid w.r.t. the configuration
     */
    Date parseDate(String date) throws ParseException;
}
