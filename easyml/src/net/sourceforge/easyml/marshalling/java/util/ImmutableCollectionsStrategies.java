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
package net.sourceforge.easyml.marshalling.java.util;

import net.sourceforge.easyml.InvalidFormatException;
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.CompositeStrategy;
import net.sourceforge.easyml.marshalling.CompositeWriter;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

import java.util.*;
import java.util.function.Consumer;

/**
 * ImmutableCollectionsStrategies class contains strategies for
 * {@linkplain List#of()}, {@linkplain Map#of()}, {@linkplain Set#of()} factory methods.
 * Implementations are thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.7.1
 * @since 1.7.1
 */
public final class ImmutableCollectionsStrategies {

    /**
     * Constant defining the value used for the list of 1-2 strategy name.
     */
    public static final String NAME_LIST12 = "immutable-lst12";
    /**
     * Constant defining the singleton list12 instance.
     */
    public static final CompositeStrategy<List> INSTANCE_LIST12 = new CollectionStrategy<>() {

        private static final Class TARGET = List.of(0, 1).getClass();

        @Override
        public Class target() {
            return TARGET;
        }

        @Override
        public String name() {
            return NAME_LIST12;
        }

        @Override
        protected void marshalAttr(List target, CompositeWriter writer) {
        }

        @Override
        public List unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
            return new ArrayList(2);
        }

        @Override
        public List unmarshalInit(List target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
            super.unmarshalInit(target, reader, ctx);
            return target.size() == 1 ? List.of(target.get(0)) : List.of(target.get(0), target.get(1));
        }
    };

    /**
     * Constant defining the value used for the list of N strategy name.
     */
    public static final String NAME_LISTN = "immutable-lstn";
    /**
     * Constant defining the singleton listN instance.
     */
    public static final CompositeStrategy<List> INSTANCE_LISTN = new CollectionStrategy<>() {

        private static final Class TARGET = List.of(0, 1, 2, 3, 4).getClass();

        @Override
        public Class target() {
            return TARGET;
        }

        @Override
        public String name() {
            return NAME_LISTN;
        }

        @Override
        public List unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
            try {
                return new ArrayList(Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_SIZE)));
            } catch (NumberFormatException nfx) {
                throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
            }
        }

        @Override
        public List unmarshalInit(List target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
            super.unmarshalInit(target, reader, ctx);
            return List.of(target.toArray());
        }
    };

    /**
     * Constant defining the value used for the map of 1-2 strategy name.
     */
    public static final String NAME_MAP12 = "immutable-map12";
    /**
     * Constant defining the singleton map12 instance.
     */
    public static final CompositeStrategy<Map> INSTANCE_MAP12 = new MapStrategy<>() {

        private static final Class TARGET = Map.of(0, 1).getClass();

        @Override
        public Class target() {
            return TARGET;
        }

        @Override
        public String name() {
            return NAME_MAP12;
        }

        @Override
        protected void marshalAttr(Map target, CompositeWriter writer) {
        }

        @Override
        public Map unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
            return new HashMap(2);
        }

        @Override
        public Map unmarshalInit(Map target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
            super.unmarshalInit(target, reader, ctx);
            final Iterator<Map.Entry> targetIt = target.entrySet().iterator();
            if (target.size() == 1) {
                final Map.Entry single = targetIt.next();
                return Map.of(single.getKey(), single.getValue());
            }
            final Map.Entry first = targetIt.next();
            final Map.Entry second = targetIt.next();
            return Map.of(first.getKey(), first.getValue(), second.getKey(), second.getValue());
        }
    };

    /**
     * Constant defining the value used for the map of N strategy name.
     */
    public static final String NAME_MAPN = "immutable-mapn";
    /**
     * Constant defining the singleton mapN instance.
     */
    public static final CompositeStrategy<Map<?, ?>> INSTANCE_MAPN = new MapStrategy<>() {

        private static final Class TARGET = Map.of(0, 0, 1, 1, 2, 2, 3, 3, 4, 4).getClass();

        @Override
        public Class target() {
            return TARGET;
        }

        @Override
        public String name() {
            return NAME_MAPN;
        }

        @Override
        public Map unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
            try {
                return new HashMap(Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_SIZE)));
            } catch (NumberFormatException nfx) {
                throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
            }
        }

        @Override
        public Map unmarshalInit(Map<?, ?> target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
            super.unmarshalInit(target, reader, ctx);
            return Map.ofEntries(target.entrySet().toArray(Map.Entry[]::new));
        }
    };

    /**
     * Constant defining the value used for the set of 1-2 strategy name.
     */
    public static final String NAME_SET12 = "immutable-set12";
    /**
     * Constant defining the singleton set12 instance.
     */
    public static final CompositeStrategy<Set> INSTANCE_SET12 = new CollectionStrategy<>() {

        private static final Class TARGET = Set.of(0, 1).getClass();

        @Override
        public Class target() {
            return TARGET;
        }

        @Override
        public String name() {
            return NAME_SET12;
        }

        @Override
        protected void marshalAttr(Set target, CompositeWriter writer) {
        }

        @Override
        public Set unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
            return new HashSet(2);
        }

        @Override
        public Set unmarshalInit(Set target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
            super.unmarshalInit(target, reader, ctx);
            final Iterator targetIt = target.iterator();
            return target.size() == 1 ? Set.of(targetIt.next()) : Set.of(targetIt.next(), targetIt.next());
        }
    };

    /**
     * Constant defining the value used for the set of N strategy name.
     */
    public static final String NAME_SETN = "immutable-setn";
    /**
     * Constant defining the singleton setN instance.
     */
    public static final CompositeStrategy<Set> INSTANCE_SETN = new CollectionStrategy<>() {

        private static final Class TARGET = Set.of(0, 1, 2, 3, 4).getClass();

        @Override
        public Class target() {
            return TARGET;
        }

        @Override
        public String name() {
            return NAME_SETN;
        }

        @Override
        public Set unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
            try {
                return new LinkedHashSet(Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_SIZE)));
            } catch (NumberFormatException nfx) {
                throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
            }
        }

        @Override
        public Set unmarshalInit(Set target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
            super.unmarshalInit(target, reader, ctx);
            return Set.of(target.toArray());
        }
    };

    /**
     * Consumes each strategy.
     *
     * @param c consumer
     */
    public static void forEach(Consumer<CompositeStrategy> c) {
        c.accept(INSTANCE_LIST12);
        c.accept(INSTANCE_LISTN);
        c.accept(INSTANCE_MAP12);
        c.accept(INSTANCE_MAPN);
        c.accept(INSTANCE_SET12);
        c.accept(INSTANCE_SETN);
    }

    private ImmutableCollectionsStrategies() {
    }
}
