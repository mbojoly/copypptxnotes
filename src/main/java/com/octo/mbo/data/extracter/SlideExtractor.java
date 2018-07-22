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

import com.octo.mbo.exceptions.CopyNotesException;
import com.octo.mbo.domain.Slide;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlideExtractor {

    private static Logger log = LoggerFactory.getLogger(SlideExtractor.class);
    final Map<String, Slide> slides = new HashMap<>();

    /**
     * Key: First string in the slide
     * Value: Slide object
     *
     * @return Map
     */
    public Map<String, Slide> getSlides() {
        return Collections.unmodifiableMap(slides);
    }

    public void processNoteEntry(Map.Entry<PartName, Part> hme) throws CopyNotesException {
        if (hme == null) {
            throw new CopyNotesException("HashMap entry of a document part is null");
        }
        final PartName tgtPartName = hme.getKey();
        if (tgtPartName == null) {
            throw new CopyNotesException("Key of HashMap entry of a document part is null");
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
            throw new CopyNotesException("Error extracting comments, {}", docx4jEx);
        }
    }

    /**
     * Add the slide by creating a key from the title (first string found) and
     * if required the part name (identifier of the slide which contains the slide number)
     * @param tgtPartName Identifier of the slide e.g. "/ppt/slides/slide4.xml"
     * @param slideSrc : Content of the slide
     * @param firstString : First String found in the slide structure. Expected to be the title
     * @throws Docx4JException throw by the library
     */
    void addToListOfSlides(PartName tgtPartName, SlidePart slideSrc, String firstString) throws Docx4JException {
        String nonNullFirstString = firstString == null ? "" : firstString;
        List<String> notesParagraphs = extractParagraphsOfComments(slideSrc);
        logParagraphs(notesParagraphs);

        Slide s = new Slide(tgtPartName.getName(), nonNullFirstString, notesParagraphs);

        Slide alreadyExist = slides.putIfAbsent(nonNullFirstString, s);
        if (alreadyExist != null) {
            String existingPartName = alreadyExist.getPartName();
            String newPartName = s.getPartName();
            Optional<Short> existingSlideIdx = extractSlideIdFromPartName(existingPartName);
            Optional<Short> newSlideIdx = extractSlideIdFromPartName(newPartName);
            if(existingSlideIdx.isPresent() && newSlideIdx.isPresent()) {
                int delta = newSlideIdx.get() - existingSlideIdx.get();
                nonNullFirstString = nonNullFirstString + "+" + delta;
            } else {
                nonNullFirstString = nonNullFirstString + "+" + newPartName;
            }
            Slide secondTryAlreadyExist = slides.putIfAbsent(nonNullFirstString, s);
            if(secondTryAlreadyExist != null) {
                log.warn("The slide '{}' is already associated with the key '{}' and partName '{}'",
                        s.getPartName(),
                        nonNullFirstString,
                        secondTryAlreadyExist.getPartName());
            } else {
                log.info("The slide '{}' has been associated with the key '{}' because initial key " +
                                "is already associated with partName '{}'",
                        s.getPartName(),
                        nonNullFirstString,
                        alreadyExist.getPartName());
            }
        }
    }

    private static final Pattern p = Pattern.compile("^/ppt/slides/slide([0-9]+).xml");

    private Optional<Short> extractSlideIdFromPartName(String partName) {
        // create matcher for pattern p and given string
        Matcher m = p.matcher(partName);
        if(m.find()) {
            return Optional.of(Short.parseShort(m.group(1)));
        } else {
            return Optional.empty();
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

        List<String> result = new ArrayList<>();
        if((partSrc != null) && (partSrc.getNotesSlidePart() != null)) {
            Notes notesSrc = partSrc.getNotesSlidePart().getContents();
            GroupShape shapeNotesSrc = notesSrc.getCSld().getSpTree();
            //Append each paragraph content in the list of paragraphs
            Appender<String, List<String>> paragraphAppender = new ParagraphAppender();

            doOnEachParagraph(shapeNotesSrc, paragraphAppender);
            result = paragraphAppender.getContent();
        }
        return result;
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

    Optional<String> extractFirstString(SlidePart slide) throws CopyNotesException {
        if (slide == null || slide.getResolvedLayout() == null || slide.getResolvedLayout().getShapeTree() == null) {
            throw new CopyNotesException("A part of the shape tree of the slide is null");
        }

        Appender<String, Optional<String>> firstOneExtractor = new FirstStringAppender();

        doOnEachParagraph(slide.getResolvedLayout().getShapeTree(), firstOneExtractor);

        return firstOneExtractor.getContent();
    }

    static class FirstStringAppender implements Appender<String, Optional<String>> {
        public static final int MIN_FIRSTSTRING_LENGTH = 8;
        String firstString;

        @Override
        public Optional<String> getContent() {
            return firstString == null ? Optional.empty() : Optional.of(firstString);
        }
        @Override
        public void accept(String s) {
            //Sometimes the title is not the first string that is parsed
            //Sometimes there is no title in the slide
            //MIN_FIRSTSTRING_LENGTH is a threshold  to try only once to find a String that has a chance to be the title
            if (firstString == null) {
                firstString = s;
            } else if(firstString.length() < MIN_FIRSTSTRING_LENGTH) {
                firstString = firstString + " " + s;
            } else if(s != null && s.length() > MIN_FIRSTSTRING_LENGTH && s.length() > firstString.length()) {
                firstString = s;
            }
            log.trace("accept finished, firstString is now {}", firstString);
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
