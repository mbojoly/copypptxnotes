/**
 * Copyright (C) 2017 mbojoly (mbojoly@octo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.octo.mbo.data;

import com.octo.mbo.domain.Slide;
import com.octo.mbo.domain.SlideDocument;
import com.octo.mbo.exceptions.CopyNotesException;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.util.Arrays;
import java.util.List;

public class JAXBMashallerTest {

    private static final String EXPECTED_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<slides>\n" +
            "    <slide>\n" +
            "        <title>Slide 1</title>\n" +
            "        <partName>/partname1</partName>\n" +
            "        <notes>\n" +
            "            <p>Paragraph 1</p>\n" +
            "            <p>Paragraph 2</p>\n" +
            "            <p>Paragraph 3</p>\n" +
            "        </notes>\n" +
            "    </slide>\n" +
            "    <slide>\n" +
            "        <title>Slide 2</title>\n" +
            "        <partName>/partname2</partName>\n" +
            "        <notes>\n" +
            "            <p>Paragraph 1</p>\n" +
            "            <p>Paragraph 2</p>\n" +
            "            <p>Paragraph 3</p>\n" +
            "        </notes>\n" +
            "    </slide>\n" +
            "    <slide>\n" +
            "        <title>Slide 3</title>\n" +
            "        <partName>/partname3</partName>\n" +
            "        <notes>\n" +
            "            <p>Paragraph 1</p>\n" +
            "            <p>Paragraph 2</p>\n" +
            "            <p>Paragraph 3</p>\n" +
            "        </notes>\n" +
            "    </slide>\n" +
            "</slides>\n";

    @Test
    public void marshallTest() throws JAXBException, CopyNotesException {
        List<Slide> slides = Arrays.asList(
                new Slide("/partname1", "Slide 1", Arrays.asList("Paragraph 1", "Paragraph 2", "Paragraph 3")),
                new Slide("/partname2", "Slide 2", Arrays.asList("Paragraph 1", "Paragraph 2", "Paragraph 3")),
                new Slide("/partname3", "Slide 3", Arrays.asList("Paragraph 1", "Paragraph 2", "Paragraph 3"))
        );

        SlideDocument document = new SlideDocument(slides);

        String result = JAXBMarshaller.marshall(document);

        Assert.assertEquals(EXPECTED_STRING, result);
    }

    @Test
    public void unmarshallTest() throws JAXBException, CopyNotesException {
        Object unmarshalledObject = JAXBMarshaller.unmarshall(SlideDocument.class, EXPECTED_STRING);

        Assert.assertNotNull(unmarshalledObject);
        Assert.assertTrue(unmarshalledObject instanceof SlideDocument);
        SlideDocument slideDocument = (SlideDocument) unmarshalledObject;

        Assert.assertEquals(3, slideDocument.getSlides().size());
        Slide s = slideDocument.getSlides().get(0);
        Assert.assertEquals("/partname1", s.getPartName());
        Assert.assertEquals("Slide 1", s.getTitle());
        Assert.assertEquals(3, s.getParagraphs().size());
        Assert.assertEquals("Paragraph 1", s.getParagraphs().get(0));
    }
}
