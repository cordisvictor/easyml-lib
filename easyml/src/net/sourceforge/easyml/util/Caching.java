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
package net.sourceforge.easyml.util;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Caching utility class used to handle differences between concurrent and non-
 * concurrent map implementations. This class was written in order to not depend
 * on Java 8 features, keeping the EasyML framework minimal requirement to Java
 * 7.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.4.4
 * @version 1.4.4
 */
public final class Caching {

    /**
     * CachePutStrategy function that defines how put is done within a cache
     * map.
     */
    public interface CachePutStrategy<M extends Map> {

        /**
         * Puts the given <code>key</code> and <code>value</code> in the target
         * map.
         *
         * @param target out parameter
         * @param key in parameter
         * @param value in parameter
         */
        void put(M target, Object key, Object value);
    }

    /**
     * Strategy to put in cache.
     */
    public static final CachePutStrategy STRATEGY_PUT = new CachePutStrategy<Map>() {

        @Override
        public void put(Map target, Object key, Object value) {
            target.put(key, value);
        }
    };

    /**
     * Strategy to put in cache only when key is absent.
     */
    public static final CachePutStrategy STRATEGY_PUT_IF_ABSENT = new CachePutStrategy<ConcurrentMap>() {

        @Override
        public void put(ConcurrentMap target, Object key, Object value) {
            target.putIfAbsent(key, value);
        }
    };

    private Caching() {
    }
}
