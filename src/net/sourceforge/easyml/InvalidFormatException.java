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
 * InvalidFormatException class is thrown when the format of the XML that is
 * being parsed is invalid w.r.t. the EasyML DTD.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.3
 */
public class InvalidFormatException extends RuntimeException {

    private final String positionDescriptor;

    /**
     * Creates a new instance of <code>InvalidFormatException</code>.
     *
     * @param positionDescriptor detailing the current reader position
     * @param msg the detail message
     */
    public InvalidFormatException(String positionDescriptor, String msg) {
        this(positionDescriptor, msg, null);
    }

    /**
     * Creates a new instance of <code>InvalidFormatException</code>.
     *
     * @param positionDescriptor detailing the current reader position
     * @param cause the cause
     */
    public InvalidFormatException(String positionDescriptor, Throwable cause) {
        super(positionDescriptor, cause);
        this.positionDescriptor = positionDescriptor;
    }

    /**
     * Creates a new instance of <code>InvalidFormatException</code> .
     *
     * @param positionDescriptor detailing the current reader position
     * @param msg the detail message
     * @param cause the cause
     */
    public InvalidFormatException(String positionDescriptor, String msg, Throwable cause) {
        super(positionDescriptor + ": " + msg, cause);
        this.positionDescriptor = positionDescriptor;
    }

    /**
     * Gets the {@linkplain #positionDescriptor} property.
     *
     * @return the property value
     */
    public String getPositionDescriptor() {
        return this.positionDescriptor;
    }
}
