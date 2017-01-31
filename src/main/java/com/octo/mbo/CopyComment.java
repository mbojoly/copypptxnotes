package com.octo.mbo;

import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.pml.GroupShape;
import org.pptx4j.pml.Notes;
import org.pptx4j.pml.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * @author jharrop
 */
public class CopyComment {

    private static Logger log = LoggerFactory.getLogger(CopyComment.class);
    private static final String TARGETFILE_PATH = System.getProperty("user.dir") + "/sample-docs/pptx-copy.pptx";
    private static final String SRCFILE_PATH = System.getProperty("user.dir") + "/sample-docs/pptx-test.pptx";

    public static void main(String[] args) {

        try {
            PresentationMLPackage pMLpkgSrc = loadSourcePckge(SRCFILE_PATH);
            PresentationMLPackage pMLpkgTgt = loadTargetPckge(TARGETFILE_PATH);

            if (pMLpkgSrc == null ||
                    pMLpkgSrc.getParts() == null ||
                    pMLpkgSrc.getParts().getParts() == null) {
                throw new CopyCommentException("Source document tree has a null element. Exiting");
            }

            final HashMap<PartName, Part> hmSrc = pMLpkgSrc.getParts().getParts();
            final HashMap<PartName, Part> hmTgt = pMLpkgTgt.getParts().getParts();
            final int srcSize = hmSrc.size();
            final int tgtSize = hmTgt.size();

            if (srcSize != tgtSize) {
                throw new CopyCommentException(String.format("Source document has %s slides and target document %s.", srcSize, tgtSize));
            }
            log.debug("HashMap of documents parts have the same size {} for src, {} for target", srcSize, tgtSize);
            log.info("Processing HashMap of document parts side by side driven by the target...");

            for (HashMap.Entry<PartName, Part> hmeTgt : hmTgt.entrySet()) {
                processTargetEntry(hmSrc, hmeTgt);
            }

            log.info("Saving modified target...");

            // All done: save it
            pMLpkgTgt.save(new java.io.File(TARGETFILE_PATH));

            log.info("done .. saved " + TARGETFILE_PATH);
        } catch (Docx4JException d4jex) {
            log.error("Exception processing the document: {}. Exiting", d4jex);
            System.exit(-1);
        } catch (CopyCommentException ccex) {
            log.error("Internal error processing the document: {}. Exiting", ccex);
            System.exit(-1);
        }
    }

    private static void processTargetEntry(HashMap<PartName, Part> hmSrc, Map.Entry<PartName, Part> hmeTgt) throws CopyCommentException, Docx4JException {
        if (hmeTgt == null) {
            throw new CopyCommentException("HashMap entry of a document part is null");
        }
        final PartName tgtPartName = hmeTgt.getKey();
        if (tgtPartName == null) {
            throw new CopyCommentException("Key of HashMap entry of a document part is null");
        }

        Part partSrc = hmSrc.get(tgtPartName);

        if (hmeTgt.getValue() instanceof SlidePart) {
            SlidePart slideTgt = (SlidePart) hmeTgt.getValue();
            GroupShape shapeTgt = slideTgt.getResolvedLayout().getShapeTree();
            Notes notesTgt = slideTgt.getNotesSlidePart().getContents();
            GroupShape shapeNotesTgt = notesTgt.getCSld().getSpTree();

            if (partSrc != null && partSrc instanceof SlidePart) {
                SlidePart slideSrc = (SlidePart) partSrc;

                List<String> notesParagraphs = extractParagraphsOfComments(slideSrc);

                log.info("List of notes paragraph");
                for(String s : notesParagraphs) {
                    log.debug(s);
                }

                GroupShape shapeScr = slideSrc.getResolvedLayout().getShapeTree();
            }

        }
    }

    public static List<String> extractParagraphsOfComments(SlidePart partSrc) throws Docx4JException {
        final List<String> paragraphs = new ArrayList<>();

        Notes notesSrc = partSrc.getNotesSlidePart().getContents();
        GroupShape shapeNotesSrc = notesSrc.getCSld().getSpTree();

        for (Object o : shapeNotesSrc.getSpOrGrpSpOrGraphicFrame()) {
            if (o != null) {
                if (o instanceof Shape) {
                    CTTextBody txBody = ((Shape) o).getTxBody();
                    if (txBody != null) {
                        for (CTTextParagraph tp : txBody.getP()) {
                            if (tp != null) {
                                final StringBuffer parContent = new StringBuffer();
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
                                paragraphs.add(parContent.toString());
                            }
                        }
                    }
                }
            }
        }
        return paragraphs;
    }


    private static PresentationMLPackage loadTargetPckge(final String targetFilePath) throws Docx4JException {
        log.info("Loading target. {}..", targetFilePath);
        PresentationMLPackage pMLpkgTgt =
                (PresentationMLPackage) OpcPackage.load(new java.io.File(targetFilePath));
        log.info("Target {} loaded", targetFilePath);
        return pMLpkgTgt;
    }

    private static PresentationMLPackage loadSourcePckge(final String srcFilePath) throws Docx4JException {

        log.info("Loading source. {}..", srcFilePath);
        PresentationMLPackage pMLpkgSrc =
                (PresentationMLPackage) OpcPackage.load(new java.io.File(srcFilePath));
        log.info("Source {} loaded", srcFilePath);
        return pMLpkgSrc;
    }

    public static void processPart(Map.Entry<PartName, Part> hmeTgt, PartName tgtPartName) {
        log.debug("Processing {}", tgtPartName);
        final Part tgtPart = hmeTgt.getValue();
    }


    public static Optional<String> extractFirstString(PresentationMLPackage pMLPackage) throws org.docx4j.openpackaging.exceptions.Docx4JException {
        for (HashMap.Entry<PartName, Part> hme : pMLPackage.getParts().getParts().entrySet()) {
            final PartName partName = hme.getKey();

            if (hme.getValue() instanceof SlidePart) {
                SlidePart slide = (SlidePart) hme.getValue();
                GroupShape shape = slide.getResolvedLayout().getShapeTree();
                //TraverseSlide.doOnShape(shape, TraverseSlide::print);
            }
        }

        return Optional.empty();
    }


}