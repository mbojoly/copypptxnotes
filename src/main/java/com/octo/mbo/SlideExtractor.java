package com.octo.mbo;

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

class SlideExtractor {

    private static Logger log = LoggerFactory.getLogger(SlideExtractor.class);
    private final Map<String, Slide> slides = new HashMap<>();

    Map<String, Slide> getSlides() {
        return Collections.unmodifiableMap(slides);
    }

    void processEntry(Map.Entry<PartName, Part> hme) throws CopyCommentException, Docx4JException {
        if (hme == null) {
            throw new CopyCommentException("HashMap entry of a document part is null");
        }
        final PartName tgtPartName = hme.getKey();
        if (tgtPartName == null) {
            throw new CopyCommentException("Key of HashMap entry of a document part is null");
        }

        Part partSrc = hme.getValue();

        if (hme.getValue() instanceof SlidePart) {

            if (partSrc != null && partSrc instanceof SlidePart) {
                SlidePart slideSrc = (SlidePart) partSrc;

                Optional<String> optFirstString = extractFirstString(slideSrc);

                if(!optFirstString.isPresent()) {
                    log.warn("Slide {} has no first string in the layout. This slide has been ignored.", tgtPartName);
                }
                else  {
                    final String firstString = optFirstString.get();


                    log.debug("First String of {} is {}", tgtPartName, firstString);

                    List<String> notesParagraphs = extractParagraphsOfComments(slideSrc);

                    log.debug("List of notes paragraph");
                    for(String s : notesParagraphs) {
                        log.debug(s);
                    }

                    Slide s = new Slide(tgtPartName.getName(), firstString, notesParagraphs);

                    Slide alreadyExist = slides.putIfAbsent(firstString, s);
                    if(alreadyExist != null) {
                        log.warn("The slide {} is already associated with the key {} ", alreadyExist.getPartName(), firstString);
                    }

                }
            }

        }
    }

    private List<String> extractParagraphsOfComments(SlidePart partSrc) throws Docx4JException {

        Notes notesSrc = partSrc.getNotesSlidePart().getContents();
        GroupShape shapeNotesSrc = notesSrc.getCSld().getSpTree();
        //Append each paragraph content in the list of paragraphs
        Appender<String, List<String>> paragraphAppender = new Appender<String, List<String>>() {
            List<String> appender = new ArrayList<>();
            @Override
            public List<String> getContent() {
                return appender;
            }

            @Override
            public void accept(String s) {
                appender.add(s);
            }
        };

        doOnEachParagraph(shapeNotesSrc, paragraphAppender);
        return paragraphAppender.getContent();
    }

    private <T> void doOnEachParagraph(GroupShape shapeNotesSrc, Appender<String, T> paragraphAppender) {
        for (Object o : shapeNotesSrc.getSpOrGrpSpOrGraphicFrame()) {
            if (o != null) {
                if (o instanceof Shape) {
                    CTTextBody txBody = ((Shape) o).getTxBody();
                    if (txBody != null) {
                        for (CTTextParagraph tp : txBody.getP()) {
                            if (tp != null) {
                                final StringBuilder parContent = new StringBuilder();
                                List<Object> textRuns = tp.getEGTextRun();
                                if (textRuns != null) {
                                    for (Object otr : textRuns) {
                                        if (otr != null && otr instanceof CTRegularTextRun) {
                                            CTRegularTextRun tr = (CTRegularTextRun) otr;
                                            final String txt = tr.getT();
                                            log.trace("Reading {}", txt);
                                            parContent.append(txt);
                                        }
                                    }
                                }
                                paragraphAppender.accept(parContent.toString());
                            }
                        }
                    }
                }
            }
        }
    }

    private Optional<String> extractFirstString(SlidePart slide) throws CopyCommentException {
        if(slide == null || slide.getResolvedLayout() == null || slide.getResolvedLayout().getShapeTree() == null) {
            throw new CopyCommentException("A part of the shape tree of the slide is null");
        }

        Appender<String, Optional<String>> firstOneExtractor = new Appender<String, Optional<String>>() {
            String firstString;

            @Override
            public Optional<String> getContent() {
                return firstString == null ? Optional.empty() : Optional.of(firstString) ;
            }

            @Override
            public void accept(String s) {
                if(firstString == null) firstString = s;
            }
        };

        doOnEachParagraph(slide.getResolvedLayout().getShapeTree() , firstOneExtractor);

        return firstOneExtractor.getContent();
    }
}
