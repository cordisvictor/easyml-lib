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

import net.sourceforge.easyml.marshalling.java.io.SerializableStrategy;
import net.sourceforge.easyml.marshalling.java.lang.ObjectStrategy;
import net.sourceforge.easyml.marshalling.java.util.ArrayListStrategy;
import net.sourceforge.easyml.testmodel.AbstractDTO;
import net.sourceforge.easyml.testmodel.PersonDTO;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * @author Victor Cordis ( cordis.victor at gmail.com)
 */
public class ExcludeDeserializeTest {

    @Test
    public void testExcludeDeserializeReader() throws Exception {
        final PersonDTO allValues = new PersonDTO(187, "fn", "ln");
        final PersonDTO expected = new PersonDTO(PersonDTO.DEFAULT_ID, PersonDTO.DEFAULT_FIRST_NAME, "ln");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document dom = dBuilder.newDocument();

        final XMLWriter xos = new XMLWriter(dom);
        xos.write(allValues);
        xos.close();

        final XMLReader xis = new XMLReader(dom);
        xis.exclude(AbstractDTO.class, "id");
        xis.exclude(PersonDTO.class, "firstName");
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testExcludeDeserializeObject() {
        final ModifiedPojo expected = new ModifiedPojo("someText");
        final String modifiedPojoXml = "<easyml><objectx id=\"1\" class=\"net.sourceforge.easyml.ExcludeDeserializeTest$ModifiedPojo\"><removedVersionField>1</removedVersionField><text>someText</text><removedNamesField><arraylst id=\"2\" size=\"2\"><string>e1</string><string>e2</string></arraylst></removedNamesField></objectx></easyml>";

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(modifiedPojoXml.getBytes()));
        xis.getCompositeStrategies().put(ObjectStrategy.NAME, ObjectStrategy.INSTANCE);
        xis.getCompositeStrategies().put(ArrayListStrategy.NAME, ArrayListStrategy.INSTANCE);
        xis.exclude(ModifiedPojo.class, "removedVersionField");
        xis.exclude(ModifiedPojo.class, "removedNamesField");
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testExcludeDeserializeSerializable() {
        final ModifiedDTO expected = new ModifiedDTO("someText");
        final String modifiedPojoXml = "<easyml><serial id=\"1\" class=\"net.sourceforge.easyml.ExcludeDeserializeTest$ModifiedDTO\"><this.fields><text>someText</text><removedVersionField>1</removedVersionField><removedPersonField><serial id=\"2\" class=\"net.sourceforge.easyml.testmodel.PersonDTO\"><this.fields><firstName nil=\"true\"/><lastName nil=\"true\"/></this.fields><this.fields><id>1</id></this.fields></serial></removedPersonField></this.fields></serial></easyml>\n";

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(modifiedPojoXml.getBytes()));
        xis.getCompositeStrategies().put(SerializableStrategy.NAME, new SerializableStrategy());
        xis.exclude(ModifiedDTO.class, "removedVersionField");
        xis.exclude(ModifiedDTO.class, "removedPersonField");
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testExcludeDeserializeSerializableDOM() throws Exception {
        final UnmodifiedDTO allValues = new UnmodifiedDTO("someText", 1, new PersonDTO(1, "fn", "ln"));
        final UnmodifiedDTO expected = new UnmodifiedDTO("someText", 0, null);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document dom = dBuilder.newDocument();

        final XMLWriter xos = new XMLWriter(dom);
        xos.getCompositeStrategies().add(new SerializableStrategy());
        xos.write(allValues);
        xos.close();

        final XMLReader xis = new XMLReader(dom);
        xis.getCompositeStrategies().put(SerializableStrategy.NAME, new SerializableStrategy());
        xis.exclude(UnmodifiedDTO.class, "versionField");
        xis.exclude(UnmodifiedDTO.class, "personField");
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testExcludeDeserializeEasyMlBuilder() {
        final ModifiedDTO expected = new ModifiedDTO("someText");
        final String oldXml = "<easyml><serial id=\"1\" class=\"net.sourceforge.easyml.ExcludeDeserializeTest$ModifiedDTO\"><this.fields><text>someText</text><removedVersionField>1</removedVersionField><removedPersonField><serial id=\"2\" class=\"net.sourceforge.easyml.testmodel.PersonDTO\"><this.fields><firstName nil=\"true\"/><lastName nil=\"true\"/></this.fields><this.fields><id>1</id></this.fields></serial></removedPersonField></this.fields></serial></easyml>\n";

        final EasyML eml = new EasyMLBuilder()
                .withExcludedName(ModifiedDTO.class, "removedVersionField")
                .withExcludedName(ModifiedDTO.class, "removedPersonField")
                .build();
        assertEquals(expected, eml.deserialize(oldXml));
    }

    public static final class ModifiedPojo {

        private String text;

        public ModifiedPojo(String text) {
            this.text = text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ModifiedPojo that = (ModifiedPojo) o;
            return Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text);
        }

        @Override
        public String toString() {
            return "ModifiedPojo{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }

    public static final class ModifiedDTO implements Serializable {

        private String text;

        public ModifiedDTO(String text) {
            this.text = text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ModifiedDTO that = (ModifiedDTO) o;
            return Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text);
        }

        @Override
        public String toString() {
            return "ModifiedDTO{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }

    public static final class UnmodifiedDTO implements Serializable {

        private String text;
        private int versionField;
        private PersonDTO personField;

        public UnmodifiedDTO(String text, int versionField, PersonDTO personField) {
            this.text = text;
            this.versionField = versionField;
            this.personField = personField;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UnmodifiedDTO other = (UnmodifiedDTO) o;
            return versionField == other.versionField && Objects.equals(text, other.text) && Objects.equals(personField, other.personField);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, versionField, personField);
        }

        @Override
        public String toString() {
            return "UnmodifiedDTO{" +
                    "text='" + text + '\'' +
                    ", versionField=" + versionField +
                    ", personField=" + personField +
                    '}';
        }
    }

}
