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
 * CollectionsStrategies class contains strategies for {@linkplain List#of()}, {@linkplain Map#of()},
 * {@linkplain Set#of()} factory methods.
 * Implementations are thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.7.1
 * @since 1.7.1
 */
public final class CollectionsStrategies {

    /**
     * Constant defining the value used for the unmodifiable list strategy name.
     */
    public static final String NAME_UNMODIFIABLE_LIST = "unmodif-lst";
    /**
     * Constant defining the singleton unmodifiable list instance.
     */
    public static final CompositeStrategy<List> INSTANCE_UNMODIFIABLE_LIST = new CollectionStrategy<>() {

        private static final Class TARGET = Collections.unmodifiableList(new LinkedList<>()).getClass();

        @Override
        public Class target() {
            return TARGET;
        }

        @Override
        public String name() {
            return NAME_UNMODIFIABLE_LIST;
        }

        @Override
        protected void marshalAttr(List target, CompositeWriter writer) {
        }

        @Override
        public List unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
            return new LinkedList();
        }

        @Override
        public List unmarshalInit(List target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
            super.unmarshalInit(target, reader, ctx);
            return Collections.unmodifiableList(target);
        }
    };

    /**
     * Constant defining the value used for the unmodifiable random access list strategy name.
     */
    public static final String NAME_UNMODIFIABLE_LIST_RA = "unmodif-lst-ra";
    /**
     * Constant defining the singleton unmodifiable random access list instance.
     */
    public static final CompositeStrategy<List> INSTANCE_UNMODIFIABLE_LIST_RA = new CollectionStrategy<>() {

        private static final Class TARGET = Collections.unmodifiableList(Collections.EMPTY_LIST).getClass();

        @Override
        public Class target() {
            return TARGET;
        }

        @Override
        public String name() {
            return NAME_UNMODIFIABLE_LIST_RA;
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
            return Collections.unmodifiableList(target);
        }
    };

    /**
     * Constant defining the value used for the unmodifiable map strategy name.
     */
    public static final String NAME_UNMODIFIABLE_MAP = "unmodif-map";
    /**
     * Constant defining the singleton unmodifiable map instance.
     */
    public static final CompositeStrategy<Map> INSTANCE_UNMODIFIABLE_MAP = new MapStrategy<>() {

        private static final Class TARGET = Collections.unmodifiableMap(Collections.EMPTY_MAP).getClass();

        @Override
        public Class target() {
            return TARGET;
        }

        @Override
        public String name() {
            return NAME_UNMODIFIABLE_MAP;
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
        public Map unmarshalInit(Map target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
            super.unmarshalInit(target, reader, ctx);
            return Collections.unmodifiableMap(target);
        }
    };

    /**
     * Constant defining the value used for the unmodifiable set strategy name.
     */
    public static final String NAME_UNMODIFIABLE_SET = "unmodif-set";
    /**
     * Constant defining the singleton unmodifiable set instance.
     */
    public static final CompositeStrategy<Set> INSTANCE_UNMODIFIABLE_SET = new CollectionStrategy<>() {

        private static final Class TARGET = Collections.unmodifiableSet(Collections.EMPTY_SET).getClass();

        @Override
        public Class target() {
            return TARGET;
        }

        @Override
        public String name() {
            return NAME_UNMODIFIABLE_SET;
        }

        @Override
        public Set unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
            try {
                return new HashSet(Integer.parseInt(reader.elementRequiredAttribute(ATTRIBUTE_SIZE)));
            } catch (NumberFormatException nfx) {
                throw new InvalidFormatException(ctx.readerPositionDescriptor(), nfx);
            }
        }

        @Override
        public Set unmarshalInit(Set target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException {
            super.unmarshalInit(target, reader, ctx);
            return Collections.unmodifiableSet(target);
        }
    };

    /**
     * Consumes each strategy.
     *
     * @param c consumer
     */
    public static void forEach(Consumer<CompositeStrategy> c) {
        c.accept(INSTANCE_UNMODIFIABLE_LIST);
        c.accept(INSTANCE_UNMODIFIABLE_MAP);
        c.accept(INSTANCE_UNMODIFIABLE_SET);
    }

    private CollectionsStrategies() {
    }
}
