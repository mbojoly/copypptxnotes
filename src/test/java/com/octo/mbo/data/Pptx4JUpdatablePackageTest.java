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

import com.octo.mbo.data.extracter.SlideExtractor;
import com.octo.mbo.data.extracter.SlideExtractorFactory;
import com.octo.mbo.data.updater.SlideUpdator;
import com.octo.mbo.domain.Slide;
import com.octo.mbo.exceptions.CopyNotesException;
import org.apache.commons.io.IOUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.junit.Assert;
import org.junit.Test;
import org.pptx4j.Pptx4jException;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;


public class Pptx4JUpdatablePackageTest {

    @Test
    public void loadPackageTest() throws CopyNotesException, InvalidFormatException {
        InputStream testFileStream = ClassLoader.getSystemResourceAsStream("pptx-test.pptx");

        Pptx4jPackageFactory factory = new Pptx4jPackageFactory();
        Pptx4jPackage pptx4jPackage = factory.loadPackage(null, testFileStream);
        Assert.assertEquals(26, pptx4jPackage.pMLpkg.getParts().getParts().size());
        Assert.assertNotNull(pptx4jPackage.pMLpkg.getParts().get(new PartName("/ppt/slides/slide1.xml")));
        Assert.assertNotNull(pptx4jPackage.pMLpkg.getParts().get(new PartName("/ppt/slides/slide2.xml")));
        Assert.assertNull(pptx4jPackage.pMLpkg.getParts().get(new PartName("/ppt/slides/slide3.xml")));
    }

    @Test
    public void loadPptxPartMapTest() throws Docx4JException, CopyNotesException {
        PresentationMLPackage pMLpkgSrc = new PresentationMLPackage();
        pMLpkgSrc.getParts().put(new SlidePart());

        Pptx4jPackage pptx4jPackage = new Pptx4jPackage(pMLpkgSrc, null);
        Map<PartName, Part> result = pptx4jPackage.getPptxPartMap();
        Assert.assertEquals(1, result.size());
    }

    @Test(expected = CopyNotesException.class)
    public void loadPptxPartMapWithNullElementTest() throws Docx4JException, CopyNotesException {
        PresentationMLPackage pMLpkgSrc = mock(PresentationMLPackage.class);
        when(pMLpkgSrc.getParts()).thenReturn(null);

        Pptx4jPackage pptx4jPackage = new Pptx4jPackage(pMLpkgSrc, null);
        pptx4jPackage.getPptxPartMap();
    }

    @Test
    public void saveChangesTest() throws CopyNotesException, InvalidFormatException, Pptx4jException, IOException, JAXBException {
        InputStream testFileStream = ClassLoader.getSystemResourceAsStream("pptx-test.pptx");
        byte[] inputBytes = IOUtils.toByteArray(testFileStream);
        //In order to read it again
        ByteArrayInputStream testStream = new ByteArrayInputStream(inputBytes);
        Pptx4jPackageFactory factory = new Pptx4jPackageFactory();
        Pptx4jPackage pptx4jPackage = factory.loadPackage(null, testStream);
        SlidePart slidePart = new SlidePart(new PartName("/ppt/slides/slide3.xml"));
        slidePart.setContents(SlidePart.createSld());
        pptx4jPackage.pMLpkg.getMainPresentationPart().addSlide(2, slidePart);
        //This pptx will be invalid but the test is ok if the output file is different from the input file
        ByteArrayOutputStream outputValue = new ByteArrayOutputStream();
        pptx4jPackage.saveChanges(outputValue);
        byte[] outputBytes = outputValue.toByteArray();
        Assert.assertNotEquals(inputBytes, outputBytes);

    }

    @Test
    public void updatePptxStructureTest() throws Docx4JException, CopyNotesException {

        SlideUpdator slideUpdatorMock = mock(SlideUpdator.class);

        SlidePart part = new SlidePart();
        String key = "/PartName";
        PartName partName = new PartName(key);
        PresentationMLPackage presentationMLPackage = new PresentationMLPackage();
        presentationMLPackage.getParts().getParts().putIfAbsent(partName, part);

        Map<String, Slide> slidesPerPartName = new HashMap<>();
        List<String> paragraphs = Collections.singletonList("paragraph");
        slidesPerPartName.put(key, new Slide(key, "Title", paragraphs));

        Pptx4jUpdatablePackage pptx4jUpdatablePackage = new Pptx4jUpdatablePackage(slideUpdatorMock, null, presentationMLPackage);

        pptx4jUpdatablePackage.updateStructure(slidesPerPartName);

        verify(slideUpdatorMock).processEntry(part, paragraphs);
    }

    @Test
    public void extractCommentTest() throws Docx4JException, CopyNotesException {
        SlideExtractor slideExtractorMock = mock(SlideExtractor.class);
        SlideExtractorFactory slideExtractorFactoryMock = mock(SlideExtractorFactory.class);

        when(slideExtractorFactoryMock.build()).thenReturn(slideExtractorMock);

        PresentationMLPackage presentationMLPackage = new PresentationMLPackage();
        presentationMLPackage.getParts().getParts().putIfAbsent(new PartName("/PartName1"), new SlidePart());
        presentationMLPackage.getParts().getParts().putIfAbsent(new PartName("/PartName2"), new SlidePart());

        Pptx4jPackage pptx4jPackage = new Pptx4jPackage(presentationMLPackage, slideExtractorFactoryMock);

        pptx4jPackage.getSlides();

        verify(slideExtractorMock, times(2)).processNoteEntry(any(Map.Entry.class));
    }

    @Test()
    public void extractNoteCheckReturnDifferentExtractors() throws CopyNotesException, Docx4JException {
        SlideExtractorFactory sefMock = mock(SlideExtractorFactory.class);
        SlideExtractor seMock = mock(SlideExtractor.class);
        when(sefMock.build()).thenReturn(seMock);

        PresentationMLPackage presentationMLPackage = new PresentationMLPackage();
        presentationMLPackage.getParts().getParts().putIfAbsent(new PartName("/PartName1"), new SlidePart());
        presentationMLPackage.getParts().getParts().putIfAbsent(new PartName("/PartName2"), new SlidePart());

        Pptx4jPackage pptx4jPackage = new Pptx4jPackage(presentationMLPackage, sefMock);
        pptx4jPackage.getSlides();
        pptx4jPackage.getSlides();

        verify(sefMock, times(2)).build();

    }
}
