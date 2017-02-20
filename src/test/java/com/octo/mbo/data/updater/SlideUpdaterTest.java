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
package com.octo.mbo.data.updater;


import com.octo.mbo.exceptions.CopyNotesException;
import org.docx4j.dml.*;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.PresentationML.NotesSlidePart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.pptx4j.pml.GroupShape;
import org.pptx4j.pml.Shape;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({SlidePart.class, NotesSlidePart.class})
public class SlideUpdaterTest {
    @Test
    public void createNewCTTextParagraphTest() {
        List<String> newParagraphs = Arrays.asList("Paragraph 1", "Paragraph 2", "Paragraph 3");
        CTTextParagraphProperties paragraphProperties = new CTTextParagraphProperties();
        CTTextCharacterProperties characterProperties = new CTTextCharacterProperties();
        CTTextParagraph referCtTextParagraph = new CTTextParagraph();
        referCtTextParagraph.setEndParaRPr(characterProperties);
        referCtTextParagraph.setPPr(paragraphProperties);

        SlideUpdator slideUpdator = new SlideUpdator();
        List<CTTextParagraph> result = slideUpdator.createNewCTTextParagraphs(newParagraphs, referCtTextParagraph);

        Assert.assertEquals(3, result.size());

        for (int i = 0; i < 3; i++) {
            CTTextParagraph ctTextParagraph = result.get(i);
            Assert.assertEquals(1, ctTextParagraph.getEGTextRun().size());
            Assert.assertEquals("Paragraph " + (i + 1), ((CTRegularTextRun) ctTextParagraph.getEGTextRun().get(0)).getT());
            Assert.assertEquals(paragraphProperties, result.get(0).getPPr());
            Assert.assertEquals(characterProperties, result.get(0).getEndParaRPr());
        }
    }

    @Test
    public void updateSlideTest() throws CopyNotesException {
        SlideUpdator slideUpdatorMock = PowerMockito.spy(new SlideUpdator());

        CTTextParagraph referenCtTextParagraph = new CTTextParagraph();
        CTTextParagraph anotherParagraph = new CTTextParagraph();
        CTTextBody ctTextBody = new CTTextBody();
        ctTextBody.getP().add(referenCtTextParagraph);
        ctTextBody.getP().add(anotherParagraph);
        Shape shape = new Shape();
        shape.setTxBody(ctTextBody);
        GroupShape groupShape = new GroupShape();
        groupShape.getSpOrGrpSpOrGraphicFrame().add(shape);

        List<String> paragraphs = Arrays.asList("Paragraph 1", "Pragraph 2", "Paragraph 3");

        List<CTTextParagraph> ctTextParagraphs = paragraphs
                .stream()
                .map(
                        p -> {
                            CTTextParagraph ctp = new CTTextParagraph();
                            CTRegularTextRun textRun = new CTRegularTextRun();
                            textRun.setT(p);
                            ctp.getEGTextRun().add(textRun);
                            return ctp;
                        })
                .collect(
                        Collectors.toList()
                );

        doReturn(ctTextParagraphs).when(slideUpdatorMock).createNewCTTextParagraphs(anyListOf(String.class), any(CTTextParagraph.class));

        slideUpdatorMock.updateSlide(groupShape, paragraphs);

        Assert.assertEquals(3, ctTextBody.getP().size());
        //The 2 first ones are the automatically generated paragraphs by the structure
        Assert.assertEquals(((CTRegularTextRun) ctTextParagraphs.get(0).getEGTextRun().get(0)).getT(), ((CTRegularTextRun) ctTextBody.getP().get(0).getEGTextRun().get(0)).getT());
        Assert.assertEquals(((CTRegularTextRun) ctTextParagraphs.get(1).getEGTextRun().get(0)).getT(), ((CTRegularTextRun) ctTextBody.getP().get(1).getEGTextRun().get(0)).getT());
        Assert.assertEquals(((CTRegularTextRun) ctTextParagraphs.get(2).getEGTextRun().get(0)).getT(), ((CTRegularTextRun) ctTextBody.getP().get(2).getEGTextRun().get(0)).getT());
    }

    @Test
    public void processEntryTest() throws Docx4JException, CopyNotesException {
        List<String> paragraphs = Arrays.asList("Paragraph 1", "Paragraph 2", "Paragraph 3");
        SlidePart slidePartMock = mock(SlidePart.class, RETURNS_DEEP_STUBS);
        NotesSlidePart notesSlidePartMock = mock(NotesSlidePart.class, RETURNS_DEEP_STUBS);
        GroupShape groupShape = mock(GroupShape.class);
        when(slidePartMock
                .getNotesSlidePart()
        ).thenReturn(notesSlidePartMock);

        when(notesSlidePartMock
                .getContents()
                .getCSld()
                .getSpTree()
        ).thenReturn(groupShape);

        SlideUpdator slideUpdatorMockInjected = mock(SlideUpdator.class);
        SlideUpdator slideUpdator = new SlideUpdator(slideUpdatorMockInjected);
        slideUpdator.processEntry(slidePartMock, paragraphs);

        verify(slideUpdatorMockInjected).updateSlide(any(GroupShape.class), anyListOf(String.class));
    }
}
