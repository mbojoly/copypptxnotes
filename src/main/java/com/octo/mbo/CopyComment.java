package com.octo.mbo;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.pml.GroupShape;
import org.pptx4j.pml.Notes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class CopyComment {

    private static Logger log = LoggerFactory.getLogger(CopyComment.class);
    private static final String TARGET_FILE_PATH = System.getProperty("user.dir") + "/sample-docs/pptx-copy.pptx";
    private static final String SRC_FILE_PATH = System.getProperty("user.dir") + "/sample-docs/pptx-test.pptx";

    public static void main(String[] args) {

        try {
            PresentationMLPackage pMLpkgSrc = loadSourcePackage(SRC_FILE_PATH);
            PresentationMLPackage pMLpkgTgt = loadTargetPackage(TARGET_FILE_PATH);

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

            log.info("Extracting comment from source file...");
            SlideExtractor srcSlideExtractor = new SlideExtractor();
            for (HashMap.Entry<PartName, Part> hmeSrc : hmSrc.entrySet()) {
                srcSlideExtractor.processEntry(hmeSrc);
            }

            log.info("Extracting comment from target file...");
            SlideExtractor tgtSlideExtractor = new SlideExtractor();
            for (HashMap.Entry<PartName, Part> hmeTgt : hmTgt.entrySet()) {
                tgtSlideExtractor.processEntry(hmeTgt);
            }

            Map<String, Slide> srcSlides = srcSlideExtractor.getSlides();
            Map<String, Slide> tgtSlides = tgtSlideExtractor.getSlides();

            log.info("Merging source and target slides");
            Set<String> processedKeys = new HashSet<>();

            Map<String, Slide> slidesPerPartName = new HashMap<>();
            for(Map.Entry<String, Slide> e : tgtSlides.entrySet()) {
                if(!srcSlides.containsKey(e.getKey())) {
                    log.warn("No slide with key {} in the source document. This slide has been ignored.", e.getKey());
                } else {
                    Slide srcSlide = srcSlides.get(e.getKey());
                    if(srcSlide == null || srcSlide.getParagraphs() == null) {
                        log.error("Slide data are null. Slide {} has been ignored.", e.getKey());
                    } else {
                        processedKeys.add(e.getKey());
                        List<String> srcParagraphs = srcSlide.getParagraphs();
                        List<String> tgtParagraphs = e.getValue().getParagraphs();
                        //Preprend source comments (they are the reference)
                        tgtParagraphs.add(0, "=== Original comments from " + TARGET_FILE_PATH + "===");
                        tgtParagraphs.addAll(0, srcParagraphs);
                    }
                }
                //Switch key  from first string to partName
                slidesPerPartName.put(e.getValue().getPartName(), e.getValue());
            }

            //Check if some source slides are missing
            for(String s : srcSlides.keySet()) {
                if(!processedKeys.contains(s)) {
                    log.warn("No slide with first string {} has been found in the target document. The corresponding comments are not copied", s);
                }
            }

            log.info("Updating target...");
            for (HashMap.Entry<PartName, Part> hmeTgt : hmTgt.entrySet()) {
                String partName = hmeTgt.getKey().getName();
                if(slidesPerPartName.containsKey(partName)) {
                    SlideUpdator.processEntry(hmeTgt.getValue(), slidesPerPartName.get(partName).getParagraphs());
                }
            }
            log.info("Target updated.");

            log.info("Saving modified target...");

            // All done: save it
            pMLpkgTgt.save(new java.io.File(TARGET_FILE_PATH));

            log.info("done .. saved " + TARGET_FILE_PATH);
        } catch (Docx4JException d4jex) {
            log.error("Exception processing the document. Exiting", d4jex);
            System.exit(-1);
        } catch (CopyCommentException ccex) {
            log.error("Internal error processing the document. Exiting", ccex);
            System.exit(-1);
        }
    }


    private static PresentationMLPackage loadTargetPackage(final String targetFilePath) throws Docx4JException {
        log.info("Loading target. {}..", targetFilePath);
        PresentationMLPackage pMLpkgTgt =
                (PresentationMLPackage) OpcPackage.load(new java.io.File(targetFilePath));
        log.info("Target {} loaded", targetFilePath);
        return pMLpkgTgt;
    }

    private static PresentationMLPackage loadSourcePackage(final String srcFilePath) throws Docx4JException {

        log.info("Loading source. {}..", srcFilePath);
        PresentationMLPackage pMLpkgSrc =
                (PresentationMLPackage) OpcPackage.load(new java.io.File(srcFilePath));
        log.info("Source {} loaded", srcFilePath);
        return pMLpkgSrc;

    }
}