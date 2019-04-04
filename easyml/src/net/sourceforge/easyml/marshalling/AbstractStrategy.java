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
 * AbstractStrategy class that implements the {@linkplain Strategy} interface
 * leaving it abstract, only to implement other behavior characteristics.
 *
 * @param <T> target class
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractStrategy<T> implements Strategy<T> {

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Strategy) {
            return this.name().equals(((Strategy) obj).name());
        }
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int hashCode() {
        return this.name().hashCode();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String toString() {
        return this.name();
    }
}
