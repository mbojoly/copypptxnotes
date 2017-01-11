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
import org.docx4j.samples.AbstractSample;
import org.pptx4j.pml.GroupShape;
import org.pptx4j.pml.Notes;
import org.pptx4j.pml.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;


/**
 * @author jharrop
 *
 */
public class CopyComment {

    protected static Logger log = LoggerFactory.getLogger(CopyComment.class);

    public static void main(String[] args) throws Docx4JException {

        final String srcfilepath = System.getProperty("user.dir") + "/sample-docs/pptx-test.pptx";
        final String targetfilePath = System.getProperty("user.dir") + "/sample-docs/pptx-copy.pptx";

        log.info("Loading source. %s..", srcfilepath);
        PresentationMLPackage pMLpkgSrc =
                (PresentationMLPackage) OpcPackage.load(new java.io.File(srcfilepath));
        log.info("Source %s loaded", srcfilepath);

        log.info("Loading target. %s..", targetfilePath);
        PresentationMLPackage pMLpkgTgt =
                (PresentationMLPackage) OpcPackage.load(new java.io.File(targetfilePath));
        log.info("Target %s loaded", targetfilePath);

        final HashMap<PartName, Part> hmSrc = pMLpkgSrc.getParts().getParts();
        final HashMap<PartName, Part> hmTgt = pMLpkgTgt.getParts().getParts();
        final int srcSize = hmSrc.size();
        final int tgtSize = hmTgt.size();

        if(srcSize != tgtSize) {
            log.error("Source document has %s slides and target document %s. Exiting", srcSize, tgtSize);
            System.exit(-1);
        }

        log.info("Processing...");

        for (HashMap.Entry<PartName, Part> hmeTgt : hmTgt.entrySet()) {
            final PartName tgtPartName = hmeTgt.getKey();

            if(hmSrc.containsKey(tgtPartName)) {
                processPart(hmeTgt, tgtPartName);
            } else {
                log.warn("Src pptx does not contains %s. Entry has been ignored.", tgtPartName);
            }

            Part partSrc = hmSrc.get(tgtPartName);

            if (hmeTgt.getValue() instanceof SlidePart) {
                SlidePart slideTgt = (SlidePart) hmeTgt.getValue();
                GroupShape shapeTgt = slideTgt.getResolvedLayout().getShapeTree();
                Notes notesTgt = slideTgt.getNotesSlidePart().getContents();
                GroupShape shapeNotesTgt = notesTgt.getCSld().getSpTree();

                if (partSrc instanceof SlidePart) {
                    SlidePart slideSrc = (SlidePart) partSrc;
                    GroupShape shapeScr = slideSrc.getResolvedLayout().getShapeTree();
                    Notes notesSrc = slideSrc.getNotesSlidePart().getContents();
                    GroupShape shapeNotesSrc = notesSrc.getCSld().getSpTree();

                    for (Object o : shapeNotesSrc.getSpOrGrpSpOrGraphicFrame()) {
                        if (o instanceof Shape) {
                            CTTextBody txBody = ((Shape) o).getTxBody();
                            if (txBody != null) {
                                for (CTTextParagraph tp : txBody.getP()) {
                                    if (tp != null) {
                                        List<Object> textRuns = tp.getEGTextRun();
                                        for (Object otr : textRuns) {
                                            if (otr instanceof CTRegularTextRun) {
                                                CTRegularTextRun tr = (CTRegularTextRun) otr;
                                                processPart(tr.getT(), );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }






        log.info("Saving modified target...");

        // All done: save it
        pMLpkgTgt.save(new java.io.File(targetfilePath));

        log.info("done .. saved " + targetfilePath);
    }

    public static void processPart(Map.Entry<PartName, Part> hmeTgt, PartName tgtPartName) {
        log.debug("Processing %s", tgtPartName);
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