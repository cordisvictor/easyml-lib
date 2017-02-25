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
