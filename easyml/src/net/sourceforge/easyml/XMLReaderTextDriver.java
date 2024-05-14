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

import net.sourceforge.easyml.util.XMLUtil;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;

/**
 * XMLReaderTextDriver class is the XML reader driver implementation for reading
 * XML text from input streams, pull-parse style. The default pull-parser
 * implementation is {@linkplain KXmlParser}, but other
 * {@linkplain XmlPullParser} implementations can be specified as construction
 * parameters.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.6.1
 * @since 1.1.0
 */
final class XMLReaderTextDriver extends XMLReader.Driver {

    private XmlPullParser parser;
    private Reader readerToClose;

    /**
     * Creates a new instance.
     *
     * @param target to use and be used by
     * @param in     to read from
     */
    public XMLReaderTextDriver(XMLReader target, Reader in) {
        this(target, in, new KXmlParser());// single LOC depending on kXML2.
    }

    /**
     * Creates a new instance.
     *
     * @param target to use and be used by
     * @param in     to read from
     * @param parser user defined parser class instance
     */
    public XMLReaderTextDriver(XMLReader target, Reader in, XmlPullParser parser) {
        super(target);
        this.parser = parser;
        this.init(in);
    }

    private void init(Reader in) {
        try {
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
                return XMLUtil.unescapeXMLTag(this.parser.getName());
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
     * Resets this current instance to read using the given reader and,
     * optionally, to parse using the given custom pull-parser. This method is
     * needed for performance reasons since the pull-parsers can be expensive to
     * recreate and should be reused.
     *
     * @param in     the required input reader
     * @param parser optional custom parser to replace the default pull-parse
     *               implementation
     */
    public void reset(Reader in, XmlPullParser parser) {
        if (parser != null) {
            this.parser = parser;
        }
        this.init(in);
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
