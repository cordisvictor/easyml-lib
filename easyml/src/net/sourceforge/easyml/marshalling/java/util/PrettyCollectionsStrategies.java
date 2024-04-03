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
import net.sourceforge.easyml.marshalling.*;
import net.sourceforge.easyml.marshalling.java.util.concurrent.ConcurrentHashMapStrategy;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * PrettyCollectionsStrategies class contains strategies for {@linkplain Collection}s and {@linkplain Map}s,
 * decorated with a more readable generic format.
 * The new format describes Java Collections as <code>list</code>, <code>set</code>, <code>queue</code>, <code>map</code>.
 * Mapping to the various implementations is done by the {@linkplain #ATTRIBUTE_TYPE} attribute.
 * Implementations are thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.8.1
 * @since 1.8.1
 */
public final class PrettyCollectionsStrategies {

    /**
     * Constant defining the value used for the list strategies name.
     */
    public static final String NAME_LIST = "list";
    /**
     * Constant defining the value used for the set strategies name.
     */
    public static final String NAME_SET = "set";
    /**
     * Constant defining the value used for the queue strategies name.
     */
    public static final String NAME_QUEUE = "queue";
    /**
     * Constant defining the value used for the map strategies name.
     */
    public static final String NAME_MAP = "map";
    /**
     * Constant defining collection type attribute name.
     */
    public static final String ATTRIBUTE_TYPE = "type";

    private static final List<DecoratorCompositeStrategy> instances = List.of(
            decorateQueue(ArrayDequeStrategy.INSTANCE),
            decorateList(ArrayListStrategy.INSTANCE),
            decorateMap(ConcurrentHashMapStrategy.INSTANCE),
            decorateMap(EnumMapStrategy.INSTANCE),
            decorateSet(EnumSetStrategy.INSTANCE),
            // hashtable
            decorateMap(HashMapStrategy.INSTANCE),
            decorateSet(HashSetStrategy.INSTANCE),
            decorateMap(IdentityHashMapStrategy.INSTANCE),
            decorateMap(LinkedHashMapStrategy.INSTANCE),
            decorateSet(LinkedHashSetStrategy.INSTANCE),
            decorateList(LinkedListStrategy.INSTANCE),
            decorateQueue(PriorityQueueStrategy.INSTANCE),
            // stack
            decorateMap(TreeMapStrategy.INSTANCE),
            decorateSet(TreeSetStrategy.INSTANCE)
            // vector
    );

    private static final Map<String, CompositeStrategy> typeToStrategy = instances.stream()
            .map(DecoratorCompositeStrategy::decorated)
            .collect(toMap(Named::name, Function.identity()));

    private static final Set<DecoratorCompositeStrategy> instanceNames = instances.stream()
            .collect(toSet());

    /**
     * Consumes each distinct pretty strategy {@linkplain #ATTRIBUTE_TYPE}.
     *
     * @param c consumer
     */
    public static void forEachType(Consumer<CompositeStrategy> c) {
        instances.forEach(c);
    }

    /**
     * Consumes each distinct pretty strategy {@linkplain Named#name()}.
     *
     * @param c consumer
     */
    public static void forEachName(Consumer<CompositeStrategy> c) {
        instanceNames.forEach(c);
    }

    private static DecoratorCollectionStrategy decorateList(CollectionStrategy strategy) {
        return new DecoratorCollectionStrategy(NAME_LIST, strategy);
    }

    private static DecoratorCollectionStrategy decorateSet(CollectionStrategy strategy) {
        return new DecoratorCollectionStrategy(NAME_SET, strategy);
    }

    private static DecoratorCollectionStrategy decorateQueue(CollectionStrategy strategy) {
        return new DecoratorCollectionStrategy(NAME_QUEUE, strategy);
    }

    private static DecoratorMapStrategy decorateMap(MapStrategy strategy) {
        return new DecoratorMapStrategy(NAME_MAP, strategy);
    }

    private static final class DecoratorCollectionStrategy<C extends Collection> extends CollectionStrategy<C> implements DecoratorCompositeStrategy<C> {

        private final String name;
        private final CollectionStrategy<C> decoratedStrategy;

        private DecoratorCollectionStrategy(String name, CollectionStrategy<C> decoratedStrategy) {
            this.name = name;
            this.decoratedStrategy = decoratedStrategy;
        }

        @Override
        public CompositeStrategy<C> decorated() {
            return decoratedStrategy;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        protected void marshalAttrs(C target, CompositeWriter writer, MarshalContext ctx) {
            writer.setAttribute(ATTRIBUTE_TYPE, decoratedStrategy.name());
            decoratedStrategy.marshalAttrs(target, writer, ctx);
        }
    }

    private static final class DecoratorMapStrategy<M extends Map> extends MapStrategy<M> implements DecoratorCompositeStrategy<M> {

        private final String name;
        private final MapStrategy<M> decoratedStrategy;

        private DecoratorMapStrategy(String name, MapStrategy<M> decoratedStrategy) {
            this.name = name;
            this.decoratedStrategy = decoratedStrategy;
        }

        @Override
        public CompositeStrategy<M> decorated() {
            return decoratedStrategy;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        protected void marshalAttrs(M target, CompositeWriter writer, MarshalContext ctx) {
            writer.setAttribute(ATTRIBUTE_TYPE, decoratedStrategy.name());
            decoratedStrategy.marshalAttrs(target, writer, ctx);
        }
    }

    private interface DecoratorCompositeStrategy<T> extends CompositeStrategy<T> {

        CompositeStrategy<T> decorated();

        @Override
        default boolean strict() {
            return decorated().strict();
        }

        @Override
        default boolean appliesTo(Class<T> c) {
            return decorated().appliesTo(c);
        }

        @Override
        default Class target() {
            return decorated().target();
        }

        @Override
        default T unmarshalNew(CompositeReader reader, UnmarshalContext ctx)
                throws ClassNotFoundException, InstantiationException, IllegalAccessException {
            final String type = reader.elementRequiredAttribute(ATTRIBUTE_TYPE);
            final CompositeStrategy<T> decorated = typeToStrategy.get(type);
            if (decorated == null) {
                throw new InvalidFormatException(reader.positionDescriptor(), "invalid element type attribute: " + type);
            }
            return decorated.unmarshalNew(reader, ctx);
        }
    }

    private PrettyCollectionsStrategies() {
    }
}
