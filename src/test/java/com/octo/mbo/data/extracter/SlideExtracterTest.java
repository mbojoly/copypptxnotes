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
package com.octo.mbo.data.extracter;


import com.octo.mbo.domain.Slide;
import com.octo.mbo.exceptions.CopyNotesException;
import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.NotesSlidePart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.pptx4j.model.ResolvedLayout;
import org.pptx4j.pml.CommonSlideData;
import org.pptx4j.pml.GroupShape;
import org.pptx4j.pml.Notes;
import org.pptx4j.pml.Shape;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

//Warning : PowerMockito not compatible with JUnit5
@RunWith(PowerMockRunner.class)
@PrepareForTest({SlidePart.class, PartName.class, NotesSlidePart.class})
public class SlideExtracterTest {
    @Test
    public void doOnEachParagraphTest() {
        GroupShape shapeNotesMock = mock(GroupShape.class, RETURNS_DEEP_STUBS);
        Shape shapeMock = mock(Shape.class, RETURNS_DEEP_STUBS);
        when(shapeNotesMock.getSpOrGrpSpOrGraphicFrame()).thenReturn(Collections.singletonList(shapeMock));
        List<CTTextParagraph> textParagraphMocks = Stream.of("Sample mock text 1", "Sample mock text 2", "Sample mock text 3")
                .map(
                        s -> {
                            CTRegularTextRun textRun = new CTRegularTextRun();
                            textRun.setT(s);
                            return textRun;
                        }
                )
                .map(tr -> {
                    CTTextParagraph ctTextParagraphMock = mock(CTTextParagraph.class, RETURNS_DEEP_STUBS);
                    when(ctTextParagraphMock.getEGTextRun()).thenReturn(Collections.singletonList(tr));
                    return ctTextParagraphMock;
                })
                .collect(Collectors.toList());


        when(shapeMock.getTxBody().getP()).thenReturn(textParagraphMocks);


        //Verify seems to no work with PowerMock
        Appender<String, List<String>> appenderMock = new Appender<String, List<String>>() {
            List<String> storage = new ArrayList<>(3);

            @Override
            public List<String> getContent() {
                return storage;
            }

            @Override
            public void accept(String s) {
                storage.add(s);
            }
        };

        SlideExtractor extractor = new SlideExtractor();
        extractor.doOnEachParagraph(shapeNotesMock, appenderMock);

        Assert.assertArrayEquals(new String[]{"Sample mock text 1", "Sample mock text 2", "Sample mock text 3"},
                appenderMock.getContent().toArray(new String[0]));
    }

    @Test
    public void FirstStringAppenderTest() {
        SlideExtractor.FirstStringAppender firstStringAppender = new SlideExtractor.FirstStringAppender();

        firstStringAppender.accept("String 1");
        firstStringAppender.accept("String 2");

        Assert.assertEquals(Optional.of("String 1"), firstStringAppender.getContent());
    }

    @Test
    public void extractFirstStringTest() throws CopyNotesException {
        SlidePart slidePartMock = mock(SlidePart.class, RETURNS_DEEP_STUBS);
        SlideExtractor slideExtractor = PowerMockito.spy(new SlideExtractor());
        slideExtractor.extractFirstString(slidePartMock);
        verify(slideExtractor, times(1)).doOnEachParagraph(anyObject(), any(SlideExtractor.FirstStringAppender.class));
    }

    @Test
    public void paragraphAppenderTest() {
        SlideExtractor.ParagraphAppender paragraphAppender = new SlideExtractor.ParagraphAppender();

        paragraphAppender.accept("String 1");
        paragraphAppender.accept("String 2");

        Assert.assertArrayEquals(new String[]{"String 1", "String 2"}, paragraphAppender.getContent().toArray(new String[0]));
    }

    @Test
    public void extractParagraphOfCommentsTest() throws CopyNotesException, Docx4JException {
        SlidePart slidePartMock = mock(SlidePart.class, RETURNS_DEEP_STUBS);
        NotesSlidePart notesSlidePartMock = mock(NotesSlidePart.class, RETURNS_DEEP_STUBS);
        Notes notesMock = mock(Notes.class, RETURNS_DEEP_STUBS);
        doReturn(notesMock).when(notesSlidePartMock).getContents();
        doReturn(notesSlidePartMock).when(slidePartMock).getNotesSlidePart();
        SlideExtractor slideExtractor = PowerMockito.spy(new SlideExtractor());
        slideExtractor.extractParagraphsOfComments(slidePartMock);
        verify(slideExtractor, times(1)).doOnEachParagraph(anyObject(), any(SlideExtractor.ParagraphAppender.class));
    }

