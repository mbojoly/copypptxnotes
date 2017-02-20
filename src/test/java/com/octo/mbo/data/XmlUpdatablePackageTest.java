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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

public class XmlUpdatablePackageTest {

    @Test
    public void getSlidesTest() {
        SlideDocument document = new SlideDocument();
        Slide s1 = new Slide();
        s1.setTitle("Title1");
        Slide s2 = new Slide();
        s2.setTitle("Title2");
        document.getSlides().add(s1);
        document.getSlides().add(s2);

        XmlUpdatablePackage xmlUpdatablePackage = new XmlUpdatablePackage(document);

        Map<String, Slide> slidesMap = xmlUpdatablePackage.getSlides();

        Assert.assertEquals(s1, slidesMap.get("Title1"));
        Assert.assertEquals(s2, slidesMap.get("Title2"));
        Assert.assertEquals(2, slidesMap.size());
    }

    @Test
    public void loadTest() throws CopyNotesException {
        XmlPackageFactory factory = new XmlPackageFactory();
        final String xmlTest = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<slides />";
        ByteArrayInputStream bais = new ByteArrayInputStream(xmlTest.getBytes());

        XmlUpdatablePackage xmlUpdatablePackage = factory.loadXml(bais);

        Assert.assertEquals(0, xmlUpdatablePackage.slideDocument.getSlides().size());
    }

    @Test
    public void saveChangesTest() throws CopyNotesException {
        SlideDocument slideDocument = new SlideDocument();
        Slide slide = new Slide();
        slide.setPartName("/partname1");
        slide.setTitle("Slide 1");
        slideDocument.getSlides().add(slide);
        XmlUpdatablePackage xmlUpdatablePackage = new XmlUpdatablePackage(slideDocument);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        xmlUpdatablePackage.saveChanges(baos);
        String xml = new String(baos.toByteArray());

        final String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<slides>\n" +
                "    <slide>\n" +
                "        <title>Slide 1</title>\n" +
                "        <partName>/partname1</partName>\n" +
                "        <notes/>\n" +
                "    </slide>\n" +
                "</slides>\n";

        Assert.assertEquals(expectedXml, xml);
    }
}
