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
package net.sourceforge.easyml.marshalling.java.io;

import java.io.File;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;

/**
 * FileStrategy class that implements the {@linkplain SimpleStrategy} interface
 * for the Java {@linkplain File}. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.0
 */
public final class FileStrategy extends AbstractStrategy<File>
        implements SimpleStrategy<File> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "file";
    /**
     * Constant defining the singleton instance.
     */
    public static final FileStrategy INSTANCE = new FileStrategy();

    private FileStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<File> c) {
        return c == File.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean strict() {
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<File> target() {
        return File.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return FileStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(File target, MarshalContext ctx) {
        return target.getAbsolutePath();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public File unmarshal(String text, UnmarshalContext ctx) {
        return new File(text);
    }
}//class FileStrategy.
