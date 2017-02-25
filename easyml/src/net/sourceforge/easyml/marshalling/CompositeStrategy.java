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
package net.sourceforge.easyml.marshalling;

/**
 * CompositeStrategy interface extends the {@linkplain Strategy} interface with
 * the methods used to marshal a composite datatype to a hierarchical XML format
 * and back again. The leafs elements of the composite datatype will eventually
 * be processed by {@linkplain CompositeWriter#writeValue(java.lang.String) }
 * and {@linkplain CompositeReader#readValue() }, and
 * {@linkplain SimpleStrategy} instances.
 *
 * @param <T> target class
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.4.4
 */
public interface CompositeStrategy<T> extends Strategy<T> {

    /**
     * Marshals the given non-null <code>target</code> to XML format using the
     * <code>writer</code> to write XML elements.<br/>
     * <b>Note:</b> for recursive marshalling one must use the
     * {@linkplain CompositeWriter#write(java.lang.Object)} method because of
     * internal computations such as graph-traversal marking.
     *
     * @param target to marshal
     * @param writer writes XML elements
     * @param ctx the marshalling context
     */
    void marshal(T target, CompositeWriter writer, MarshalContext ctx);

    /**
     * Un-marshaling first step, that is recreate the new instance,possibly
     * using the <code>reader</code> to read XML attributes which can
     * parameterize the instantiation. <br/> <b>Note:</b> the reader's position
     * is at the composite root element start and must not be moved after the
     * root element end.
     *
     * @param reader reads XML
     * @param ctx the un-marshalling context
     *
     * @throws ClassNotFoundException if a class instantiation fails
     * @throws InstantiationException if a class instantiation fails
     * @throws IllegalAccessException if a class instantiation fails
     *
     * @return the non-initialized instance
     */
    T unmarshalNew(CompositeReader reader, UnmarshalContext ctx)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException;

    /**
     * Un-marshaling second step, that is initialize the instance created at
     * step one, possibly using the <code>reader</code> to read XML elements.
     * <br/> <b>Note:</b> the reader's position is where it was left at the
     * first step and must be left at the root element end. <br/> <b>Note:</b>
     * for recursive un-marshalling one must use the {@linkplain CompositeReader#read()
     * } method because of internal computations such as graph-traversal
     * marking.
     *
     * @param target to initialize
     * @param reader reads XML elements
     * @param ctx the un-marshalling context
     *
     * @throws IllegalAccessException if a class initialization fails
     *
     * @return the initialized instance, possibly not the one referred by
     * <code>target</code>
     */
    Object unmarshalInit(T target, CompositeReader reader, UnmarshalContext ctx) throws IllegalAccessException;
}//interface CompositeStrategy.
