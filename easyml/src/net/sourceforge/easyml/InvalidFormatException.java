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
package net.sourceforge.easyml;

/**
 * InvalidFormatException class is thrown when the format of the XML that is
 * being parsed is invalid w.r.t. the EasyML DTD.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.3.3
 * @since 1.0
 */
public class InvalidFormatException extends RuntimeException {

    private final String positionDescriptor;

    /**
     * Creates a new instance of <code>InvalidFormatException</code>.
     *
     * @param positionDescriptor detailing the current reader position
     * @param msg                the detail message
     */
    public InvalidFormatException(String positionDescriptor, String msg) {
        this(positionDescriptor, msg, null);
    }

    /**
     * Creates a new instance of <code>InvalidFormatException</code>.
     *
     * @param positionDescriptor detailing the current reader position
     * @param cause              the cause
     */
    public InvalidFormatException(String positionDescriptor, Throwable cause) {
        super(positionDescriptor, cause);
        this.positionDescriptor = positionDescriptor;
    }

    /**
     * Creates a new instance of <code>InvalidFormatException</code> .
     *
     * @param positionDescriptor detailing the current reader position
     * @param msg                the detail message
     * @param cause              the cause
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