    @Test
    public void processEntryTest() throws CopyNotesException, Docx4JException {
        SlideExtractor slideExtractor = PowerMockito.spy(new SlideExtractor());
        Map<PartName, Part> partMap = new HashMap<>();
        PartName partNameMock = mock(PartName.class, RETURNS_DEEP_STUBS);
        SlidePart slidePartMock = mock(SlidePart.class, RETURNS_DEEP_STUBS);
        ResolvedLayout resolvedLayoutMock = mock(ResolvedLayout.class, RETURNS_DEEP_STUBS);
        GroupShape groupShapeMock = mock(GroupShape.class, RETURNS_DEEP_STUBS);
        doReturn(groupShapeMock).when(resolvedLayoutMock).getShapeTree();
        doReturn(resolvedLayoutMock).when(slidePartMock).getResolvedLayout();
        doReturn(Optional.of("Sample first string")).when(slideExtractor).extractFirstString(any(SlidePart.class));
        NotesSlidePart notesSlidePartMock = mock(NotesSlidePart.class, RETURNS_DEEP_STUBS);
        Notes notesMock = mock(Notes.class, RETURNS_DEEP_STUBS);
        doReturn(notesMock).when(notesSlidePartMock).getContents();
        doReturn(notesSlidePartMock).when(slidePartMock).getNotesSlidePart();
        CommonSlideData commonSlideDataMock = mock(CommonSlideData.class, RETURNS_DEEP_STUBS);
        doReturn(groupShapeMock).when(commonSlideDataMock).getSpTree();
        doReturn(commonSlideDataMock).when(notesMock).getCSld();
        partMap.put(partNameMock, slidePartMock);
        slideExtractor.processNoteEntry(partMap.entrySet().iterator().next());
        //verify(slideExtractor, times(1)).extractFirstString(any(SlidePart.class));
        verify(slideExtractor, times(1)).extractParagraphsOfComments(any(SlidePart.class));
    }

    @Test
    public void addToListOfSlidesASlideWithSameFirstStringTest() throws Docx4JException {
        //Given
        SlideExtractor slideExtractor = PowerMockito.spy(new SlideExtractor());
        Map<PartName, Part> partMap = new HashMap<>();

        Slide slideMock = mock(Slide.class, RETURNS_DEEP_STUBS);
        doReturn("/ppt/slides/slide4.xml").when(slideMock).getPartName();
        String existingKey = "Title";
        slideExtractor.slides.put(existingKey, slideMock);
        when(slideExtractor.extractParagraphsOfComments(any(SlidePart.class))).thenReturn(new ArrayList<>());
        PartName partNameMock = mock(PartName.class, RETURNS_DEEP_STUBS);
        doReturn("/ppt/slides/slide6.xml").when(partNameMock).getName();
        SlidePart slidePartMock = mock(SlidePart.class, RETURNS_DEEP_STUBS);
        //When
        slideExtractor.addToListOfSlides(partNameMock, slidePartMock, existingKey);
        //Then
        assertThat(slideExtractor.slides.keySet(), containsInAnyOrder(existingKey, existingKey + "+2"));
    }

    @Test
    public void addToListOfSlidesASlideWithSameNullFirstStringTestAndUnexpectedPartName() throws Docx4JException {
        //Given
        SlideExtractor slideExtractor = PowerMockito.spy(new SlideExtractor());
        Map<PartName, Part> partMap = new HashMap<>();

        Slide slideMock = mock(Slide.class, RETURNS_DEEP_STUBS);
        doReturn("foo").when(slideMock).getPartName();
        String existingKey = null;
        slideExtractor.slides.put(existingKey == null ? "" : existingKey, slideMock);
        when(slideExtractor.extractParagraphsOfComments(any(SlidePart.class))).thenReturn(new ArrayList<>());
        PartName partNameMock = mock(PartName.class, RETURNS_DEEP_STUBS);
        doReturn("bar").when(partNameMock).getName();
        SlidePart slidePartMock = mock(SlidePart.class, RETURNS_DEEP_STUBS);
        //When
        slideExtractor.addToListOfSlides(partNameMock, slidePartMock, existingKey);
        //Then
        assertThat(slideExtractor.slides.keySet(), containsInAnyOrder("", "+bar"));
    }
}
