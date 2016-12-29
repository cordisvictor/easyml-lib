/*
 * Copyright (c) 2015, Victor Cordis. All rights reserved.
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
package net.sourceforge.easyml;

import java.io.IOException;
import java.io.Reader;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * XMLReaderTextDriver class is the XML reader driver implementation for reading
 * XML text from input streams, pull-parse style. The default pull-parser
 * implementation is {@linkplain KXmlParser}, but other
 * {@linkplain XmlPullParser} implementations can be specified as construction
 * parameters.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.1.0
 * @version 1.3.3
 */
/* default */ final class XMLReaderTextDriver extends XMLReader.Driver {

    private XmlPullParser parser;
    private Reader readerToClose;

    /**
     * Creates a new instance.
     *
     * @param target to use and be used by
     * @param in to read from
     */
    public XMLReaderTextDriver(XMLReader target, Reader in) {
        this(target, in, new KXmlParser());// single LOC depending on kXML2.
    }

    /**
     * Creates a new instance.
     *
     * @param target to use and be used by
     * @param in to read from
     * @param parser user defined parser class instance
     */
    public XMLReaderTextDriver(XMLReader target, Reader in, XmlPullParser parser) {
        super(target);
        try {
            this.parser = parser;
            this.parser.setInput(in);
            this.readerToClose = in;
        } catch (XmlPullParserException xppX) {
            throw new IllegalStateException("not initialized", xppX);
        }
    }

    /**
     * Returns a line-column pair to indicate the position in the XML text.
     *
     * @return line-column position string
     */
    @Override
    public String positionDescriptor() {
        return this.parser.getLineNumber() + "," + this.parser.getColumnNumber();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean next() {
        try {
            if (this.parser.getEventType() == XmlPullParser.END_DOCUMENT) {
                return false;
            }
            this.parser.nextTag();
            return true;
        } catch (XmlPullParserException | IOException xppX) {
            throw new InvalidFormatException(this.positionDescriptor(), xppX);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean atElementStart() {
        try {
            return this.parser.getEventType() == XmlPullParser.START_TAG;
        } catch (XmlPullParserException xppX) {
            throw new InvalidFormatException(this.positionDescriptor(), xppX);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean atElementEnd() {
        try {
            return this.parser.getEventType() == XmlPullParser.END_TAG;
        } catch (XmlPullParserException xppX) {
            throw new InvalidFormatException(this.positionDescriptor(), xppX);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String elementName() {
        try {
            final int eventType = this.parser.getEventType();
            if (eventType == XmlPullParser.START_TAG || eventType == XmlPullParser.END_TAG) {
                return this.parser.getName();
            }
            throw new IllegalStateException("expected element start or end: " + this.parser.getLineNumber() + "," + this.parser.getColumnNumber());
        } catch (XmlPullParserException xppX) {
            throw new InvalidFormatException(this.positionDescriptor(), xppX);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String elementAttribute(String name) {
        try {
            if (parser.getEventType() == XmlPullParser.START_TAG) {
                return parser.getAttributeValue(null, name);
            }
            throw new IllegalStateException("not at element start at: " + parser.getLineNumber() + "," + parser.getColumnNumber());
        } catch (XmlPullParserException xppX) {
            throw new InvalidFormatException(this.positionDescriptor(), xppX);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String elementRequiredAttribute(String name) {
        try {
            if (parser.getEventType() == XmlPullParser.START_TAG) {

                final String value = this.parser.getAttributeValue(null, name);
                if (value == null) {
                    throw new InvalidFormatException(this.positionDescriptor(),
                            "element missing attribute: " + name);
                }
                return value;
            }
            throw new IllegalStateException("not at element start  at: "
                    + parser.getLineNumber() + "," + parser.getColumnNumber());
        } catch (XmlPullParserException xppX) {
            throw new InvalidFormatException(this.positionDescriptor(), xppX);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String readValue() {
        try {
            return parser.nextText();
        } catch (XmlPullParserException | IOException xppX) {
            throw new InvalidFormatException(this.positionDescriptor(), xppX);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void consumeFully() {
        try {
            // move up until easyml is parent:
            int et = parser.getEventType();
            while (!(et == XmlPullParser.END_TAG && parser.getDepth() == 2)
                    && et != XmlPullParser.END_DOCUMENT) {
                et = parser.next();
            }
            // move on sibling start tag:
            if (et != XmlPullParser.END_DOCUMENT) {
                parser.nextTag();
            }
        } catch (XmlPullParserException | IOException xppX) {
            throw new InvalidFormatException(this.positionDescriptor(), xppX);
        }
    }

    /**
     * Closes the underlying reader.
     */
    @Override
    public void close() {
        if (this.readerToClose != null) {
            try {
                this.readerToClose.close();
            } catch (IOException ignore) {
            }
        }
    }
}
