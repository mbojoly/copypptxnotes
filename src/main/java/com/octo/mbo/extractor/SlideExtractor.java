/**
 * Copyright (C) 2017 mbojoly (mbojoly@octo.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.octo.mbo.extractor;

import com.octo.mbo.exceptions.CopyCommentException;
import com.octo.mbo.xml.Slide;
import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.pml.GroupShape;
import org.pptx4j.pml.Notes;
import org.pptx4j.pml.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SlideExtractor {

    private static Logger log = LoggerFactory.getLogger(SlideExtractor.class);
    private final Map<String, Slide> slides = new HashMap<>();

    public Map<String, Slide> getSlides() {
        return Collections.unmodifiableMap(slides);
    }

    public void processNoteEntry(Map.Entry<PartName, Part> hme) throws CopyCommentException {
        if (hme == null) {
            throw new CopyCommentException("HashMap entry of a document part is null");
        }
        final PartName tgtPartName = hme.getKey();
        if (tgtPartName == null) {
            throw new CopyCommentException("Key of HashMap entry of a document part is null");
        }

        try {

            Part partSrc = hme.getValue();

            if (partSrc != null && partSrc instanceof SlidePart) {
                SlidePart slideSrc = (SlidePart) partSrc;

                Optional<String> optFirstString = extractFirstString(slideSrc);

                if (!optFirstString.isPresent()) {
                    log.warn("Slide {} has no first string in the layout. This slide has been ignored.", tgtPartName);
                } else {
                    final String firstString = optFirstString.get();
                    log.debug("First String of {} is {}", tgtPartName, firstString);
                    addToListOfSlides(tgtPartName, slideSrc, firstString);
                }
            }
        } catch (Docx4JException docx4jEx) {
            throw new CopyCommentException("Error extracting comments, {}", docx4jEx);
        }
    }

    private void addToListOfSlides(PartName tgtPartName, SlidePart slideSrc, String firstString) throws Docx4JException {
        List<String> notesParagraphs = extractParagraphsOfComments(slideSrc);

        logParagraphs(notesParagraphs);

        Slide s = new Slide(tgtPartName.getName(), firstString, notesParagraphs);

        Slide alreadyExist = slides.putIfAbsent(firstString, s);
        if (alreadyExist != null) {
            log.warn("The slide {} is already associated with the key {} ", alreadyExist.getPartName(), firstString);
        }
    }

    private void logParagraphs(List<String> notesParagraphs) {
        if (log.isTraceEnabled()) {
            log.trace("List of notes paragraph");
            for (String s : notesParagraphs) {
                log.trace(s);
            }
        }
    }

    List<String> extractParagraphsOfComments(SlidePart partSrc) throws Docx4JException {

        Notes notesSrc = partSrc.getNotesSlidePart().getContents();
        GroupShape shapeNotesSrc = notesSrc.getCSld().getSpTree();
        //Append each paragraph content in the list of paragraphs
        Appender<String, List<String>> paragraphAppender = new ParagraphAppender();

        doOnEachParagraph(shapeNotesSrc, paragraphAppender);
        return paragraphAppender.getContent();
    }

    <T> void doOnEachParagraph(GroupShape shapeNotesSrc, Appender<String, T> paragraphAppender) {
        for (Object o : shapeNotesSrc.getSpOrGrpSpOrGraphicFrame()) {
            if (o != null && o instanceof Shape) {
                CTTextBody txBody = ((Shape) o).getTxBody();
                doOnBody(paragraphAppender, txBody);
            }
        }
    }

    private <T> void doOnBody(Appender<String, T> paragraphAppender, CTTextBody txBody) {
        if (txBody != null) {
            for (CTTextParagraph tp : txBody.getP()) {
                final StringBuilder parContent = concatenateCTRegularTextRun(tp);
                paragraphAppender.accept(parContent.toString());
            }
        }
    }

    private StringBuilder concatenateCTRegularTextRun(CTTextParagraph tp) {
        StringBuilder parContent1 = new StringBuilder();
        if (tp != null) {
            List<Object> textRuns = tp.getEGTextRun();
            if (textRuns != null) {
                for (Object otr : textRuns) {
                    StringBuilder builder = readCTRegularTextRun(otr);
                    parContent1.append(builder);
                }
            }
        }
        return parContent1;
    }

    private StringBuilder readCTRegularTextRun(Object otr) {
        StringBuilder builder = new StringBuilder();
        if (otr != null && otr instanceof CTRegularTextRun) {
            CTRegularTextRun tr = (CTRegularTextRun) otr;
            final String txt = tr.getT();
            builder.append(txt);
        }
        return builder;
    }

    Optional<String> extractFirstString(SlidePart slide) throws CopyCommentException {
        if (slide == null || slide.getResolvedLayout() == null || slide.getResolvedLayout().getShapeTree() == null) {
            throw new CopyCommentException("A part of the shape tree of the slide is null");
        }

        Appender<String, Optional<String>> firstOneExtractor = new FirstStringAppender();

        doOnEachParagraph(slide.getResolvedLayout().getShapeTree(), firstOneExtractor);

        return firstOneExtractor.getContent();
    }

    static class FirstStringAppender implements Appender<String, Optional<String>> {
        String firstString;

        @Override
        public Optional<String> getContent() {
            return firstString == null ? Optional.empty() : Optional.of(firstString);
        }

        @Override
        public void accept(String s) {
            if (firstString == null) {
                firstString = s;
            }
        }
    }

    static class ParagraphAppender implements Appender<String, List<String>> {
        List<String> appender = new ArrayList<>();

        @Override
        public List<String> getContent() {
            return appender;
        }

        @Override
        public void accept(String s) {
            appender.add(s);
        }
    }
}
