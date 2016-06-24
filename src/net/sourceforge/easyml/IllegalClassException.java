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
package net.sourceforge.easyml;

/**
 * IllegalClassException class is thrown when the XML that is being parsed
 * contains details to unmarshal a class which is illegal w.r.t the
 * {@linkplain XMLReader}'s security policy.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.2.5
 * @version 1.3.3
 */
public class IllegalClassException extends RuntimeException {

    private final Class illegalClass;

    /**
     * Creates a new instance of <code>IllegalClassException</code> with the
     * <code>illegalClass</code> as details.
     *
     * @param illegalClass triggering this exception
     */
    public IllegalClassException(Class illegalClass) {
        super(illegalClass.getName());
        this.illegalClass = illegalClass;
    }

    /**
     * Gets the {@linkplain #illegalClass} property.
     *
     * @return the property value
     */
    public Class getIllegalClass() {
        return this.illegalClass;
    }
}
