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
